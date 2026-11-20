# ArenaFlow Prototype — Báo Cáo Tiến Độ & Ma Trận Theo Dõi (Progress & Traceability)

> Ngày lập: 18/11/2026 · Ngôn ngữ: Tiếng Việt · Phiên bản: 1.0-complete  
> Tài liệu thuộc bộ bàn giao UI Prototype ArenaFlow trong `docs/prototype/complete/`.

---

## 1. Tổng Quan Tiến Độ Các Gói Công Việc (Work Packages)

Theo quy định tại Mục 13 của `docs/prototype/PROTOTYPE_HANDOFF.md`, tiến độ được phân chia và theo dõi qua 7 gói công việc:

| Gói | Thứ tự | Tên gói / Phạm vi | Trạng thái | Ghi chú & Đầu ra |
|---|---|---|---|---|
| **WP-01** | P0 | **Nền tảng & Kiến trúc (Foundation & Shell)**<br>- Đọc nguồn, cấu trúc thư mục `docs/prototype/complete/`<br>- Router, Store phản ứng, Seed Fixtures, Demo Panel<br>- Token CSS & Components nền tảng | **HOÀN THÀNH** | Shell chạy HTML5/ES6, đầy đủ router view/sheet, store pub-sub, mock clock, character switcher. |
| **WP-02** | P0 | **Xác thực, Hồ sơ & Đội (Auth, Profile & Team)**<br>- APP-01..02, AUTH-01..06, PROF-01..06<br>- TEAM-01..14 | **HOÀN THÀNH** | Đầy đủ luồng user mới, user cũ, OTP states, xóa tài khoản có chặn captain (AC-P02), quản trị đội, chuyển quyền tức thì. |
| **WP-03** | P0 | **Địa điểm, Kèo & Thương lượng (Venues, Match & Pairing)**<br>- VEN-01..03, MATCH-01..14, PAIR-01..08 | **HOÀN THÀNH** | Đầy đủ A/B lifecycle, chọn sân giữ form, danh sách B 1-5 sân & 1-5 giờ, gói thương lượng độc lập, revision conflict (AC-P04). |
| **WP-04** | P0 | **Đá lẻ, Trận ngoài & Chat/Notif cơ bản (Mercenary & External)**<br>- MERC-01..12, EXT-01..05, CHAT-01..03, NOTIF-01 | **HOÀN THÀNH** | Tuyển người hai phía, bảo vệ riêng tư sân trước duyệt (AC-P05), rút approved trừ điểm (AC-P06), khai báo trận ngoài. |
| **WP-05** | P1 | **Sau trận, Uy tín & Chat/Notif nâng cao (Reputation & Comms)**<br>- REP-01..10, VEN-04..07, CHAT-04..05, NOTIF-02..03 | **HOÀN THÀNH** | Đánh giá ẩn (AC-P07), điểm/tỷ lệ uy tín tách biệt, tranh chấp chỉ cho NOSHOW trong 24h (AC-P08), catalog 35 loại notification (AC-P09). |
| **WP-06** | P2 | **Quản trị hệ thống (Admin Portal)**<br>- ADM-01..09 | **HOÀN THÀNH** | Layout admin riêng biệt, cấm USER thường, duyệt tranh chấp đội/mercenary (quyết định bắt buộc 1-500 ký tự), kiểm duyệt sân/bộ môn. |
| **WP-07** | Bắt buộc | **Nghiệm thu, Kiểm tra trực quan & Bàn giao Compose**<br>- 24 Hành trình J-01..J-24, AC-P01..AC-P10<br>- Toàn bộ tài liệu bàn giao Compose & QA report | **HOÀN THÀNH** | Bộ 7 tài liệu handoff hoàn chỉnh, ma trận 101 IDs, kiểm tra 360/390/430px, touch target >= 48dp. |

---

## 2. Ma Trận Truy Vết Màn Hình & Trạng Thái (101 IDs Traceability)

Phân loại: **M** = Màn hình toàn trang (Screen); **S** = Bottom Sheet / Dialog; **V** = Biến thể trạng thái (Variant / State).

### 2.1. Nhóm Khởi đầu, Xác thực & Tài khoản (APP-01..02, AUTH-01..06, PROF-01..06) — 14 IDs

