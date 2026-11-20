# ArenaFlow — Tài Liệu Bàn Giao Kỹ Thuật UI Cho Android Jetpack Compose (UI Handoff Guide)

> Ngày lập: 18/11/2026 · Ngôn ngữ: Tiếng Việt · Phiên bản: 1.0-complete  
> Đích triển khai: **Android Jetpack Compose (Material 3)** · Kotlin 2.0+ · Navigation 2.8+ Type-Safe  
> Bao phủ đầy đủ **101 Screen/Sheet/Variant IDs** từ Mục 6 của `docs/prototype/PROTOTYPE_HANDOFF.md`.

---

## 1. Kiến Trúc UI & Quy Chuẩn Kỹ Thuật Android (Architectural Blueprint)

### 1.1. Mô Hình Luồng Dữ Liệu Một Chiều (MVI / Unidirectional Data Flow - UDF)

Mọi màn hình trong ứng dụng ArenaFlow bắt buộc phải tuân theo cấu trúc UDF tiêu chuẩn:
- **`UiState` (Immutable Data Class):** Đại diện cho toàn bộ trạng thái thị giác của màn hình tại một thời điểm. Được quản lý trong `ViewModel` thông qua `StateFlow<UiState>`.
- **`UiEvent` (Sealed Interface):** Đại diện cho mọi tương tác của người dùng từ giao diện (bấm nút, nhập text, chọn filter, kéo làm mới).
- **`UiEffect` (SharedFlow / Channel):** Đại diện cho các tác vụ xảy ra một lần (One-time side effects: hiển thị Snackbar, rung xúc giác, điều hướng sang route khác).

```kotlin
// Mẫu cấu trúc chuẩn cho mọi Feature Composable
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FeatureEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
                is FeatureEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    FeatureContent(
        state = uiState,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState
    )
}
```

### 1.2. Điều Hướng Type-Safe Với Jetpack Navigation 2.8+

Sử dụng Kotlin `@Serializable` objects và data classes làm Route definition, loại bỏ hoàn toàn string-based routes truyền thống:

```kotlin
// Định nghĩa Routes Type-Safe
@Serializable object RouteLaunch
@Serializable object RouteOnboarding
@Serializable object RouteLogin
@Serializable data class RouteOtp(val phoneNumber: String)
@Serializable data class RouteMatchDetail(val matchId: String)
@Serializable data class RoutePairingRoom(val pairingId: String)
@Serializable data class RouteMercenaryDetail(val postId: String)
@Serializable data class RouteTeamDetail(val teamId: String)
@Serializable data class RouteVenuePicker(val returnKey: String)
```

### 1.3. Xử Lý Safe Area Insets & Bàn Phím Ảo (IME Handling)

1. **Scaffold Insets:** Mọi màn hình chính đều dùng `Scaffold` với `contentWindowInsets = WindowInsets.safeDrawing`.
2. **IME Padding:** Đối với mọi màn hình chứa biểu mẫu (Forms / TextFields):
   - Bắt buộc gắn `Modifier.imePadding()` vào container bao ngoài.
   - Thùng chứa cuộn (`Column(Modifier.verticalScroll(rememberScrollState()))`) để đảm bảo các trường nhập liệu phía dưới không bị che khuất.
   - Sử dụng `BringIntoViewRequester` tự động cuộn đến ô đang nhập khi bàn phím xuất hiện.
3. **Thanh hành động chân trang (Bottom Action Bar):**
   - Luôn áp dụng `Modifier.navigationBarsPadding()` để tránh bị thanh cử chỉ Android (Navigation Bar) đè lên nút bấm.

### 1.4. Quy Định Vùng Chạm (Touch Target >= 48dp)

Tuân thủ nghiêm ngặt tiêu chuẩn tiếp cận: Mọi `IconButton`, `Button`, `Chip`, `Checkbox` và `ListItem` phải đạt kích thước tối thiểu **48 x 48 dp**. Khi icon nhỏ (ví dụ 24dp), sử dụng `Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)` hoặc `IconButton` tiêu chuẩn của Material 3.

---

## 2. Đặc Tả Chi Tiết 101 Screen/Sheet/Variant IDs Cho Jetpack Compose

---

### PHẦN I: KHỞI ĐẦU, XÁC THỰC & HỒ SƠ (APP-01..02, AUTH-01..06, PROF-01..06)

#### [APP-01] Màn Hình Khởi Động & Kiểm Tra Phiên (Launch Screen)
- **Loại:** Variant / Screen State · **Route:** `RouteLaunch`
- **Quyền & Vai trò:** Mọi người dùng.
- **ScreenState:**
  ```kotlin
  data class LaunchUiState(
      val isCheckingSession: Boolean = true,
      val isNetworkError: Boolean = false
  )
  ```
- **Events:** `LaunchEvent.RetrySessionCheck`, `LaunchEvent.NavigateToOnboarding`, `LaunchEvent.NavigateToHome`.
- **Hành vi:**
  - Kiểm tra JWT trong DataStore: Nếu hợp lệ -> vào `RouteHome`.
  - Nếu hết hạn -> gọi `POST /api/v1/auth/refresh`. Thành công -> vào `RouteHome`; Thất bại -> vào `RouteOnboarding` (nếu lần đầu) hoặc `RouteLogin`.
  - Nếu mất mạng -> hiện nút "Thử lại kết nối" (Touch target 48dp).

#### [APP-02] Giới Thiệu Ứng Dụng (Onboarding Screen)
- **Loại:** M (Screen) · **Route:** `RouteOnboarding`
- **Quyền & Vai trò:** Khách / Người dùng mới mở app lần đầu [PROPOSAL DEP-08].
- **ScreenState:**
  ```kotlin
  data class OnboardingUiState(
      val currentPage: Int = 0,
      val totalPages: Int = 3
  )
  ```
- **Events:** `OnboardingEvent.PageChanged(val page: Int)`, `OnboardingEvent.SkipClicked`, `OnboardingEvent.GetStartedClicked`.
- **UI Elements:**
  - 3 Slides: "Tạo & Ghép Kèo Nhanh Chóng", "Tìm Người Đá Lẻ Gần Bạn", "Hệ Thống Uy Tín Minh Bạch".
  - Nút "Bỏ qua" (góc phải trên, cao 48dp). Nút "Bắt đầu ngay" (Primary Button, rộng match_parent, cao 48dp).
- **Hành vi:** Bấm Skip hoặc Bắt đầu -> Lưu `has_seen_onboarding = true` vào DataStore -> điều hướng sang `RouteLogin`.

#### [AUTH-01] Chọn Phương Thức Đăng Nhập (Login Method Selection)
- **Loại:** M (Screen) · **Route:** `RouteLogin` (Nguồn: Stitch S1)
- **Quyền & Vai trò:** Khách chưa xác thực.
- **ScreenState:**
  ```kotlin
  data class LoginUiState(
      val isLoading: Boolean = false,
      val errorMessage: String? = null
  )
  ```
- **Events:** `LoginEvent.SignInWithGoogle`, `LoginEvent.SignInWithFacebook`, `LoginEvent.SignInWithPhoneClicked`.
- **UI Elements:**
  - Logo ArenaFlow, Tiêu đề "Tham gia cộng đồng bóng đá".
  - Nút Google (Outlined, 48dp, icon Google). Nút Facebook (Nền xanh navy `#1877f2`, 48dp). Nút Số điện thoại (Primary `#3d6943`, 48dp).
  - Chú thích: *"Tài khoản sẽ được tự động tạo nếu đây là lần đầu đăng nhập."* Link "Điều khoản dịch vụ" [DEP-09].

