# ArenaFlow — Truy vết màn hình, sheet và trạng thái Android

Ngày lập: 21/11/2026. Kế hoạch cha: [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md).
Nguồn thị giác: [prototype/index.html](../prototype/index.html).
Nguồn danh mục: mục 6 [PROTOTYPE_HANDOFF.md](../prototype/PROTOTYPE_HANDOFF.md); nghiệp vụ phải kiểm với API chuyên biệt mới nhất.

## Cách đọc và cập nhật

- **101 ID** = 101 đơn vị chức năng/trạng thái/sheet cần truy vết; không phải số lượng màn độc lập hay số file.
- M: màn; S: sheet/dialog; V: biến thể/state. M/S cho phép chọn native surface phù hợp.
- Cột hiện trạng là khảo sát code tại commit c0997ba, không phải chứng nhận UI/flow/API đã pass.
- **Mọi hàng hiện Pending theo kế hoạch mới**, kể cả đã có UI cũ. “Hoàn thành” trong inventory HTML không truyền sang Android.
- Đợt là owner của phần hoàn thiện ID. Một ID có thể được dùng ở đợt trước với trạng thái chưa tích hợp; cần test lại khi dependency hoàn tất.
- Thời hạn/enum/quyền/mã lỗi trong mô tả là yêu cầu kiểm tra; contract gốc mới nhất quyết định payload thực tế.
- Theo dõi riêng sau mỗi đợt: UI = Pending/In progress/Accepted; Flow = Pending/Verified; API = Fake/Integrated/Blocked; Evidence = đường dẫn ảnh/test/commit.
- Không đếm một sheet thành “màn thứ hai” để hoàn tất branch. Các lựa chọn destination đầy trang cho ID M/S và ADM-06 được ghi rõ ở plan.
- Các nút cần đích chưa làm phải có trạng thái có lý do; không chấp nhận callback rỗng.

## Phạm vi theo nhóm

| Nhóm | ID | Số đơn vị |
| --- | --- | --- |
| Khởi động | APP-01..02 | 2 |
| Xác thực | AUTH-01..06 | 6 |
| Hồ sơ/tài khoản | PROF-01..06 | 6 |
| Đội/thành viên | TEAM-01..14 | 14 |
| Kèo/yêu cầu | MATCH-01..14 | 14 |
| Thương lượng B | PAIR-01..08 | 8 |
| Đá lẻ | MERC-01..12 | 12 |
| Trận ngoài | EXT-01..05 | 5 |
| Sân/địa điểm | VEN-01..07 | 7 |
| Chat | CHAT-01..05 | 5 |
| Thông báo | NOTIF-01..03 | 3 |
| Sau trận/uy tín | REP-01..10 | 10 |
| Quản trị | ADM-01..09 | 9 |
| **Tổng** | | **101** |

## APP — Khởi động

| ID | Loại | Màn / chức năng | Đợt | Hiện trạng khảo sát | Điểm vào phải có | State / điều kiện bắt buộc |
| --- | --- | --- | --- | --- | --- | --- |
| APP-01 | V | Splash và kiểm tra phiên | 02 | Có system splash; thiếu launch/session gate | App start | token hợp lệ; refresh; mất mạng; hết phiên |
| APP-02 | M | Onboarding | 02 | Chưa thấy flow/route hoàn chỉnh | Launch lần đầu | Bỏ qua/Bắt đầu; lưu đã xem |

## AUTH — Xác thực

| ID | Loại | Màn / chức năng | Đợt | Hiện trạng khảo sát | Điểm vào phải có | State / điều kiện bắt buộc |
| --- | --- | --- | --- | --- | --- | --- |
| AUTH-01 | M | Chọn phương thức đăng nhập | 03 | Chưa thấy flow/route hoàn chỉnh | Launch / tài khoản | Google/Facebook/điện thoại; hủy; lỗi |
| AUTH-02 | M | Nhập số điện thoại | 03 | Chưa thấy flow/route hoàn chỉnh | Đăng nhập | phone 10 số; gửi mã; thử lại |
| AUTH-03 | M | Xác thực OTP | 04 | Chưa thấy flow/route hoàn chỉnh | Gửi mã thành công | 6 chữ số; countdown; đổi số; gửi lại |
| AUTH-04 | V | Phân luồng sau đăng nhập | 04 | Chưa thấy flow/route hoàn chỉnh | OAuth / OTP thành công | isNewUser; profile; đích chờ hợp lệ |
| AUTH-05 | V | Tài khoản đã xóa | 05 | Chưa thấy flow/route hoàn chỉnh | Auth / refresh trả AUTH_003 | xóa phiên; chọn tài khoản khác; không khôi phục |
| AUTH-06 | S/V | Phiên hết hạn | 05 | Chưa thấy flow/route hoàn chỉnh | Refresh không hợp lệ | không lặp refresh; đăng nhập lại; giữ đích hợp lệ |