| ID | Tên màn / Trạng thái | Loại | Route / View ID | Vai trò | Tình trạng | Đạt tiêu chuẩn |
|---|---|---|---|---|---|---|
| **APP-01** | Khởi động & Kiểm tra phiên | V | `app/launch` | Mọi user | Hoàn thành | Xử lý phiên hợp lệ, hết hạn, refresh token, lỗi mạng retry. |
| **APP-02** | Giới thiệu / Onboarding | M | `app/onboarding` | Khách / Mới | Hoàn thành | 3 slide giá trị cốt lõi, nút Bỏ qua & Bắt đầu ngay. |
| **AUTH-01** | Lựa chọn phương thức đăng nhập | M | `auth/login` | Khách | Hoàn thành | Google, Facebook, Số điện thoại (Stitch S1). |
| **AUTH-02** | Nhập số điện thoại đăng nhập | M | `auth/phone` | Khách | Hoàn thành | Validate regex VN phone (+84/0), Stitch S2a. |
| **AUTH-03** | Xác thực mã OTP | M | `auth/otp` | Khách | Hoàn thành | 6 ô OTP, đếm ngược 60s, đổi số, mock code, Stitch S2b. |
| **AUTH-04** | Điều hướng sau xác thực | V | `auth/callback` | Mọi user | Hoàn thành | User mới sang PROF-02; user cũ về trang trước đó (AC-P01). |
| **AUTH-05** | Tài khoản đã bị xóa (AUTH_003) | V | `auth/deleted` | User xóa | Hoàn thành | Thông báo khóa, nút quay lại đăng nhập, không có CTA giả. |
| **AUTH-06** | Hết hạn phiên đăng nhập | S/V | `sheet/session-expired` | Mọi user | Hoàn thành | Dialog cảnh báo, nút đăng nhập lại, bảo lưu deep link đích. |
| **PROF-01** | Hồ sơ cá nhân của tôi | M | `profile/me` | User đăng nhập | Hoàn thành | Thông tin cá nhân, vị trí sở trường, khu vực, Stitch S3. |
| **PROF-02** | Hoàn thiện / Chỉnh sửa hồ sơ | M | `profile/edit` | User đăng nhập | Hoàn thành | Tên, avatar, bio, vị trí, ngày sinh >= 13t, tỉnh/phường (S4). |
| **PROF-03** | Menu cài đặt tài khoản | M/S | `profile/account-menu`| User đăng nhập | Hoàn thành | Lối vào sửa hồ sơ, đăng xuất, xóa tài khoản, trợ giúp. |
| **PROF-04** | Xác nhận xóa tài khoản | S | `sheet/delete-account`| User cá nhân | Hoàn thành | Cảnh báo hậu quả, xác nhận xóa vĩnh viễn (Stitch S5). |
| **PROF-05** | Chặn xóa tài khoản đội trưởng | M/S | `profile/delete-blocked`| Captain | Hoàn thành | Danh sách đội đang làm captain, link giải tán/chuyển quyền (AC-P02). |
| **PROF-06** | Xác nhận đăng xuất | S | `sheet/logout` | User đăng nhập | Hoàn thành | Dialog xác nhận đăng xuất và xóa session cục bộ. |

### 2.2. Nhóm Đội & Lời mời (TEAM-01..14) — 14 IDs

| ID | Tên màn / Trạng thái | Loại | Route / View ID | Vai trò | Tình trạng | Đạt tiêu chuẩn |
|---|---|---|---|---|---|---|
| **TEAM-01** | Danh sách đội của tôi | M | `team/my-teams` | User đăng nhập | Hoàn thành | Hiển thị đội tham gia, vai trò (Captain/Member), tạo đội. |
| **TEAM-02** | Danh sách đội toàn hệ thống | M | `team/system-teams` | Mọi user | Hoàn thành | Danh sách phân trang, tìm kiếm, xem chi tiết (Stitch S6). |
| **TEAM-03** | Tạo đội bóng mới | M | `team/create` | User đăng nhập | Hoàn thành | Tên, logo, khu vực, trình độ (tối đa 3 tag) (Stitch S7). |
| **TEAM-04** | Chi tiết đội ACTIVE | M | `team/detail` | Captain/Member/Outsider| Hoàn thành | Biến thể theo quyền: quản trị, rời đội, xem lịch (S8a). |
| **TEAM-05** | Chỉnh sửa thông tin đội | M | `team/edit` | Captain | Hoàn thành | Cập nhật tên, logo, khu vực, trình độ đội bóng. |
| **TEAM-06** | Danh sách thành viên đội | M/S | `team/members` | Mọi user | Hoàn thành | Danh sách thành viên, nhãn vai trò, user đã xóa ẩn tên. |
| **TEAM-07** | Mời thành viên vào đội | S | `sheet/team-invite` | Captain | Hoàn thành | Chọn user từ danh bạ/fixture, kiểm tra đã mời (Stitch S9). |
| **TEAM-08** | Phản hồi lời mời từ thông báo | M/S | `team/invitation-response`| Thành viên được mời | Hoàn thành | Chấp nhận/Từ chối, thông báo kết quả tức thì. |
| **TEAM-09** | Chuyển quyền đội trưởng | S | `sheet/transfer-captain`| Captain | Hoàn thành | Chọn thành viên, chuyển tức thì không cần duyệt (S10). |
| **TEAM-10** | Lịch sử đội trưởng | M/S | `team/captain-history` | Thành viên đội | Hoàn thành | Danh sách các đời đội trưởng và mốc thời gian bàn giao. |
| **TEAM-11** | Thành viên rời đội | S | `sheet/leave-team` | Thành viên thường | Hoàn thành | Dialog xác nhận rời đội (Stitch S11). Captain bị chặn. |
| **TEAM-12** | Giải tán đội bóng | S | `sheet/disband-team` | Captain | Hoàn thành | Cảnh báo hủy toàn bộ kèo, giải tán vĩnh viễn (Stitch S12). |
| **TEAM-13** | Chi tiết đội INACTIVE/DISBANDED | M/V | `team/disbanded-detail`| Mọi user | Hoàn thành | Banner giải tán, vô hiệu hóa mọi nút hành động (S8b). |
| **TEAM-14** | Chọn đội bóng đang thao tác | S | `sheet/select-acting-team`| Captain nhiều đội| Hoàn thành | Sheet chọn ngữ cảnh đội khi tạo kèo, ứng tuyển, nhận kèo. |

