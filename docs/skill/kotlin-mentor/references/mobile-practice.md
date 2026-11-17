# Dẫn đường học mobile, ưu tiên Kotlin/Android

Dùng khi cần biến kiến thức ngôn ngữ thành bài mobile hoặc chọn nguồn kỹ thuật tiếp theo. Nguồn GitHub đã đọc và giới hạn áp dụng trong [github-sources.md](github-sources.md). Các skill kỹ thuật ngoài gói là tài liệu tham khảo tùy chọn; mentor vẫn dạy được khi chúng chưa cài hoặc không truy cập mạng.

## Một tính năng nhỏ xuyên suốt

Ưu tiên miền của dự án hiện tại. Nếu chưa có dự án, dùng danh sách Ticket: mỗi lượt mở rộng một hành vi quan sát được. Không dựng toàn app ở bước đầu và không khẳng định đây là bài người học chưa biết trước khi lấy mẫu.

| Bước thực hành | Kotlin cần dùng | Ngữ cảnh Android và bằng chứng cần thấy |
|---|---|---|
| Đổi tên một Ticket | data class, nullable, hàm, `copy` | Model trước/sau, giá trị gốc giữ hay đổi; tự viết hàm bằng input khác |
| Tìm/lọc Ticket | List, lambda, function type, `firstOrNull`, `map/filter` | Ô tìm kiếm dùng hàm thuần; giải thích khi không tìm thấy, chưa cần network |
| Một action đổi state | Callback, sealed/when khi cần, visibility | UI gửi action, ViewModel cập nhật UiState, UI render; chỉ ra state owner |
| Tải dữ liệu fake có delay/lỗi | `suspend`, coroutine scope, cancellation | loading/success/error theo từng bước; đổi query liên tiếp và dự đoán task cũ |
| Lưu và refresh | Interface/implementation, mapping | Fake repository trước; Room/Retrofit khi cần; phân biệt dữ liệu đang hiển thị và nguồn refresh |
| Khôi phục và quay lại màn hình | Các khái niệm lifecycle và state đã học | Phân biệt recomposition, rotation, back stack, process recreation; thử đúng hành vi đang cần |
| Kiểm chứng một biến thể | Hàm thuần, fake, assertion, coroutine test theo stack | Ca tìm rỗng hoặc request lỗi; kiểm tra hành vi có ý nghĩa, không chạy cả chiến dịch test cho một ví dụ nhỏ |

Mỗi bước gồm phần giảng, một ví dụ và phần tự làm theo teaching-playbook. Có thể bỏ qua bước đã có bằng chứng vững. UI đẹp, animation, nhiều module, framework DI và tối ưu sâu là các nhánh sau khi tính năng tạo nhu cầu.

## Khi dùng kho skill kỹ thuật

| Tín hiệu từ bài/project | Nguồn nên tra | Chuyển thành bài học như thế nào |
|---|---|---|
| Muốn biết kiểm thử logic/UI ở đâu, đang dùng framework gì | `android/skills` → `testing/testing-setup` | Đọc stack test/source set hiện tại, chọn một behavior và fake tối thiểu; giải thích local test khác instrumented test. Không áp nguyên quy trình cài DI, coverage, screenshot suite của upstream chỉ để học một hàm |
| UI cuộn giật, nghi request/recomposition lặp, muốn hiểu đo hiệu năng | `android/skills` → `jetpack-compose/jetpack-compose-performance` | Tách phỏng đoán và phép đo; đọc reference ứng với phát hiện rồi giải thích một nguyên nhân trong source. Không suy số lần recomposition là mức độ giật và không dùng audit toàn repo làm bài nhập môn |
| Thực sự đang xử lý navigation, back hoặc adaptive layout | Danh mục `android/skills` → `navigation/navigation-3`, `navigation/navigation-event`, `jetpack-compose/adaptive` | Đây mới là điểm tra cứu theo danh mục đã xem, chưa thẩm định sâu nội dung. Mở skill hiện tại trước khi dùng, đối chiếu API trong project; không tự chuyển navigation hoặc bật API mới |
| Bị chặn bởi Gradle/Kotlin toolchain hoặc đang được giao migration | Danh mục `Kotlin/kotlin-agent-skills` | Chọn skill đúng tác vụ, đọc và xác minh trước khi áp dụng. Toolchain/backend/migration không phải khóa học Kotlin nền tảng; không mở vì cùng tên Kotlin |

Nếu cần chạy hoặc sửa một tác vụ kỹ thuật thật, thực hiện theo yêu cầu người dùng và phạm vi đang có, dùng skill tương ứng nếu sẵn. Chỉ hỏi học không tự tạo quyền refactor, nâng phiên bản, cài framework hoặc chạy dịch vụ.

## Khám phá nhánh mobile tiếp theo

Khi người học muốn biết “tôi còn thiếu gì”, đối chiếu sản phẩm và bài làm với các nhóm sau; đưa một ưu tiên kế tiếp cùng lý do:

- Ngôn ngữ và đọc code: chữ ký, type, lambda, collection, null/equality, contract.
- Luồng và vòng đời: state owner, coroutine cancellation, lifecycle, restoration, navigation.
- Dữ liệu: network failure, DTO/domain, DB/migration, cache/source of truth.
- Khả năng kiểm chứng: unit/Flow/UI tests, debug, đo performance khi có triệu chứng.
- Chất lượng sản phẩm: empty/error/offline UI, accessibility/font scale, kích thước màn hình, permissions khi tính năng cần.

Đây là bản đồ xem xét, không phải danh sách tất cả đều còn thiếu. Chọn kiến thức sát mục tiêu và tiền đề hiện tại. Khi người dùng chủ động mở sang stack mobile khác, dùng lại phương pháp mentor và tạo hồ sơ riêng cho stack đó; không tự coi kiến thức Kotlin chuyển thành năng lực ở framework khác.
