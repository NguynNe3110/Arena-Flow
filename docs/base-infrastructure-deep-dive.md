# Phân tích chuyên sâu Hạ tầng Base: `build-logic`, `:app`, `:konsist-test`, `:library:test-utils`

Tài liệu này phân tích chi tiết 4 thành phần nền móng quan trọng nhất của **ArenaFlow**. Đây là những phần quyết định cách dự án build, lắp ráp dependency, điều hướng, kiểm soát quy tắc kiến trúc và phục vụ viết unit test.

---

## 1. `build-logic`: Trái tim cấu hình của toàn bộ Project

Thay vì cấu hình lặp đi lặp lại ở file `build.gradle.kts` của từng module, ArenaFlow sử dụng mô hình **Gradle Convention Plugins** thông qua **Composite Build** (`includeBuild("build-logic")`).

### 1.1. Tại sao dùng `build-logic` thay vì `buildSrc`?
* `buildSrc` truyền thống làm mất **Configuration Cache** của Gradle mỗi khi sửa một dòng code nhỏ trong đó, khiến toàn bộ dự án bị build lại từ đầu.
* `build-logic` là một build độc lập được include qua `settings.gradle.kts`, tận dụng được cache tốt hơn, cho phép viết các plugin Kotlin DSL type-safe và dễ dàng bảo trì khi dự án phình to.

