---
name: explain-codebase
description: "Explain unfamiliar code and codebases with source evidence, execution traces, and a guided reading path, adapting to the learner's background and gaps. Use for codebase onboarding, understanding a feature/file, or requests such as 'giải thích codebase này chi tiết' and 'không biết bắt đầu đọc code từ đâu', across languages including Kotlin, Dart/Flutter, React Native, and Swift. Focus on understanding; do not turn the request into implementation or a general code review."
---

# Giải thích codebase

Giúp người đọc hình thành mô hình đủ chính xác để tự lần theo code, giải thích một tính năng và biết bắt đầu thay đổi ở đâu. Chi tiết phải đến từ quan hệ giữa các đoạn code và hành vi cụ thể, không phải từ độ dài bài viết.

## Bắt đầu dù người dùng chưa biết hỏi gì

- Với yêu cầu rộng như “giải thích codebase này chi tiết”, tự khảo sát và chọn lộ trình. Không yêu cầu người dùng chọn file, kiến trúc hay tính năng trước khi cung cấp giá trị.
- Dùng ngôn ngữ người dùng đang dùng; mặc định tiếng Việt khi chưa có tín hiệu khác. Giữ nguyên tên symbol, API và thuật ngữ tìm kiếm được.
- Ưu tiên điều người dùng nói về chỗ còn vướng, rồi hồ sơ học tập, sau đó mới dùng giả định mặc định. Nếu chưa có thông tin, giả định người dùng biết code cơ bản nhưng chưa quen dự án và framework cụ thể. Giải nghĩa khái niệm tại lần đầu nó cần thiết; điều chỉnh theo phản hồi, không gắn nhãn trình độ cho người dùng.
- Với một file/hàm/tính năng cụ thể, tập trung vào nó và chỉ mở rộng đến caller, dependency, state và cấu hình cần để hiểu. Đọc [walkthrough.md](references/walkthrough.md) khi cần giải thích sâu theo luồng hoặc theo đoạn code.
- Chỉ hỏi khi thiếu quyền truy cập/mã nguồn hoặc có nhiều sản phẩm độc lập mà chọn sai sẽ đổi hẳn câu trả lời. Trong lúc đó vẫn khảo sát phần đã rõ. Nếu không có repo, nêu chính xác phần mã hoặc đường dẫn cần cung cấp; không giả lập kết quả khảo sát.
- Đây là tác vụ đọc và giải thích. Không tự sửa code, tạo tài liệu trong repo, cài dependency, chạy ứng dụng hay gọi dịch vụ thật chỉ để giải thích. Nếu người dùng đồng thời yêu cầu thay đổi hoặc chạy kiểm chứng, thực hiện trong phạm vi đã được giao và phân biệt kết quả đọc code với kết quả chạy.

## Cá nhân hóa theo người học

Với chủ skill đang quay lại Kotlin (ưu tiên xác nhận ngày 2026-11-12), khi cần xem phần đã tiếp cận trong MY-Kotlin/MY-Flutter hoặc chọn một tiền đề để giải thích, đọc [hồ sơ Kotlin mentor](../kotlin-mentor/references/learner-profile.md) và [tiến độ hiện tại](../kotlin-mentor/references/progress.md) nếu gói này có sẵn. Hồ sơ cũ bên dưới vẫn là mốc lịch sử, không lấn át ưu tiên Kotlin mới. Khi người dùng muốn được dạy, ôn có hệ thống hoặc tìm kiến thức chưa tiếp cận, phối hợp [kotlin-mentor](../kotlin-mentor/SKILL.md): trace code thật trước, rồi nối sang một bài học liên quan. Yêu cầu giải thích đơn thuần vẫn được trả lời trọn vẹn, không biến thành bài thi hoặc tự ghi đánh giá năng lực. Nếu thiếu gói mentor, tiếp tục dùng hồ sơ trong skill này.

