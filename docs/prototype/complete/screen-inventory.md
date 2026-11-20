# ArenaFlow Prototype — Danh Mục Kiểm Kê Màn Hình & Trạng Thái (Screen Inventory)

> Ngày lập: 18/11/2026 · Ngôn ngữ: Tiếng Việt · Phiên bản: 1.0-complete  
> Tài liệu thuộc bộ bàn giao UI Prototype ArenaFlow trong `docs/prototype/complete/`.  
> Bao phủ đầy đủ **101 Screen/Sheet/Variant IDs** từ Mục 6 của `docs/prototype/PROTOTYPE_HANDOFF.md`.

---

## 1. Tổng Quan Kiểm Kê & Thống Kê (Inventory Statistics)

| Nhóm nghiệp vụ | Phạm vi IDs | Số lượng Screen (M) | Số lượng Sheet/Dialog (S) | Số lượng Variant/State (V) | Tổng IDs |
|---|---|---|---|---|---|
| **1. Khởi đầu, Xác thực & Hồ sơ** | APP-01..02, AUTH-01..06, PROF-01..06 | 7 | 4 | 3 | **14** |
| **2. Đội bóng & Lời mời** | TEAM-01..14 | 7 | 6 | 1 | **14** |
| **3. Khám phá & Vòng đời Kèo** | MATCH-01..14 | 8 | 4 | 2 | **14** |
| **4. Thương lượng Kèo B & Gói** | PAIR-01..08 | 4 | 2 | 2 | **8** |
| **5. Đá lẻ & Trận ngoài hệ thống**| MERC-01..12, EXT-01..05 | 10 | 6 | 1 | **17** |
| **6. Địa điểm & Sân bóng** | VEN-01..07 | 4 | 2 | 1 | **7** |
| **7. Tin nhắn & Thông báo** | CHAT-01..05, NOTIF-01..03 | 4 | 1 | 3 | **8** |
| **8. Sau trận, Uy tín & Tranh chấp**| REP-01..10 | 5 | 3 | 2 | **10** |
| **9. Quản trị hệ thống (Admin)** | ADM-01..09 | 6 | 3 | 0 | **9** |
| **TỔNG CỘNG HỆ THỐNG** | **101 IDs** | **55 M** | **31 S** | **15 V** | **101 IDs** |

*Ghi chú định nghĩa loại phần tử:*
- **M (Screen):** Màn hình hiển thị toàn trang (Full screen page / Top-level destination).
- **S (Sheet / Dialog):** Modal Bottom Sheet hoặc Dialog đè lên màn hình hiện tại để thực hiện hành động nhanh hoặc xác nhận.
- **V (Variant / State):** Biến thể giao diện hoặc trạng thái chuyên biệt nằm trong cùng một layout hoặc xử lý điều hướng đặc thù (Loading, Empty, Error, Conflict, Resolution).

---

## 2. Bảng Danh Mục Chi Tiết 101 Màn Hình & Trạng Thái

### 2.1. Khởi Đầu, Xác Thực & Hồ Sơ (APP-01..02, AUTH-01..06, PROF-01..06)

| ID | Tên gọi & Chức năng | Loại | Route / View ID | Vai trò & Quyền | Nguồn thiết kế | API Contract / UC | Tiến độ | Ghi chú bố cục & Hành vi |
|---|---|---|---|---|---|---|---|---|
| **APP-01** | Splash & Kiểm tra phiên đăng nhập | V | `app/launch` | Tất cả | USER-001 | USER-001 §2.1 | Hoàn thành | Kiểm tra JWT token trong local storage: Hợp lệ -> Shell; Hết hạn -> Refresh token -> Thất bại -> AUTH-01; Lỗi mạng -> Retry button. |
| **APP-02** | Giới thiệu ứng dụng (Onboarding) | M | `app/onboarding` | Khách / Mới | PROPOSAL | PROPOSAL [DEP-08] | Hoàn thành | 3 slides carousel (Tìm kèo, Tuyển đá lẻ, Uy tín minh bạch). Nút "Bỏ qua" và "Bắt đầu". Chỉ hiển thị lần đầu khi mở app. |
| **AUTH-01** | Chọn phương thức đăng nhập | M | `auth/login` | Khách | Stitch S1 | USER-001 §2.1 | Hoàn thành | Nút Đăng nhập Google, Facebook, Số điện thoại. Ghi chú tài khoản tự tạo ở lần đầu. Link Điều khoản dịch vụ. |
| **AUTH-02** | Nhập số điện thoại đăng ký/đăng nhập | M | `auth/phone` | Khách | Stitch S2a | USER-001 §2.2 | Hoàn thành | Input SĐT kèm đầu số +84. Nút "Tiếp tục" / "Gửi mã OTP". Nút Back quay lại AUTH-01. Kiểm tra regex định dạng SĐT Việt Nam. |
| **AUTH-03** | Nhập mã OTP xác thực | M | `auth/otp` | Khách | Stitch S2b | USER-001 §2.3 | Hoàn thành | 6 ô nhập mã OTP, đếm ngược 60s gửi lại mã, link "Đổi số điện thoại". Trạng thái Default, Loading, Error, Expired, Success. |
| **AUTH-04** | Điều hướng phân luồng sau đăng nhập | V | `auth/callback` | Đã xác thực | UC-U01 | UC-U01 | Hoàn thành | User mới (`isNewUser=true`) -> điều hướng sang PROF-02 hoàn thiện hồ sơ. User cũ (`isNewUser=false`) -> điều hướng về trang trước (AC-P01). |
| **AUTH-05** | Thông báo tài khoản đã xóa (AUTH_003) | V | `auth/deleted` | User đã xóa | USER-001 | USER-001 §2.3 | Hoàn thành | Hiển thị thông báo tài khoản đã bị đóng vĩnh viễn theo yêu cầu. Nút quay lại chọn tài khoản khác. Tuyệt đối không có nút khôi phục giả. |
| **AUTH-06** | Cảnh báo phiên làm việc hết hạn | S/V | `sheet/session-expired` | Đã hết hạn | USER-001 | USER-001 §2.4 | Hoàn thành | Dialog thông báo session hết hạn. Nút "Đăng nhập lại". Giữ lại deep link đích (`redirectUrl`) để sau khi đăng nhập thành công quay lại đúng ngữ cảnh. |
| **PROF-01** | Hồ sơ của tôi (My Profile) | M | `profile/me` | User đăng nhập | Stitch S3 | USER-001 §2.5 | Hoàn thành | Avatar, Tên hiển thị, Bio, Vị trí sở trường, Giới tính, Khu vực (Tỉnh/Phường), Điểm uy tín cá nhân, Nút "Sửa hồ sơ" và "Cài đặt". |
| **PROF-02** | Hoàn thiện & Chỉnh sửa hồ sơ | M | `profile/edit` | User đăng nhập | Stitch S4 | USER-001 §2.6 | Hoàn thành | Form nhập: Tên (bắt buộc, không chỉ khoảng trắng), Avatar mockup, Bio, Vị trí (GK/DF/MF/FW), Ngày sinh (>= 13 tuổi), Giới tính, Tỉnh/Phường. |
| **PROF-03** | Menu tài khoản & Cài đặt | M/S | `profile/account-menu` | User đăng nhập | PROPOSAL | PROPOSAL [DEP-08] | Hoàn thành | Danh sách tùy chọn: Thông tin cá nhân, Đổi tài khoản, Trợ giúp & Hỗ trợ, Đăng xuất (mở PROF-06), Xóa tài khoản (mở PROF-04). |
| **PROF-04** | Xác nhận xóa tài khoản | S | `sheet/delete-account` | User cá nhân | Stitch S5, USER-002 | USER-002 §2.1 | Hoàn thành | Dialog cảnh báo hậu quả mất toàn bộ dữ liệu, lịch sử trận và điểm uy tín. Nút "Hủy bỏ" và "Xác nhận xóa vĩnh viễn". |
| **PROF-05** | Chặn xóa tài khoản đội trưởng | M/S | `profile/delete-blocked` | Captain | USER-002 | USER-002 §2.2 | Hoàn thành | **AC-P02**: Hiển thị danh sách các đội đang làm Captain. Giải thích phải Chuyển quyền (TEAM-09) hoặc Giải tán đội (TEAM-12) trước khi xóa. |
| **PROF-06** | Xác nhận đăng xuất | S | `sheet/logout` | User đăng nhập | PROPOSAL | PROPOSAL | Hoàn thành | Bottom sheet / Dialog xác nhận đăng xuất. Xóa session/token lưu trữ cục bộ và đưa người dùng về AUTH-01. |