## PROF — Hồ sơ và tài khoản

| ID | Loại | Màn / chức năng | Đợt | Hiện trạng khảo sát | Điểm vào phải có | State / điều kiện bắt buộc |
| --- | --- | --- | --- | --- | --- | --- |
| PROF-01 | M | Hồ sơ cá nhân | 05 | Chưa thấy flow/route hoàn chỉnh | Avatar / menu shell | đọc profile; uy tín; sửa |
| PROF-02 | M | Hoàn thiện và sửa hồ sơ | 04 | Chưa thấy flow/route hoàn chỉnh | Auth mới / Hồ sơ | field tùy chọn theo contract; validation; giữ draft |
| PROF-03 | M/S | Menu tài khoản | 05 | Chưa thấy flow/route hoàn chỉnh | Avatar / Hồ sơ | hồ sơ; hỗ trợ; đăng xuất; xóa |
| PROF-04 | S | Xác nhận xóa tài khoản | 05 | Chưa thấy flow/route hoàn chỉnh | Menu tài khoản | hậu quả; 204; thất bại không xóa phiên nhầm |
| PROF-05 | M/S | Chặn xóa khi còn đội trưởng | 05 | Chưa thấy flow/route hoàn chỉnh | 422 USER_003 | đội ACTIVE/INACTIVE chưa giải tán; chuyển quyền/giải tán |
| PROF-06 | S | Xác nhận đăng xuất | 05 | Chưa thấy flow/route hoàn chỉnh | Menu tài khoản | xóa token/cache theo user; ngắt realtime; về Auth |

## TEAM — Đội bóng và lời mời

| ID | Loại | Màn / chức năng | Đợt | Hiện trạng khảo sát | Điểm vào phải có | State / điều kiện bắt buộc |
| --- | --- | --- | --- | --- | --- | --- |
| TEAM-01 | M | Danh sách đội của tôi | 11 | Chưa thấy flow/route hoàn chỉnh | Tab Đội / từ trang đội | nhiều đội; không có đội; quyền thành viên |
| TEAM-02 | M | Danh sách đội hệ thống | 11 | Chưa thấy flow/route hoàn chỉnh | Khám phá đội | tìm/lọc/phân trang; không coi là đội của tôi |
| TEAM-03 | M | Tạo đội | 12 | Có code một phần; cần sửa/hoàn thiện | Danh sách đội / trang đội | validate tên/khu vực/trình độ; chống submit trùng |
| TEAM-04 | M | Trang đội / chi tiết đội đang hoạt động | 08 | Có code một phần; cần sửa/hoàn thiện | Tab Đội / danh sách đội | đội trưởng/thành viên/người ngoài |
| TEAM-05 | M | Sửa đội | 12 | Chưa thấy flow/route hoàn chỉnh | Quản lý đội | đội trưởng; đổi quyền khi đang mở; lưu/Back |
| TEAM-06 | M/S | Thành viên đội | 13 | Có code một phần; cần sửa/hoàn thiện | Chi tiết đội | tên bị ẩn theo API thường; quyền quản lý |
| TEAM-07 | S | Mời thành viên | 13 | Có code một phần; cần sửa/hoàn thiện | Danh sách thành viên | chọn userId; đã mời/đã là thành viên; lỗi |
| TEAM-08 | M/S | Phản hồi lời mời | 13 | Chưa thấy flow/route hoàn chỉnh | Thông báo lời mời | chấp nhận/từ chối; đã xử lý; đội giải tán |
| TEAM-09 | S | Chuyển quyền đội trưởng | 13 | Chưa thấy flow/route hoàn chỉnh | Quản lý thành viên | chọn thành viên hợp lệ; cập nhật quyền ngay |
| TEAM-10 | M/S | Lịch sử đội trưởng | 14 | Chưa thấy flow/route hoàn chỉnh | Chi tiết đội | timeline; empty; tên tài khoản xóa |
| TEAM-11 | S | Rời đội | 13 | Chưa thấy flow/route hoàn chỉnh | Chi tiết đội | thành viên xác nhận; đội trưởng bị chặn |
| TEAM-12 | S | Giải tán đội | 13 | Chưa thấy flow/route hoàn chỉnh | Quản lý đội | lý do/xác nhận; hậu quả; không khôi phục |
| TEAM-13 | M/V | Đội ngừng hoạt động / đã giải tán | 08 | Chưa thấy flow/route hoàn chỉnh | Chi tiết đội / thông báo | INACTIVE khác DISBANDED; hành động theo contract |
| TEAM-14 | S | Chọn đội đang thao tác | 11 | Chưa thấy flow/route hoàn chỉnh | Đăng kèo / yêu cầu / tuyển / đánh giá | chỉ đội đủ quyền; không mang quyền sang đội khác |