#### [AUTH-02] Nhập Số Điện Thoại Đăng Ký / Đăng Nhập (Phone Input Screen)
- **Loại:** M (Screen) · **Route:** `RoutePhoneInput` (Nguồn: Stitch S2a)
- **ScreenState:**
  ```kotlin
  data class PhoneInputUiState(
      val phoneNumber: String = "",
      val isValidFormat: Boolean = false,
      val isLoading: Boolean = false,
      val errorMessage: String? = null
  )
  ```
- **Events:** `PhoneEvent.PhoneNumberChanged(val text: String)`, `PhoneEvent.SubmitPhone`, `PhoneEvent.BackClicked`.
- **Field Validation:**
  - Định dạng SĐT Việt Nam: Bắt đầu bằng `0` (10 chữ số) hoặc `+84` (9 chữ số sau mã quốc gia).
  - Regex: `^(0|\+84)(3|5|7|8|9)[0-9]{8}$`. Khi hợp lệ thì bật sáng nút "Tiếp tục".
- **Hành vi:** Nhấn "Tiếp tục" -> gọi `POST /api/v1/auth/otp/send` -> điều hướng sang `RouteOtp(phoneNumber)`.

#### [AUTH-03] Nhập Mã OTP Xác Thực (OTP Verification Screen)
- **Loại:** M (Screen) · **Route:** `RouteOtp(phoneNumber: String)` (Nguồn: Stitch S2b)
- **ScreenState:**
  ```kotlin
  data class OtpUiState(
      val phoneNumber: String,
      val otpCode: String = "", // 6 chữ số
      val remainingSeconds: Int = 60,
      val canResend: Boolean = false,
      val isLoading: Boolean = false,
      val errorMessage: String? = null
  )
  ```
- **Events:** `OtpEvent.OtpChanged(val code: String)`, `OtpEvent.ResendCodeClicked`, `OtpEvent.ChangePhoneClicked`, `OtpEvent.VerifyOtp`.
- **UI Elements:**
  - 6 ô nhập mã OTP độc lập, tự động focus ô tiếp theo khi gõ, tự động xóa ngược khi bấm Backspace.
  - Đồng hồ đếm ngược 60 giây gửi lại mã. Khi `remainingSeconds == 0` -> hiện nút "Gửi lại mã OTP" màu xanh `#3d6943`.
  - Nút "Đổi số điện thoại" dẫn quay lại `AUTH-02`.

#### [AUTH-04] Phân Luồng Điều Hướng Sau Xác Thực (Post-Auth Routing)
- **Loại:** V (State Handler)
- **Hợp đồng & Nghiệm thu AC-P01:**
  - Server trả về `{ "token": "...", "isNewUser": Boolean }`.
  - **Nếu `isNewUser == true`:** Điều hướng bắt buộc sang `RouteProfileEdit(isFirstTime = true)` (PROF-02). Không cho phép vào màn hình chính trước khi hoàn tất.
  - **Nếu `isNewUser == false`:** Kiểm tra `redirectUrl` (nếu có do phiên hết hạn hoặc deep link) -> điều hướng về trang đó, ngược lại vào `RouteHome`.

#### [AUTH-05] Màn Hình Tài Khoản Đã Bị Xóa (Deleted Account Error View)
- **Loại:** V (Error Screen) · **Route:** `RouteAccountDeleted`
- **Mã lỗi API:** `AUTH_003: Account has been deleted` [DEP-06].
- **UI Elements:**
  - Icon cảnh báo ổ khóa màu đỏ `#a73b21`. Tiêu đề: *"Tài khoản đã bị xóa vĩnh viễn"*.
  - Nội dung giải thích: *"Tài khoản liên kết với số điện thoại này đã được yêu cầu đóng vĩnh viễn và không thể khôi phục."*
  - Nút "Đăng nhập bằng tài khoản khác" (Primary, 48dp). Tuyệt đối **không có nút Khôi phục tài khoản giả**.

#### [AUTH-06] Dialog Phiên Hết Hạn (Session Expired Dialog)
- **Loại:** S/V (Modal Dialog)
- **Kích hoạt:** Khi Interceptor bắt mã HTTP 401 Unauthorized từ bất kỳ API có token.
- **UI Elements:**
  - Tiêu đề: *"Phiên làm việc đã hết hạn"*.
  - Mô tả: *"Vui lòng đăng nhập lại để tiếp tục sử dụng ứng dụng."*
  - Nút "Đăng nhập lại" (Primary, 48dp).
- **Hành vi:** Lưu `currentRoute` vào biến tạm. Đăng nhập thành công -> quay lại đúng trang này.

#### [PROF-01] Màn Hình Hồ Sơ Của Tôi (My Profile Screen)
- **Loại:** M (Screen) · **Route:** `RouteMyProfile` (Nguồn: Stitch S3)
- **Quyền & Vai trò:** Người dùng đã đăng nhập.
- **ScreenState:**
  ```kotlin
  data class MyProfileUiState(
      val user: UserProfile? = null,
      val personalReputationScore: Int = 100,
      val completedMatchesCount: Int = 0,
      val isLoading: Boolean = false
  )
  ```
- **Events:** `ProfileEvent.EditProfileClicked`, `ProfileEvent.SettingsClicked`, `ProfileEvent.ViewReputationCard`.
- **UI Elements:** Avatar lớn (80dp tròn), Tên hiển thị (`TitleLarge 20sp Bold`), Tag vị trí sở trường (GK/DF/MF/FW), Bio cá nhân, Thẻ tóm tắt điểm uy tín cá nhân, Nút "Chỉnh sửa hồ sơ" (Outlined, 48dp), Nút Menu tài khoản (Icon bánh răng, 48dp).

#### [PROF-02] Hoàn Thiện & Sửa Hồ Sơ (Profile Form Screen)
- **Loại:** M (Screen) · **Route:** `RouteProfileEdit(isFirstTime: Boolean)` (Nguồn: Stitch S4)
- **ScreenState:**
  ```kotlin
  data class ProfileEditUiState(
      val isFirstTime: Boolean = false,
      val displayName: String = "",
      val avatarUrl: String? = null,
      val bio: String = "",
      val selectedPosition: Position? = null, // GK, DF, MF, FW
      val dateOfBirth: LocalDate? = null,
      val gender: Gender? = null, // MALE, FEMALE, OTHER
      val selectedWardId: Int? = null,
      val isSaving: Boolean = false,
      val validationErrors: Map<String, String> = emptyMap()
  )
  ```
- **Field Validations:**
  - `displayName`: Bắt buộc, không được chỉ chứa khoảng trắng, độ dài từ 2 đến 50 ký tự.
  - `dateOfBirth`: Nếu nhập, tính đến ngày hiện tại phải **đủ 13 tuổi** (`Period.between(dob, today).years >= 13`). Nếu < 13 tuổi -> Báo lỗi: *"Người dùng phải đủ 13 tuổi trở lên"*.
  - `preferredPosition`: Chọn 1 trong 4 giá trị chuẩn API: GK (Thủ môn), DF (Hậu vệ), MF (Tiền vệ), FW (Tiền đạo).
  - `wardId`: Chọn từ danh mục Tỉnh -> Phường/Xã [DEP-03].