### 2.2. Đội Bóng & Lời Mời (TEAM-01..14)

| ID | Tên gọi & Chức năng | Loại | Route / View ID | Vai trò & Quyền | Nguồn thiết kế | API Contract / UC | Tiến độ | Ghi chú bố cục & Hành vi |
|---|---|---|---|---|---|---|---|---|
| **TEAM-01** | Đội của tôi (My Teams) | M | `team/my-teams` | User đăng nhập | DEPENDENCY | USER-001 [DEP-01] | Hoàn thành | Danh sách các đội người dùng đang tham gia kèm nhãn vai trò (Đội trưởng / Thành viên). Trạng thái trống gợi ý Tạo đội hoặc Xem lời mời. |
| **TEAM-02** | Danh sách đội toàn hệ thống | M | `team/system-teams` | Tất cả | Stitch S6 | USER-001 §2.7 | Hoàn thành | Danh sách đội bóng trên toàn hệ thống có phân trang (pagination), bộ lọc khu vực, nút xem chi tiết đội. |
| **TEAM-03** | Tạo đội bóng mới | M | `team/create` | User đăng nhập | Stitch S7 | USER-001 §2.8 | Hoàn thành | Form tạo: Tên đội, Ảnh đại diện, Khu vực hoạt động (Tỉnh/Phường), Tối đa 3 nhãn trình độ (Vui vẻ, Trung bình, Bán chuyên). Validate trùng tên. |
| **TEAM-04** | Chi tiết đội ACTIVE | M | `team/detail` | Captain/Member/Outsider| Stitch S8a | USER-001 §2.9 | Hoàn thành | Biến thể theo vai trò: Captain (nút Quản trị, Mời, Giải tán), Member (nút Rời đội), Outsider (nút Gửi yêu cầu giao lưu). Tabs: Thông tin, Thành viên, Lịch đấu. |
| **TEAM-05** | Chỉnh sửa thông tin đội | M | `team/edit` | Captain | USER-001 | USER-001 §2.10 | Hoàn thành | Form tái sử dụng TEAM-03. Chỉ Captain mới có quyền lưu. Báo lỗi 403 nếu quyền không hợp lệ. |
| **TEAM-06** | Danh sách thành viên đội | M/S | `team/members` | Tất cả | USER-001 | USER-001 §2.11 | Hoàn thành | Danh sách thành viên: Tên, Avatar, Vị trí, Vai trò (Captain/Thành viên). Với user đã bị xóa tài khoản -> ẩn tên thành "Người dùng ArenaFlow" (AC-P02). |
| **TEAM-07** | Mời thành viên mới vào đội | S | `sheet/team-invite` | Captain | Stitch S9 | USER-001 §2.12 [DEP-02] | Hoàn thành | Bottom sheet nhập/chọn người dùng cần mời. Hiển thị thông báo nếu đã mời hoặc đã là thành viên của đội. |
| **TEAM-08** | Phản hồi lời mời từ thông báo | M/S | `team/invitation-response`| Thành viên được mời | UC-T03 | UC-T03 [DEP-04] | Hoàn thành | Mở từ thông báo: Hiển thị thông tin đội mời, nút "Chấp nhận" hoặc "Từ chối". Cảnh báo nếu đội bóng đã bị giải tán trước khi phản hồi. |
| **TEAM-09** | Chuyển quyền đội trưởng | S | `sheet/transfer-captain` | Captain | Stitch S10 | USER-001 §2.13 | Hoàn thành | Bottom sheet danh sách thành viên trong đội. Chọn thành viên để bàn giao. Chuyển giao tức thì không cần bên nhận đồng ý. |
| **TEAM-10** | Lịch sử các đời đội trưởng | M/S | `team/captain-history` | Thành viên đội | USER-001 | USER-001 §2.14 | Hoàn thành | Danh sách mốc thời gian chuyển giao chức vụ đội trưởng, tên người tiền nhiệm và người kế nhiệm. |
| **TEAM-11** | Thành viên rời đội | S | `sheet/leave-team` | Thành viên thường | Stitch S11 | USER-001 §2.15 | Hoàn thành | Dialog xác nhận tự nguyện rời đội. Nếu Captain bấm -> Hiển thị cảnh báo buộc phải chuyển quyền (TEAM-09) hoặc giải tán (TEAM-12). |
| **TEAM-12** | Giải tán đội bóng vĩnh viễn | S | `sheet/disband-team` | Captain | Stitch S12 | USER-002 §2.3 | Hoàn thành | Dialog cảnh báo hậu quả nghiêm trọng: Hủy tất cả các kèo đang mở/đã ghép, xóa đội vĩnh viễn không thể khôi phục. Cần gõ xác nhận. |
| **TEAM-13** | Chi tiết đội INACTIVE/DISBANDED | M/V | `team/disbanded-detail` | Tất cả | Stitch S8b | USER-002 §2.4 | Hoàn thành | Màn hình chi tiết đội đã giải tán: Banner cảnh báo màu đỏ/xám "Đội bóng đã ngừng hoạt động", toàn bộ nút hành động và quản trị bị khóa/ẩn. |
| **TEAM-14** | Chọn đội bóng đang thao tác | S | `sheet/select-acting-team` | Captain nhiều đội | Dùng chung | Quy ước chung | Hoàn thành | Bottom sheet xuất hiện khi người dùng là Captain của nhiều hơn 1 đội lúc bấm Đăng kèo, Gửi yêu cầu, Nhận kèo B hoặc Tuyển người. |

