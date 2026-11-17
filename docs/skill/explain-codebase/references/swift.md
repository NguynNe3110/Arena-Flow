# Swift, SwiftUI và UIKit

## Target và entry point

- Xác định Xcode project/workspace, target, build configuration, target membership và Swift package liên quan. File tồn tại trong repo chưa đảm bảo được build vào target đang xét.
- Tìm entry thực tế: `@main App`, app/scene delegate, storyboard hoặc entry point của command-line/package. SwiftUI và UIKit có thể cùng tồn tại; theo integration đang dùng thay vì gắn toàn app vào một kiểu.
- Theo nơi tạo scene/window/root view/controller, dependency injection, coordinator/router và điều kiện chọn màn hình đầu. Phân biệt hệ thống gọi lifecycle callback với lời gọi method do app chủ động thực hiện.
- Đối chiếu minimum deployment target/toolchain/dependency khi nó ảnh hưởng API observation/concurrency đang được giải thích; không tự thay code cũ bằng API mới trong bài giảng.

## State, observation và lifecycle

Theo user action/delegate callback → view model/controller/state owner → service/use case nếu có → kết quả → observable/binding/publisher/callback → view hoặc consumer cập nhật.

- Với SwiftUI, tìm property wrapper và observation thật: `@State`, `@Binding`, `@StateObject`, `@ObservedObject`, environment hoặc `@Observable`/`@Bindable` tùy dự án. Giải thích ai sở hữu instance và ai chỉ nhận tham chiếu/binding; không coi các cơ chế này hoán đổi được.
- Phân biệt view value và identity/lifetime của state. `body` được đánh giá lại không đồng nghĩa tạo lại mọi object. Theo `.task`, `.onAppear` và key/identity liên quan khi chúng quyết định thời điểm và hủy việc.
- Với UIKit, theo controller lifecycle, delegate/data source, target-action, completion và coordinator. Nêu chỗ cập nhật view thay vì ép vào cơ chế SwiftUI observation.
- Với async/await, `Task`, actor, `@MainActor` hoặc Combine, xác định nơi tạo task/subscription, isolation/scheduler, thời điểm đổi state, error propagation và cancellation. Không coi `async` là bảo đảm chạy background; không coi mọi `Task` đều có lifecycle/hủy giống nhau.
- Khi closure giữ reference và ownership ảnh hưởng hành vi/lifetime, đọc capture list, holder và cleanup. Giải thích `weak`/`unowned` theo quan hệ cụ thể; không tuyên bố memory leak chỉ từ một closure.
- Với model/DTO, Codable, storage/Core Data/SwiftData hoặc URLSession/SDK, chỉ ra schema/adapter/mapping và consumer thực tế. Không suy ra persistence, caching hay validation chỉ từ tên type.

## Ranh giới và test

Khi luồng qua Objective-C, C/C++, Kotlin shared hoặc SDK, xác minh adapter/bridge/header/API và target liên quan nếu có; đánh dấu source bên ngoài. Đọc XCTest/Swift Testing/UI test đúng phạm vi, phân biệt dữ liệu fixture/mock với runtime thật. Lộ trình nên dẫn entry/root → view/controller → owner/handler → dependency → state/result → consumer, kèm điều kiện target và lifecycle có tác động.