#### [PROF-03] Menu Tài Khoản & Cài Đặt (Account Settings Menu)
- **Loại:** M/S (Menu Screen / Sheet) · **Route:** `RouteAccountMenu` [PROPOSAL DEP-08]
- **Danh sách mục:**
  - 1. Chỉnh sửa thông tin cá nhân (dẫn sang PROF-02).
  - 2. Đổi tài khoản / Thông tin bảo mật.
  - 3. Trợ giúp & Phản hồi.
  - 4. Đăng xuất (mở sheet PROF-06).
  - 5. Xóa tài khoản vĩnh viễn (mở sheet PROF-04, chữ đỏ `#a73b21`).

#### [PROF-04] Xác Nhận Xóa Tài Khoản (Delete Account Confirmation Sheet)
- **Loại:** S (Modal Bottom Sheet) (Nguồn: Stitch S5, USER-002)
- **Quyền & Vai trò:** Người dùng cá nhân (không phải Captain đội active).
- **UI Elements:**
  - Icon thùng rác đỏ `#a73b21`. Tiêu đề: *"Xóa tài khoản vĩnh viễn?"*.
  - Liệt kê cảnh báo: Mất toàn bộ lịch sử đấu, điểm uy tín, danh sách đội bóng.
  - Nút "Hủy bỏ" (Outlined, 48dp). Nút "Xác nhận xóa tài khoản" (Destructive đỏ `#a73b21`, 48dp).
- **Hành vi:** Nhấn xác nhận -> gọi `DELETE /api/v1/users/me`. Thành công -> xóa session, về `AUTH-01`.

#### [PROF-05] Chặn Xóa Tài Khoản Đội Trưởng (Blocked Delete Account View)
- **Loại:** M/S (Error / Action Resolution Screen) (Nguồn: USER-002)
- **Quy tắc nghiệm thu AC-P02:**
  - Khi `DELETE /api/v1/users/me` trả về lỗi HTTP 409 `USER_004: Captain of active teams`.
  - Mở giao diện `PROF-05`: Tiêu đề *"Không thể xóa tài khoản"*.
  - Giải thích: *"Bạn hiện đang là Đội trưởng của các đội bóng sau. Vui lòng chuyển quyền Đội trưởng hoặc giải tán đội trước khi xóa tài khoản."*
  - Danh sách các đội liên quan kèm 2 nút cho mỗi đội:
    - Nút 1: "Chuyển quyền đội trưởng" -> mở `TEAM-09`.
    - Nút 2: "Giải tán đội bóng" -> mở `TEAM-12`.
  - Nút "Kiểm tra lại điều kiện xóa" (Refresh kiểm tra lại).

#### [PROF-06] Xác Nhận Đăng Xuất (Logout Confirmation Sheet)
- **Loại:** S (Modal Bottom Sheet) [PROPOSAL]
- **UI Elements:** Tiêu đề *"Đăng xuất khỏi ArenaFlow?"*, Nút "Hủy", Nút "Đăng xuất" (Đỏ `#a73b21`, 48dp).
- **Hành vi:** Xóa DataStore token, chuyển hướng về `RouteLogin`.

---

### PHẦN II: ĐỘI BÓNG & LỜI MỜI (TEAM-01..14)

#### [TEAM-01] Danh Sách Đội Của Tôi (My Teams Screen)
- **Loại:** M (Screen) · **Route:** `RouteMyTeams` [DEP-01]
- **ScreenState:**
  ```kotlin
  data class MyTeamsUiState(
      val myTeams: List<TeamMembershipItem> = emptyList(),
      val isLoading: Boolean = false
  )
  ```
- **UI Elements:**
  - Mỗi thẻ đội hiển thị: Tên đội, Logo, Khu vực, Badge vai trò (`CAPTAIN` màu xanh `#3d6943` hoặc `MEMBER` màu xám).
  - Nút FAB hoặc Header Action: "Tạo đội mới" (dẫn sang TEAM-03).
  - Trạng thái rỗng: Hình minh họa quả bóng, dòng chữ *"Bạn chưa tham gia đội bóng nào"*, nút "Tạo đội ngay" và "Khám phá đội".

#### [TEAM-02] Danh Sách Đội Hệ Thống (System Teams Directory)
- **Loại:** M (Screen) · **Route:** `RouteSystemTeams` (Nguồn: Stitch S6)
- **Phân trang:** `page`, `limit = 20`. Kéo xuống cuối danh sách để nạp trang tiếp theo.
- **Hành vi:** Nhấn vào thẻ đội -> mở `RouteTeamDetail(teamId)` (TEAM-04).

#### [TEAM-03] Tạo Đội Bóng Mới (Create Team Screen)
- **Loại:** M (Screen) · **Route:** `RouteTeamCreate` (Nguồn: Stitch S7)
- **ScreenState:**
  ```kotlin
  data class TeamFormUiState(
      val teamName: String = "",
      val logoUrl: String? = null,
      val wardId: Int? = null,
      val selectedSkillLevels: Set<SkillLevel> = emptySet(), // Tối đa 3 nhãn
      val isSubmitting: Boolean = false,
      val errorMap: Map<String, String> = emptyMap()
  )
  ```
- **Field Validations:**
  - `teamName`: Bắt buộc, độ dài từ 3 đến 50 ký tự, không được chỉ có khoảng trắng.
  - `wardId`: Bắt buộc chọn Phường/Xã hoạt động.
  - `skillLevels`: Chọn tối thiểu 1 và **tối đa 3 nhãn trình độ** (Vui vẻ, Trung bình, Bán chuyên). Nếu chọn cái thứ 4 -> vô hiệu hóa hoặc báo lỗi.

#### [TEAM-04] Chi Tiết Đội Bóng ACTIVE (Team Detail Screen)
- **Loại:** M (Screen) · **Route:** `RouteTeamDetail(teamId: String)` (Nguồn: Stitch S8a)
- **Phân quyền theo vai trò (Role Variants):**
  - **Là Captain:** Hiển thị Nút "Quản trị đội", "Mời thành viên" (TEAM-07), "Chuyển quyền" (TEAM-09), "Giải tán đội" (TEAM-12).
  - **Là Member:** Hiển thị Nút "Rời đội" (TEAM-11). Không có các nút quản trị.
  - **Là Outsider (Người ngoài):** Hiển thị Nút "Gửi yêu cầu giao lưu" nếu đội có kèo mở.
- **Tabs:** 1. Thông tin chung & Uy tín (REP-04) · 2. Thành viên (TEAM-06) · 3. Lịch thi đấu (MATCH-07).

#### [TEAM-05] Sửa Thông Tin Đội (Edit Team Screen)
- **Loại:** M (Screen) · **Route:** `RouteTeamEdit(teamId: String)`
- **Quyền:** Chỉ Captain. Tái sử dụng `TeamFormUiState` của TEAM-03.

#### [TEAM-06] Danh Sách Thành Viên Đội (Team Members List)
- **Loại:** M/S · **Route:** `RouteTeamMembers(teamId: String)`
- **Quy tắc hiển thị:**
  - Thành viên bình thường: Tên thật, Avatar, Vị trí.
  - **Tài khoản đã bị xóa:** Tên bị ẩn thành *"Người dùng ArenaFlow"*, avatar mặc định màu xám.

#### [TEAM-07] Mời Thành Viên Mới (Invite Member Sheet)
- **Loại:** S (Modal Bottom Sheet) (Nguồn: Stitch S9) [DEP-02]
- **Quyền:** Chỉ Captain.
- **UI Elements:** Danh sách bạn bè/người chơi gợi ý từ Fixture. Nút "Mời" (cao 48dp). Nếu đã mời -> hiện badge *"Đã gửi lời mời"*.