### 2.3. Khám Phá, Tạo & Vòng Đời Kèo (MATCH-01..14)

| ID | Tên gọi & Chức năng | Loại | Route / View ID | Vai trò & Quyền | Nguồn thiết kế | API Contract / UC | Tiến độ | Ghi chú bố cục & Hành vi |
|---|---|---|---|---|---|---|---|---|
| **MATCH-01**| Khám phá & Tìm kiếm kèo đấu | M | `match/explore` | Tất cả | Prototype cũ + MATCH-001 | MATCH-001 §2.1 | Hoàn thành | Danh sách bài đăng kèo A và B. Thẻ tóm tắt: Đội chủ kèo, Giờ đấu, Sân đấu / Khu vực, Trình độ, Badge trạng thái (OPEN/CONFIRMED). |
| **MATCH-02**| Bộ lọc tìm kiếm kèo đấu | S | `sheet/match-filter` | Tất cả | MATCH-001 | MATCH-001 §2.2 | Hoàn thành | Bottom sheet lọc: Định dạng sân (5/7/11 người), Khung thời gian (Sáng, Chiều, Tối), Trình độ, Khu vực quận/huyện. Nút Áp dụng và Đặt lại. |
| **MATCH-03**| Chi tiết bài đăng kèo A | M | `match/detail-a` | Chủ kèo / Đối thủ | MATCH-001 | MATCH-001 §2.3 | Hoàn thành | Kèo A cố định: 1 sân + 1 khung giờ cụ thể, thỏa thuận tiền sân/cọc. Nút "Gửi yêu cầu bắt kèo" (cho đối thủ) hoặc "Xem danh sách yêu cầu" (cho chủ kèo). |
| **MATCH-04**| Chi tiết bài đăng kèo B | M | `match/detail-b` | Đối thủ | MATCH-001 / MATCH-002 | MATCH-002 §2.1 | Hoàn thành | Kèo B linh hoạt: Danh sách 1-5 sân gợi ý và 1-5 khung giờ lựa chọn độc lập. Nút "Nhận kèo & Bắt đầu thương lượng" (tạo phòng thương lượng riêng). |
| **MATCH-05**| Tạo bài đăng kèo mới (A hoặc B) | M | `match/create` | Captain | UC-M01 / UC-M02 | MATCH-001 §2.1 | Hoàn thành | Chọn loại bài đăng (A hay B), chọn đội bóng (nếu nhiều đội), định dạng sân, trình độ. Kèo A: 1 sân + 1 giờ. Kèo B: Chọn 1-5 sân và 1-5 giờ. |
| **MATCH-06**| Xem lại thông tin trước khi đăng | M/S | `match/preview` | Captain | PROPOSAL | PROPOSAL [DEP-08] | Hoàn thành | Modal xem trước bài đăng, kiểm tra lại toàn bộ thông tin chi phí, giờ đấu, địa điểm. Nút "Xác nhận đăng bài" chống double-click. |
| **MATCH-07**| Lịch thi đấu & Kèo của đội tôi | M | `match/team-schedule` | Thành viên đội | MATCH-001 | MATCH-001 §2.11 | Hoàn thành | Quản lý kèo của đội theo các tab: "Sắp tới" (OPEN, CONFIRMED), "Đã kết thúc" (COMPLETED, ARCHIVED), "Đã hủy / Quá hạn" (CANCELLED, EXPIRED). |
| **MATCH-08**| Danh sách yêu cầu bắt kèo đã gửi | M | `match/sent-requests` | Captain gửi | MATCH-001 | MATCH-001 §2.12 | Hoàn thành | Danh sách yêu cầu gửi cho kèo A. Trạng thái PENDING (chờ duyệt), ACCEPTED, REJECTED, WITHDRAWN. Nút Rút yêu cầu. |
| **MATCH-09**| Danh sách yêu cầu gửi đến kèo A | M | `match/incoming-requests`| Chủ kèo A | MATCH-001 | MATCH-001 §2.7 | Hoàn thành | Chủ kèo duyệt danh sách đối thủ nộp đơn: Thẻ đội đối thủ, điểm uy tín, nút Nhắn tin (CHAT-02), nút "Chấp nhận" hoặc "Từ chối". |
| **MATCH-10**| Chi tiết & Duyệt yêu cầu đối thủ | M/S | `match/request-detail` | Chủ kèo A | MATCH-001 | MATCH-001 §2.8..2.9 | Hoàn thành | **AC-P03**: Khi bấm "Chấp nhận", yêu cầu chuyển thành ACCEPTED, kèo chuyển sang CONFIRMED, mọi yêu cầu PENDING khác tự động chuyển thành SUPERSEDED. |
| **MATCH-11**| Rút yêu cầu bắt kèo đã gửi | S | `sheet/withdraw-request`| Captain gửi | MATCH-001 | MATCH-001 §2.10 | Hoàn thành | Dialog xác nhận rút yêu cầu: Rút lúc PENDING không bị phạt điểm uy tín; Rút sau khi đã ACCEPTED bị coi là hành vi hủy trận và bị trừ điểm uy tín. |
| **MATCH-12**| Hủy bài đăng kèo đấu | S | `sheet/cancel-match` | Chủ kèo | MATCH-001 | MATCH-001 §2.5 | Hoàn thành | Bottom sheet hủy kèo: Nhập lý do bắt buộc. Chặn hủy nếu kèo A đang có yêu cầu PENDING (phải từ chối trước) hoặc trận đấu đã đến giờ bắt đầu. |
| **MATCH-13**| Biến thể trạng thái vòng đời kèo | M/V | `match/status-variants`| Tất cả | Dùng lại chi tiết | MATCH-001 §3.1 | Hoàn thành | Biến thể hiển thị cho các trạng thái: CONFIRMED (đã chốt), Đang diễn ra, COMPLETED (kết thúc -> mở đánh giá REP-01), CANCELLED, EXPIRED. |
| **MATCH-14**| Chỉnh sửa thỏa thuận chi phí kèo A | M/S | `sheet/match-cost-edit`| Chủ kèo A | MATCH-001 | MATCH-001 §2.4 | Hoàn thành | Bottom sheet điều chỉnh cơ cấu chi phí khi bài kèo A còn ở trạng thái OPEN: Tiền sân, tỷ lệ chia (50-50, thua trả,...), tiền đặt cọc giữ sân. |