### 2.3. Nhóm Kèo, Khám phá & Vòng đời (MATCH-01..14) — 14 IDs

| ID | Tên màn / Trạng thái | Loại | Route / View ID | Vai trò | Tình trạng | Đạt tiêu chuẩn |
|---|---|---|---|---|---|---|
| **MATCH-01**| Khám phá & Tìm kèo đấu | M | `match/explore` | Mọi user | Hoàn thành | Danh sách kèo A/B, badge trạng thái, lọc nhanh. |
| **MATCH-02**| Bộ lọc tìm kiếm kèo | S | `sheet/match-filter` | Mọi user | Hoàn thành | Lọc ngày, khung giờ, định dạng sân (5/7/11), trình độ. |
| **MATCH-03**| Chi tiết bài đăng kèo A | M | `match/detail-a` | Chủ kèo/Khách | Hoàn thành | 1 sân + 1 khung giờ cố định, thỏa thuận chi phí, danh sách đội gửi yêu cầu. |
| **MATCH-04**| Chi tiết bài đăng kèo B | M | `match/detail-b` | Đội đối phương | Hoàn thành | Danh sách 1-5 sân & 1-5 khung giờ độc lập, nút Nhận kèo. |
| **MATCH-05**| Tạo bài đăng kèo A hoặc B | M | `match/create` | Captain | Hoàn thành | Form tạo kèo A (1 sân, 1 giờ) hoặc B (đa sân, đa giờ). |
| **MATCH-06**| Xem lại trước khi đăng kèo | M/S | `match/preview` | Captain | Hoàn thành | Tóm tắt thông tin bài đăng, xác nhận, chống bấm lặp. |
| **MATCH-07**| Lịch thi đấu & Kèo của đội | M | `match/team-schedule` | Thành viên đội | Hoàn thành | Tab Sắp tới, Đã kết thúc, Đã hủy / Hết hạn. |
| **MATCH-08**| Yêu cầu ghép đội đã gửi | M | `match/sent-requests` | Captain gửi | Hoàn thành | Trạng thái Pending/Accepted/Rejected/Withdrawn. |
| **MATCH-09**| Danh sách yêu cầu đến (Kèo A) | M | `match/incoming-requests`| Chủ kèo A | Hoàn thành | Duyệt đối thủ: xem hồ sơ, mở chat, Chấp nhận / Từ chối. |
| **MATCH-10**| Chi tiết / Xử lý yêu cầu gửi đến | M/S | `match/request-detail` | Chủ kèo A | Hoàn thành | Chấp nhận đối thủ, tự động Superseded các yêu cầu khác (AC-P03). |
| **MATCH-11**| Rút yêu cầu giao lưu đã gửi | S | `sheet/withdraw-request`| Captain gửi | Hoàn thành | Rút Pending không phạt; rút Accepted phạt uy tín. |
| **MATCH-12**| Hủy bài đăng kèo đấu | S | `sheet/cancel-match` | Captain tạo | Hoàn thành | Hủy OPEN; hủy CONFIRMED kèm phạt uy tín; chặn sau giờ bắt đầu. |
| **MATCH-13**| Biến thể trạng thái kèo hoàn tất | M/V | `match/status-variants`| Mọi user | Hoàn thành | Trạng thái CONFIRMED, Đang đá, COMPLETED, CANCELLED, EXPIRED. |
| **MATCH-14**| Thỏa thuận chi phí kèo A | M/S | `sheet/match-cost-edit`| Chủ kèo A | Hoàn thành | Tùy chỉnh tỷ lệ cọc, chia tiền sân, ghi chú chi phí. |

### 2.4. Nhóm Thương lượng Kèo B & Chốt Gói (PAIR-01..08) — 8 IDs

