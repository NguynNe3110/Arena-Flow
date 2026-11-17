# Kotlin, Android và Kotlin Multiplatform

## Xác định ứng dụng thực sự được lắp như thế nào

- Đối chiếu `settings.gradle(.kts)`, build file của module, version catalog, convention plugin hoặc `build-logic` khi chúng quyết định plugin/dependency/source set. Phân biệt module có trên đĩa với module được include và dependency của app target đang xét.
- Với Android, đọc manifest của source set/variant liên quan để tìm `Application`, launcher `Activity`, service, receiver, provider và deep link. Nếu chưa có merged manifest, nêu giới hạn khi manifest overlay có thể đổi kết quả.
- Theo `Application.onCreate`, khởi tạo DI và `Activity.onCreate`/`setContent` hoặc XML/Fragment. Không coi `main()` là entry point mặc định của app Android; với Kotlin JVM/server/CLI thì xác định entry point đúng môi trường.
- Xác minh màn hình đầu bằng router/NavHost, graph, start destination và điều kiện session/onboarding thực tế, không chỉ bằng một class tên `Main`.

## Theo feature và chiều state quay về

Với Compose, tìm composable route/screen, event lambda, ViewModel hoặc state holder, method được gọi, nghiệp vụ/data source và nơi expose/collect state. Chỉ dùng các layer có thật. Với View/XML, theo listener, Fragment/Activity, ViewModel/presenter và binding/render tương ứng.

- Phân biệt state nằm trong `remember`, được lưu bằng cơ chế saveable, nằm ở ViewModel hay persisted storage. Giải thích phạm vi tồn tại từ code và chủ sở hữu, không khẳng định mọi state đều sống qua process death.
- Với `StateFlow`, `SharedFlow`, `Flow`, channel hoặc LiveData, tìm producer và consumer. Kiểm tra initial value, sharing policy, collect/observe, lifecycle và xử lý event một lần nếu chúng ảnh hưởng luồng.
- Phân biệt composition/recomposition với khởi tạo Activity. Side effect như `LaunchedEffect` phải được giải thích theo key và chỗ hủy; không mô tả body composable như chỉ chạy một lần.
- Với coroutine, xác minh scope, dispatcher/context, suspend point, cancellation và error handling. `suspend` không tự chuyển việc sang background; `launch` không trả kết quả như lời gọi đồng bộ; Flow chưa collect có thể chưa làm I/O. Dùng implementation cụ thể để kết luận.
- Đọc repository implementation, mapper DTO/entity/domain/UI nếu có. Với Retrofit/Ktor/Room/DataStore hoặc SDK khác, chỉ nói cơ chế thực sự được cấu hình và gọi.

## DI và đa nền tảng

- Koin: theo module được load, `single`/`factory`/ViewModel registration, qualifier và tham số. Hilt/Dagger: theo component/module/binding/scope phù hợp. Manual DI: theo constructor/factory và composition root. Một annotation hoặc interface riêng lẻ chưa đủ chứng minh object đang được dùng.
- Với KMP, xác định `commonMain`, platform source set, target, `expect`/`actual`, adapter và biên gọi Swift/JVM khi luồng đi qua đó. Không gán Android lifecycle cho code shared thuần Kotlin.
- Phân biệt `test` và instrumented test/`androidTest`. Tìm test minh họa quy tắc hoặc state transition thật; lệnh chạy cần khớp module/variant và không được tuyên bố pass nếu chưa chạy.

## Điểm người mới cần tự lần được

Một thao tác trên màn hình → callback/intent → state holder → dependency đã được lắp → nghiệp vụ/I/O nếu có → kết quả/state → consumer cập nhật UI. Giải thích symbol thật ở từng đoạn, phạm vi task và điều gì xảy ra khi người dùng rời màn hình. Nếu source thiếu hoặc module là scaffold, chỉ ra phần wiring/implementation còn thiếu.