### 2.4. Thương Lượng Kèo B & Chốt Gói (PAIR-01..08)

| ID | Tên gọi & Chức năng | Loại | Route / View ID | Vai trò & Quyền | Nguồn thiết kế | API Contract / UC | Tiến độ | Ghi chú bố cục & Hành vi |
|---|---|---|---|---|---|---|---|---|
| **PAIR-01**| Danh sách thương lượng (Chủ kèo B)| M | `pairing/host-list` | Chủ kèo B | MATCH-002 | MATCH-002 §2.2 | Hoàn thành | Quản lý danh sách các đội đã nhận kèo B. Hiển thị tiến độ thỏa thuận của từng đội (Gói đề xuất, trạng thái xác nhận của từng bên). |
| **PAIR-02**| Danh sách thương lượng của đội tôi | M | `pairing/my-negotiations`| Captain nhận | MATCH-002 | MATCH-002 §2.3 | Hoàn thành | Danh sách các phòng thương lượng mà đội đang tham gia thảo luận với các chủ kèo B khác nhau. Trạng thái Đang thương lượng / Thất bại. |
| **PAIR-03**| Chi tiết phòng thương lượng gói B | M | `pairing/room` | Captain 2 đội | MATCH-002 | MATCH-002 §2.4 | Hoàn thành | Không gian làm việc riêng biệt giữa 2 đội trưởng: Phương án đã chọn (1 sân + 1 giờ), Chi phí đề xuất, Chat đính kèm, Trạng thái xác nhận của 2 bên. |
| **PAIR-04**| Chọn phương án sân & giờ thi đấu | M/S | `sheet/select-venue-time`| Captain 2 đội | MATCH-002 | MATCH-002 §2.5 | Hoàn thành | Chọn chính xác 1 sân và 1 khung giờ từ các phương án bài B đã đăng. Khóa các sân đã bị vô hiệu hóa (INACTIVE) trong danh mục. |
| **PAIR-05**| Tùy chỉnh chi phí & đặt cọc gói | M/S | `sheet/edit-package-cost`| Captain 2 đội | MATCH-002 | MATCH-002 §2.6 | Hoàn thành | Form điều chỉnh tiền sân, chia tiền, tiền cọc và bên giữ cọc trung gian ngoài đời. Validate dữ liệu không được âm. |
| **PAIR-06**| Xem lại & Xác nhận gói thương lượng | S/V | `sheet/confirm-package`| Captain 2 đội | MATCH-002 | MATCH-002 §2.7 | Hoàn thành | **AC-P04**: Nút xác nhận gói. Khi một bên thay đổi phương án sân/giờ hoặc chi phí, hệ thống tự động tăng revision và xóa toàn bộ xác nhận trước đó. |
| **PAIR-07**| Xung đột phiên bản gói (PAIRING_006)| V | `pairing/conflict` | Captain 2 đội | MATCH-002 | MATCH-002 §2.7 | Hoàn thành | Cảnh báo xuất hiện khi đối phương vừa chỉnh sửa gói trong lúc mình đang xem. Buộc tải lại phiên bản mới nhất và yêu cầu tự bấm xác nhận lại. |
| **PAIR-08**| Trạng thái kết thúc phòng thương lượng| V | `pairing/final-state` | Captain 2 đội | MATCH-002 | MATCH-002 §3.2 | Hoàn thành | Biến thể hiển thị khi: CONFIRMED (ghép thành công), SUPERSEDED (chủ kèo đã chốt với đội khác), CANCELLED (kèo bị hủy), EXPIRED (quá hạn). Khóa sửa form. |

### 2.5. Tuyển Người Đá Lẻ, Ứng Tuyển & Trận Ngoài (MERC-01..12, EXT-01..05)