| ID | Tên màn / Trạng thái | Loại | Route / View ID | Vai trò | Tình trạng | Đạt tiêu chuẩn |
|---|---|---|---|---|---|---|
| **PAIR-01**| Danh sách thương lượng (Chủ kèo B)| M | `pairing/host-list` | Chủ kèo B | Hoàn thành | Các đội đã nhận bài B, tiến độ chốt gói từng đội. |
| **PAIR-02**| Danh sách thương lượng của đội tôi | M | `pairing/my-negotiations`| Captain nhận | Hoàn thành | Kèo B đang thương lượng, trạng thái gói, revision. |
| **PAIR-03**| Chi tiết phòng thương lượng gói B | M | `pairing/room` | Captain 2 đội | Hoàn thành | Xem phương án sân/giờ, thỏa thuận chi phí, trạng thái xác nhận 2 bên. |
| **PAIR-04**| Chọn phương án sân & giờ thi đấu | M/S | `sheet/select-venue-time`| Captain 2 đội | Hoàn thành | Chọn 1 sân + 1 giờ từ các phương án bài B đã đăng. |
| **PAIR-05**| Tùy chỉnh chi phí & đặt cọc gói | M/S | `sheet/edit-package-cost`| Captain 2 đội | Hoàn thành | Chỉnh chia tiền sân, tiền cọc, ghi chú thanh toán. |
| **PAIR-06**| Xem lại & Xác nhận gói thương lượng | S/V | `sheet/confirm-package`| Captain 2 đội | Hoàn thành | Xác nhận gói. Khi 1 bên đổi gói -> xóa xác nhận (AC-P04). |
| **PAIR-07**| Xung đột phiên bản gói (PAIRING_006)| V | `pairing/conflict` | Captain 2 đội | Hoàn thành | Cảnh báo gói vừa thay đổi bởi đối phương, buộc xem lại. |
| **PAIR-08**| Trạng thái cuối của phòng thương lượng| V | `pairing/final-state` | Captain 2 đội | Hoàn thành | Giao diện khi CONFIRMED (thành công) hoặc SUPERSEDED (đội khác chốt). |

### 2.5. Nhóm Tuyển Người Đá Lẻ & Trận Ngoài (MERC-01..12, EXT-01..05) — 17 IDs

| ID | Tên màn / Trạng thái | Loại | Route / View ID | Vai trò | Tình trạng | Đạt tiêu chuẩn |
|---|---|---|---|---|---|---|
| **MERC-01**| Khám phá & Tìm tin đá lẻ | M | `mercenary/explore` | Mọi user | Hoàn thành | Lọc vị trí, GPS bán kính 1-20km (mặc định 3km), fallback khu vực hồ sơ. |
| **MERC-02**| Chi tiết tin tuyển đá lẻ | M | `mercenary/detail` | Ứng viên | Hoàn thành | **AC-P05**: Ẩn sân/địa chỉ cụ thể khi chưa Approved, chỉ hiện quận/huyện. |
| **MERC-03**| Xác nhận ứng tuyển đá lẻ | S | `sheet/apply-mercenary`| Ứng viên | Hoàn thành | Kiểm tra trùng giờ, trùng đội, gửi đơn trạng thái PENDING. |
| **MERC-04**| Danh sách đơn ứng tuyển của tôi | M | `mercenary/my-applications`| Ứng viên | Hoàn thành | Quản lý đơn: Đang chờ, Đã duyệt, Bị từ chối, Đã rút. |
| **MERC-05**| Chi tiết đơn ứng tuyển của tôi | M | `mercenary/application-detail`| Ứng viên | Hoàn thành | Đã duyệt: hiển thị tên sân/tọa độ, chat với captain, nút rút đơn. |
| **MERC-06**| Rút đơn ứng tuyển đá lẻ | S | `sheet/withdraw-mercenary`| Ứng viên | Hoàn thành | **AC-P06**: Rút Pending 0 điểm; rút Approved trừ uy tín. |
| **MERC-07**| Chọn trận đấu để tuyển người | M/S | `mercenary/select-match`| Captain | Hoàn thành | Chọn từ kèo hệ thống đã ghép hoặc khai báo trận ngoài. |
| **MERC-08**| Đăng tin tuyển người đá lẻ | M | `mercenary/create-post`| Captain | Hoàn thành | Đăng 1-4 vị trí, số lượng 1-10 người/vị trí. |
| **MERC-09**| Danh sách tin tuyển của đội tôi | M | `mercenary/team-posts` | Captain | Hoàn thành | Theo dõi tin OPEN/FILLED/CLOSED/EXPIRED và số lượng slot. |
| **MERC-10**| Danh sách ứng viên nộp đơn | M | `mercenary/applicants` | Captain | Hoàn thành | Danh sách ứng viên, điểm uy tín, vị trí, mở chat riêng. |
| **MERC-11**| Duyệt hoặc từ chối ứng viên | M/S | `sheet/review-applicant`| Captain | Hoàn thành | Chấp nhận/Từ chối ứng viên; chặn khi đã đủ người (FILLED). |
| **MERC-12**| Điều chỉnh số lượng & Đóng tin | S | `sheet/manage-post` | Captain | Hoàn thành | Giảm/tăng slot (không dưới số đã duyệt), đóng tin sớm. |
| **EXT-01** | Khai báo trận đấu ngoài hệ thống | M | `external/create` | Captain | Hoàn thành | Sân tự do, ngày giờ, đối thủ giao lưu tùy chọn. |
| **EXT-02** | Danh sách & Chi tiết trận ngoài | M | `external/detail` | Thành viên đội | Hoàn thành | Quản lý trận ngoài, gắn tin tuyển đá lẻ đi kèm. |
| **EXT-03** | Chỉnh sửa thông tin trận ngoài | M | `external/edit` | Captain | Hoàn thành | Khóa sửa sân/giờ nếu đã có ứng viên được Approved. |
| **EXT-04** | Hủy trận đấu ngoài hệ thống | S | `sheet/cancel-external`| Captain | Hoàn thành | Cảnh báo tự động hủy các tin tuyển và đơn liên quan. |
| **EXT-05** | Thêm vị trí tuyển cho trận ngoài | M/S | `external/add-position`| Captain | Hoàn thành | Bổ sung thêm vị trí cần tuyển vào trận ngoài đang có. |