#### [TEAM-08] Phản Hồi Lời Mời Từ Thông Báo (Invitation Response Screen)
- **Loại:** M/S · **Route:** `RouteTeamInvitation(invitationId: String)` [DEP-04]
- **UI Elements:** Thông tin đội mời, Tên Captain gửi lời mời, Nút "Từ chối" (Outlined, 48dp), Nút "Chấp nhận" (Primary, 48dp).
- **Ngoại lệ:** Nếu đội bóng đã giải tán trước khi bấm -> Báo lỗi: *"Đội bóng này đã giải tán, lời mời không còn hiệu lực."*

#### [TEAM-09] Chuyển Quyền Đội Trưởng (Transfer Captaincy Sheet)
- **Loại:** S (Modal Bottom Sheet) (Nguồn: Stitch S10)
- **Quyền:** Chỉ Captain hiện tại.
- **Quy tắc nghiệp vụ:**
  - Danh sách thành viên chính thức trong đội.
  - Khi chọn 1 thành viên và bấm "Xác nhận chuyển quyền": **Chuyển quyền có hiệu lực tức thì (Instant Effect)**, không cần người nhận phê duyệt.
  - Sau khi chuyển thành công: Quyền của user hiện tại lập tức hạ cấp xuống `MEMBER`, giao diện mất các nút quản lý.

#### [TEAM-10] Lịch Sử Các Đời Đội Trưởng (Captain History Screen)
- **Loại:** M/S · **Route:** `RouteCaptainHistory(teamId: String)`
- **UI Elements:** Timeline hiển thị: Ngày chuyển giao, Tên Đội trưởng cũ, Tên Đội trưởng mới.

#### [TEAM-11] Thành Viên Rời Đội (Leave Team Sheet)
- **Loại:** S (Modal Bottom Sheet) (Nguồn: Stitch S11)
- **Quyền:** Chỉ Member thường.
- **Chặn Captain:** Nếu Captain bấm rời đội -> Chặn lại và hiện thông báo: *"Đội trưởng không thể rời đội. Vui lòng chuyển quyền đội trưởng hoặc giải tán đội bóng."*

#### [TEAM-12] Giải Tán Đội Bóng (Disband Team Sheet)
- **Loại:** S (Modal Bottom Sheet) (Nguồn: Stitch S12, USER-002)
- **Quyền:** Chỉ Captain.
- **UI Elements:**
  - Cảnh báo màu đỏ: *"Giải tán đội bóng là hành động vĩnh viễn không thể khôi phục. Mọi kèo đang mở và lịch thi đấu của đội sẽ bị hủy bỏ."*
  - Trường gõ chữ xác nhận: Gõ đúng tên đội để kích hoạt nút xóa.
  - Nút "Hủy", Nút "Xác nhận giải tán đội" (Đỏ `#a73b21`, 48dp).

#### [TEAM-13] Chi Tiết Đội INACTIVE/DISBANDED (Disbanded Team View)
- **Loại:** M/V (Variant Screen) (Nguồn: Stitch S8b)
- **UI Elements:** Banner màu xám/đỏ đầu trang: *"Đội bóng này đã giải tán vào ngày DD/MM/YYYY"*. Toàn bộ các nút hành động (Mời, Rời đội, Gửi kèo) bị ẩn hoặc vô hiệu hóa.

#### [TEAM-14] Bộ Chọn Đội Thao Tác (Acting Team Selector Sheet)
- **Loại:** S (Modal Bottom Sheet)
- **Kích hoạt:** Khi Captain của từ 2 đội trở lên thực hiện các hành động: Tạo kèo (`MATCH-05`), Nhận bài B (`MATCH-04`), Tuyển người (`MERC-08`).
- **UI Elements:** Danh sách các đội đủ điều kiện kèm radio button. Chọn đội xong đóng sheet và truyền `teamId` vào form.

---

### PHẦN III: KHÁM PHÁ & VÒNG ĐỜI KÈO (MATCH-01..14)

#### [MATCH-01] Khám Phá & Tìm Kèo Đấu (Explore Matches Screen)
- **Loại:** M (Screen) · **Route:** `RouteMatchExplore` (Tab 1 BottomNav)
- **ScreenState:**
  ```kotlin
  data class MatchExploreUiState(
      val matches: List<MatchSummaryItem> = emptyList(),
      val activeFilterCount: Int = 0,
      val isLoading: Boolean = false,
      val isRefreshing: Boolean = false
  )
  ```
- **UI Elements:**
  - Thanh tìm kiếm + Nút mở bộ lọc (MATCH-02) có badge số lượng filter đang chọn.
  - Danh sách bài kèo: Thẻ kèo A có icon ghim sân cố định; Thẻ kèo B có nhãn *"Nhiều phương án sân & giờ"*.
  - Pull-to-refresh (`SwipeRefresh`).

#### [MATCH-02] Bộ Lọc Tìm Kiếm Kèo (Match Filter Sheet)
- **Loại:** S (Modal Bottom Sheet)
- **Tiêu chí lọc:** Định dạng sân (5, 7, 11 người), Khung thời gian (Sáng 6h-12h, Chiều 12h-18h, Tối 18h-23h), Trình độ (Vui vẻ, Trung bình, Bán chuyên), Quận/Huyện.
- **Nút:** "Đặt lại" (Ghost button) và "Áp dụng" (Primary button, 48dp).

#### [MATCH-03] Chi Tiết Bài Đăng Kèo A (Match-A Detail Screen)
- **Loại:** M (Screen) · **Route:** `RouteMatchDetailA(matchId: String)`
- **Đặc trưng Kèo A:** Đúng **1 sân bóng** cố định + Đúng **1 khung giờ** cố định + Cơ cấu chi phí.
- **Phân quyền:**
  - **Là Chủ kèo A:** Hiển thị danh sách các đội gửi yêu cầu (MATCH-09).
  - **Là Đội khác:** Nút "Gửi yêu cầu bắt kèo" (mở sheet xác nhận). Nếu đã gửi -> hiện trạng thái `PENDING` và nút "Rút yêu cầu" (MATCH-11).

#### [MATCH-04] Chi Tiết Bài Đăng Kèo B (Match-B Detail Screen)
- **Loại:** M (Screen) · **Route:** `RouteMatchDetailB(matchId: String)`
- **Đặc trưng Kèo B:** Danh sách **1 đến 5 sân bóng gợi ý** và **1 đến 5 khung giờ độc lập**.
- **Hành động:** Nút "Nhận kèo & Bắt đầu thương lượng" (tạo phòng thương lượng độc lập PAIR-03).

#### [MATCH-05] Tạo Bài Đăng Kèo Mới (Create Match Form)
- **Loại:** M (Screen) · **Route:** `RouteMatchCreate`
- **Quy tắc nghiệp vụ:**
  - Chọn bài Kèo A hay Kèo B.
  - Chọn Đội đăng kèo (nếu quản lý nhiều đội).
  - Giờ bắt đầu phải **cách thời điểm hiện tại ít nhất 1 giờ**.
  - Thời lượng trận đấu tối đa không quá 12 giờ.
  - Đối với Kèo B: Cho phép thêm 1-5 sân và 1-5 giờ (không ép buộc phải theo cặp cố định).
  - Nút chọn sân: Dẫn sang `VEN-01`, khi chọn xong quay lại phải giữ nguyên các giá trị đã nhập.