| ID | Tên gọi & Chức năng | Loại | Route / View ID | Vai trò & Quyền | Nguồn thiết kế | API Contract / UC | Tiến độ | Ghi chú bố cục & Hành vi |
|---|---|---|---|---|---|---|---|---|
| **MERC-01**| Khám phá & Tìm tin tuyển đá lẻ | M | `mercenary/explore` | Tất cả | MERCENARY-001 | MERCENARY-001 §2.3 | Hoàn thành | Danh sách tin tuyển: Vị trí (GK/DF/MF/FW), Khung giờ, Bán kính GPS (1-20km, mặc định 3km). Fallback theo khu vực Tỉnh/Phường nếu không cấp GPS. |
| **MERC-02**| Chi tiết tin tuyển người đá lẻ | M | `mercenary/detail` | Ứng viên | MERCENARY-001 | MERCENARY-001 §2.4 | Hoàn thành | **AC-P05**: Ẩn tuyệt đối tên sân, địa chỉ số nhà, tọa độ đối với ứng viên chưa được duyệt. Chỉ hiển thị Quận/Huyện để bảo đảm quyền riêng tư. |
| **MERC-03**| Xác nhận nộp đơn ứng tuyển đá lẻ | S | `sheet/apply-mercenary`| Ứng viên | MERCENARY-001 | MERCENARY-001 §2.7 | Hoàn thành | Bottom sheet gửi đơn: Kiểm tra trùng giờ thi đấu, kiểm tra đã nộp đơn hay chưa, kiểm tra có thuộc đội đang tuyển hay không. Trạng thái PENDING. |
| **MERC-04**| Danh sách đơn ứng tuyển của tôi | M | `mercenary/my-applications`| Ứng viên | MERCENARY-001 | MERCENARY-001 §2.9 | Hoàn thành | Quản lý các đơn đá lẻ cá nhân đã nộp: PENDING (đang chờ), APPROVED (được duyệt), REJECTED (từ chối), WITHDRAWN (đã rút). |
| **MERC-05**| Chi tiết đơn ứng tuyển đã nộp | M | `mercenary/application-detail`| Ứng viên | MERCENARY-001 | MERCENARY-001 §2.10 | Hoàn thành | Khi trạng thái là APPROVED -> Hiển thị tên sân, địa chỉ chi tiết, bản đồ vị trí, nút chat trực tiếp với Captain (CHAT-03) và nút Rút đơn. |
| **MERC-06**| Rút đơn ứng tuyển đá lẻ | S | `sheet/withdraw-mercenary`| Ứng viên | MERCENARY-001 | MERCENARY-001 §2.13 | Hoàn thành | **AC-P06**: Rút đơn PENDING không bị trừ uy tín. Rút đơn đã APPROVED sẽ bị trừ 10 điểm uy tín cá nhân và làm tin tuyển mở lại slot cho người khác. |
| **MERC-07**| Chọn trận đấu để tuyển người | M/S | `mercenary/select-match`| Captain | UC-ME01 | UC-ME01 [DEP-07] | Hoàn thành | Chọn trận cần tuyển: Kèo hệ thống đã ở trạng thái CONFIRMED hoặc Khai báo trận ngoài hệ thống (EXT-01). |
| **MERC-08**| Đăng tin tuyển người đá lẻ | M | `mercenary/create-post`| Captain | MERCENARY-001 | MERCENARY-001 §2.1 | Hoàn thành | Đăng tuyển 1-4 vị trí, mỗi vị trí số lượng 1-10 người. Mỗi vị trí được lưu trữ thành một tin tuyển riêng biệt theo API contract. |
| **MERC-09**| Quản lý tin tuyển của đội tôi | M | `mercenary/team-posts` | Captain | MERCENARY-001 | MERCENARY-001 §2.2 | Hoàn thành | Danh sách các tin đang tuyển của đội: Trạng thái OPEN, FILLED, CLOSED, EXPIRED; số lượng slot đã duyệt / tổng số cần tuyển. |
| **MERC-10**| Danh sách ứng viên nộp đơn đá lẻ | M | `mercenary/applicants` | Captain | MERCENARY-001 | MERCENARY-001 §2.8 | Hoàn thành | Danh sách ứng viên cho từng tin tuyển: Tên, Avatar, Vị trí, Điểm uy tín, Nút mở chat cá nhân, Nút Duyệt hoặc Từ chối ứng viên. |
| **MERC-11**| Duyệt hoặc từ chối ứng viên | M/S | `sheet/review-applicant`| Captain | MERCENARY-001 | MERCENARY-001 §2.11..2.12| Hoàn thành | Xác nhận duyệt ứng viên. Nếu đã đủ số lượng tuyển (FILLED) -> Chặn duyệt thêm. Nếu ứng viên bị xung đột lịch -> Thông báo lỗi. Không có nút hủy duyệt. |
| **MERC-12**| Điều chỉnh số lượng & Đóng tin | S | `sheet/manage-post` | Captain | MERCENARY-001 | MERCENARY-001 §2.5..2.6 | Hoàn thành | Bottom sheet chỉnh sửa: Tăng/giảm số lượng cần tuyển (không được nhỏ hơn số ứng viên đã duyệt); Nút Đóng tin tuyển sớm (chuyển sang CLOSED). |
| **EXT-01** | Khai báo trận đấu ngoài hệ thống | M | `external/create` | Captain | MERCENARY-001 | MERCENARY-001 §2.28 | Hoàn thành | Dành cho đội đã có đối thủ hoặc đá nội bộ: Chọn sân, ngày giờ, đối thủ giao lưu tùy chọn, thêm các vị trí cần tuyển người đá lẻ đi kèm. |
| **EXT-02** | Danh sách & Chi tiết trận đấu ngoài | M | `external/detail` | Thành viên đội | MERCENARY-001 | MERCENARY-001 §2.29..2.30| Hoàn thành | Xem thông tin trận đấu tự do của đội, danh sách tin tuyển người đi kèm và trạng thái các ứng viên tham gia. |
| **EXT-03** | Chỉnh sửa thông tin trận ngoài | M | `external/edit` | Captain | MERCENARY-001 | MERCENARY-001 §2.31 | Hoàn thành | Cập nhật thời gian, địa điểm trận ngoài. Khóa sửa sân và giờ nếu trận đấu đã có ít nhất một ứng viên đá lẻ được APPROVED. |
| **EXT-04** | Hủy trận đấu ngoài hệ thống | S | `sheet/cancel-external`| Captain | MERCENARY-001 | MERCENARY-001 §2.32 | Hoàn thành | Dialog xác nhận hủy trận ngoài trước giờ bóng lăn. Hệ thống tự động đóng các tin tuyển và hủy các đơn ứng tuyển liên quan. |
| **EXT-05** | Thêm vị trí tuyển cho trận ngoài | M/S | `external/add-position`| Captain | MERCENARY-001 | MERCENARY-001 §2.33 | Hoàn thành | Form bổ sung thêm vị trí cần tuyển (ví dụ cần tuyển thêm 1 Thủ môn) vào trận ngoài hiện có. |

