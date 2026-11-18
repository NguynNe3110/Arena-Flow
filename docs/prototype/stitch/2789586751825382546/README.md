# ArenaFlow — Thiết kế nguồn từ dự án được chia sẻ

Nguồn chính: https://stitch.google.com/u/4/projects/2789586751825382546?pli=1

Đã xuất chỉ đọc ngày 17/11/2026 bằng MCP Stitch với quyền COMMENTER. Không tạo, sửa hay xóa màn trong dự án này.

- Mở [index.html](index.html) để chọn màn: 32 màn đang hiển thị, gồm 16 mobile và 16 desktop.
- `screens/`: HTML nguyên bản tải từ Stitch, không chỉnh sửa.
- `previews/`: ảnh PNG nguyên bản nhúng dạng data URL vào HTML, xem offline; không phải ảnh render lại từ HTML.
- [manifest.json](manifest.json): screen ID, resource ID, đường dẫn, theme và metadata nguồn. Các phiên bản ẩn được ghi riêng và không xuất để tránh dùng nhầm bản cũ.

Bộ màn gồm đăng nhập, đăng nhập số điện thoại bước 1/2, hồ sơ, sửa/xóa tài khoản, danh sách/tạo/chi tiết đội, đội đã giải tán, mời thành viên, chuyển đội trưởng, rời/giải tán đội, admin quản lý đội và lịch sử thành viên.

Theme nguồn sử dụng Be Vietnam Pro, light, customColor #16A34A. Giữ nguyên thiết kế tác giả; không áp brief 6 màn của dự án tạo nhầm trước đó. Nhiều màn mobile web có metadata DESKTOP: phân loại theo tên màn, giữ metadata nguyên bản trong manifest để đối chiếu.

HTML có thể cần mạng cho Tailwind CDN, Google Fonts hoặc ảnh ngoài; ảnh xem trước không cần mạng. HTML này là tài liệu tham khảo thiết kế, không phải mã Kotlin/Compose. Chưa kiểm thử toàn bộ tương tác hay triển khai vào app Android.
