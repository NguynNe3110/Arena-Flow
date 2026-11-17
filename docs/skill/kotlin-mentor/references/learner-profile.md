# Hồ sơ người học Kotlin

Tổng hợp ngày 2026-11-12 từ yêu cầu hiện tại, hai hồ sơ MY và chín file my-time-line. Đây là bằng chứng lịch sử và tự báo; chưa phải kết quả kiểm tra trực tiếp. Nguồn chi tiết trong [source-map](source-map.md). Kết quả thực hành mới nằm trong [progress](progress.md).

## Định hướng được xác nhận

- Chuyên tâm Kotlin, hướng đến Android Native; muốn quay lại nhanh và có mentor chủ động chọn điều cần học. Không tự đưa Flutter trở lại làm hướng học song song.
- Muốn AI biết điều đã tiếp cận, điều còn yếu và mở những phần chưa có cơ hội học. Không muốn tiếp tục tự mò nhiều kiến thức mới cùng lúc.
- Cần hiểu bản chất, luồng dữ liệu và vai trò abstraction; thích một project xuyên suốt, cách đặt tên và kiến trúc nhất quán.
- Hồ sơ explain-codebase ngày 2026-11-04 ghi tự báo “Chưa chắc cú pháp, API và khái niệm nền tảng”. Đây là mốc trước yêu cầu này, cần điều chỉnh bằng bài làm mới thay vì đóng đinh mức độ.

## Bản đồ bằng chứng ban đầu

| Nhóm | Bằng chứng trong tài liệu | Cách dùng khi dạy |
|---|---|---|
| `val/var`, data class, constructor/default/nullable, `copy`, `?.`, `?:`, `!!` | MY-Kotlin §2–4 mô tả đã tiếp xúc; §4 yêu cầu kiểm tra smart cast, `as?`, `requireNotNull` | Khởi động bằng một model, một biến đổi và dự đoán output; không giảng lại nhập môn lập trình |
| Package/import, resource annotation, Parcelable | MY-Kotlin §5–7 mô tả thảo luận cụ thể; resource ID và annotation được hồ sơ báo đã hiểu | Dùng khi luồng Android cần; chưa phải ưu tiên lấy lại ngôn ngữ |
| Class/interface, visibility, generics, lambda, higher-order, extension, scope functions, collections, sealed | MY-Kotlin §8–9 chủ yếu là lộ trình; §XI đánh dấu cần xác minh | Đây là các tiền đề cần lấy mẫu trực tiếp, chưa thể kết luận đã học hết hoặc chưa biết |
| UI action → ViewModel → state → UI; repository, UiState, immutable exposure | MY-Kotlin §10–15 có mô tả cách hiểu và thảo luận; timeline có bài state/MVVM | Dùng làm ngữ cảnh quen, nhưng yêu cầu trace/copy state cụ thể để biết còn nhớ gì |
| Coroutine/dispatcher/Flow | MY-Kotlin §16–18 có cả mục đã tiếp cận và mục tiêu; §XI yêu cầu kiểm tra sâu | Chưa xác minh `suspend`, cancellation, scope, hot/cold, operators; dạy trong request/search thực tế |
| DTO/domain/mapper/repository/use case | MY-Kotlin §19–20 và MY-Flutter §4 có thảo luận | Có vốn từ kiến trúc để nối bài; chưa chứng minh tự cài đặt không mẫu |
| Retrofit/Room/Hilt/Compose/navigation/Firebase/cache | MY-Kotlin §21–30; timeline liệt kê nhiều chặng, không có nhật ký hoàn thành | Kiểm tra đúng phần cần dùng; không coi các bài đánh số là bài đã xong |
| Dart generic/constraint/mixin/factory, Future, cast và fromJson | MY-Flutter §2, §5 ghi các câu hỏi cụ thể | Mốc so sánh tùy chọn; generic Dart không chứng minh nắm variance/reified của Kotlin |
| BlocBuilder/Listener, Base/Common/AppBloc, DI generated, Supabase/cart | MY-Flutter §3–7, §12 và bloc_clean.txt | Người học từng bóc cơ chế của base; giữ cách giảng ai gọi/ai tạo/ai nhận dữ liệu, không dựng lại cả framework |
| Tự viết, tự debug, giải thích biến thể hôm nay | Chưa có bài làm trực tiếp trong phiên xây skill | Để chưa xác minh theo từng kỹ năng; không xếp junior/middle hoặc chấm điểm tổng |

## Mốc dự án từ hồ sơ explain-codebase cũ

Nguồn: `explain-codebase/references/learner-profile.md`, khảo sát ghi ngày 2026-11-04; phiên xây mentor này **không mở lại code các repo**.

- Customer, customer_v2, customer_v3 dưới `D:/AppData/Code/Prodech/adroi/`: hồ sơ ghi người dùng tự viết phần lớn; có login Fragment/ViewModel, Compose, đăng ký còn dở ở thời điểm khảo sát.
- Flutter `D:/AppData/Code/Prodech/adroi/fahion`: đúng mốc tự viết phần lớn trong hồ sơ cũ; dùng cart quantity/callback/DI khi so sánh thật sự hữu ích.
- Base `D:/AppData/Code/base/android-showcase`: mốc đang học theo hồ sơ cũ. `D:/AppData/Code/Project/Android/DiuAppChat` là checkout riêng, không dùng file của base để kết luận về checkout đó.
- Các đường dẫn này có thể thiếu trên máy khác hoặc đã thay đổi. Việc có symbol trong repo chỉ chứng minh đã gặp, không chứng minh hiểu hoặc tự viết symbol đó.

## Những mâu thuẫn phải giữ rõ

- MY-Flutter mô tả giai đoạn thủ công trước code generation; FLUTTER_CONTRACT nói production đã mở khóa; file tiêu chuẩn có cả hai. Không biết thứ tự thật chỉ từ tên file hoặc mtime. Đây là lịch sử mục tiêu, không phải một bộ luật hợp nhất.
- Ghi chú định hướng cho phép loading/error trong render state; contract production cấm chúng trong feature state và gom CommonBloc. bloc_clean mô tả CommonBloc theo trang còn contract gọi singleton. Không suy ra scope hiện tại nếu chưa xem wiring thật.
- Timeline có cả yêu cầu map Kotlin/Flutter 1:1 và các giản lược về coroutine/event/lifecycle. Giữ giá trị sư phạm “tận dụng kinh nghiệm cũ”, kiểm tra lại kỹ thuật theo tài liệu chính thức; không khẳng định các runtime tương đương.
- “8 tuần”, “35 bài”, “sau bài này đã build được app” là kế hoạch/kỳ vọng của tài liệu. Không phải ngày học thật, bằng chứng hoàn thành hoặc cam kết tốc độ.

## Điểm bắt đầu được đề xuất

Lấy lại **đọc chữ ký → nhận diện hàm được truyền/chạy → biến đổi model/collection**. Đây là giả thuyết sư phạm từ chỗ vướng đã tự báo, cần điều chỉnh ngay nếu người học làm tốt. Sau đó mới kiểm tra `suspend`/Flow và lifecycle qua một tính năng. Android là ứng dụng của nền tảng Kotlin, không thay thế việc tự viết Kotlin.