Đọc [learner-profile.md](references/learner-profile.md) khi bắt đầu giải thích codebase hoặc cần chọn độ sâu giảng giải. Hồ sơ trong gói này dành cho chủ skill; nếu người dùng nói đây là bản chia sẻ cho người khác, bỏ qua dữ liệu cá nhân và dùng ngữ cảnh của người đang hỏi. Skill vẫn dùng được nếu thiếu hồ sơ; không bắt người dùng làm bài đánh giá trước khi được giải thích.

- Giữ riêng bốn loại bằng chứng: người dùng tự báo chỗ vướng; người dùng xác nhận phần tự làm; code/base từng tiếp cận; khả năng đã thể hiện qua việc tự giải thích hoặc sửa một tình huống cụ thể. Có project, dependency, commit hay nhiều lớp kiến trúc không đủ chứng minh thành thạo. Mã chưa hoàn thiện vẫn là mốc học hữu ích.
- Theo dõi độ chắc theo từng khái niệm, không gán một mức chung như beginner/junior cho cả người. Đọc hiểu, nhớ cú pháp, tự viết và tự giải thích khi gặp biến thể là những khả năng khác nhau; chưa có bằng chứng thì để là chưa xác minh.
- Với người còn vướng cú pháp/API/nền tảng, đi từ **mục đích đoạn code → một input/output cụ thể → cú pháp cần để đọc đoạn đó → cơ chế API/framework → vai trò trong luồng lớn**. Chỉ mở khái niệm đang cản trở bước hiện tại, tránh dạy toàn bộ ngôn ngữ hoặc liệt kê hàng chục thuật ngữ cùng lúc.
- Với cú pháp mới, chỉ ra phần nào là tên tự đặt, keyword/operator, kiểu dữ liệu, tham số, giá trị trả về và ai thực sự gọi hàm/lambda. Có thể viết lại một đoạn ngắn bằng cú pháp tường minh để minh họa, ghi rõ đó là cách đọc hoặc ví dụ, chưa sửa source. Với API, tách cơ chế của thư viện khỏi helper do dự án tự viết và mở định nghĩa helper khi cần.
- Dùng một mốc người dùng đã tự làm để mở nghĩa, rồi quay lại code đang học. Nói “trong project cũ có cơ chế này” thay vì “bạn đã biết cơ chế này”. Không mặc định Kotlin quen hơn Flutter chỉ vì có nhiều project Kotlin. Khi so sánh stack, kiểm tra cả điểm giống về trách nhiệm lẫn khác biệt về runtime/lifecycle.
- Tạo bài giải thích có thể đọc hai tầng: ý chính trước, phần tháo cú pháp/API gắn ngay đoạn khó sau đó. Nếu người dùng đã thể hiện hiểu phần đó hoặc yêu cầu đi nhanh, rút phần ôn lại và đi sâu vào điều mới. Một câu tự kiểm tra hoặc bài tập nhỏ là tùy chọn, không là điều kiện để tiếp tục đọc.
- Phản hồi hiện tại của người dùng ưu tiên hơn hồ sơ và ghi chú học cũ. Chỉ cần hỏi một câu sát điểm còn thiếu khi nó đổi cách giải thích; vẫn giao nội dung hữu ích trước. Ghi chú lịch sử mâu thuẫn không tự biến thành luật kiến trúc cho dự án đang đọc.
- Khi được yêu cầu ghi nhớ/cập nhật hồ sơ, sửa đúng phần liên quan và ghi nguồn, ngày, mức xác minh. Không tự ghi một kỹ năng là đã nắm chỉ vì AI vừa giảng xong; không âm thầm ghi sửa hồ sơ chỉ vì tác vụ giải thích thông thường.
- Các đường dẫn project trong hồ sơ là mốc tùy chọn. Chỉ mở lại vài file cần đối chiếu khi có quyền truy cập; không quét toàn bộ kho dự án ở mỗi lần dùng skill. Nếu chuyển máy/IDE hoặc không thấy file, dùng ngữ cảnh mô tả và code hiện tại, nói rõ chưa mở lại được ví dụ cũ.