### 2.6. Địa Điểm & Sân Bóng (VEN-01..07)

| ID | Tên gọi & Chức năng | Loại | Route / View ID | Vai trò & Quyền | Nguồn thiết kế | API Contract / UC | Tiến độ | Ghi chú bố cục & Hành vi |
|---|---|---|---|---|---|---|---|---|
| **VEN-01** | Tìm kiếm & Chọn sân bóng | M/S | `venue/picker` | Tất cả | VENUE-001 | VENUE-001 §2.2 | Hoàn thành | Bộ chọn sân bóng dùng chung: Tìm kiếm, lọc theo quận/huyện, phân trang. Khi chọn sân -> Quay lại form tạo kèo/trận ngoài và bảo lưu nguyên vẹn dữ liệu form. |
| **VEN-02** | Chi tiết thông tin sân bóng | M | `venue/detail` | Tất cả | VENUE-001 | VENUE-001 §2.3 | Hoàn thành | Tên sân, Địa chỉ, Tọa độ, Số điện thoại liên hệ, Loại sân (5/7/11), Tiện ích (đèn, nước, gửi xe), Thư viện ảnh sân, Nút Báo cáo sai thông tin. |
| **VEN-03** | Thêm sân bóng mới | M | `venue/create` | User đăng nhập | VENUE-001 | VENUE-001 §2.1 | Hoàn thành | Form thêm sân: Tên sân, Địa chỉ, Quận/Huyện, Loại sân, Số liên hệ. Tạo xong có hiệu lực tức thì và có thể chọn ngay vào bài đăng kèo. |
| **VEN-04** | Chỉnh sửa thông tin sân bóng | M | `venue/edit` | Người tạo / Admin | VENUE-001 | VENUE-001 §2.12 | Hoàn thành | Cập nhật thông tin sân. Kiểm tra quyền sở hữu (người tạo sân hoặc ADMIN hệ thống). Báo lỗi 403 nếu không đủ thẩm quyền. |
| **VEN-05** | Quản lý thư viện ảnh sân bóng | M/S | `venue/photos` | User / Admin | VENUE-001 | VENUE-001 §2.4..2.6 | Hoàn thành | Xem gallery ảnh, tải ảnh lên (tối đa 5 ảnh, mock upload), đặt 1 ảnh làm ảnh đại diện chính (Primary photo), xóa ảnh theo quyền sở hữu. |
| **VEN-06** | Báo cáo sai lệch / Sân đóng cửa | S/M | `sheet/report-venue` | User đăng nhập | VENUE-001 | VENUE-001 §2.7 | Hoàn thành | Form gửi phản ánh sân bóng: Sai số điện thoại, sai địa chỉ, sân đã ngừng kinh doanh / giải tỏa. Gửi vào hàng đợi duyệt của Admin. |
| **VEN-07** | Trạng thái sân ngừng hoạt động | V | `venue/inactive-state` | Tất cả | VENUE-001 | VENUE-001 §3.1 | Hoàn thành | Giao diện sân có cờ INACTIVE: Banner cảnh báo màu vàng "Sân đã ngừng hoạt động", cấm chọn sân này khi tạo bài kèo hoặc gói thương lượng mới. |

### 2.7. Tin Nhắn & Thông Báo (CHAT-01..05, NOTIF-01..03)

| ID | Tên gọi & Chức năng | Loại | Route / View ID | Vai trò & Quyền | Nguồn thiết kế | API Contract / UC | Tiến độ | Ghi chú bố cục & Hành vi |
|---|---|---|---|---|---|---|---|---|
| **CHAT-01**| Hộp thư & Danh sách cuộc trò chuyện | M | `chat/inbox` | User đăng nhập | MATCH-001 | MATCH-001 §2.20 | Hoàn thành | Danh sách hội thoại phân chia theo ngữ cảnh: Chat kèo 2 đội, Chat ứng viên đá lẻ. Hiển thị tin nhắn cuối cùng, thời gian, huy hiệu chưa đọc. |
| **CHAT-02**| Phòng chat giao lưu giữa hai đội | M | `chat/match-room` | Thành viên 2 đội | MATCH-001 | MATCH-001 §2.21 | Hoàn thành | Header hiển thị thông tin kèo đấu. Bong bóng chat hiển thị Avatar và Tên thật thành viên gửi tin, mốc thời gian, trạng thái đã đọc. |
| **CHAT-03**| Phòng chat Captain & Ứng viên đá lẻ| M/V | `chat/mercenary-room` | Captain / Ứng viên | MERCENARY-001 | MERCENARY-001 §2.21 | Hoàn thành | Header hiển thị đơn ứng tuyển và vị trí. Lối tắt xem hồ sơ cá nhân ứng viên và xem lại bài đăng tuyển người. |
| **CHAT-04**| Trạng thái phòng chat (Read-only/Lỗi)| V | `chat/room-states` | Người trong phòng | Contract Chat | MATCH-001 §2.22 | Hoàn thành | Xử lý `canSend=false`: Hiển thị thanh thông báo "Phòng chat ở chế độ chỉ đọc do kèo/đơn đã kết thúc hoặc quá hạn lưu trữ 90 ngày". Thanh nhập liệu bị khóa. |
| **CHAT-05**| Báo cáo vi phạm trong phòng chat | S | `sheet/report-chat` | Người trong phòng | REPUTATION / MERCENARY | REPUTATION §2.3 | Hoàn thành | Bottom sheet báo cáo nội dung quấy rối, xúc phạm hoặc lừa đảo phát sinh trong hội thoại. Không cho gửi trùng loại báo cáo cho cùng đối tượng. |
| **NOTIF-01**| Trung tâm thông báo của tôi | M | `notifications/center`| User đăng nhập | NOTIFICATION-001 | NOTIFICATION-001 §2.1 | Hoàn thành | Tabs "Tất cả" và "Chưa đọc". Nút "Đánh dấu tất cả là đã đọc". Phân trang thông báo. Lưu trữ tối đa 30 ngày theo quy định spec. |
| **NOTIF-02**| Xử lý điều hướng thông báo (Target)| V | `notifications/resolver`| User đăng nhập | NOTIFICATION-001 | NOTIFICATION-001 §4.3 | Hoàn thành | **AC-P09**: Khi nhấn vào thông báo trỏ tới kèo/đội/đơn đã bị xóa hoặc mất quyền truy cập -> Điều hướng đến trang thông báo lý do rõ ràng, tuyệt đối không màn trắng. |
| **NOTIF-03**| Danh mục 35 loại thông báo mẫu | V | `notifications/catalog` | Tất cả | NOTIFICATION-001 | NOTIFICATION-001 §4.2 | Hoàn thành | Bảng catalog 35 notification types với icon đại diện, mẫu thông điệp tiếng Việt và mapping chính xác tới Screen ID tương ứng. |