## MATCH — Kèo và yêu cầu

| ID | Loại | Màn / chức năng | Đợt | Hiện trạng khảo sát | Điểm vào phải có | State / điều kiện bắt buộc |
| --- | --- | --- | --- | --- | --- | --- |
| MATCH-01 | M | Khám phá kèo | 01 | Có code một phần; cần sửa/hoàn thiện | Tab Tìm kèo | bố cục home index; ngày/loại; empty/error |
| MATCH-02 | S | Bộ lọc kèo | 01 | Có code một phần; cần sửa/hoàn thiện | Khám phá kèo | ngày ở layout chính; lọc mở rộng; reset |
| MATCH-03 | M | Chi tiết kèo A | 01 | Có code một phần; cần sửa/hoàn thiện | Thẻ đã có sân | bố cục detail index; sân/giờ cố định; yêu cầu |
| MATCH-04 | M | Chi tiết kèo B | 01 | Có code một phần; cần sửa/hoàn thiện | Thẻ chưa có sân | options độc lập; mở nhận kèo/thương lượng |
| MATCH-05 | M | Tạo kèo A/B | 06 | Có code một phần; cần sửa/hoàn thiện | CTA Tạo kèo | 1 sân/giờ A; 1–5 lựa chọn B; validation thời gian |
| MATCH-06 | M/S | Xem lại trước khi đăng | 06 | Có code một phần; cần sửa/hoàn thiện | Form tạo kèo | đúng draft; sửa lại; gửi một lần |
| MATCH-07 | M | Lịch thi đấu đội | 14 | Chưa thấy flow/route hoàn chỉnh | Trang đội / trận sắp tới | sắp tới/kết thúc/hủy-quá hạn; chọn đúng đội |
| MATCH-08 | M | Yêu cầu bắt kèo đã gửi | 15 | Chưa thấy flow/route hoàn chỉnh | Quản lý kèo của đội | PENDING/ACCEPTED/REJECTED/WITHDRAWN/SUPERSEDED |
| MATCH-09 | M | Yêu cầu gửi đến | 15 | Chưa thấy flow/route hoàn chỉnh | Chủ kèo A / thông báo | xem đối thủ; chat; duyệt/từ chối |
| MATCH-10 | M/S | Chi tiết và duyệt yêu cầu | 15 | Chưa thấy flow/route hoàn chỉnh | Yêu cầu gửi đến | chấp nhận một; yêu cầu khác SUPERSEDED; 409 |
| MATCH-11 | S | Rút yêu cầu | 15 | Có code một phần; cần sửa/hoàn thiện | Yêu cầu đã gửi | pending không phạt; accepted là hủy có hậu quả |
| MATCH-12 | S | Hủy kèo | 15 | Chưa thấy flow/route hoàn chỉnh | Chi tiết kèo / đội | expectedStatus; lý do; pending; bắt đầu; mức phạt |
| MATCH-13 | M/V | Trạng thái vòng đời kèo | 01 | Có code một phần; cần sửa/hoàn thiện | Chi tiết kèo | đã ghép/đang diễn ra/kết thúc/hủy/hết hạn |
| MATCH-14 | M/S | Sửa chi phí kèo A | 15 | Chưa thấy flow/route hoàn chỉnh | Chủ kèo A còn OPEN | cost-agreement đúng contract; không thêm cọc A |

## PAIR — Thương lượng kèo B