### 2.6. Nhóm Địa Điểm & Sân Bóng (VEN-01..07) — 7 IDs

| ID | Tên màn / Trạng thái | Loại | Route / View ID | Vai trò | Tình trạng | Đạt tiêu chuẩn |
|---|---|---|---|---|---|---|
| **VEN-01** | Tìm & Chọn sân bóng | M/S | `venue/picker` | Mọi user | Hoàn thành | Chọn sân trả kết quả về form tạo kèo mà không mất dữ liệu đã nhập. |
| **VEN-02** | Chi tiết thông tin sân bóng | M | `venue/detail` | Mọi user | Hoàn thành | Tên sân, địa chỉ, ảnh, liên hệ, loại sân, tiện ích. |
| **VEN-03** | Thêm địa điểm sân bóng mới | M | `venue/create` | User đăng nhập | Hoàn thành | Nhập thông tin sân mới, có hiệu lực sử dụng ngay lập tức. |
| **VEN-04** | Chỉnh sửa thông tin sân bóng | M | `venue/edit` | Người tạo / Admin | Hoàn thành | Quyền sửa thông tin theo API specification. |
| **VEN-05** | Quản lý & Xem ảnh sân bóng | M/S | `venue/photos` | User / Admin | Hoàn thành | Xem gallery, thêm ảnh (tối đa 5), đặt ảnh chính, xóa ảnh. |
| **VEN-06** | Báo cáo sai lệch / Đóng cửa sân | S/M | `sheet/report-venue` | User đăng nhập | Hoàn thành | Gửi report sai địa chỉ, số điện thoại, sân ngừng hoạt động. |
| **VEN-07** | Trạng thái sân ngừng hoạt động | V | `venue/inactive-state` | Mọi user | Hoàn thành | Banner cảnh báo INACTIVE, khóa không cho chọn vào kèo mới. |

### 2.7. Nhóm Tin Nhắn & Thông Báo (CHAT-01..05, NOTIF-01..03) — 8 IDs

| ID | Tên màn / Trạng thái | Loại | Route / View ID | Vai trò | Tình trạng | Đạt tiêu chuẩn |
|---|---|---|---|---|---|---|
| **CHAT-01**| Hộp thư & Danh sách hội thoại | M | `chat/inbox` | User đăng nhập | Hoàn thành | Danh sách phòng chat kèo, chat ứng viên, số tin chưa đọc. |
| **CHAT-02**| Phòng chat giao lưu giữa hai đội | M | `chat/match-room` | Thành viên 2 đội | Hoàn thành | Header ngữ cảnh kèo, hiển thị tên thật người gửi, thời gian. |
| **CHAT-03**| Chat giữa Captain & Ứng viên đá lẻ| M/V | `chat/mercenary-room` | Captain / Ứng viên | Hoàn thành | Ngữ cảnh đơn ứng tuyển, nút xem hồ sơ, xem tin tuyển. |
| **CHAT-04**| Trạng thái phòng chat (Read-only/Lỗi)| V | `chat/room-states` | Người trong phòng | Hoàn thành | `canSend=false` khi hết quyền/hết hạn, mất mạng reconnecting. |
| **CHAT-05**| Báo cáo vi phạm trong phòng chat | S | `sheet/report-chat` | Người trong phòng | Hoàn thành | Report quấy rối, ngôn từ thù địch trực tiếp từ đoạn chat. |
| **NOTIF-01**| Trung tâm thông báo của tôi | M | `notifications/center`| User đăng nhập | Hoàn thành | Tab Tất cả / Chưa đọc, đánh dấu đã đọc, phân trang. |
| **NOTIF-02**| Xử lý điều hướng thông báo (Target)| V | `notifications/resolver`| User đăng nhập | Hoàn thành | **AC-P09**: Khi đối tượng bị xóa/hết hạn -> giải thích, không màn trắng. |
| **NOTIF-03**| Danh mục 35 loại thông báo mẫu | V | `notifications/catalog` | Mọi user | Hoàn thành | Catalog mẫu và bảng map 35 notification types đến màn đích. |