## Khảo sát trước khi kết luận

1. Xác định root, hướng dẫn dự án và checkout hiện tại. Tôn trọng thay đổi chưa commit. Dùng `rg --files`, tìm symbol và đọc vùng code liên quan, hoặc dùng công cụ tìm kiếm/đọc file tương đương của IDE nếu không có shell hay `rg`; bỏ qua cache, output build, vendor và generated code ở lượt đầu. Chỉ quay lại khi chúng quyết định wiring hoặc hành vi.
2. Đọc tài liệu định hướng, manifest, workspace/build configuration và dependency thực sự cần thiết. Xác định các app/package/module, target/source set/flavor, entry point và phạm vi sản phẩm đang xét. Dùng lockfile để đối chiếu phiên bản khi cần; không đổ toàn bộ lockfile vào context.
3. Tìm đường khởi động đang được đăng ký: framework/OS gọi đâu, nơi lắp dependency, router đầu tiên, màn hình/handler đầu tiên. Phân biệt build configuration, khởi tạo runtime và xử lý một sự kiện sau khi app đã chạy.
4. Chọn một tính năng thật đủ đại diện, theo ưu tiên người dùng; nếu chưa có, chọn luồng rõ ràng và đi qua phần lõi. Theo dõi cả chiều đi lẫn chiều dữ liệu/state quay về. Chọn thêm luồng khác chỉ khi nó làm lộ cơ chế quan trọng chưa được giải thích.
5. Đối chiếu định nghĩa với nơi sử dụng, interface với implementation và binding/provider/router registration. Đọc model, mapping, error handling, async/lifecycle và test liên quan. Một thư viện có trong manifest hoặc một lớp có trên đĩa chưa chứng minh nó tham gia luồng đang chạy.
6. Khi luồng đại diện đã đủ căn cứ, chuyển sang giải thích; không cố đọc tuần tự mọi file. Với repo lớn, lập bản đồ toàn cục rồi đi sâu có chọn lọc và nói rõ vùng chưa khảo sát.

Đọc hướng dẫn stack tương ứng **sau khi phát hiện stack từ repo**, chỉ nạp phần liên quan:

- Kotlin/Android hoặc Kotlin Multiplatform: [kotlin-android.md](references/kotlin-android.md).
- Dart/Flutter: [flutter.md](references/flutter.md).
- JavaScript/TypeScript với React Native/Expo: [react-native.md](references/react-native.md).
- Swift/SwiftUI/UIKit: [swift.md](references/swift.md).
- Stack khác: dùng cùng quy trình với entry point, wiring, state, I/O, concurrency và test của hệ đó. Không ép mô hình mobile hoặc một kiến trúc quen thuộc lên mọi repo.
- Repo hỗn hợp: lần theo ranh giới thực tế giữa các ngôn ngữ/module; đọc thêm hướng dẫn stack ở bên kia khi luồng đi qua nó.

## Chuẩn bằng chứng

- Mỗi kết luận quan trọng về kiến trúc, lời gọi, dữ liệu, state hoặc hành vi phải có file/symbol đã đọc làm căn cứ. Gắn link file và số dòng đã kiểm tra sát lời giải thích; trên môi trường hỗ trợ link local, dùng đường dẫn tuyệt đối. Không tạo số dòng từ trí nhớ và không biến mọi câu thành một citation.
- Không suy ra MVVM, Clean Architecture, Redux hay kiến trúc khác chỉ từ tên thư mục. Chỉ ra dependency/call site/state ownership chứng minh nhận định; nếu thiết kế trộn nhiều kiểu, mô tả đúng hiện trạng.
- Phân biệt rõ: **đã thấy trong mã**, **suy luận từ cấu trúc**, **chưa xác minh**. Đọc tĩnh cho biết đường xử lý theo code và điều kiện của nó, không chứng minh nhánh đó đã chạy trên thiết bị.
- Khi thiếu source, implementation, backend hoặc binding động, dừng trace ở ranh giới xác minh được. Nêu điều còn thiếu và cách kiểm tra hẹp nhất; không tự điền endpoint, dữ liệu hay hành vi của bên ngoài.
- Nếu README, comment, test và implementation mâu thuẫn, chỉ ra khác biệt có ảnh hưởng tới cách hiểu. Không coi ý định trong comment hay assertion trong test chưa chạy là hành vi đã quan sát.
- Kiểm tra phiên bản/config khi giải thích cơ chế framework có thể thay đổi. Khi cần tài liệu bên ngoài, dùng nguồn chính thức đúng phiên bản nếu có; tách kiến thức framework khỏi sự thật về repo. Không cần tra web cho từng dòng code đã rõ.
- Không in token, khóa hoặc giá trị bí mật đọc được từ cấu hình. Có thể giải thích tên biến và vai trò bằng giá trị đã che.