### 1.2. Cơ chế expose Version Catalog vào Build Logic
Trong [`build-logic/build.gradle.kts`](file:///D:/AppData/Code/Project/Android/ArenaFlow/build-logic/build.gradle.kts):
```kotlin
/*
Expose generated type-safe version catalogs accessors accessible from precompiled script plugins
e.g. add("implementation", libs.koin)
*/
implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
```
Kỹ thuật này cho phép các convention plugin viết bằng Kotlin (như `FeatureConventionPlugin`) có thể truy cập trực tiếp vào đối tượng `libs` từ `gradle/libs.versions.toml` một cách an toàn và có gợi ý code (type-safe).

### 1.3. Các Convention Plugin cốt lõi

Các plugin này được đăng ký trong khối `gradlePlugin` của `build-logic/build.gradle.kts`:

#### A. `com.igorwojda.showcase.convention.feature` ([`FeatureConventionPlugin.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/build-logic/src/main/kotlin/com/igorwojda/showcase/buildlogic/FeatureConventionPlugin.kt))
Đây là plugin quan trọng nhất cho lập trình viên khi thêm tính năng mới. Mỗi khi một module feature apply plugin này:
1. **Tự động áp dụng các plugin nền tảng**:
   * `com.android.library`
   * `KotlinConventionPlugin` (target Java 17 / JVM toolchain)
   * `TestConventionPlugin` (JUnit 5 + parallel test execution)
   * `com.google.devtools.ksp`
   * `org.jetbrains.kotlin.plugin.compose`
2. **Cấu hình Android Library Extension**:
   * Thiết lập `compileSdk`, `minSdk` lấy từ Version Catalog.
   * Kích hoạt `buildFeatures { compose = true, buildConfig = true, viewBinding = true }`.
   * Cấu hình đóng gói `packaging { excludeLicenseAndMetaFiles() }`.
3. **Tự động inject dependency nền tảng**:
   * **Tự động thêm `:feature:base`**: Trừ chính `:feature:base`, mọi module feature khác đều tự động có `implementation(project(":feature:base"))` mà không cần khai báo tay!
   * Koin BOM & bundles, Compose BOM & bundles, Room + KSP, Retrofit bundle, Coroutines, Timber.
   * Bộ test: tự động chèn `testImplementation(project(":library:test-utils"))` cùng JUnit 5, MockK, Coroutines Test.

> **Kết quả**: File `feature/album/build.gradle.kts` chỉ cần đúng 7 dòng code:
> ```kotlin
> plugins {
>     id("com.igorwojda.showcase.convention.feature")
> }
> android {
>     namespace = "com.igorwojda.showcase.feature.album"
> }
> ```

#### B. `com.igorwojda.showcase.convention.application` ([`ApplicationConventionPlugin.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/build-logic/src/main/kotlin/com/igorwojda/showcase/buildlogic/ApplicationConventionPlugin.kt))
* Dành riêng cho module `:app`.
* Cấu hình `targetSdk`, `multiDexEnabled = true`, `vectorDrawables`, `core.splashscreen`, `navigation.compose`.
* Tự động áp dụng `EasyLauncherConventionPlugin` và `AboutLibrariesConventionPlugin`.

#### C. `com.igorwojda.showcase.convention.test` ([`TestConventionPlugin.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/build-logic/src/main/kotlin/com/igorwojda/showcase/buildlogic/TestConventionPlugin.kt))
* Áp dụng `com.adarshr.test-logger` với theme Mocha giúp log chạy test ở terminal trực quan, đẹp mắt.
* Bật chế độ chạy Unit Test song song bằng JUnit 5:
  ```kotlin
  systemProperties = mapOf(
      "junit.jupiter.execution.parallel.enabled" to "true",
      "junit.jupiter.execution.parallel.mode.default" to "concurrent",
  )
  ```

#### D. `com.igorwojda.showcase.convention.easylauncher` ([`EasyLauncherConventionPlugin.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/build-logic/src/main/kotlin/com/igorwojda/showcase/buildlogic/EasyLauncherConventionPlugin.kt))
* Tự động overlay một dải ruy-băng (ribbon) màu xanh lá ghi chữ `"debug"` và version name lên icon ứng dụng khi build variant `debug`, giúp bạn không bị nhầm lẫn giữa bản Debug và Release khi cài trên điện thoại.

---

## 2. Module `:app`: Orchestrator & Hạ tầng khởi chạy

Module `:app` không chứa bất kỳ logic nghiệp vụ nào của các tính năng. Trách nhiệm duy nhất của nó là **"hợp nhất" (glue code)**.

### 2.1. Khởi tạo App & DI Root ([`ShowcaseApplication.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/app/src/main/kotlin/com/igorwojda/showcase/app/ShowcaseApplication.kt))
```kotlin
class ShowcaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
        initTimber()
    }

    private fun initKoin() {
        GlobalContext.startKoin {
            androidLogger()
            androidContext(this@ShowcaseApplication)

            modules(appModule)
            modules(featureFavouriteModules)
            modules(featureAlbumModules)
            modules(featureSettingsModules)
        }
    }
}
```
* `:app` phụ thuộc vào tất cả các feature module và đóng vai trò nạp toàn bộ Koin module vào Koin Container tại thời điểm khởi động.

### 2.2. Hạ tầng Network & Supabase trong `:app`
Nằm trong `app/src/main/kotlin/com/igorwojda/showcase/app/data/`:
1. **[`RetrofitModule.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/app/src/main/kotlin/com/igorwojda/showcase/app/data/retrofit/RetrofitModule.kt)**:
   * **Interceptors**:
     * `AuthenticationInterceptor`: Tự động gắn token (`apiToken` từ gradle properties).
     * `UserAgentInterceptor`: Gắn header User-Agent.
     * `HttpLoggingInterceptor`: Chỉ log mức `BODY` trong bản `DEBUG` và chuyển hướng toàn bộ log sang Timber với tag `"Network"`. Trong bản Release, mức log chuyển về `NONE` để tránh hao phí CPU và rò rỉ dữ liệu.
   * **Converter**: Cấu hình `kotlinx.serialization` với `ignoreUnknownKeys = true` (không bị crash khi backend trả thêm trường mới).
   * **CallAdapter**: Tích hợp `ApiResultAdapterFactory` (từ `:feature:base`) để bọc mọi response Retrofit thành `ApiResult<T>`.
2. **[`SupabaseModule.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/app/src/main/kotlin/com/igorwojda/showcase/app/data/supabase/SupabaseModule.kt)**:
   * Khởi tạo `SupabaseClient` dạng `single` lười (lazy): chỉ khi nào code thực sự gọi đến Supabase thì client mới đọc URL và Anon Key từ `BuildConfig.GRADLE_SUPABASE_URL`.
   * Cài đặt các plugin: `Auth`, `Postgrest`, `Realtime`, `Storage`.

### 2.3. Điều phối Navigation Compose Type-Safe ([`MainShowcaseScreen.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/app/src/main/kotlin/com/igorwojda/showcase/app/presentation/MainShowcaseScreen.kt))
* Vì các feature không biết nhau, `:app` định nghĩa toàn bộ cây điều hướng và kết nối các màn hình lại với nhau.
* Sử dụng **Type-Safe Navigation** với Kotlin Serialization ([`NavigationRoute.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/app/src/main/kotlin/com/igorwojda/showcase/app/presentation/NavigationRoute.kt)):
  ```kotlin
  @Serializable
  sealed interface NavigationRoute {
      @Serializable data object AlbumsGraph : NavigationRoute
      @Serializable data object AlbumList : NavigationRoute
      @Serializable data class AlbumDetail(
          val albumName: String,
          val artistName: String,
          val albumMbId: String?,
      ) : NavigationRoute
      // ...
  }
  ```
* Trong `MainShowcaseScreen.kt`, việc chuyển màn hình và truyền đối số hoàn toàn kiểm tra type tại compile time:
  ```kotlin
  composable<NavigationRoute.AlbumDetail> { backStackEntry ->
      val args = backStackEntry.toRoute<NavigationRoute.AlbumDetail>()
      AlbumDetailScreen(
          albumName = args.albumName,
          artistName = args.artistName,
          albumMbId = args.albumMbId,
          onBackClick = { navController.popBackStack() },
      )
  }
  ```
* Tích hợp [`NavigationDestinationLogger`](file:///D:/AppData/Code/Project/Android/ArenaFlow/app/src/main/kotlin/com/igorwojda/showcase/app/presentation/util/NavigationDestinationLogger.kt) để ghi log mỗi lần chuyển màn hình trong `DEBUG`.

---

## 3. Module `:konsist-test`: Người gác cổng kiến trúc (Architecture as Code)

Konsist là một thư viện kiểm thử tĩnh (Static Architectural Testing). Trong dự án này, nó đóng vai trò biến các quy ước của team thành các **Unit Test tự động**. Nếu bạn viết sai quy chuẩn, lệnh test sẽ fail ngay lập tức!

Các test class trong [`konsist-test`](file:///D:/AppData/Code/Project/Android/ArenaFlow/konsist-test/src/test/kotlin/com/igorwojda/showcase/konsisttest/):

### 3.1. [`CleanArchitectureKonsistTest.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/konsist-test/src/test/kotlin/com/igorwojda/showcase/konsisttest/CleanArchitectureKonsistTest.kt)
Kiểm tra tính độc lập tuyệt đối giữa các tầng:
```kotlin
domain.dependsOnNothing()
presentation.dependsOn(domain)
data.dependsOn(domain)
```
* **Ý nghĩa**: Nếu bạn vô tình import một class ở package `data` hoặc `presentation` vào `domain`, test sẽ fail ngay!

### 3.2. [`UseCaseKonsistTest.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/konsist-test/src/test/kotlin/com/igorwojda/showcase/konsisttest/UseCaseKonsistTest.kt)
Đặt ra các luật khắt khe cho tầng UseCase:
1. **Mọi UseCase bắt buộc phải có Unit Test**: Tìm class có hậu tố `UseCase`, kiểm tra class đó phải có test class tương ứng và có thuộc tính `sut` (System Under Test).
2. **Constructor parameters phải xếp theo thứ tự bảng chữ cái**:
   ```kotlin
   // ĐÚNG: a -> b -> c
   class GetAlbumUseCase(
       private val albumRepository: AlbumRepository,
       private val userManager: UserManager,
   )
   // SAI: u trước a -> FAIL TEST!
   ```
3. **Tên tham số constructor phải dẫn xuất từ tên kiểu dữ liệu**: Tham số kiểu `AlbumRepository` phải được đặt tên là `albumRepository`.
4. **Chỉ có đúng 1 method public duy nhất là `operator fun invoke`**:
   * Cấm viết các hàm public lung tung trong UseCase như `execute()`, `getData()`.
   * Bắt buộc dùng `operator fun invoke(...)`.
5. **Bắt buộc nằm trong package `..domain..usecase..`**.

### 3.3. [`ViewModelKonsistTest.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/konsist-test/src/test/kotlin/com/igorwojda/showcase/konsisttest/ViewModelKonsistTest.kt)
1. Mọi ViewModel kế thừa từ `BaseViewModel` bắt buộc phải có test class.
2. Tên tham số constructor của ViewModel cũng phải khớp với kiểu dữ liệu.

### 3.4. [`GeneralKonsistTest.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/konsist-test/src/test/kotlin/com/igorwojda/showcase/konsisttest/GeneralKonsistTest.kt) & [`ModuleKonsistTest.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/konsist-test/src/test/kotlin/com/igorwojda/showcase/konsisttest/ModuleKonsistTest.kt)
* **Cấm dùng `android.util.Log`**: Bắt buộc dùng `Timber`.
* **Cấm đặt tên biến có prefix `m` kiểu cũ** (như `mUser`).
* **Cấm file rỗng**.
* **Package name phải khớp hoàn toàn với đường dẫn thư mục và tên module**: Ví dụ code trong `:feature:album` bắt buộc phải bắt đầu bằng `com.igorwojda.showcase.feature.album`.

---

## 4. Module `:library:test-utils`: Hậu cần cho Unit Test (JUnit 5)

Dự án sử dụng **JUnit 5 (Jupiter)** cho toàn bộ unit test, kết hợp **MockK**. Hai tiện ích cốt lõi trong `:library:test-utils` giải quyết các bài toán bất đồng bộ trong Android test:

### 4.1. [`CoroutinesTestDispatcherExtension.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/library/test-utils/src/main/kotlin/com/igorwojda/showcase/library/testutils/CoroutinesTestDispatcherExtension.kt)
```kotlin
@ExperimentalCoroutinesApi
class CoroutinesTestDispatcherExtension : BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext) {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    override fun afterEach(context: ExtensionContext) {
        Dispatchers.resetMain()
    }
}
```
* **Nhiệm vụ**: Trong môi trường JVM Unit Test thông thường, `Dispatchers.Main` không tồn tại (sẽ ném lỗi `IllegalStateException: Module with the Main dispatcher had failed to initialize`). Extension này tự động tráo `Dispatchers.Main` thành `StandardTestDispatcher()` trước mỗi test case và dọn dẹp sau khi chạy xong.

### 4.2. [`InstantTaskExecutorExtension.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/library/test-utils/src/main/kotlin/com/igorwojda/showcase/library/testutils/InstantTaskExecutorExtension.kt)
* Tương đương với `InstantTaskExecutorRule` của JUnit 4 nhưng viết cho JUnit 5.
* Ép toàn bộ các tác vụ background của Android Architecture Components (như `ArchTaskExecutor`, `SavedStateHandle`) chạy ngay lập tức (synchronously) trên thread hiện tại, giúp test kết quả được xác định rõ ràng, không bị race condition.

### 4.3. Ví dụ áp dụng thực tế khi test ViewModel
Xem [`AlbumListViewModelTest.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/feature/album/src/test/kotlin/com/igorwojda/showcase/feature/album/presentation/screen/albumlist/AlbumListViewModelTest.kt):
```kotlin
class AlbumListViewModelTest {
    @RegisterExtension
    val coroutinesTestExtension = CoroutinesTestDispatcherExtension()

    @RegisterExtension
    val instantTaskExecutorExtension = InstantTaskExecutorExtension()

    private val mockGetAlbumListUseCase: GetAlbumListUseCase = mockk()

    // sut = System Under Test (quy ước đặt tên để thỏa mãn Konsist rule)
    private val sut = AlbumListViewModel(
        savedStateHandle = SavedStateHandle(),
        getAlbumListUseCase = mockGetAlbumListUseCase,
    )

    @Test
    fun `when onInit and use case returns success then state is Content`() = runTest {
        // Given
        val albums = listOf(Album(name = "Album 1", ...))
        coEvery { mockGetAlbumListUseCase(any()) } returns Result.Success(albums)

        // When
        sut.onInit()

        // Then
        sut.uiStateFlow.value shouldBeEqualTo AlbumListUiState.Content(albums)
    }
}
```

---

## 5. Cheat Sheet thực hành (Ghi nhớ khi bắt tay vào code)

Khi bạn code tính năng mới hoặc chỉnh sửa codebase này:

1. **Khi thêm một UseCase mới**:
   * Đặt trong package `com.igorwojda.showcase.feature.<name>.domain.usecase`.
   * Constructor parameters: Đặt tên biến giống tên kiểu dữ liệu và **sắp xếp theo thứ tự a -> z**.
   * Chỉ viết duy nhất một phương thức: `suspend operator fun invoke(...): Result<T>`.
   * **Bắt buộc tạo ngay file test** `XxxUseCaseTest` với biến `private val sut = XxxUseCase(...)`.
2. **Khi thêm ViewModel mới**:
   * Kế thừa `BaseViewModel<State, Action>(initialState)`.
   * Định nghĩa State kín qua `sealed interface` hoặc `data class`.
   * Định nghĩa Action kế thừa `BaseAction<State>` và override hàm `reduce()`.
   * Khai báo trong `PresentationModule.kt` bằng `viewModelOf(::XxxViewModel)`.
   * **Bắt buộc tạo ngay file test** `XxxViewModelTest` có biến `private val sut = XxxViewModel(...)`.
3. **Khi thêm API / Repository**:
   * Retrofit service trả về `ApiResult<T>`.
   * Repository implementation dùng `dataResult { ... }` để tự động bảo vệ coroutine cancellation và map exception sang `AppError`.
   * Đăng ký repository trong `DataModule.kt` bằng `singleOf(::XxxRepositoryImpl) { bind<XxxRepository>() }`.
4. **Khi đăng ký màn hình vào App**:
   * Khai báo route trong [`NavigationRoute.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/app/src/main/kotlin/com/igorwojda/showcase/app/presentation/NavigationRoute.kt) với `@Serializable`.
   * Tạo graph extension trong [`MainShowcaseScreen.kt`](file:///D:/AppData/Code/Project/Android/ArenaFlow/app/src/main/kotlin/com/igorwojda/showcase/app/presentation/MainShowcaseScreen.kt).
   * Thêm module Koin của feature vào `ShowcaseApplication.initKoin()`.
5. **Kiểm tra trước khi commit**:
   * Chạy lệnh test: `./gradlew test` (hoặc chạy test trong `:konsist-test` để đảm bảo không vi phạm bất kỳ luật kiến trúc nào).