#### [MATCH-06] Xem Lại Trước Khi Đăng Kèo (Match Review Modal)
- **Loại:** M/S [PROPOSAL DEP-08]
- **UI Elements:** Bảng tóm tắt thông tin: Đội, Sân, Giờ, Chi phí, Hạn chót. Nút "Chỉnh sửa lại" và Nút "Xác nhận đăng bài" (Chống double-click bằng cờ `isSubmitting`).

#### [MATCH-07] Lịch Thi Đấu & Kèo Của Đội (Team Match Schedule)
- **Loại:** M (Screen) · **Route:** `RouteTeamSchedule(teamId: String)`
- **3 Tabs:** 1. Sắp tới (`OPEN`, `CONFIRMED`) · 2. Đã kết thúc (`COMPLETED`, `ARCHIVED`) · 3. Đã hủy / Hết hạn (`CANCELLED`, `EXPIRED`).

#### [MATCH-08] Yêu Cầu Giao Lưu Đã Gửi (Sent Match Requests)
- **Loại:** M (Screen) · **Route:** `RouteSentMatchRequests`
- **UI Elements:** Thẻ danh sách các request đã gửi kèm badge trạng thái: `PENDING` (Chờ duyệt), `ACCEPTED` (Đã chấp thuận), `REJECTED`, `WITHDRAWN`.

#### [MATCH-09] Danh Sách Yêu Cầu Gửi Đến Kèo A (Incoming Requests List)
- **Loại:** M (Screen) · **Route:** `RouteIncomingRequests(matchId: String)`
- **Quyền:** Chỉ Chủ kèo A.
- **UI Elements:** Thẻ thông tin từng đội nộp đơn, Điểm uy tín đội đó, Nút Nhắn tin (CHAT-02), Nút Từ chối và Nút Chấp nhận (MATCH-10).

#### [MATCH-10] Duyệt Yêu Cầu Đối Thủ (Accept Match Request Action)
- **Loại:** M/S · **Nghiệm thu AC-P03:**
  - Khi Chủ kèo A nhấn "Chấp nhận" một yêu cầu:
    1. Yêu cầu được chọn chuyển sang `ACCEPTED`.
    2. Kèo A chuyển sang `CONFIRMED`.
    3. **Toàn bộ các yêu cầu PENDING khác của kèo này tự động chuyển thành `SUPERSEDED`**.

#### [MATCH-11] Rút Yêu Cầu Bắt Kèo (Withdraw Request Sheet)
- **Loại:** S (Modal Bottom Sheet)
- **Quy tắc phạt:**
  - Rút khi đang `PENDING`: Không bị phạt điểm uy tín.
  - Rút sau khi đã `ACCEPTED`: Coi như hành vi hủy kèo sau ghép -> Bị trừ điểm uy tín đội bóng (-15 điểm).

#### [MATCH-12] Hủy Bài Đăng Kèo Đấu (Cancel Match Sheet)
- **Loại:** S (Modal Bottom Sheet)
- **Quy tắc:**
  - Chặn hủy nếu kèo A đang có yêu cầu ở trạng thái `PENDING` (buộc phải từ chối hoặc duyệt trước).
  - Chặn hủy nếu trận đấu đã đến giờ bắt đầu.
  - Hủy khi `CONFIRMED`: Trừ điểm uy tín đội hủy, tự động gửi thông báo cho đội đối phương.

#### [MATCH-13] Biến Thể Trạng Thái Vòng Đời Kèo (Match Status Variants)
- **Loại:** M/V
- **Trạng thái:**
  - `CONFIRMED`: Hiện thông tin 2 đội, sân, giờ, nút mở phòng chat chung.
  - `IN_PROGRESS`: Banner xanh "Trận đấu đang diễn ra". Khóa mọi nút hủy.
  - `COMPLETED`: Mở nút "Đánh giá trận đấu" (REP-01).

#### [MATCH-14] Chỉnh Sửa Chi Phí Kèo A (Edit Match-A Cost Sheet)
- **Loại:** M/S
- **Quyền:** Chủ kèo A khi kèo đang ở trạng thái `OPEN` và chưa có request nào được duyệt.

---

### PHẦN IV: THƯƠNG LƯỢNG KÈO B & CHỐT GÓI (PAIR-01..08)

#### [PAIR-01] Danh Sách Thương Lượng Của Kèo B (Host Pairing List)
- **Loại:** M (Screen) · **Route:** `RouteHostPairings(matchId: String)`
- **Quyền:** Chủ bài B. Hiển thị danh sách các đội đang đàm phán kèm tiến độ xác nhận của từng phòng.

#### [PAIR-02] Danh Sách Thương Lượng Của Đội Tôi (My Team Pairings)
- **Loại:** M (Screen) · **Route:** `RouteMyTeamPairings`
- **UI Elements:** Các phòng thương lượng mà đội đang là bên nhận (Guest).

#### [PAIR-03] Chi Tiết Phòng Thương Lượng Gói B (Pairing Room Screen)
- **Loại:** M (Screen) · **Route:** `RoutePairingRoom(pairingId: String)`
- **ScreenState:**
  ```kotlin
  data class PairingRoomUiState(
      val pairingId: String,
      val hostTeam: TeamSummary,
      val guestTeam: TeamSummary,
      val currentPackage: MatchPackage,
      val revision: Int = 1,
      val confirmedByHost: Boolean = false,
      val confirmedByGuest: Boolean = false,
      val isConflict: Boolean = false,
      val isFinalized: Boolean = false
  )
  ```
- **UI Elements:** Khối Sân & Giờ đã chọn (PAIR-04), Khối Chi phí & Cọc (PAIR-05), Lối vào Chat 2 đội (CHAT-02), Vùng trạng thái xác nhận của 2 bên.

#### [PAIR-04] Chọn Phương Án Sân & Giờ Gói B (Select Venue & Time Sheet)
- **Loại:** M/S
- **UI Elements:** Radio list chọn 1 trong số các sân và 1 trong số các khung giờ bài B đã đăng. Vô hiệu hóa các sân `INACTIVE`.

#### [PAIR-05] Tùy Chỉnh Chi Phí & Đặt Cọc (Edit Package Cost Sheet)
- **Loại:** M/S
- **UI Elements:** Tiền sân tổng, Tỷ lệ chia (50/50, Thua trả, Đội mời trả 100%), Tiền cọc giữ chân, Bên giữ cọc ngoài đời.

#### [PAIR-06] Xác Nhận Gói Thương Lượng (Confirm Package Action)
- **Loại:** S/V · **Nghiệm thu AC-P04:**
  - Mỗi bên nhấn "Xác nhận gói". Khi cả 2 bên cùng xác nhận trên cùng 1 `revision` -> Kèo B ghép thành công (`CONFIRMED`).
  - Nếu bên A đã xác nhận, nhưng bên B nhấn sửa sân/giờ (PAIR-04) hoặc sửa chi phí (PAIR-05) -> **Hệ thống tự động tăng `revision` và xóa cờ xác nhận của bên A**!

#### [PAIR-07] Cảnh Báo Xung Đột Phiên Bản (Revision Conflict Screen)
- **Loại:** V (Warning State) · **Mã lỗi:** `PAIRING_006: Revision Mismatch`
- **UI Elements:** Hộp thoại cảnh báo: *"Phương án thi đấu vừa được đối phương chỉnh sửa. Vui lòng xem lại chi tiết mới nhất và xác nhận lại."* Tải lại dữ liệu mới nhất.