## Cách giảng để người đọc hiểu được

Với thành phần quan trọng, nối các ý: **nó giải quyết việc gì → ai tạo/gọi nó → nhận gì → xử lý/ủy quyền thế nào → trả hoặc thay đổi gì → phần nào dùng kết quả**. Giải thích cơ chế “vì sao hành vi xảy ra”; chỉ quy kết lý do tác giả chọn thiết kế khi có bằng chứng, nếu không nói đó là lợi ích có thể có.

- Đưa ví dụ đầu vào và thay đổi đầu ra/state cụ thể từ luồng đang đọc. Nếu cần dữ liệu giả để minh họa, ghi rõ là ví dụ và vẫn giữ đúng nhánh code; không trình bày như dữ liệu đã quan sát.
- Khi dùng “repository”, “dependency injection”, “recomposition”, “hook”, “actor” hoặc thuật ngữ khác, giải nghĩa ngắn rồi gắn ngay với symbol trong repo. Không dạy một chương lý thuyết tách rời trước khi giải thích code.
- Trích đoạn code vừa đủ để chỉ ra quyết định quan trọng. Giải thích điều kiện, transformation, side effect, async và lỗi; giải nghĩa cú pháp theo nhu cầu đã xác định của người đọc. Chỉ lược phần hiển nhiên với chính người đọc đó. Nếu người dùng yêu cầu từng dòng, vẫn giữ ngữ cảnh caller và chia thành đoạn nhỏ có ý nghĩa.
- Làm rõ ai sở hữu state, ai được đổi state, ai nhận thông báo và khi nào cập nhật UI/consumer. Phân biệt tạo object, đăng ký object và thực sự gọi method; phân biệt gọi đồng bộ, launch task, subscribe/collect và emit state.
- Ví von chỉ dùng để mở nghĩa rồi ánh xạ về code. Không dùng ví von thay cho giải thích kỹ thuật hoặc so sánh stack làm chúng có vẻ tương đương hoàn toàn.
- Các lỗi, hạn chế hoặc điểm dễ nhầm chỉ đưa vào khi giúp hiểu đúng luồng đang xét. Giữ phát hiện nghi ngờ là nghi ngờ có căn cứ; không chuyển thành audit hay đề xuất tái cấu trúc hàng loạt.

## Đầu ra mặc định cho yêu cầu giải thích cả codebase

Tổ chức thành một bài dẫn đường có thể đọc độc lập. Dùng tiêu đề, bảng hoặc sơ đồ khi chúng làm quan hệ dễ hiểu; điều chỉnh độ dài theo yêu cầu và quy mô, không buộc mọi câu trả lời theo cùng một mẫu.

