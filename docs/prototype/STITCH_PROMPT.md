# Prompt thiết kế ArenaFlow trên Google Stitch

Thiết kế prototype app Android ArenaFlow, toàn bộ tiếng Việt, cho bóng đá phong trào. Mobile 390px, light theme, xanh sân cỏ #146B46, nền #FAFBF7, chữ #172E27, nút chạm tối thiểu 48px. Phong cách gọn, rõ, dùng ngoài trời; không dùng giao diện đặt sân/thanh toán. Dữ liệu minh họa TP.HCM ngày 21/11/2026, ghi rõ dữ liệu mẫu.

Tạo 6 màn nhất quán:

1. Khám phá: tiêu đề Tìm đối thủ. Ra sân thôi.; bộ lọc đã có sân/chưa có sân, ngày, khu vực, định dạng và trình độ; danh sách Thảo Điền FC (đã có sân) và Sài Gòn United (chưa có sân). Đá 7, trung bình. Nút tạo kèo; bottom navigation Tìm kèo, Đá lẻ, Tin nhắn, Đội của tôi.
2. Chi tiết kèo đã có sân: Thảo Điền FC, 21/11 19:00–20:30, Sân An Phú, Phường An Khánh TP.HCM. Điểm đội 92/100, sao 4,8/5; sân 600.000đ, chia 50–50. Chọn đội Bình Minh FC trước khi gửi yêu cầu. Sau khi gửi: chờ duyệt và mở chat riêng giữa hai đội trưởng.
3. Tạo kèo: chọn đã có sân/chưa có sân. Luồng A một sân, một giờ; luồng B 1–5 sân và 1–5 khung giờ là hai danh sách độc lập. Có định dạng, trình độ, thời hạn bài đăng không quá giờ bắt đầu. Không sửa kèo sau tạo; hủy và tạo lại.
4. Thương lượng Sài Gòn United ↔ Bình Minh FC: hiển thị chat và gói sân+giờ+chi phí riêng. Chọn từ 2 sân, 2 giờ. Sân 600.000đ chia 50–50; cọc 200.000đ do đội Sài Gòn giữ bên ngoài. Nút xác nhận gói, trạng thái xác nhận mỗi đội; chỉ cả hai xác nhận mới ghép. Sửa gói xóa xác nhận cả hai. Chat không thay thế xác nhận. Không có rút/kết thúc thương lượng.
5. Đá lẻ: vị trí thủ môn/hậu vệ/tiền vệ/tiền đạo; GPS mặc định 3km, chỉnh 1–20km, không GPS dùng khu vực hồ sơ. Tin Bình Minh FC cần 1 thủ môn, 21/11 19:00–20:30, Phường An Khánh TP.HCM, 2,5km. Trước duyệt KHÔNG hiển thị tên sân, địa chỉ hoặc tọa độ. Nút ứng tuyển → chờ duyệt.
6. Đội của tôi: Bình Minh FC, vai trò đội trưởng, điểm uy tín đội 92/100, đánh giá 4,8/5, tỉ lệ hoàn thành 96% trên 25 trận mẫu. Nếu dưới 5 trận hiện Mới tham gia X/5 trận, không hiện tỉ lệ. Uy tín cá nhân độc lập đội. Lịch kèo và quản lý thành viên.

Ràng buộc: ArenaFlow chỉ ghép kèo và tuyển người; không đặt sân, không thu/giữ/hoàn tiền. Không tạo nút thanh toán. Chỉ có thông báo trong app, push làm sau. Các thao tác là mô phỏng chưa nối backend. Ưu tiên danh sách dễ quét và nút rõ, tương phản tốt, nhãn trạng thái có chữ, hỗ trợ chữ tiếng Việt.