| ID | Loại | Màn / chức năng | Đợt | Hiện trạng khảo sát | Điểm vào phải có | State / điều kiện bắt buộc |
| --- | --- | --- | --- | --- | --- | --- |
| PAIR-01 | M | Thương lượng của chủ kèo | 07 | Chưa thấy flow/route hoàn chỉnh | Chủ kèo B | nhiều đội; từng phòng; không lộ phòng khác |
| PAIR-02 | M | Thương lượng đội tham gia | 07 | Chưa thấy flow/route hoàn chỉnh | Quản lý kèo / Tin nhắn | cùng layout list, scope khác |
| PAIR-03 | M | Phòng thương lượng | 07 | Có code một phần; cần sửa/hoàn thiện | Nhận kèo B / danh sách | bố cục pair index; gói hiện tại và revision |
| PAIR-04 | M/S | Chọn sân và giờ | 07 | Có code một phần; cần sửa/hoàn thiện | Gói thương lượng | mỗi loại 1 lựa chọn; sân inactive; reset xác nhận |
| PAIR-05 | M/S | Sửa chi phí và cọc gói | 07 | Chưa thấy flow/route hoàn chỉnh | Gói thương lượng | chi phí không âm; người giữ cọc; tăng revision |
| PAIR-06 | S/V | Xác nhận gói | 07 | Có code một phần; cần sửa/hoàn thiện | Gói hiện hành | hai bên cùng revision; không dùng chat thay xác nhận |
| PAIR-07 | V | Xung đột phiên bản gói | 07 | Chưa thấy flow/route hoàn chỉnh | Xác nhận gặp 409 | tải mới; hiển thị thay đổi; tự xác nhận lại |
| PAIR-08 | V | Kết thúc thương lượng | 07 | Chưa thấy flow/route hoàn chỉnh | Phòng / realtime | CONFIRMED/SUPERSEDED/CANCELLED/EXPIRED; khóa sửa |

## MERC — Tuyển người và ứng tuyển

| ID | Loại | Màn / chức năng | Đợt | Hiện trạng khảo sát | Điểm vào phải có | State / điều kiện bắt buộc |
| --- | --- | --- | --- | --- | --- | --- |
| MERC-01 | M | Khám phá đá lẻ | 08 | Có code một phần; cần sửa/hoàn thiện | Tab Đá lẻ | bố cục people index; vị trí/bán kính; fallback khu vực |
| MERC-02 | M | Chi tiết tin tuyển | 19 | Chưa thấy flow/route hoàn chỉnh | Thẻ tuyển | ẩn sân/địa chỉ/tọa độ trước duyệt |
| MERC-03 | S | Nộp đơn đá lẻ | 19 | Có code một phần; cần sửa/hoàn thiện | Tin tuyển | trùng giờ; đã ứng tuyển; thuộc đội; pending |
| MERC-04 | M | Đơn ứng tuyển của tôi | 19 | Chưa thấy flow/route hoàn chỉnh | Tab Đá lẻ / hồ sơ | lọc trạng thái; phân trang; mở đơn |
| MERC-05 | M | Chi tiết đơn ứng tuyển | 20 | Chưa thấy flow/route hoàn chỉnh | Đơn của tôi / thông báo | APPROVED mới có sân và chat; đích hết quyền |
| MERC-06 | S | Rút đơn đá lẻ | 20 | Chưa thấy flow/route hoàn chỉnh | Chi tiết đơn | pending miễn phạt; approved cảnh báo -10; giờ bắt đầu |
| MERC-07 | M/S | Chọn trận cần tuyển | 21 | Chưa thấy flow/route hoàn chỉnh | Quản lý đội / tin tuyển | kèo CONFIRMED hoặc trận ngoài; đúng đội |
| MERC-08 | M | Đăng tin tuyển | 21 | Chưa thấy flow/route hoàn chỉnh | Bộ chọn trận | 1–4 vị trí, mỗi 1–10 người; partial failure |
| MERC-09 | M | Tin tuyển của đội | 20 | Chưa thấy flow/route hoàn chỉnh | Chi tiết đội | OPEN/FILLED/CLOSED/EXPIRED; số slot |
| MERC-10 | M | Danh sách ứng viên | 22 | Chưa thấy flow/route hoàn chỉnh | Tin tuyển của đội | điểm cá nhân; chat theo đơn; xem ứng viên |
| MERC-11 | M/S | Chi tiết và duyệt ứng viên | 22 | Chưa thấy flow/route hoàn chỉnh | Danh sách ứng viên | approve/reject; đủ slot; không hủy duyệt |
| MERC-12 | S | Sửa số lượng / đóng tin | 20 | Chưa thấy flow/route hoàn chỉnh | Quản lý tin tuyển | không giảm dưới đã duyệt; cập nhật slot |