### 2.8. Sau Trận, Uy Tín & Tranh Chấp (REP-01..10)

| ID | Tên gọi & Chức năng | Loại | Route / View ID | Vai trò & Quyền | Nguồn thiết kế | API Contract / UC | Tiến độ | Ghi chú bố cục & Hành vi |
|---|---|---|---|---|---|---|---|---|
| **REP-01** | Đánh giá đối phương sau trận đấu | M | `reputation/rate-match` | Captain 2 bên | REPUTATION-001 | REPUTATION-001 §2.1 | Hoàn thành | Chấm điểm sao 1-5, viết nhận xét tự do, cập nhật tỷ số trận đấu (tùy chọn) hoặc bật cờ Bùng kèo (Không đến / NO-SHOW). |
| **REP-02** | Xem kết quả đánh giá trận đấu | M/V | `reputation/match-ratings`| Captain 2 bên | REPUTATION-001 | REPUTATION-001 §2.2 | Hoàn thành | **AC-P07**: Cơ chế Đánh giá ẩn (Blind Rating). Trước khi bên mình gửi đánh giá, giao diện hoàn toàn không tiết lộ đối phương đã gửi hay chưa. Khóa sửa/xóa sau khi gửi. |
| **REP-03** | Đánh giá hai chiều với người đá lẻ | M | `reputation/rate-mercenary`| Captain / Ứng viên | MERCENARY-001 | MERCENARY-001 §2.18..2.19| Hoàn thành | Đội trưởng chấm điểm cho từng cầu thủ đá lẻ; Cầu thủ đá lẻ chấm điểm cho phong cách thi đấu của đội bóng. Hỗ trợ cờ Không đến riêng. |
| **REP-04** | Thẻ uy tín đội bóng | M/S | `reputation/team-card` | Tất cả | REPUTATION-001 | REPUTATION-001 §2.9 | Hoàn thành | Điểm uy tín (Reputation score), Tỷ lệ hoàn thành trận (chỉ hiển thị khi đã đấu >= 5 trận), Huy hiệu uy tín (Badge). Đánh giá từ đá lẻ hiển thị ở mục riêng. |
| **REP-05** | Thẻ uy tín cá nhân người đá lẻ | M/S | `reputation/player-card`| Tất cả | MERCENARY-001 | MERCENARY-001 §2.24 | Hoàn thành | Thẻ uy tín của cầu thủ: Điểm uy tín cá nhân (khởi tạo 100), số trận đá lẻ đã hoàn thành, số lần bị trừ điểm do hủy/rút đơn muộn. |
| **REP-06** | Gửi báo cáo vi phạm (Submit Report)| M/S | `sheet/submit-report` | User đăng nhập | REPUTATION-001 | REPUTATION-001 §2.3 | Hoàn thành | Form báo cáo: Chọn đối tượng bị báo cáo (Đội/Cá nhân), chọn loại vi phạm (NOSHOW, VIOLENCE, CHEATING, BAD_BEHAVIOR), nhập lý do cụ thể. |
| **REP-07** | Danh sách báo cáo vi phạm liên quan | M | `reputation/reports-list`| Đội / Cá nhân | Các endpoint Report | REPUTATION-001 §2.4 | Hoàn thành | Danh sách các biên bản báo cáo vi phạm: Tab "Báo cáo đã gửi" và Tab "Bị báo cáo vi phạm". |
| **REP-08** | Chi tiết báo cáo vi phạm | M | `reputation/report-detail`| Các bên liên quan | REPUTATION-001 | REPUTATION-001 §2.4 | Hoàn thành | Xem nội dung tố cáo, mức phạt điểm dự kiến, đồng hồ đếm ngược thời hạn khiếu nại tranh chấp (24 giờ đối với vi phạm NO-SHOW). |
| **REP-09** | Gửi đơn tranh chấp báo cáo NO-SHOW | M/S | `sheet/dispute-noshow` | Bên bị báo cáo | REPUTATION-001 | REPUTATION-001 §2.5 | Hoàn thành | **AC-P08**: Chỉ áp dụng cho loại vi phạm NO-SHOW và còn trong vòng 24h. Form nhập bằng chứng văn bản giải trình. Chặn với các vi phạm khác. |
| **REP-10** | Biến thể kết quả xử lý tranh chấp | V | `reputation/dispute-result`| Các bên liên quan | REPUTATION-001 | REPUTATION-001 §2.8 | Hoàn thành | Các trạng thái kết luận: "Chờ Admin phân xử", "Chấp thuận khiếu nại (Hủy báo cáo, hoàn điểm)", "Bác bỏ khiếu nại (Giữ nguyên mức phạt)". Quyết định cuối cùng. |