#### [PAIR-08] Trạng Thái Kết Thúc Phòng Thương Lượng (Finalized Pairing State)
- **Loại:** V
- **Trạng thái:**
  - `CONFIRMED`: Thông báo chúc mừng ghép đội thành công.
  - `SUPERSEDED`: Thông báo *"Chủ kèo đã hoàn tất ghép đấu với một đội bóng khác."* Khóa chỉnh sửa.

---

### PHẦN V: TUYỂN NGƯỜI ĐÁ LẺ & TRẬN NGOÀI (MERC-01..12, EXT-01..05)

#### [MERC-01] Khám Phá & Tìm Tin Tuyển Đá Lẻ (Mercenary Explore Screen)
- **Loại:** M (Screen) · **Route:** `RouteMercenaryExplore` (Tab 2 BottomNav)
- **Bộ lọc:** Vị trí (GK, DF, MF, FW), Thanh trượt bán kính GPS từ 1 đến 20 km (mặc định 3 km).
- **Fallback GPS:** Nếu từ chối GPS -> Tự động lọc theo Quận/Huyện trong hồ sơ cá nhân.

#### [MERC-02] Chi Tiết Tin Tuyển Người Đá Lẻ (Mercenary Post Detail)
- **Loại:** M (Screen) · **Route:** `RouteMercenaryDetail(postId: String)`
- **QUY CHUẨN BẢO MẬT AC-P05:**
  - Đối với ứng viên chưa được duyệt (`PENDING` hoặc chưa nộp đơn):
    - **TUYỆT ĐỐI ẨN TÊN SÂN, SỐ NHÀ, TỌA ĐỘ VÀ LINK BẢN ĐỒ**.
    - Chỉ hiển thị: Quận/Huyện, Khung giờ, Trình độ đội, Vị trí tuyển, Điểm uy tín đội tuyển.
  - Nút "Nộp đơn ứng tuyển" (mở sheet MERC-03).

#### [MERC-03] Xác Nhận Nộp Đơn Ứng Tuyển (Apply Mercenary Sheet)
- **Loại:** S (Modal Bottom Sheet)
- **Kiểm tra hợp lệ trước khi gửi:**
  - Kiểm tra không trùng giờ với các trận khác đã tham gia.
  - Kiểm tra không phải là thành viên của đội đang tuyển.
  - Nộp thành công -> Đơn ở trạng thái `PENDING`.

#### [MERC-04] Danh Sách Đơn Ứng Tuyển Của Tôi (My Applications Screen)
- **Loại:** M (Screen) · **Route:** `RouteMyMercenaryApplications`
- **Tabs:** 1. Đang chờ (`PENDING`) · 2. Đã duyệt (`APPROVED`) · 3. Đã từ chối / Đã rút (`REJECTED`, `WITHDRAWN`).

#### [MERC-05] Chi Tiết Đơn Ứng Tuyển (Application Detail Screen)
- **Loại:** M (Screen) · **Route:** `RouteApplicationDetail(applicationId: String)`
- **Hành vi khi APPROVED:**
  - **MỞ KHÓA HIỂN THỊ ĐẦY ĐỦ TÊN SÂN BÓNG, ĐỊA CHỈ VÀ BẢN ĐỒ ĐỊNH VỊ**.
  - Hiển thị nút "Nhắn tin cho Đội trưởng" (CHAT-03) và nút "Rút đơn" (MERC-06).

#### [MERC-06] Rút Đơn Ứng Tuyển Đá Lẻ (Withdraw Application Sheet)
- **Loại:** S (Modal Bottom Sheet) · **Nghiệm thu AC-P06:**
  - Rút đơn `PENDING`: Không bị trừ uy tín (0 điểm).
  - Rút đơn `APPROVED`: **Tự động trừ 10 điểm uy tín cá nhân**. Tin tuyển của đội tự động mở lại slot (`FILLED` -> `OPEN`). Chặn rút sau giờ bóng lăn.

#### [MERC-07] Chọn Trận Đấu Để Tuyển Người (Select Match For Recruitment)
- **Loại:** M/S
- **UI Elements:** Danh sách các trận đủ điều kiện: Kèo hệ thống đã `CONFIRMED` hoặc Trận đấu ngoài hệ thống (`EXT-01`).

#### [MERC-08] Đăng Tin Tuyển Đá Lẻ Mới (Create Recruitment Post)
- **Loại:** M (Screen) · **Route:** `RouteMercenaryPostCreate`
- **Quy tắc:** Chọn từ 1 đến 4 vị trí thi đấu. Mỗi vị trí nhập số lượng tuyển từ 1 đến 10 người. Mỗi vị trí được lưu trữ thành 1 bài đăng độc lập theo API spec [DEP-07].

#### [MERC-09] Quản Lý Tin Tuyển Của Đội (Team Recruitment Posts)
- **Loại:** M (Screen) · **Route:** `RouteTeamMercenaryPosts(teamId: String)`
- **Trạng thái tin:** `OPEN`, `FILLED` (đủ người), `CLOSED` (đóng sớm), `EXPIRED`.

#### [MERC-10] Danh Sách Ứng Viên Nộp Đơn (Applicants List)
- **Loại:** M (Screen) · **Route:** `RouteApplicantsList(postId: String)`
- **Quyền:** Chỉ Captain. Hiển thị danh sách ứng viên kèm Điểm uy tín cá nhân, Nút Chat riêng, Nút Duyệt / Từ chối.

#### [MERC-11] Duyệt Hoặc Từ Chối Ứng Viên (Review Applicant Sheet)
- **Loại:** M/S
- **Quy tắc:**
  - Bấm Duyệt -> Đơn thành `APPROVED`.
  - Nếu đã đủ số lượng tuyển -> Tin tự động thành `FILLED` và chặn duyệt thêm.
  - Không có chức năng hủy duyệt (revoke approve) từ phía Đội trưởng.

#### [MERC-12] Điều Chỉnh Số Lượng & Đóng Tin (Manage Post Sheet)
- **Loại:** S (Modal Bottom Sheet)
- **Quy tắc:** Cho phép tăng/giảm `quantity` nhưng **không được nhỏ hơn số lượng ứng viên đã duyệt**. Nút "Đóng tin tuyển ngay" (chuyển sang `CLOSED`).

#### [EXT-01] Khai Báo Trận Đấu Ngoài Hệ Thống (Create External Match)
- **Loại:** M (Screen) · **Route:** `RouteExternalMatchCreate`
- **Fields:** Sân bóng (chọn từ VEN-01), Ngày giờ bắt đầu/kết thúc, Tên đối thủ giao lưu (tùy chọn), Khai báo các vị trí cần tuyển đá lẻ đi kèm.

#### [EXT-02] Chi Tiết Trận Đấu Ngoài Hệ Thống (External Match Detail)
- **Loại:** M (Screen) · **Route:** `RouteExternalMatchDetail(matchId: String)`
- **UI Elements:** Thông tin trận, Danh sách tin tuyển đá lẻ gắn liền, Danh sách ứng viên tham gia.

#### [EXT-03] Sửa Thông Tin Trận Ngoài (Edit External Match)
- **Loại:** M (Screen) · **Route:** `RouteExternalMatchEdit(matchId: String)`
- **Quy tắc:** Khóa không cho sửa Sân và Giờ thi đấu nếu trận đấu đã có ít nhất 1 ứng viên đá lẻ ở trạng thái `APPROVED`.