### 2.8. Nhóm Sau Trận, Uy Tín & Tranh Chấp (REP-01..10) — 10 IDs

| ID | Tên màn / Trạng thái | Loại | Route / View ID | Vai trò | Tình trạng | Đạt tiêu chuẩn |
|---|---|---|---|---|---|---|
| **REP-01** | Đánh giá đối phương sau trận đấu | M | `reputation/rate-match` | Captain 2 bên | Hoàn thành | Chấm 1-5 sao, nhận xét, cập nhật tỷ số hoặc cờ Không đến (No-Show). |
| **REP-02** | Xem kết quả đánh giá trận đấu | M/V | `reputation/match-ratings`| Captain 2 bên | Hoàn thành | **AC-P07**: Đánh giá ẩn (Blind Rating), chỉ hiện khi cả hai bên đã nộp. |
| **REP-03** | Đánh giá hai chiều với người đá lẻ | M | `reputation/rate-mercenary`| Captain / Ứng viên | Hoàn thành | Captain đánh giá mercenary; mercenary đánh giá đội bóng. |
| **REP-04** | Thẻ uy tín đội bóng | M/S | `reputation/team-card` | Mọi user | Hoàn thành | Điểm uy tín, tỷ lệ hoàn thành (hiện khi >= 5 trận), review mercenary tách biệt. |
| **REP-05** | Thẻ uy tín cá nhân người đá lẻ | M/S | `reputation/player-card`| Mọi user | Hoàn thành | Điểm cá nhân, số trận đã đá, tỷ lệ tham gia uy tín. |
| **REP-06** | Gửi báo cáo vi phạm (Submit Report)| M/S | `sheet/submit-report` | User đăng nhập | Hoàn thành | Chọn đối tượng, loại report (NOSHOW/VIOLENCE/CHEATING/BEHAVIOR), lý do. |
| **REP-07** | Danh sách báo cáo vi phạm liên quan | M | `reputation/reports-list`| Đội / Cá nhân | Hoàn thành | Báo cáo đã gửi và báo cáo bị nhận. |
| **REP-08** | Chi tiết báo cáo vi phạm | M | `reputation/report-detail`| Các bên liên quan | Hoàn thành | Trạng thái xử lý, điểm bị trừ, đếm ngược thời hạn tranh chấp 24h. |
| **REP-09** | Gửi đơn tranh chấp báo cáo NO-SHOW | M/S | `sheet/dispute-noshow` | Bên bị report | Hoàn thành | **AC-P08**: Chỉ cho phép tranh chấp NOSHOW trong vòng 24h, bằng chứng văn bản. |
| **REP-10** | Kết quả xử lý tranh chấp cuối cùng | V | `reputation/dispute-result`| Các bên liên quan | Hoàn thành | Trạng thái Chờ Admin, Chấp thuận, Bác bỏ đơn (không khiếu nại lại). |

### 2.9. Nhóm Quản Trị Hệ Thống (ADM-01..09) — 9 IDs

| ID | Tên màn / Trạng thái | Loại | Route / View ID | Vai trò | Tình trạng | Đạt tiêu chuẩn |
|---|---|---|---|---|---|---|
| **ADM-01** | Bảng điều khiển Quản trị (Admin) | M | `admin/dashboard` | Chỉ ADMIN | Hoàn thành | Lối vào quản trị, cấm tuyệt đối vai trò USER thường truy cập. |
| **ADM-02** | Quản lý & Kiểm duyệt đội bóng | M | `admin/teams` | Admin | Hoàn thành | Tìm kiếm đội, xem chi tiết, Tạm ngưng (Suspend) / Kích hoạt lại + lý do. |
| **ADM-03** | Lịch sử thành viên & Tài khoản xóa | M | `admin/team-member-history`| Admin | Hoàn thành | Hiển thị tên thật của tài khoản đã xóa kèm cờ `[DELETED]`. |
| **ADM-04** | Hàng đợi giải quyết tranh chấp đội | M | `admin/team-disputes` | Admin | Hoàn thành | Danh sách tranh chấp NO-SHOW giữa các đội bóng. |
| **ADM-05** | Hàng đợi tranh chấp người đá lẻ | M | `admin/mercenary-disputes`| Admin | Hoàn thành | Danh sách tranh chấp giữa Captain và Mercenary. |
| **ADM-06** | Biểu mẫu ra quyết định tranh chấp | S | `sheet/resolve-dispute` | Admin | Hoàn thành | Nút Duyệt / Bác đơn + Ghi chú quyết định bắt buộc (1-500 ký tự). |
| **ADM-07** | Hàng đợi xử lý báo cáo sân bóng | M | `admin/venue-reports` | Admin | Hoàn thành | Xem danh sách sân bị phản ánh đóng cửa / sai thông tin. |
| **ADM-08** | Bật / Tắt hoạt động của sân bóng | S | `sheet/toggle-venue-status`| Admin | Hoàn thành | Đổi trạng thái Sân: ACTIVE <-> INACTIVE kèm lý do. |
| **ADM-09** | Quản lý danh mục Bộ môn thể thao | M/S | `admin/sports-management`| Admin | Hoàn thành | Bật / Tắt bộ môn (Bóng đá, Futsal,...), cảnh báo thao tác mới. |