## EXT — Trận ngoài hệ thống

| ID | Loại | Màn / chức năng | Đợt | Hiện trạng khảo sát | Điểm vào phải có | State / điều kiện bắt buộc |
| --- | --- | --- | --- | --- | --- | --- |
| EXT-01 | M | Khai báo trận ngoài | 24 | Chưa thấy flow/route hoàn chỉnh | Danh sách trận ngoài / tuyển | sân/giờ/đối thủ tùy chọn; quay lại nơi gọi |
| EXT-02 | M | Danh sách và chi tiết trận ngoài | 23 | Chưa thấy flow/route hoàn chỉnh | Đội / lịch đấu | 2 destination dùng cùng ID; tin tuyển đi kèm |
| EXT-03 | M | Sửa trận ngoài | 24 | Chưa thấy flow/route hoàn chỉnh | Chi tiết trận ngoài | đã có approved: khóa sân/giờ; giữ field khác |
| EXT-04 | S | Hủy trận ngoài | 24 | Chưa thấy flow/route hoàn chỉnh | Chi tiết trận ngoài | trước giờ; lý do/hậu quả; đóng tin và đơn |
| EXT-05 | M/S | Thêm vị trí tuyển trận ngoài | 24 | Chưa thấy flow/route hoàn chỉnh | Chi tiết trận ngoài | chọn vị trí/số lượng; phản ánh slot mới |

## VEN — Sân và địa điểm

| ID | Loại | Màn / chức năng | Đợt | Hiện trạng khảo sát | Điểm vào phải có | State / điều kiện bắt buộc |
| --- | --- | --- | --- | --- | --- | --- |
| VEN-01 | M/S | Tìm và chọn sân | 16 | Chưa thấy flow/route hoàn chỉnh | Form kèo / thương lượng / trận ngoài | tìm/lọc/phân trang; trả đúng caller và draft |
| VEN-02 | M | Chi tiết sân | 16 | Chưa thấy flow/route hoàn chỉnh | Bộ chọn / liên kết sân | thông tin/liên hệ/ảnh; xem không đồng nghĩa đặt sân |
| VEN-03 | M | Thêm sân | 17 | Chưa thấy flow/route hoàn chỉnh | Bộ chọn sân | validate; tạo xong chọn ngay |
| VEN-04 | M | Sửa sân | 17 | Chưa thấy flow/route hoàn chỉnh | Chi tiết sân | chỉ người tạo/admin; 403; đồng bộ nơi dùng |
| VEN-05 | M/S | Thư viện ảnh sân | 18 | Chưa thấy flow/route hoàn chỉnh | Chi tiết sân | upload/delete/set-primary; giới hạn theo API; retry |
| VEN-06 | S/M | Báo cáo thông tin sân | 18 | Chưa thấy flow/route hoàn chỉnh | Chi tiết sân | sai dữ liệu/đóng cửa; gửi/đã gửi/lỗi |
| VEN-07 | V | Sân ngừng hoạt động | 16 | Chưa thấy flow/route hoàn chỉnh | Sân cũ / chọn sân | xem được theo quyền; cấm chọn cho gói mới |

## CHAT — Hội thoại

| ID | Loại | Màn / chức năng | Đợt | Hiện trạng khảo sát | Điểm vào phải có | State / điều kiện bắt buộc |
| --- | --- | --- | --- | --- | --- | --- |
| CHAT-01 | M | Hộp thư hội thoại | 09 | Tab placeholder | Tab Tin nhắn | unread; ngữ cảnh đội/đá lẻ; phân trang |
| CHAT-02 | M | Chat giữa hai đội | 09 | Thiếu room dùng chung; có chat nhúng mẫu | Yêu cầu A / pairing B / inbox | chat index; REST gửi/lịch sử; người gửi thật |
| CHAT-03 | M/V | Chat đội trưởng và ứng viên | 09 | Chưa thấy flow/route hoàn chỉnh | Đơn ứng tuyển / inbox | dùng layout chung; quyền theo application |
| CHAT-04 | V | Chat chỉ đọc và lỗi | 09 | Chưa thấy flow/route hoàn chỉnh | canSend=false / thiếu quyền | đọc lịch sử theo quyền; khóa composer; lý do |
| CHAT-05 | S | Báo cáo nội dung chat | 09 | Chưa thấy flow/route hoàn chỉnh | Menu phòng chat | đúng conversation/application; enum; không gửi trùng |