#### [EXT-04] Hủy Trận Đấu Ngoài Hệ Thống (Cancel External Match Sheet)
- **Loại:** S (Modal Bottom Sheet)
- **Hành vi:** Hủy trận ngoài trước giờ đá -> Tự động đóng toàn bộ tin tuyển và hủy các đơn đá lẻ liên quan.

#### [EXT-05] Thêm Vị Trí Tuyển Cho Trận Ngoài (Add Position Sheet)
- **Loại:** M/S
- **Hành vi:** Bổ sung thêm vị trí cần tuyển người mới vào trận ngoài đang có sẵn.

---

### PHẦN VI: ĐỊA ĐIỂM & SÂN BÓNG (VEN-01..07)

#### [VEN-01] Tìm & Chọn Sân Bóng (Venue Picker Screen / Sheet)
- **Loại:** M/S · **Route:** `RouteVenuePicker(returnKey: String)`
- **YÊU CẦU BẢO LƯU DỮ LIỆU FORM (J-16):**
  - Khi mở từ Form tạo kèo (`MATCH-05`) hoặc Trận ngoài (`EXT-01`), sau khi chọn sân xong -> **Quay lại form cũ và giữ nguyên 100% dữ liệu đã nhập**.

#### [VEN-02] Chi Tiết Sân Bóng (Venue Detail Screen)
- **Loại:** M (Screen) · **Route:** `RouteVenueDetail(venueId: String)`
- **UI Elements:** Gallery ảnh, Tên sân, Địa chỉ, Số liên hệ, Loại sân (5/7/11), Tiện ích, Nút "Báo cáo sai lệch" (VEN-06). Tuyệt đối **không có chức năng đặt sân hay thanh toán tiền sân**.

#### [VEN-03] Thêm Sân Bóng Mới (Add Venue Screen)
- **Loại:** M (Screen) · **Route:** `RouteVenueCreate`
- **Fields:** Tên sân, Địa chỉ chi tiết, Phường/Xã, Số liên hệ, Loại sân. Tạo xong có hiệu lực chọn ngay lập tức.

#### [VEN-04] Sửa Thông Tin Sân Bóng (Edit Venue Screen)
- **Loại:** M (Screen) · **Route:** `RouteVenueEdit(venueId: String)`
- **Quyền:** Chỉ Người tạo sân hoặc ADMIN hệ thống. Báo lỗi 403 nếu không đủ quyền.

#### [VEN-05] Quản Lý Thư Viện Ảnh Sân (Venue Photos Management)
- **Loại:** M/S · **Route:** `RouteVenuePhotos(venueId: String)`
- **Quy định API:** Tối đa 5 ảnh, đúng 1 ảnh chính (`isPrimary: true`). Hỗ trợ xóa ảnh và đặt ảnh chính.

#### [VEN-06] Báo Cáo Sân Sai Lệch / Đóng Cửa (Report Venue Sheet)
- **Loại:** S (Modal Bottom Sheet)
- **Lý do:** Sai số điện thoại, Sai địa chỉ, Sân đã đóng cửa / giải tỏa. Gửi vào hàng đợi Admin (ADM-07).

#### [VEN-07] Trạng Thái Sân Ngừng Hoạt Động (Inactive Venue State)
- **Loại:** V
- **UI Elements:** Banner cảnh báo màu vàng: *"Sân bóng đã ngừng hoạt động"*. Trong bộ chọn `VEN-01`, sân này bị disable không cho chọn.

---

### PHẦN VII: TIN NHẮN & THÔNG BÁO (CHAT-01..05, NOTIF-01..03)

#### [CHAT-01] Hộp Thư & Danh Sách Trò Chuyện (Inbox Screen)
- **Loại:** M (Screen) · **Route:** `RouteInbox` (Tab 3 BottomNav)
- **Tabs:** 1. Kèo giao lưu · 2. Người đá lẻ. Hiển thị tin nhắn cuối cùng, thời gian, số tin chưa đọc.

#### [CHAT-02] Phòng Chat Giao Lưu 2 Đội (Match Chat Room)
- **Loại:** M (Screen) · **Route:** `RouteMatchChat(roomId: String)`
- **UI Elements:** Header thông tin kèo. Bong bóng chat hiển thị Tên thật người gửi và nhãn Đội A / Đội B.

#### [CHAT-03] Phòng Chat Captain & Người Đá Lẻ (Mercenary Chat Room)
- **Loại:** M/V · **Route:** `RouteMercenaryChat(applicationId: String)`
- **Header:** Thông tin đơn ứng tuyển, vị trí thi đấu, nút xem hồ sơ cá nhân.

#### [CHAT-04] Trạng Thái Phòng Chat (Room States & Read-Only)
- **Loại:** V · **Quy tắc `canSend`:**
  - `canSend == false` khi: Kèo bị hủy, hoặc phòng chat quá hạn lưu trữ 90 ngày.
  - Thanh nhập tin nhắn bị ẩn hoặc khóa xám kèm thông báo: *"Phòng chat ở chế độ chỉ đọc."*

#### [CHAT-05] Báo Cáo Vi Phạm Trong Chat (Report Chat Sheet)
- **Loại:** S (Modal Bottom Sheet)
- **Lý do:** Ngôn từ xúc phạm, Quấy rối, Lừa đảo.

#### [NOTIF-01] Trung Tâm Thông Báo (Notifications Center Screen)
- **Loại:** M (Screen) · **Route:** `RouteNotifications`
- **Tabs:** "Tất cả" và "Chưa đọc". Nút "Đánh dấu tất cả là đã đọc". Thời hạn lưu trữ tối đa 30 ngày.

#### [NOTIF-02] Xử Lý Điều Hướng An Toàn (Safe Target Resolver)
- **Loại:** V · **Nghiệm thu AC-P09:**
  - Khi nhấn vào thông báo trỏ tới đối tượng đã bị xóa hoặc mất quyền truy cập:
  - **Không bao giờ hiển thị màn hình trắng (White Screen)**.
  - Điều hướng tới trang thông báo thân thiện: *"Nội dung này không còn tồn tại hoặc bạn không có quyền xem"* kèm nút quay lại.

#### [NOTIF-03] Danh Mục 35 Loại Thông Báo Mẫu (35 Notification Catalog)
- **Loại:** V
- **Mapping:** Bao phủ toàn bộ 35 mã thông báo (Kèo, Lời mời đội, Tuyển người, Đánh giá, Báo cáo, Tranh chấp) tới đúng Screen ID tương ứng.

---

### PHẦN VIII: SAU TRẬN, UY TÍN & TRANH CHẤP (REP-01..10)

#### [REP-01] Đánh Giá Đối Phương Sau Trận (Post-Match Rating Screen)
- **Loại:** M (Screen) · **Route:** `RouteRateMatch(matchId: String)`
- **UI Elements:**
  - Chấm 1 đến 5 sao nguyên.
  - Nhập nhận xét tự do (tối đa 500 ký tự).
  - Nhập tỷ số trận đấu (tùy chọn).
  - Công tắc cờ *"Đối thủ không đến (NO-SHOW)"*.

#### [REP-02] Xem Kết Quả Đánh Giá Trận Đấu (Match Ratings View)
- **Loại:** M/V · **QUY CHUẨN ĐÁNH GIÁ ẨN AC-P07 (BLIND RATING):**
  - **Khi bên mình CHƯA gửi đánh giá:** Giao diện hoàn toàn **KHÔNG TIẾT LỘ** đối phương đã đánh giá hay chưa. Chỉ hiện form nhắc bên mình đánh giá.
  - **Khi CẢ HAI BÊN ĐÃ GỬI:** Mở khóa hiển thị công khai số sao và nhận xét của cả 2 phía.
  - Không cho phép chỉnh sửa hoặc xóa đánh giá đã nộp.