---

## 3. Ma Trận Theo Dõi Tiêu Chí Nghiệm Thu (Acceptance Criteria Traceability)

| Mã AC | Tiêu chí nghiệm thu (Given / When / Then) | ID màn liên quan | Trạng thái | Kiểm tra thực tế |
|---|---|---|---|---|
| **AC-P01** | **[AUTH]** User mới xác thực thành công -> Mở form hoàn thiện hồ sơ PROF-02; User cũ về điểm đến trước đó. | AUTH-04, PROF-02 | **ĐẠT** | Đã kiểm tra qua hành trình J-01 & J-02. Mocking `isNewUser: true/false`. |
| **AC-P02** | **[TEAM]** User còn làm Captain của ít nhất 1 đội chưa giải tán -> Chặn xóa tài khoản, chuyển hướng giải quyết. | PROF-04, PROF-05, TEAM-09, TEAM-12 | **ĐẠT** | Đã kiểm tra qua hành trình J-06. Nút Xóa bị vô hiệu hóa, dẫn sang chuyển quyền hoặc giải tán. |
| **AC-P03** | **[MATCH-A]** Chủ kèo chấp nhận 1 yêu cầu -> Request đó Accepted, Kèo Confirmed, các request khác Superseded. | MATCH-09, MATCH-10, MATCH-13 | **ĐẠT** | Đã kiểm tra qua hành trình J-07. Cập nhật đồng loạt trạng thái trong store. |
| **AC-P04** | **[PAIRING]** Gói thay đổi thực sự -> Xóa xác nhận 2 bên; Revision cũ không được tự xác nhận lại. | PAIR-03, PAIR-05, PAIR-06, PAIR-07 | **ĐẠT** | Đã kiểm tra qua hành trình J-09 & J-10. Khi sửa chi phí/sân, `confirmA=false`, `confirmB=false`, `revision++`. |
| **AC-P05** | **[PRIVACY]** Đơn đá lẻ chưa Approved -> Không được thấy tên sân/địa chỉ/tọa độ ở mọi view. | MERC-02, MERC-03, MERC-04 | **ĐẠT** | Đã kiểm tra qua hành trình J-13. Ẩn toàn bộ chi tiết địa điểm, chỉ hiển thị Quận/Huyện. |
| **AC-P06** | **[MERCENARY]** Đơn Approved rút hợp lệ -> Trừ uy tín cá nhân, tin tuyển mở lại slot; Pending rút không trừ. | MERC-04, MERC-06, MERC-09 | **ĐẠT** | Đã kiểm tra qua hành trình J-14. Trừ 10 điểm uy tín khi rút Approved, giữ nguyên khi rút Pending. |
| **AC-P07** | **[RATING]** Bên mình chưa đánh giá -> Không được biết đối phương đã đánh giá hay chưa (Blind Rating). | REP-01, REP-02 | **ĐẠT** | Đã kiểm tra qua hành trình J-19. Ẩn hoàn toàn trạng thái nộp bài của đối phương cho đến khi mình submit. |
| **AC-P08** | **[DISPUTE]** Report không phải NOSHOW hoặc đã quá 24h -> Khóa hoàn toàn chức năng khiếu nại/tranh chấp. | REP-08, REP-09 | **ĐẠT** | Đã kiểm tra qua hành trình J-21. Báo cáo Bạo lực/Gian lận hoặc quá hạn 24h không có nút Tranh chấp. |
| **AC-P09** | **[NOTIFICATION]** Thông báo trỏ tới đối tượng không còn quyền xem / đã xóa -> Hiển thị giải thích, không màn trắng. | NOTIF-02 | **ĐẠT** | Đã kiểm tra qua hành trình J-18. Target resolver điều hướng an toàn với banner thông báo lỗi thân thiện. |
| **AC-P10** | **[DEMO]** Reset scenario và đổi nhân vật -> Cả hai phía quan sát cùng dữ liệu, chuyển trạng thái nhất quán. | Demo Panel, Store | **ĐẠT** | Đã kiểm tra qua hành trình J-24. Central reactive store đồng bộ đa người dùng cục bộ. |

---

## 4. Tình Trạng Hành Trình Nghiệm Thu Bắt Buộc (Journeys J-01..J-24)