## NOTIF — Thông báo

| ID | Loại | Màn / chức năng | Đợt | Hiện trạng khảo sát | Điểm vào phải có | State / điều kiện bắt buộc |
| --- | --- | --- | --- | --- | --- | --- |
| NOTIF-01 | M | Trung tâm thông báo | 10 | Chưa thấy flow/route hoàn chỉnh | Chuông header | all/unread; đánh dấu đọc; lưu trữ 30 ngày |
| NOTIF-02 | V | Điều hướng đích an toàn | 10 | Chưa thấy flow/route hoàn chỉnh | Bấm thông báo / deep link | đích xóa/mất quyền/hết phiên; back/recovery |
| NOTIF-03 | V | Catalog loại thông báo | 10 | Chưa thấy flow/route hoàn chỉnh | Fixtures / kiểm thử resolver | mọi type/reference trong spec; không thêm tab catalog |

## REP — Sau trận, uy tín và tranh chấp

| ID | Loại | Màn / chức năng | Đợt | Hiện trạng khảo sát | Điểm vào phải có | State / điều kiện bắt buộc |
| --- | --- | --- | --- | --- | --- | --- |
| REP-01 | M | Đánh giá đối thủ sau trận | 25 | Chưa thấy flow/route hoàn chỉnh | Kèo kết thúc | 1–5 sao; nhận xét; điều kiện gửi |
| REP-02 | M/V | Kết quả đánh giá trận | 25 | Chưa thấy flow/route hoàn chỉnh | Sau gửi / chi tiết trận | blind rating; không tiết lộ trạng thái đối phương |
| REP-03 | M | Đánh giá đá lẻ hai chiều | 26 | Chưa thấy flow/route hoàn chỉnh | Đơn/trận hoàn tất | đội chấm cá nhân; cá nhân chấm đội; quyền |
| REP-04 | M/S | Uy tín đội | 26 | Có code một phần; cần sửa/hoàn thiện | Trang đội / hồ sơ đội | điểm/sao/badge; completion chỉ khi đủ 5 trận |
| REP-05 | M/S | Uy tín cá nhân | 27 | Chưa thấy flow/route hoàn chỉnh | Hồ sơ / ứng viên | điểm cá nhân độc lập điểm đội |
| REP-06 | M/S | Gửi báo cáo vi phạm | 28 | Chưa thấy flow/route hoàn chỉnh | Kèo/đơn theo quyền | enum và field đúng API chuyên biệt; lý do |
| REP-07 | M | Báo cáo liên quan | 27 | Chưa thấy flow/route hoàn chỉnh | Hồ sơ / đội | gửi/nhận; team/player đúng quyền |
| REP-08 | M | Chi tiết báo cáo | 28 | Chưa thấy flow/route hoàn chỉnh | Danh sách / thông báo | nội dung; kết quả; thời hạn khiếu nại |
| REP-09 | M/S | Khiếu nại NO-SHOW | 28 | Chưa thấy flow/route hoàn chỉnh | Report còn hạn | chỉ NO-SHOW trong 24 giờ; bằng chứng; 409 |
| REP-10 | V | Kết quả tranh chấp | 28 | Chưa thấy flow/route hoàn chỉnh | Chi tiết report / thông báo | đang xét/chấp thuận/bác; điểm theo server |

## ADM — Quản trị

