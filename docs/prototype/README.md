# ArenaFlow — Thiết kế tham khảo

## Nguồn thiết kế chính hiện tại

Dùng dự án được chia sẻ [ArenaFlow — 2789586751825382546](https://stitch.google.com/u/4/projects/2789586751825382546?pli=1) để đọc thiết kế và triển khai app Android. Chỉ đọc trên Stitch, không tạo hay chỉnh sửa giao diện nguồn.

Đã tải 32 màn (16 mobile, 16 desktop) ngày 17/11/2026. Mở [danh mục thiết kế](stitch/2789586751825382546/index.html), [hướng dẫn](stitch/2789586751825382546/README.md) hoặc [manifest](stitch/2789586751825382546/manifest.json). Bản xuất gồm HTML gốc và ảnh xem trước offline. Chưa triển khai vào app.

## Hướng dẫn hoàn thiện prototype để bàn giao UI

Dùng [PROTOTYPE_HANDOFF.md](PROTOTYPE_HANDOFF.md) để giao AI khác xây dựng prototype đầy đủ trước khi triển khai UI Android. Tài liệu có prompt sao chép ở mục 2, danh mục màn/sheet/trạng thái, hành trình hai phía, phụ thuộc API, gói thực hiện và tiêu chí nghiệm thu. Bản làm việc mới được đề xuất đặt tại `docs/prototype/complete/`; tài liệu hướng dẫn đã tạo, prototype đầy đủ chưa được triển khai.

## Tài liệu thử nghiệm cũ — không dùng làm nguồn thiết kế

Các phần bên dưới, `index.html`, `STITCH_PROMPT.md`, `stitch-project.json`, `stitch-preview.html` và `stitch/original/` thuộc lần thử trước khi làm rõ yêu cầu. Dự án `16710701957767846160` được tạo riêng do hiểu nhầm; không phải dự án được bạn chia sẻ. Trạng thái timeout bên dưới là ghi nhận lịch sử, không phản ánh bản xuất nguồn hiện tại.

Bản thử giao diện cục bộ dựa trên spec trước đó. Dữ liệu minh hoạ, không kết nối backend.

## Nguồn

- [Tổng quan hệ thống](../ArenaFlow-spec/business-spec/SYSTEM-001/Overview.md)
- [Kèo và vòng đời](../ArenaFlow-spec/api/MATCH-001/API_Spec.md)
- [Thương lượng hai đội](../ArenaFlow-spec/api/MATCH-002/API_Spec.md)
- [Tuyển người lẻ](../ArenaFlow-spec/api/MERCENARY-001/API_Spec.md)

## Phạm vi bản thử

1. Khám phá kèo: lọc thời gian, khu vực, định dạng, phân biệt đã có sân / chưa có sân.
2. Chi tiết kèo đã có sân: đội, uy tín, sân, giờ, thỏa thuận chi phí; chọn đội trước khi gửi yêu cầu; mở chat khi yêu cầu chờ duyệt.
3. Tạo kèo: hai lựa chọn đã có sân / chưa có sân; luồng B có 1–5 địa điểm và 1–5 khung giờ.
4. Thương lượng: chat và gói xác nhận riêng; chọn sân + giờ; sửa gói xóa xác nhận hai bên; chỉ hoàn tất khi cả hai xác nhận.
5. Tìm người lẻ: vị trí, khoảng cách mặc định 3 km; trước khi duyệt chỉ hiện phường/tỉnh và khoảng cách, ẩn tên sân/địa chỉ.
6. Hồ sơ đội: điểm uy tín độc lập điểm cá nhân, tỉ lệ hoàn thành chỉ khi đủ 5 trận.

## Hướng thiết kế dự kiến

Giao diện tiếng Việt, mobile-first, nền sáng dễ đọc ngoài trời, xanh sân cỏ và chữ tối. Dùng bố cục danh sách rõ ràng, nút chạm ít nhất 48px, trạng thái có nhãn chữ. Đây là hướng thử nghiệm có thể đổi sau khi xem.

## Giới hạn nghiệp vụ

ArenaFlow không đặt sân hay thu/giữ/hoàn tiền. Chi phí/cọc chỉ là thỏa thuận. Không dùng chat để thay thế nút xác nhận gói. Không cho sửa kèo sau tạo; phải hủy và tạo lại. Push điện thoại chưa nằm trong bản hiện tại.

## Xem bản thử

Mở [index.html](index.html) bằng trình duyệt (double-click file hoặc Open in Browser trong IDE). File tự chứa CSS và JavaScript, không cần cài package hay chạy server.

Có thể thử: lọc loại kèo → xem chi tiết → gửi yêu cầu → chat; tạo kèo; mở kèo chưa có sân → xác nhận một bên → mô phỏng bên kia; đổi sân/giờ trước khi hoàn tất để thấy hai xác nhận được xóa; ứng tuyển đá lẻ.

## Trạng thái kiểm tra

- Kiểm tra script: 8 màn render không lỗi; đổi phương án xóa xác nhận; đủ hai xác nhận mới hiển thị đã ghép.
- Kiểm tra cơ học Impeccable: không có phát hiện.
- Chưa kiểm tra trực quan trên trình duyệt do công cụ computer-use không kết nối được.
- Bản này là prototype cục bộ, chưa phải bản xuất từ Stitch; chưa mô phỏng đầy đủ mọi validation, phân trang, cạnh tranh đồng thời hoặc mọi trạng thái lỗi API.
- Ngày 17/11/2026: MCP Stitch đã kết nối. Đã kiểm tra cả `view=owned` và `view=shared`, đều trả về danh sách trống trước khi tạo dự án.
- Dự án đã tạo: [ArenaFlow trên Stitch](https://stitch.withgoogle.com/projects/16710701957767846160), ID `16710701957767846160`. Thông tin tiếp tục: [stitch-project.json](stitch-project.json).
- Đã gửi nguyên brief 6 màn đúng một lần. Lệnh generation trả về `Request timed out`. Sau 10 lần kiểm tra (cách khoảng 30 giây), `list_screens` vẫn trả về `{}`; chưa lấy được ID màn hình hay HTML riêng lẻ. Không gửi lại generation để tránh trùng.
- `get_project` đã có thumbnail. Đã tải bằng MCP `download_file` và lưu ảnh PNG nhúng trong [stitch-preview.html](stitch-preview.html). Đây là ảnh màn Đá lẻ do Stitch trả về, không phải bản xuất đủ 6 màn.
- Đã xem ảnh: có thông tin tuyển thủ môn và ẩn sân trước duyệt; còn lệch brief ở bán kính đang chọn 2,5 km (mặc định yêu cầu 3 km), chưa thấy nhãn dữ liệu mẫu và xuất hiện nội dung bổ sung chưa được brief xác nhận. Chưa xác minh tương tác, đủ 6 màn, hoặc toàn bộ quy tắc nghiệp vụ.
- Khi tiếp tục: dùng lại project ID này, kiểm tra `list_screens` và `get_project` trước; khi có màn thì tải HTML/ảnh và sửa các điểm lệch brief. Chưa thể tuyên bố prototype Stitch hoàn tất.
- Prompt để tiếp tục trên Stitch: [STITCH_PROMPT.md](STITCH_PROMPT.md).

`verify.cjs` là kiểm tra trên bản script chụp tại thời điểm tạo, không thay thế kiểm thử trình duyệt; cần cập nhật khi sửa index.html.