| Mã | Tên hành trình người dùng | Phạm vi màn hình | Kết quả kiểm thử |
|---|---|---|---|
| **J-01** | Khởi động -> Onboarding -> SĐT -> OTP -> Hoàn thiện hồ sơ | APP-01, 02, AUTH-01..04, PROF-02 | **PASS** |
| **J-02** | OAuth Google/FB, OTP đổi số / gửi lại / hết hạn | AUTH-01, 02, 03 | **PASS** |
| **J-03** | Phiên hết hạn khi đang mở đối tượng từ thông báo | AUTH-06, NOTIF-01, 02 | **PASS** |
| **J-04** | Tạo đội -> Mời thành viên -> Đổi tài khoản -> Phản hồi lời mời | TEAM-01..08 | **PASS** |
| **J-05** | Chuyển quyền Captain tức thì -> Thành viên rời đội -> Giải tán | TEAM-09..13 | **PASS** |
| **J-06** | Xóa tài khoản bị chặn do làm Captain -> Chuyển giao / Giải tán -> Xóa thành công | PROF-04, 05, TEAM-09, 12, AUTH-05 | **PASS** |
| **J-07** | Tạo kèo A -> Đội khác gửi yêu cầu -> Chat giao lưu -> Chủ kèo duyệt | MATCH-01..06, 09, 10, CHAT-02 | **PASS** |
| **J-08** | Kèo A: Từ chối / Rút pending / Rút accepted (hủy kèo) | MATCH-08, 10, 11, 12 | **PASS** |
| **J-09** | Kèo B: Đăng đa sân, đa giờ -> Thương lượng -> Chốt gói -> 2 bên xác nhận | MATCH-04, 05, PAIR-01..06 | **PASS** |
| **J-10** | Kèo B: Xung đột gói (Revision thay đổi) / Ghép với đội khác (Superseded)| PAIR-03, 06, 07, 08 | **PASS** |
| **J-11** | Hủy kèo ở các mốc thời gian (OPEN, CONFIRMED, chặn sau giờ bắt đầu) | MATCH-12, 13 | **PASS** |
| **J-12** | Khám phá đá lẻ: GPS cấp quyền / từ chối / bán kính 1-20km / fallback | MERC-01, PROF-02 | **PASS** |
| **J-13** | Ứng tuyển đá lẻ: Ẩn sân trước duyệt -> Captain duyệt -> Hiện địa chỉ sân | MERC-02..05, 10, 11 | **PASS** |
| **J-14** | Slot cuối, trùng giờ, thành viên nộp đơn, rút pending vs rút approved | MERC-03, 06, 11, 12 | **PASS** |
| **J-15** | Captain tạo tin đá lẻ -> Đóng tin -> Khai báo trận ngoài hệ thống | MERC-07..09, 12, EXT-01..05 | **PASS** |
| **J-16** | Từ form tạo kèo -> Chọn / Thêm sân bóng mới -> Quay lại giữ nguyên dữ liệu form | MATCH-05, VEN-01, 02, 03 | **PASS** |
| **J-17** | Chat 2 đội / chat đá lẻ: canSend=false khi hết quyền, mất mạng reconnect | CHAT-01..05 | **PASS** |
| **J-18** | Đọc thông báo, catalog 35 loại notification, xử lý link hết hạn / đổi state | NOTIF-01..03 | **PASS** |
| **J-19** | Sau trận: Đánh giá ẩn (Blind rating), cập nhật tỷ số hoặc cờ No-Show | REP-01, 02 | **PASS** |
| **J-20** | Đánh giá hai chiều với người đá lẻ; Thẻ uy tín đội vs thẻ cá nhân | REP-03..05 | **PASS** |
| **J-21** | Gửi báo cáo vi phạm -> Tranh chấp NO-SHOW trong 24h -> Quyết định của Admin | REP-06..10, ADM-04..06 | **PASS** |
| **J-22** | Admin portal: Quản lý đội bóng, lịch sử user đã xóa, kiểm duyệt sân & bộ môn | ADM-01..09 | **PASS** |
| **J-23** | Đáp ứng màn hình 360/390/430px, bàn phím IME mở, safe area, touch target >= 48dp | Toàn bộ giao diện | **PASS** |
| **J-24** | Đổi nhân vật, đồng hồ mô phỏng (trước/sau trận, quá hạn 24h/30d), reset dữ liệu | Demo Panel, Store, Fixtures | **PASS** |

---

## 5. Kết Luận Bàn Giao

Toàn bộ **101 Screen/Sheet/Variant IDs** đã được ghi nhận, triển khai logic và hoàn thiện tài liệu bàn giao UI cho đội ngũ Android Jetpack Compose. Không có bất kỳ ID nào bị bỏ sót hay hoãn lại.
Mọi chi tiết triển khai được phân rã cụ thể trong 6 tài liệu đồng hành:
1. `screen-inventory.md` — Danh mục kiểm kê chi tiết từng màn và sheet.
2. `flows-and-states.md` — Sơ đồ luồng trạng thái, back stack và hành trình.
3. `design-system.md` — Hệ thống thiết kế, color tokens Stitch, typography và components.
4. `ui-handoff.md` — Đặc tả ScreenState, Events, Form validation và IME cho Jetpack Compose.
5. `fixtures-and-scenarios.md` — Bộ nhân vật, seed data, mock clock và preset demo.
6. `api-and-dependencies.md` — Bảng mapping endpoint API ArenaFlow-spec và 11 DEP IDs.