#### [REP-03] Đánh Giá Hai Chiều Với Người Đá Lẻ (Mercenary Rating Screen)
- **Loại:** M (Screen) · **Route:** `RouteRateMercenary`
- **Quy tắc:** Đội trưởng chấm điểm cho từng cầu thủ đá lẻ; Cầu thủ đá lẻ chấm điểm cho đội bóng. Có cờ Không đến riêng.

#### [REP-04] Thẻ Uy Tín Đội Bóng (Team Reputation Card)
- **Loại:** M/S
- **Quy định spec:**
  - Điểm uy tín (Reputation Score, ví dụ: 95/100).
  - Tỷ lệ hoàn thành trận (Completion Rate): **Chỉ hiển thị khi đội đã thi đấu từ 5 trận trở lên**. Dưới 5 trận thì ẩn tỷ lệ.
  - Huy hiệu uy tín (Badge: Đội bóng uy tín, Đúng giờ). Đánh giá từ đá lẻ hiển thị ở mục riêng biệt.

#### [REP-05] Thẻ Uy Tín Cá Nhân Cầu Thủ (Player Reputation Card)
- **Loại:** M/S
- **UI Elements:** Điểm cá nhân (gốc 100), Số trận đã tham gia, Tỷ lệ đúng giờ.

#### [REP-06] Gửi Báo Cáo Vi Phạm (Submit Report Sheet)
- **Loại:** M/S
- **4 Loại vi phạm chuẩn:** `NOSHOW` (Không đến), `VIOLENCE` (Bạo lực sân cỏ), `CHEATING` (Gian lận trình độ/người), `BAD_BEHAVIOR` (Thái độ phi thể thao).

#### [REP-07] Danh Sách Báo Cáo Vi Phạm (Reports List Screen)
- **Loại:** M (Screen) · **Route:** `RouteReportsList`
- **Tabs:** "Báo cáo đã gửi" và "Bị báo cáo vi phạm".

#### [REP-08] Chi Tiết Báo Cáo Vi Phạm (Report Detail Screen)
- **Loại:** M (Screen) · **Route:** `RouteReportDetail(reportId: String)`
- **UI Elements:** Nội dung tố cáo, Mức trừ điểm uy tín dự kiến, Đồng hồ đếm ngược 24 giờ khiếu nại tranh chấp (với báo cáo NO-SHOW).

#### [REP-09] Gửi Khiếu Nại Tranh Chấp NO-SHOW (Dispute NO-SHOW Sheet)
- **Loại:** M/S · **Nghiệm thu AC-P08:**
  - **Chỉ áp dụng duy nhất cho loại báo cáo NO-SHOW và còn trong vòng 24 giờ**.
  - Các loại vi phạm khác hoặc đã quá 24h: Nút Khiếu nại bị vô hiệu hóa hoặc ẩn.
  - Form nhập bằng chứng giải trình bằng văn bản (10 - 1000 ký tự).

#### [REP-10] Biến Thể Kết Quả Xử Lý Tranh Chấp (Dispute Outcomes)
- **Loại:** V
- **Trạng thái:**
  - `IN_REVIEW`: Chờ Admin phân xử.
  - `RESOLVED_UPHELD`: Khiếu nại bị bác bỏ -> Giữ nguyên mức phạt điểm uy tín.
  - `RESOLVED_DISMISSED`: Khiếu nại được chấp thuận -> Hủy báo cáo, hoàn trả điểm uy tín. Quyết định cuối cùng không cho khiếu nại lại.

---

### PHẦN IX: QUẢN TRỊ HỆ THỐNG (ADM-01..09)

#### [ADM-01] Cổng Vào Quản Trị Hệ Thống (Admin Dashboard)
- **Loại:** M (Screen) · **Route:** `RouteAdminDashboard`
- **BẢO MẬT BẮT BUỘC:** Chỉ tài khoản có `role == "ADMIN"` mới được truy cập. Nếu user thường cố tình truy cập -> Chặn lại và trả về lỗi HTTP 403 Forbidden.

#### [ADM-02] Quản Lý & Kiểm Duyệt Đội Bóng (Admin Teams Management)
- **Loại:** M (Screen) · **Route:** `RouteAdminTeams` (Nguồn: Stitch S13)
- **Hành động:** Nút "Tạm ngưng hoạt động" (`SUSPENDED`) hoặc "Mở khóa đội" kèm ô nhập lý do bắt buộc.

#### [ADM-03] Lịch Sử Thành Viên Đội Cho Admin (Admin Team Member History)
- **Loại:** M (Screen) · **Route:** `RouteAdminTeamMemberHistory` (Nguồn: Stitch S14)
- **Đặc quyền Admin:** Khác với `TEAM-06`, tài khoản đã xóa hiển thị **Tên thật** kèm cờ đỏ `[TÀI KHOẢN ĐÃ XÓA]` theo đúng hợp đồng dữ liệu Admin.

#### [ADM-04] Hàng Đợi Tranh Chấp Đội Bóng (Admin Team Disputes Queue)
- **Loại:** M (Screen) · **Route:** `RouteAdminTeamDisputes`
- **UI Elements:** Danh sách các vụ tranh chấp NO-SHOW giữa 2 đội bóng chưa giải quyết.

#### [ADM-05] Hàng Đợi Tranh Chấp Người Đá Lẻ (Admin Mercenary Disputes Queue)
- **Loại:** M (Screen) · **Route:** `RouteAdminMercenaryDisputes`
- **UI Elements:** Danh sách các vụ khiếu nại giữa Captain và Cầu thủ đá lẻ.

#### [ADM-06] Biểu Mẫu Phán Quyết Tranh Chấp (Resolve Dispute Sheet)
- **Loại:** S (Modal Bottom Sheet)
- **UI Elements:**
  - Nút "Chấp thuận khiếu nại (Hủy phạt)" (Xanh `#3d6943`).
  - Nút "Bác bỏ khiếu nại (Giữ phạt)" (Đỏ `#a73b21`).
  - **Trường Ghi chú quyết định: Bắt buộc từ 1 đến 500 ký tự**. Không được để trống khi ra phán quyết.

#### [ADM-07] Hàng Đợi Xử Lý Báo Cáo Sân Bóng (Admin Venue Reports Queue)
- **Loại:** M (Screen) · **Route:** `RouteAdminVenueReports`
- **Hành vi:** Xem danh sách phản ánh sân sai lệch, nút mở Sửa sân hoặc Đổi trạng thái sân.

#### [ADM-08] Bật / Tắt Hoạt Động Của Sân Bóng (Toggle Venue Status Sheet)
- **Loại:** S (Modal Bottom Sheet)
- **Hành động:** Chuyển đổi trạng thái giữa `ACTIVE` và `INACTIVE` kèm lý do lưu vào hệ thống.

#### [ADM-09] Quản Lý Danh Mục Bộ Môn Thể Thao (Sports Management Screen)
- **Loại:** M/S · **Route:** `RouteAdminSports`
- **UI Elements:** Danh sách các môn (Bóng đá 5, 7, 11 người, Futsal), công tắc Switch Bật/Tắt môn thi đấu.