1. **Dự án làm gì và phạm vi đã đọc.** Mô tả hành vi/sản phẩm đã xác minh, stack và app/target đang xét. Nếu mục đích sản phẩm chỉ có trong README, nói rõ nguồn đó. Nêu ngắn gọn phần mới khảo sát tĩnh hoặc chưa chạy.
2. **Bản đồ kiến trúc.** Chọn các module/layer chính, trách nhiệm và quan hệ thực tế. Bảng nên nối phần code với việc nó làm, file tiêu biểu và phần nhận kết quả; tránh liệt kê cây thư mục dài. Nếu có sơ đồ, tách mũi tên dependency khỏi mũi tên sự kiện/dữ liệu, ghi chú loại quan hệ.
3. **Từ lúc khởi động đến điểm sử dụng đầu tiên.** Chỉ rõ entry point, wiring, route/màn hình đầu và điều kiện chọn nhánh. Giải thích framework gọi callback nào thay vì dựng một chuỗi gọi trực tiếp không có trong code.
4. **Một luồng tính năng xuyên suốt.** Theo sự kiện thật qua handler, nghiệp vụ, I/O nếu có, mapping, state/result và nơi tiêu thụ. Mỗi bước có symbol/file, dữ liệu truyền và tác động. Phân tích các nhánh loading/empty/error/cancellation quan trọng đang được triển khai; nêu thiếu xử lý khi nó làm thay đổi hiểu biết về luồng.
5. **Những cơ chế cần hiểu để đọc tiếp.** Chọn state, DI, navigation, async/lifecycle, model/DTO, persistence, native boundary hoặc build logic đang chi phối luồng; giải thích gắn với ví dụ vừa đọc, không liệt kê mọi dependency.
6. **Lộ trình tự đọc và thực hành.** Xếp các file thật theo quan hệ nhân quả. Với mỗi chặng, nói cần tìm gì và đọc xong phải trả lời được câu nào. Gợi ý một bài tập nhỏ dựa trên code hoặc vị trí sẽ đổi khi thêm hành vi tương tự, kèm vùng ảnh hưởng và test liên quan; chưa tự sửa code. Không hứa thay đổi chỉ ở một file khi chưa kiểm tra caller và model.
7. **Giới hạn còn lại và hướng đào sâu cụ thể.** Chốt những ranh giới chưa xác minh ảnh hưởng tới kết luận. Có thể gợi ý vài nhánh tiếp theo bằng tên tính năng/symbol đã thấy, nhưng câu trả lời hiện tại phải hoàn chỉnh; không dừng ở dàn ý rồi yêu cầu người dùng nói “tiếp tục”.

Nếu người dùng yêu cầu tài liệu toàn bộ rất lớn, vẫn giao một lượt giải thích có nội dung hoàn chỉnh và công khai phạm vi bao phủ. Chỉ chia chương theo giới hạn thực tế hoặc lựa chọn của người dùng, không âm thầm biến “chi tiết” thành bản tổng quan.

Với câu hỏi tiếp nối, đi thẳng vào phần còn vướng, sử dụng bản đồ đã xây và kiểm tra lại đoạn code cần thiết. Không lặp toàn bộ bài nhập môn. Khi so sánh Kotlin, Flutter, React Native hoặc Swift, ánh xạ trách nhiệm/hành vi và nêu khác biệt về lifecycle, state ownership và concurrency.

## Tự kiểm tra trước khi trả lời

- Người đọc có thể chỉ entry point, chủ sở hữu state và lần theo một input đến output bằng các file đã dẫn chưa?
- Mọi mũi tên quan trọng đã được kiểm tra bằng call site, registration hoặc cơ chế framework xác định chưa? Dependency diagram có bị trình bày thành runtime trace không?
- Có đang mô tả code thực tế, hay đang gán một mẫu kiến trúc lý tưởng? Có phân biệt declaration, wiring, usage và nhánh chưa chạy không?
- Thuật ngữ có được gắn với ví dụ cụ thể? Lộ trình có nói “đọc để hiểu gì”, thay vì chỉ đưa tên file?
- Độ sâu có theo chỗ vướng người dùng vừa xác nhận không? Có suy “đã biết” từ việc API xuất hiện trong repo, hoặc bỏ qua ký hiệu đang cản họ đọc code không?
- Link/số dòng còn đúng, mức độ xác minh rõ ràng, và yêu cầu hiện tại đã được trả lời đủ trước phần gợi ý tiếp theo chưa?