| ID | Loại | Màn / chức năng | Đợt | Hiện trạng khảo sát | Điểm vào phải có | State / điều kiện bắt buộc |
| --- | --- | --- | --- | --- | --- | --- |
| ADM-01 | M | Trang quản trị | 29 | Chưa thấy flow/route hoàn chỉnh | Menu tài khoản ADMIN | guard ở route và API; người thường 403 |
| ADM-02 | M | Quản lý đội | 29 | Chưa thấy flow/route hoàn chỉnh | Admin | tìm/lọc; mark-inactive/reactivate; lý do |
| ADM-03 | M | Lịch sử thành viên quản trị | 30 | Chưa thấy flow/route hoàn chỉnh | Admin / đội | tên thật kèm cờ đã xóa theo API admin |
| ADM-04 | M | Hàng chờ tranh chấp đội | 30 | Chưa thấy flow/route hoàn chỉnh | Admin | pending/resolved; mở chi tiết tranh chấp |
| ADM-05 | M | Hàng chờ tranh chấp đá lẻ | 31 | Chưa thấy flow/route hoàn chỉnh | Admin | đội và ứng viên; mở đúng review scope |
| ADM-06 | S | Xem xét / quyết định tranh chấp | 32 | Chưa thấy flow/route hoàn chỉnh | Hàng chờ đội/đá lẻ | nâng thành destination chi tiết + dialog quyết định; ghi chú |
| ADM-07 | M | Hàng chờ báo cáo sân | 31 | Chưa thấy flow/route hoàn chỉnh | Admin | xem/chốt phản ánh; trạng thái đã xử lý |
| ADM-08 | S | Bật / tắt sân | 31 | Chưa thấy flow/route hoàn chỉnh | Report sân / chi tiết sân admin | mark-inactive/reactivate; lý do; refresh nơi dùng |
| ADM-09 | M/S | Danh mục bộ môn | 32 | Chưa thấy flow/route hoàn chỉnh | Admin | tìm/xem/bật-tắt; không tạo enum mới |

## Các biến thể toàn cục áp dụng cho mọi màn

Các dòng này không tạo thêm ID và không làm thay đổi tổng 101.

| Tình huống | Hành vi nghiệm thu |
| --- | --- |
| Loading lần đầu / refresh | Không bật thao tác trùng; refresh lỗi giữ dữ liệu hợp lệ nếu có |
| Empty chưa có dữ liệu / lọc không có kết quả | Phân biệt hai lý do; có tạo mới/reset hợp lệ |
| Mạng/timeout/server | Lời nhắn có recovery; retry đúng tác vụ; không xóa phiên vì mất mạng |
| 401 / refresh invalid / AUTH_003 | Điều phối session theo plan; không flash nội dung bảo vệ |
| 403 / vai trò vừa đổi | Tải lại quyền, khóa thao tác và giải thích; không dựa riêng vào ẩn nút |
| 404 / đối tượng xóa | Destination Unavailable hoặc state của màn; có Back/đích an toàn |
| 409 / revision / expectedStatus | Hiển thị dữ liệu mới, yêu cầu người dùng xem/xác nhận lại; không retry ghi mù |
| Validation | Lỗi sát field; giữ các trường hợp lệ; dùng enum và format API |
| Double tap / replay / retry ghi | Không nhân request, tin tuyển, ảnh hoặc tin nhắn |
| Form dirty / picker / rotate / recreation | Draft giữ đúng; Back đóng surface trước, rồi xử lý dirty form |
| Keyboard / font lớn / TalkBack | Không che submit hoặc cắt nội dung; nhãn/semantics đúng |
| Thay user / acting team | Cache, quyền, token và subscription không còn thuộc ngữ cảnh cũ |

## Ranh giới tham chiếu 8 view nguồn

| View trong index.html | Màn/destination Android | Đợt chính |
| --- | --- | --- |
| home | MATCH-01 | 01 |
| detail | MATCH-03; MATCH-04 là biến thể mở rộng | 01 |
| create | MATCH-05; review là mở rộng đồng bộ | 06 |
| pair | PAIR-03 | 07 |
| people | MERC-01 | 08 |
| team | TEAM-04 trang đội; list quản lý là mở rộng | 08 |
| chat | CHAT-02 room; inbox/context đá lẻ là mở rộng | 09 |
| alerts | NOTIF-01; resolver là mở rộng | 10 |

Splash/auth/profile và các màn quản trị không có layout trong root index. Phải kế thừa hệ chữ, màu, field, button và spacing đã khóa, sau đó kiểm render; không dùng screenshot Stitch để điền chỗ trống.

## Mẫu bản ghi nghiệm thu một ID

~~~text
ID:
Destination hoặc màn chủ + variant:
Điểm vào và Back:
Vai trò / actingTeam:
UI: Pending | In progress | Accepted
Flow: Pending | Verified
API: Fake | Integrated | Blocked (nêu phụ thuộc)
State đã chạy:
Ảnh prototype (nếu có) / native:
Test / kết quả:
Branch + base + commit + trạng thái push:
Còn thiếu:
~~~

Các record theo đợt đặt dưới docs/android-ui/batches/index-NN.md khi triển khai. Không tạo sẵn record “pass” hoặc SHA giả trong plan.