### 2.9. Quản Trị Hệ Thống (ADM-01..09)

| ID | Tên gọi & Chức năng | Loại | Route / View ID | Vai trò & Quyền | Nguồn thiết kế | API Contract / UC | Tiến độ | Ghi chú bố cục & Hành vi |
|---|---|---|---|---|---|---|---|---|
| **ADM-01** | Bảng điều khiển Quản trị (Admin) | M | `admin/dashboard` | Chỉ ADMIN | PROPOSAL | Quy ước Admin | Hoàn thành | Cổng vào quản trị hệ thống. Cấm tuyệt đối vai trò USER thông thường truy cập (trả về 403 Forbidden). Các nhóm module quản lý và số lượng tác vụ chờ duyệt. |
| **ADM-02** | Quản lý & Kiểm duyệt đội bóng | M | `admin/teams` | Admin | Stitch S13, USER-001 | USER-001 §2.16 | Hoàn thành | Danh sách toàn bộ đội bóng trong hệ thống, tìm kiếm, lọc trạng thái. Nút "Tạm ngưng hoạt động" (Suspend) hoặc "Mở khóa" kèm nhập lý do bắt buộc. |
| **ADM-03** | Lịch sử thành viên & Tài khoản xóa | M | `admin/team-member-history`| Admin | Stitch S14, USER-002 | USER-002 §2.5 | Hoàn thành | Dành riêng cho Admin: Xem đầy đủ lịch sử gia nhập/rời đội. Tài khoản đã xóa hiển thị Tên thật kèm cờ cảnh báo `[DELETED]` theo hợp đồng dữ liệu. |
| **ADM-04** | Hàng đợi giải quyết tranh chấp đội | M | `admin/team-disputes` | Admin | REPUTATION-001 | REPUTATION-001 §2.6 | Hoàn thành | Danh sách các vụ tranh chấp NO-SHOW giữa hai đội bóng chưa xử lý. Xem nội dung tố cáo của bên A và bằng chứng giải trình của bên B. |
| **ADM-05** | Hàng đợi tranh chấp người đá lẻ | M | `admin/mercenary-disputes`| Admin | MERCENARY-001 | MERCENARY-001 §2.25 | Hoàn thành | Hàng đợi khiếu nại tranh chấp giữa Captain đội bóng và cầu thủ đá lẻ tự do. |
| **ADM-06** | Biểu mẫu ra quyết định tranh chấp | S | `sheet/resolve-dispute` | Admin | REPUTATION-001 | REPUTATION-001 §2.8 | Hoàn thành | Modal xử lý tranh chấp: Nút "Chấp thuận khiếu nại" (Hủy report) hoặc "Bác bỏ khiếu nại" (Xác nhận vi phạm). Bắt buộc nhập Ghi chú quyết định từ 1 đến 500 ký tự. |
| **ADM-07** | Hàng đợi xử lý báo cáo sân bóng | M | `admin/venue-reports` | Admin | VENUE-001 | VENUE-001 §2.8 | Hoàn thành | Danh sách các phản ánh sân bóng sai địa chỉ, sai thông tin hoặc đóng cửa do người dùng gửi lên. |
| **ADM-08** | Bật / Tắt hoạt động của sân bóng | S | `sheet/toggle-venue-status`| Admin | VENUE-001 | VENUE-001 §2.10..2.11 | Hoàn thành | Bottom sheet thay đổi trạng thái sân bóng: Chuyển đổi giữa ACTIVE <-> INACTIVE kèm lý do ghi nhận hệ thống. |
| **ADM-09** | Quản lý danh mục Bộ môn thể thao | M/S | `admin/sports-management`| Admin | MATCH-001 | MATCH-001 §2.14..2.15 | Hoàn thành | Bật / Tắt trạng thái các bộ môn thể thao (Bóng đá mini 5-7-11, Futsal,...). Cảnh báo tác động đến việc tạo bài kèo mới. |

---

## 3. Bản Đồ Tái Sử Dụng Giao Diện & Thành Phần Dùng Chung

Để tối ưu hóa trải nghiệm người dùng và tiết kiệm tài nguyên khi phát triển Android Jetpack Compose, hệ thống áp dụng chiến lược tái sử dụng layout:

1. **Bộ biểu mẫu Đội bóng:**
   - `TEAM-03` (Tạo đội) và `TEAM-05` (Sửa đội) sử dụng chung Composable `TeamFormScreen`.
2. **Chi tiết Đội bóng:**
   - `TEAM-04` (Đội Active) và `TEAM-13` (Đội Disbanded) sử dụng chung `TeamDetailScreen`, điều khiển nút và banner dựa vào `team.status`.
3. **Chi tiết Kèo đấu:**
   - `MATCH-03` (Kèo A), `MATCH-04` (Kèo B) và `MATCH-13` (Kèo Confirmed / Hoàn tất) dùng chung Composable `MatchDetailScreen` với các khối Sub-Composable linh hoạt (`FixedSlotSection`, `MultiSlotSection`, `StatusBanner`).
4. **Bộ chọn sân dùng chung:**
   - `VEN-01` (Venue Picker) được tái sử dụng cho: Form tạo kèo (`MATCH-05`), Form tạo trận ngoài (`EXT-01`), và Chọn sân thương lượng (`PAIR-04`).
5. **Dialog Xác nhận có hậu quả:**
   - `PROF-04` (Xóa tài khoản), `TEAM-12` (Giải tán đội), `MATCH-12` (Hủy kèo), `EXT-04` (Hủy trận ngoài) sử dụng chung mẫu thiết kế `DestructiveActionSheet` với cảnh báo màu đỏ (#a73b21), liệt kê hậu quả và yêu cầu xác nhận chủ động.
6. **Thẻ Uy tín (Reputation Cards):**
   - `REP-04` (Uy tín đội) và `REP-05` (Uy tín cá nhân) dùng chung mẫu `ReputationScoreCard` hiển thị điểm số, sao, tỷ lệ hoàn thành trận và huy hiệu.
