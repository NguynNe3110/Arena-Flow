# Giải thích sâu một tính năng, file hoặc hàm

Đọc khi yêu cầu tập trung vào một phần cụ thể hoặc khi xây luồng tính năng đại diện cho bài giải thích codebase. Dùng phần phù hợp; không buộc người dùng cung cấp mọi đầu vào dưới đây.

## Theo một tính năng

Tìm điểm kích hoạt thực tế: thao tác UI, route, event, request, scheduled job hoặc lời gọi thư viện. Nếu người dùng mô tả hành vi mà chưa biết tên symbol, tìm chuỗi hiển thị, route và nơi xử lý sự kiện để xác định điểm vào.

Ghi lại chuỗi đã xác minh trong ghi chú làm việc với các trường:

| Chặng | Cần xác định |
| --- | --- |
| Kích hoạt | Ai phát sự kiện, lúc nào, tham số gì, điều kiện nào cho phép? |
| Điều phối | Handler nào nhận, dependency nào được dùng, ai chọn implementation? |
| Nghiệp vụ | Kiểm tra/quy tắc/mapping nào làm thay đổi input hoặc quyết định nhánh? |
| I/O nếu có | Gọi local/network/SDK ở đâu, kiểu dữ liệu qua ranh giới, side effect gì? |
| Hoàn tất | Kết quả/lỗi được chuyển thành state/event/return value ra sao? |
| Tiêu thụ | Ai observe/render/đọc kết quả, điều kiện nào làm UI hoặc caller đổi? |
| Vòng đời | Ai sở hữu task/subscription, khi rời màn hình/hủy request thì chuyện gì xảy ra? |

Không nhất thiết mọi luồng đều có I/O, use case, repository hoặc UI. Bỏ chặng không tồn tại thay vì tạo abstraction để khớp bảng.

Đọc caller lẫn callee; tìm binding, factory, route registration và nơi subscribe nếu chuỗi đi qua cơ chế gián tiếp. Interface chỉ là hợp đồng: chưa tìm được implementation thì đánh dấu đúng ranh giới đó. Với code generated, tìm nguồn annotation/schema và điểm kết nối; chỉ mở output sinh sẵn khi cần làm rõ wiring.

Trong bài giảng, chọn một sự kiện cùng dữ liệu minh họa đơn giản, rồi lần qua symbol thật. Tại mỗi chuyển tiếp quan trọng, giải thích điều vừa đổi và nguyên nhân của bước tiếp theo. Với luồng phản ứng hoặc event-driven, không ép mọi bước thành một call stack đồng bộ; chỉ rõ emit/dispatch, đăng ký listener và callback sau đó.

Theo nhánh thành công và các nhánh lỗi/rỗng/hủy làm thay đổi kết quả. Nếu retry, cache hoặc fallback tồn tại, giải thích điều kiện và thứ tự; không gán những cơ chế phổ biến nhưng repo chưa triển khai. Dùng test để minh họa một contract/edge case đã tìm thấy và nói rõ test có được chạy hay chỉ đọc.

Sơ đồ ngắn nên thể hiện sự kiện đi và kết quả/state về. Ghi chú phần framework điều phối, việc nào bất đồng bộ và nơi trace dừng ở dịch vụ ngoài repo. Một sơ đồ đẹp không thay thế dẫn chứng các chuyển tiếp.

## Theo một file/hàm

Trước khi đi từng đoạn, cung cấp “địa chỉ” của nó trong hệ thống: module, vai trò, đường vào, nơi tạo object và consumer. Với hàm độc lập, thay bằng input/output và call site tiêu biểu.

Giải thích theo các cụm quyết định có ý nghĩa:

- Chữ ký: tham số, return type, nullable/generic/async và hợp đồng quan trọng.
- Dependency và state: dữ liệu nào được truyền vào, tự tạo, giữ lại hoặc chia sẻ; giá trị nào bất biến/có thể thay đổi.
- Control flow: nhánh, guard, vòng lặp, chuyển đổi dữ liệu và trường hợp kết thúc sớm.
- Side effect: I/O, mutation, navigation, event, task hoặc subscription được tạo.
- Kết quả: ai đọc nó, lỗi đi đâu và khi nào caller/UI thấy thay đổi.

Chỉ mở rộng cú pháp khi nó quyết định ý nghĩa đoạn code, ví dụ lambda được truyền chứ chưa gọi, closure giữ giá trị cũ, extension không sửa lớp gốc, `suspend` không tự chọn background thread, hoặc nullable tạo nhánh không thực thi. Xác nhận trường hợp cụ thể thay vì đưa tất cả ví dụ này vào mọi câu trả lời.

Khi người dùng muốn từng dòng, trích các đoạn nhỏ có số dòng thật, giải thích từ trên xuống trong từng cụm và giữ liên kết tới luồng bên ngoài. Với file lớn, ưu tiên symbol liên quan đến câu hỏi, công khai phần đã bao phủ và không tuyên bố đã giải thích toàn bộ.

## Khi người dùng vẫn chưa hiểu

Xác định điểm đứt trong mô hình của họ từ phản hồi: chưa rõ mục đích, dữ liệu, control flow, state, lifecycle hay cú pháp. Dùng lại đúng ví dụ với một mức chi tiết thấp hơn, chẳng hạn giá trị trước/sau một phép map hoặc thứ tự callback. Không chỉ viết dài hơn cùng một lời giải thích trừu tượng.

Có thể đưa một câu tự kiểm tra ngắn và đáp án gợi ý tách rõ ở cuối khi hữu ích; không bắt người dùng làm bài hoặc trả lời câu hỏi mới được đọc phần tiếp theo. Bài tập chỉ mô tả việc thử và kết quả dự kiến từ code, không tự chỉnh dự án.
