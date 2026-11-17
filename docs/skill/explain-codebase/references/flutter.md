# Dart và Flutter

## Điểm vào và cấu hình

- Đọc `pubspec.yaml`, dependency/lockfile khi cần phiên bản, file entry point và cấu hình launch/flavor liên quan. Tìm `main`, bootstrap, initialization trước `runApp`, root widget và router thực tế. Không giả định mọi target đều dùng `lib/main.dart` khi có entry point riêng.
- Xác định package/workspace, feature và mã generated. Một package có trong `pubspec` chưa chứng minh cách quản lý state hoặc navigation đang dùng; đối chiếu import, registration và call site.
- Nếu feature đi qua platform channel/plugin, đọc phần Dart và platform implementation liên quan nếu có. Ranh giới native/SDK ngoài repo phải được đánh dấu.

## Widget, state và luồng dữ liệu

Theo thao tác/callback hoặc route → state holder/business logic thực tế → service/repository nếu có → Future/Stream/result → state mới → nơi lắng nghe và build UI.

- Phân biệt widget configuration, `State`, object điều phối và cây render; chỉ giải thích sâu đến mức cần hiểu đoạn code. `build` có thể chạy lại, không phải chỗ mặc định làm I/O chỉ một lần.
- Xác định state ở local `setState`, ChangeNotifier/Provider, Riverpod, Bloc/Cubit hoặc giải pháp khác bằng mã thật. Chỉ đọc những API hệ đó đang dùng; không tự ép mọi app thành Bloc hoặc Clean Architecture.
- Tìm ai tạo/cung cấp instance, scope, nơi watch/read/listen/select, điểm mutate/emit và disposal. Việc đọc giá trị một lần khác với đăng ký rebuild; giải thích theo API và phiên bản trong repo.
- Với `StatefulWidget`, theo `initState`, `didUpdateWidget`, `dispose` khi chúng quyết định hành vi. Chỉ ra controller/subscription nào được giữ và hủy, cùng kiểm tra lifecycle sau async nếu có; không tự kết luận bug chỉ vì có một `await`.
- Với Future/Stream, chỉ ra chỗ bắt đầu, chờ/lắng nghe, completion/error, cancellation và dữ liệu loading/empty. Một `async` function không mặc nhiên chạy công việc CPU ở isolate khác; Future hoàn tất cũng không tự làm UI rebuild nếu không có cơ chế nối vào state.
- Với router, kiểm tra route definition, navigator, redirect/guard, deep link và tham số. Đọc route generated chỉ khi cần xác nhận đường nối.
- Với model generated/serialization, tìm declaration, annotation/schema, mapper và consumer. Không gán generated code là nghiệp vụ do tác giả viết tay.

## Bằng chứng và lộ trình

Chọn file bootstrap → root/router → widget feature → state holder → data/adapter → test phù hợp. Ở từng bước nói dữ liệu/state đổi thế nào và vì sao bước tiếp theo xảy ra. Phân biệt unit, widget và integration test; dùng test có assertion về hành vi liên quan. Khi native plugin quyết định kết quả nhưng thiếu source/config, nêu ranh giới đó thay vì mô tả phần native theo suy đoán.
