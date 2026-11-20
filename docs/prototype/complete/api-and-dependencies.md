# ArenaFlow Prototype — Ánh Xạ API & Quản Lý Phụ Thuộc (API & Dependencies)

> Ngày lập: 18/11/2026 · Ngôn ngữ: Tiếng Việt · Phiên bản: 1.0-complete  
> Tài liệu thuộc bộ bàn giao UI Prototype ArenaFlow trong `docs/prototype/complete/`.  
> Đối chiếu toàn bộ với tài liệu đặc tả: `docs/ArenaFlow-spec/api/` và `docs/ArenaFlow-spec/business-spec/`.

---

## 1. Nguyên Tắc Phân Loại Đặc Tả & Hợp Đồng (Classification Principles)

Nhằm đảm bảo tính minh bạch tuyệt đối giữa thiết kế giao diện và khả năng hỗ trợ thực tế của backend, mọi chức năng trong prototype được gắn nhãn theo 4 nhóm chuẩn mực:

1. **[SPEC]:** Đã có hợp đồng API chính thức (Path, Method, Request/Response Schema, Mã lỗi HTTP) trong `docs/ArenaFlow-spec/api/`. Phải tuân thủ 100%, không tự ý thay đổi tên trường hoặc kiểu dữ liệu.
2. **[PROPOSAL]:** Lựa chọn thiết kế trải nghiệm người dùng (UX) bổ sung giúp luồng đi liền mạch (ví dụ: màn Onboarding, Menu tài khoản, Xem lại trước khi đăng). Ghi nhận thành đề xuất giao diện, không tự bịa thành yêu cầu backend bắt buộc.
3. **[DEPENDENCY]:** Khoảng trống hợp đồng API chưa có, hoặc có sự mâu thuẫn giữa tài liệu nghiệp vụ (Use Cases) và API Spec. Được mô phỏng có kiểm soát và ghi rõ ràng tác động kỹ thuật.
4. **[USER]:** Yêu cầu trực tiếp từ Chủ dự án để phục vụ bàn giao và kiểm thử nhanh.

---

## 2. Bảng Ánh Xạ 101 IDs Với Hợp Đồng API ArenaFlow-Spec

### 2.1. Xác Thực & Người Dùng (USER-001, USER-002)

| Screen ID | Tên Màn Hình / Trạng Thái | HTTP Method & Endpoint | Phân Loại | Mã Lỗi & Ghi Chú Hợp Đồng |
|---|---|---|---|---|
| **APP-01** | Splash & Check Session | `POST /api/v1/auth/refresh` | [SPEC] | 200 OK (Token mới), 401 Unauthorized (Phiên hết hạn). |
| **APP-02** | Onboarding Carousel | *Client-only local state* | [PROPOSAL] | [DEP-08] Lưu cờ `has_seen_onboarding` trong SharedPreferences/DataStore. |
| **AUTH-01**| Phương thức đăng nhập | `POST /api/v1/auth/oauth` | [SPEC] | Google/Facebook OAuth token exchange [DEP-05]. |
| **AUTH-02**| Nhập số điện thoại | `POST /api/v1/auth/otp/send` | [SPEC] | Body: `{ "phoneNumber": "+84..." }`. 400 Bad Request nếu sai định dạng. |
| **AUTH-03**| Nhập mã OTP | `POST /api/v1/auth/otp/verify` | [SPEC] | Body: `{ "phoneNumber": "...", "code": "..." }`. 400 OTP_EXPIRED, OTP_INVALID. |
| **AUTH-04**| Điều hướng sau xác thực | `POST /api/v1/auth/otp/verify` | [SPEC] | Trả về cờ `isNewUser: true/false`. AC-P01 điều hướng. |
| **AUTH-05**| Tài khoản đã xóa | `POST /api/v1/auth/otp/verify` | [SPEC] | Trả về HTTP 403 với mã lỗi `AUTH_003: Account deleted` [DEP-06]. |
| **AUTH-06**| Phiên làm việc hết hạn | *Interceptor handling* | [SPEC] | Bắt mã HTTP 401 từ mọi request có kèm header `Authorization: Bearer`. |
| **PROF-01**| Hồ sơ cá nhân của tôi | `GET /api/v1/users/me` | [SPEC] | Trả về `{ id, displayName, avatarUrl, bio, preferredPosition, dateOfBirth, gender, wardId }`. |
| **PROF-02**| Sửa / Hoàn thiện hồ sơ | `PUT /api/v1/users/me` | [SPEC] | Validate: `displayName` không chỉ khoảng trắng, `dateOfBirth` >= 13 tuổi. |
| **PROF-03**| Menu cài đặt tài khoản | *Client navigation* | [PROPOSAL] | [DEP-08] Điểm truy cập UX tập trung cho Profile. |
| **PROF-04**| Xác nhận xóa tài khoản | `DELETE /api/v1/users/me` | [SPEC] | Xóa vĩnh viễn tài khoản người dùng cá nhân (USER-002 §2.1). |
| **PROF-05**| Chặn xóa tài khoản Captain | `DELETE /api/v1/users/me` | [SPEC] | **AC-P02**: HTTP 409 `USER_004: Captain of active teams`. Trả danh sách `teams: [{ id, name }]`. |
| **PROF-06**| Xác nhận đăng xuất | `POST /api/v1/auth/logout` | [PROPOSAL] | [DEP-10] Thu hồi refresh token phía client/mock. |

### 2.2. Đội Bóng & Thành Viên (USER-001, USER-002)

| Screen ID | Tên Màn Hình / Trạng Thái | HTTP Method & Endpoint | Phân Loại | Mã Lỗi & Ghi Chú Hợp Đồng |
|---|---|---|---|---|
| **TEAM-01**| Đội của tôi | `GET /api/v1/teams?myTeams=true` | [DEPENDENCY] | [DEP-01] Hợp đồng hiện tại `GET /teams` trả mọi đội; cần query filter `myTeams`. |
| **TEAM-02**| Danh sách đội hệ thống | `GET /api/v1/teams` | [SPEC] | Phân trang: `page`, `limit`. Lọc theo `wardId`. |
| **TEAM-03**| Tạo đội bóng mới | `POST /api/v1/teams` | [SPEC] | Body: `{ name, logoUrl, wardId, skillLevels: [1..3] }`. Validate trùng tên. |
| **TEAM-04**| Chi tiết đội ACTIVE | `GET /api/v1/teams/{teamId}` | [SPEC] | Trả về thông tin đội, vai trò của người gọi (`isCaptain`, `isMember`). |
| **TEAM-05**| Chỉnh sửa thông tin đội | `PUT /api/v1/teams/{teamId}` | [SPEC] | Chỉ Captain (403 Forbidden nếu là Member). |
| **TEAM-06**| Danh sách thành viên đội | `GET /api/v1/teams/{teamId}/members`| [SPEC] | Tài khoản đã xóa hiển thị `displayName: "Người dùng ArenaFlow"`. |
| **TEAM-07**| Mời thành viên mới | `POST /api/v1/teams/{teamId}/invitations`| [DEPENDENCY]| [DEP-02] Body: `{ "userId": "..." }`. Chưa có API tìm user theo username. |
| **TEAM-08**| Phản hồi lời mời | `POST /api/v1/teams/invitations/{id}/respond`| [DEPENDENCY]| [DEP-04] Body: `{ "action": "ACCEPT" / "REJECT" }`. Lấy từ payload notification. |
| **TEAM-09**| Chuyển quyền đội trưởng | `POST /api/v1/teams/{teamId}/transfer-captain`| [SPEC] | Body: `{ "newCaptainId": "..." }`. Chuyển tức thì, không cần bên nhận duyệt. |
| **TEAM-10**| Lịch sử các đời đội trưởng | `GET /api/v1/teams/{teamId}/captain-history`| [SPEC] | Trả về danh sách `{ previousCaptain, newCaptain, transferredAt }`. |
| **TEAM-11**| Thành viên rời đội | `POST /api/v1/teams/{teamId}/leave`| [SPEC] | Chặn nếu là Captain (HTTP 409 `TEAM_005`). |
| **TEAM-12**| Giải tán đội bóng | `POST /api/v1/teams/{teamId}/disband`| [SPEC] | Hủy vĩnh viễn đội bóng và toàn bộ kèo liên quan (USER-002 §2.3). |
| **TEAM-13**| Chi tiết đội DISBANDED | `GET /api/v1/teams/{teamId}` | [SPEC] | Trả về `status: "DISBANDED"`. Khóa toàn bộ hành động. |
| **TEAM-14**| Chọn đội bóng thao tác | *Client Context Picker* | [PROPOSAL] | Hỗ trợ Captain nhiều đội chọn ngữ cảnh đội thực hiện hành động. |

### 2.3. Khám Phá & Vòng Đời Kèo (MATCH-01, MATCH-02)

| Screen ID | Tên Màn Hình / Trạng Thái | HTTP Method & Endpoint | Phân Loại | Mã Lỗi & Ghi Chú Hợp Đồng |
|---|---|---|---|---|
| **MATCH-01**| Khám phá & Tìm kiếm kèo | `GET /api/v1/matches` | [SPEC] | Query: `status=OPEN`, `type=A/B`, `date`, `wardId`, `page`, `limit`. |
| **MATCH-02**| Bộ lọc tìm kiếm kèo | `GET /api/v1/matches` | [SPEC] | Ánh xạ với các query parameters của endpoint tìm kiếm. |
| **MATCH-03**| Chi tiết bài đăng kèo A | `GET /api/v1/matches/{matchId}` | [SPEC] | Chứa 1 sân + 1 khung giờ, `costSharing`, danh sách yêu cầu gửi đến nếu là Host. |
| **MATCH-04**| Chi tiết bài đăng kèo B | `GET /api/v1/matches/{matchId}` | [SPEC] | Chứa mảng 1-5 sân (`venueIds: []`) và mảng 1-5 giờ (`timeSlots: []`). |
| **MATCH-05**| Tạo bài đăng kèo A hoặc B | `POST /api/v1/matches` | [SPEC] | Giờ bắt đầu >= hiện tại + 1h. Thời lượng <= 12h. Validate 1-5 options cho kèo B. |
| **MATCH-06**| Xem lại trước khi đăng | *Client Preview Modal* | [PROPOSAL] | [DEP-08] Giảm tỷ lệ sai sót form phía người dùng. |
| **MATCH-07**| Lịch thi đấu & Kèo của đội | `GET /api/v1/teams/{teamId}/matches`| [SPEC] | Query: `scope=UPCOMING/PAST/CANCELLED`. |
| **MATCH-08**| Danh sách yêu cầu đã gửi | `GET /api/v1/matches/requests/sent` | [SPEC] | Danh sách request giao lưu do đội mình gửi đi. |
| **MATCH-09**| Yêu cầu gửi đến kèo A | `GET /api/v1/matches/{id}/requests` | [SPEC] | Danh sách request của các đối thủ gửi vào bài kèo A của mình. |
| **MATCH-10**| Duyệt yêu cầu đối thủ | `POST /api/v1/matches/requests/{id}/accept`| [SPEC] | **AC-P03**: Request -> ACCEPTED, Match -> CONFIRMED, các request khác SUPERSEDED. |
| **MATCH-11**| Rút yêu cầu bắt kèo | `POST /api/v1/matches/requests/{id}/withdraw`| [SPEC]| Rút PENDING không phạt; Rút ACCEPTED tương đương hủy trận có phạt. |
| **MATCH-12**| Hủy bài đăng kèo đấu | `POST /api/v1/matches/{id}/cancel` | [SPEC] | Body: `{ "reason": "..." }`. Chặn nếu có request PENDING hoặc đã qua giờ đá. |
| **MATCH-13**| Biến thể trạng thái kèo | `GET /api/v1/matches/{id}` | [SPEC] | Phản ánh `status: CONFIRMED, COMPLETED, CANCELLED, EXPIRED`. |
| **MATCH-14**| Chỉnh sửa chi phí kèo A | `PUT /api/v1/matches/{id}/cost` | [SPEC] | Chỉ cho phép khi kèo còn OPEN và chưa có request nào được duyệt. |

### 2.4. Thương Lượng Kèo B & Chốt Gói (MATCH-02)

| Screen ID | Tên Màn Hình / Trạng Thái | HTTP Method & Endpoint | Phân Loại | Mã Lỗi & Ghi Chú Hợp Đồng |
|---|---|---|---|---|
| **PAIR-01**| DS thương lượng của Host | `GET /api/v1/matches/{id}/pairings` | [SPEC] | Danh sách các đội đã nhận bài B và đang đàm phán riêng. |
| **PAIR-02**| DS thương lượng của đội tôi | `GET /api/v1/pairings/my-teams` | [SPEC] | Danh sách các phòng đàm phán mà đội tôi đang tham gia. |
| **PAIR-03**| Chi tiết phòng thương lượng | `GET /api/v1/pairings/{pairingId}` | [SPEC] | Trả về `{ id, matchId, guestTeam, package: { venueId, timeSlot, cost, revision, confirmedByHost, confirmedByGuest } }`. |
| **PAIR-04**| Chọn phương án sân & giờ | `PUT /api/v1/pairings/{id}/slot` | [SPEC] | Body: `{ venueId, timeSlot }`. Tự động xóa xác nhận 2 bên, tăng revision. |
| **PAIR-05**| Tùy chỉnh chi phí gói | `PUT /api/v1/pairings/{id}/cost` | [SPEC] | Body: `{ pitchFee, splitRatio, depositAmount, depositHolder }`. Tăng revision. |
| **PAIR-06**| Xác nhận chốt gói | `POST /api/v1/pairings/{id}/confirm`| [SPEC] | **AC-P04**: Body: `{ "revision": 2 }`. Bên thứ 2 bấm -> Ghép thành công. |
| **PAIR-07**| Xung đột phiên bản gói | `POST /api/v1/pairings/{id}/confirm`| [SPEC] | Trả về HTTP 409 `PAIRING_006: Revision mismatch` nếu đối phương vừa sửa gói. |
| **PAIR-08**| Trạng thái kết thúc phòng | `GET /api/v1/pairings/{id}` | [SPEC] | Trả về `status: CONFIRMED` hoặc `SUPERSEDED`. Khóa hoàn toàn nút sửa. |

### 2.5. Tuyển Người Đá Lẻ & Trận Ngoài (MERCENARY-001)

| Screen ID | Tên Màn Hình / Trạng Thái | HTTP Method & Endpoint | Phân Loại | Mã Lỗi & Ghi Chú Hợp Đồng |
|---|---|---|---|---|
| **MERC-01**| Khám phá & Tìm tin đá lẻ | `GET /api/v1/mercenary/posts` | [SPEC] | Query: `position`, `lat`, `lng`, `radiusKm` (1-20, default 3), `wardId`. |
| **MERC-02**| Chi tiết tin tuyển người | `GET /api/v1/mercenary/posts/{id}` | [SPEC] | **AC-P05**: Khi người xem chưa được duyệt -> Response **ẩn tuyệt đối venue/address**. |
| **MERC-03**| Nộp đơn ứng tuyển đá lẻ | `POST /api/v1/mercenary/posts/{id}/apply`| [SPEC] | 400 TIME_CONFLICT (trùng giờ), 400 ALREADY_APPLIED, 400 OWN_TEAM. |
| **MERC-04**| Đơn ứng tuyển của tôi | `GET /api/v1/mercenary/applications/me`| [SPEC] | Trả về danh sách đơn kèm trạng thái PENDING/APPROVED/REJECTED/WITHDRAWN. |
| **MERC-05**| Chi tiết đơn ứng tuyển | `GET /api/v1/mercenary/applications/{id}`| [SPEC] | Khi `status == APPROVED` -> Response trả về đầy đủ `venueName, address, lat, lng`. |
| **MERC-06**| Rút đơn ứng tuyển đá lẻ | `POST /api/v1/mercenary/applications/{id}/withdraw`| [SPEC]| **AC-P06**: Rút PENDING trừ 0 điểm; Rút APPROVED trừ 10 điểm uy tín cá nhân. |
| **MERC-07**| Chọn trận đấu để tuyển | *Client Selector Screen* | [PROPOSAL] | Chọn giữa Kèo hệ thống đã CONFIRMED hoặc Khai báo trận ngoài (EXT-01). |
| **MERC-08**| Đăng tin tuyển người | `POST /api/v1/mercenary/posts` | [SPEC] | Đăng 1-4 vị trí, mỗi vị trí số lượng 1-10 người [DEP-07]. |
| **MERC-09**| Quản lý tin tuyển của đội | `GET /api/v1/teams/{id}/mercenary-posts`| [SPEC] | Quản lý số lượng slot đã duyệt / cần tuyển. |
| **MERC-10**| Danh sách ứng viên nộp đơn | `GET /api/v1/mercenary/posts/{id}/applicants`| [SPEC]| Danh sách ứng viên kèm điểm uy tín cá nhân. |
| **MERC-11**| Duyệt hoặc từ chối ứng viên | `POST /api/v1/mercenary/applications/{id}/review`| [SPEC]| Action: `APPROVE` / `REJECT`. Chặn duyệt thêm khi đã đủ slot (FILLED). |
| **MERC-12**| Điều chỉnh số lượng & Đóng | `PATCH /api/v1/mercenary/posts/{id}` | [SPEC] | Không được giảm `quantity` xuống dưới số lượng ứng viên đã `APPROVED`. |
| **EXT-01** | Khai báo trận đấu ngoài | `POST /api/v1/external-matches` | [SPEC] | Body: `{ teamId, venueId, startTime, endTime, opponentName }`. |
| **EXT-02** | Chi tiết trận ngoài | `GET /api/v1/external-matches/{id}` | [SPEC] | Quản lý trận đấu tự do và các tin tuyển đá lẻ đi kèm. |
| **EXT-03** | Chỉnh sửa trận ngoài | `PUT /api/v1/external-matches/{id}` | [SPEC] | Khóa sửa sân và giờ nếu đã có ứng viên đá lẻ được APPROVED. |
| **EXT-04** | Hủy trận đấu ngoài | `POST /api/v1/external-matches/{id}/cancel`| [SPEC] | Tự động hủy toàn bộ tin tuyển và đơn ứng tuyển đi kèm. |
| **EXT-05** | Thêm vị trí tuyển trận ngoài | `POST /api/v1/mercenary/posts` | [SPEC] | Tạo thêm tin tuyển gắn với `externalMatchId`. |

### 2.6. Địa Điểm, Sân Bóng & Thư Viện Ảnh (VENUE-001)

| Screen ID | Tên Màn Hình / Trạng Thái | HTTP Method & Endpoint | Phân Loại | Mã Lỗi & Ghi Chú Hợp Đồng |
|---|---|---|---|---|
| **VEN-01** | Tìm kiếm & Chọn sân bóng | `GET /api/v1/venues` | [SPEC] | Phân trang: `search`, `wardId`, `page`, `limit`. Trả kết quả về form. |
| **VEN-02** | Chi tiết thông tin sân | `GET /api/v1/venues/{id}` | [SPEC] | Tên sân, địa chỉ, số liên hệ, tiện ích, danh sách ảnh. |
| **VEN-03** | Thêm sân bóng mới | `POST /api/v1/venues` | [SPEC] | Có hiệu lực tức thì ngay sau khi tạo thành công. |
| **VEN-04** | Chỉnh sửa thông tin sân | `PUT /api/v1/venues/{id}` | [SPEC] | Quyền: Người tạo sân hoặc ADMIN (403 Forbidden nếu không đủ quyền). |
| **VEN-05** | Quản lý thư viện ảnh sân | `POST /api/v1/venues/{id}/photos` | [SPEC] | Tối đa 5 ảnh; đúng 1 ảnh chính (`isPrimary: true`). [DEP-10] mock upload. |
| **VEN-06** | Báo cáo sân sai lệch | `POST /api/v1/venues/{id}/reports` | [SPEC] | Báo cáo sai địa chỉ, số điện thoại, sân đóng cửa. |
| **VEN-07** | Trạng thái sân INACTIVE | `GET /api/v1/venues/{id}` | [SPEC] | Trả về `status: "INACTIVE"`. Cấm chọn sân vào kèo/gói mới. |

### 2.7. Tin Nhắn & Thông Báo (MATCH-01, MERCENARY-001, NOTIFICATION-001)

| Screen ID | Tên Màn Hình / Trạng Thái | HTTP Method & Endpoint | Phân Loại | Mã Lỗi & Ghi Chú Hợp Đồng |
|---|---|---|---|---|
| **CHAT-01**| Hộp thư danh sách hội thoại | `GET /api/v1/chats` | [SPEC] | Danh sách phòng chat kèo và chat ứng viên, số lượng tin chưa đọc. |
| **CHAT-02**| Phòng chat giao lưu 2 đội | `GET /api/v1/chats/{roomId}/messages`| [SPEC] | Tin nhắn trong phòng chat kèo. Tác giả thật, avatar, thời gian. |
| **CHAT-03**| Phòng chat Captain & Đá lẻ | `GET /api/v1/chats/mercenary/{id}` | [SPEC] | Chat theo ngữ cảnh đơn ứng tuyển. |
| **CHAT-04**| Trạng thái phòng chat | `GET /api/v1/chats/{roomId}` | [SPEC] | Trả về `canSend: false` khi kèo kết thúc hoặc quá hạn lưu trữ 90 ngày. |
| **CHAT-05**| Báo cáo vi phạm trong chat | `POST /api/v1/chats/{id}/report` | [SPEC] | Gửi báo cáo quấy rối/xúc phạm phát sinh trong hội thoại. |
| **NOTIF-01**| Trung tâm thông báo | `GET /api/v1/notifications` | [SPEC] | Tabs: `all` / `unread`. Thời hạn lưu trữ tối đa 30 ngày. |
| **NOTIF-02**| Điều hướng thông báo an toàn | `GET /api/v1/notifications/{id}/target`| [SPEC] | **AC-P09**: Khi đối tượng bị xóa/mất quyền -> Trả thông báo lỗi an toàn. |
| **NOTIF-03**| Catalog 35 loại thông báo | *System Specification* | [SPEC] | Bộ mẫu 35 loại notification và ánh xạ màn đích. |

### 2.8. Sau Trận, Uy Tín & Tranh Chấp (REPUTATION-001, MERCENARY-001)

| Screen ID | Tên Màn Hình / Trạng Thái | HTTP Method & Endpoint | Phân Loại | Mã Lỗi & Ghi Chú Hợp Đồng |
|---|---|---|---|---|
| **REP-01** | Đánh giá đối phương sau trận | `POST /api/v1/matches/{id}/ratings` | [SPEC] | Chấm 1-5 sao, nhận xét, tỷ số hoặc cờ `isNoShow: true`. |
| **REP-02** | Xem đánh giá trận đấu | `GET /api/v1/matches/{id}/ratings` | [SPEC] | **AC-P07**: Blind Rating. Ẩn trạng thái đối phương khi mình chưa gửi. |
| **REP-03** | Đánh giá hai chiều với đá lẻ | `POST /api/v1/mercenary/ratings` | [SPEC] | Captain chấm điểm mercenary; Mercenary chấm điểm đội bóng. |
| **REP-04** | Thẻ uy tín đội bóng | `GET /api/v1/teams/{id}/reputation` | [SPEC] | Score, Completion Rate (chỉ hiện khi >= 5 trận), Badge uy tín. |
| **REP-05** | Thẻ uy tín cá nhân người đá lẻ| `GET /api/v1/mercenary/users/{id}/reputation`| [SPEC]| Điểm cá nhân (gốc 100), số trận hoàn thành, số lần bị trừ điểm. |
| **REP-06** | Gửi báo cáo vi phạm | `POST /api/v1/reports` | [SPEC] | Type: `NOSHOW`, `VIOLENCE`, `CHEATING`, `BAD_BEHAVIOR`. Lý do bắt buộc. |
| **REP-07** | Danh sách báo cáo vi phạm | `GET /api/v1/reports` | [SPEC] | Query: `scope=SENT/RECEIVED`. |
| **REP-08** | Chi tiết báo cáo vi phạm | `GET /api/v1/reports/{id}` | [SPEC] | Nội dung vi phạm, mức trừ điểm, đồng hồ đếm ngược 24h khiếu nại. |
| **REP-09** | Gửi khiếu nại tranh chấp | `POST /api/v1/reports/{id}/dispute` | [SPEC] | **AC-P08**: Chỉ cho phép với loại `NOSHOW` và gửi trong vòng 24h. |
| **REP-10** | Kết quả xử lý tranh chấp | `GET /api/v1/reports/{id}/dispute` | [SPEC] | Trạng thái: `IN_REVIEW`, `ACCEPTED`, `REJECTED`. Quyết định cuối cùng. |

### 2.9. Quản Trị Hệ Thống (ADMIN)

| Screen ID | Tên Màn Hình / Trạng Thái | HTTP Method & Endpoint | Phân Loại | Mã Lỗi & Ghi Chú Hợp Đồng |
|---|---|---|---|---|
| **ADM-01** | Bảng điều khiển Quản trị | *Admin Layout Container* | [PROPOSAL] | Phải có cờ `role == "ADMIN"`. Trả về 403 Forbidden nếu là user thường. |
| **ADM-02** | Quản lý & Kiểm duyệt đội | `GET /api/v1/admin/teams`, `PATCH ...`| [SPEC] | Tạm ngưng (`SUSPENDED`) hoặc Mở lại hoạt động kèm lý do. |
| **ADM-03** | Lịch sử thành viên Admin | `GET /api/v1/admin/teams/{id}/members`| [SPEC] | Tài khoản đã xóa hiển thị tên thật kèm cờ `[DELETED]`. |
| **ADM-04** | Hàng đợi tranh chấp đội | `GET /api/v1/admin/disputes/teams` | [SPEC] | Danh sách tranh chấp NO-SHOW giữa các đội bóng. |
| **ADM-05** | Hàng đợi tranh chấp đá lẻ | `GET /api/v1/admin/disputes/mercenary`| [SPEC] | Danh sách tranh chấp giữa Captain và Mercenary. |
| **ADM-06** | Ra quyết định tranh chấp | `POST /api/v1/admin/disputes/{id}/resolve`| [SPEC]| Action: `UPHOLD` / `DISMISS`. Ghi chú bắt buộc từ 1 đến 500 ký tự. |
| **ADM-07** | Hàng đợi báo cáo sân bóng | `GET /api/v1/admin/venue-reports` | [SPEC] | Danh sách phản ánh sai lệch địa điểm do người dùng gửi lên. |
| **ADM-08** | Bật / Tắt hoạt động sân | `PATCH /api/v1/admin/venues/{id}/status`| [SPEC]| Đổi trạng thái Sân: ACTIVE <-> INACTIVE kèm ghi chú lý do. |
| **ADM-09** | Quản lý Bộ môn thể thao | `PATCH /api/v1/admin/sports/{id}` | [SPEC] | Bật / Tắt trạng thái hoạt động của từng môn thể thao trong hệ thống. |

---

## 3. Quản Lý Chi Tiết 11 Phụ Thuộc Kỹ Thuật (DEP-01..DEP-11)

Bảng phân tích chuyên sâu từng khoảng trống đặc tả và giải pháp xử lý trong prototype:

| Mã DEP | Hiện trạng kỹ thuật trong đặc tả | Cách thức mô phỏng trong Prototype | Tác động khi triển khai Android Jetpack Compose |
|---|---|---|---|
| **DEP-01** | Spec `USER-001` chưa có endpoint lấy "đội của tôi"; `GET /teams` trả về toàn bộ đội hệ thống. | Central Store quản lý bảng `memberships`, lọc theo `userId` hiện tại để hiển thị `TEAM-01` và `TEAM-14`. | Backend cần bổ sung query parameter `?myTeams=true` hoặc endpoint chuyên biệt `GET /api/v1/users/me/teams` trước khi kết nối API thật. |
| **DEP-02** | Lời mời vào đội (`TEAM-07`) chỉ nhận `userId`; chưa có endpoint tìm kiếm người dùng theo username/SĐT. | Cung cấp danh sách người dùng mẫu từ Fixture để Captain chọn người mời; không tự bịa endpoint search username. | Cần bổ sung endpoint `GET /api/v1/users/search?q=...` trên backend; UI Android cần chuẩn bị trước Search Bar component. |
| **DEP-03** | Chưa có danh mục Tỉnh/Thành và Quận/Huyện/Phường chuẩn từ máy chủ. | Sử dụng danh mục mẫu tĩnh gồm các Quận/Huyện tại Đà Nẵng và TP.HCM với ID phường (`wardId`) số nguyên. | Khi có dịch vụ hành chính công hoặc bảng master data, chỉ cần thay đổi nguồn cấp dữ liệu cho Dropdown Selector. |
| **DEP-04** | Lời mời gia nhập đội chỉ được nhận qua thông báo in-app, không có thời hạn hết hạn riêng. | Người dùng mở `TEAM-08` trực tiếp từ danh sách thông báo `NOTIF-01`, sử dụng payload đính kèm trong thông báo. | Không tự tạo tab "Hộp thư lời mời" riêng biệt trong app; tuân thủ đúng luồng thông báo in-app. |
| **DEP-05** | Hợp đồng xác thực OAuth và OTP hiện là mock trên spec, chưa có nhà cung cấp SMS/OAuth thật. | Mô phỏng trọn vẹn chu kỳ gửi mã, đếm ngược 60 giây, hỗ trợ mã OTP kiểm thử mặc định `123456`, xử lý mã sai và mã hết hạn. | Khi tích hợp thật, chỉ cần gắn Firebase Auth hoặc SMS Gateway mà không phải sửa đổi cấu trúc màn hình Compose. |
| **DEP-06** | Mục Tổng quan nhắc đến việc có thể khôi phục tài khoản, nhưng API `AUTH_003` hiện chặn vĩnh viễn. | Màn hình `AUTH-05` hiển thị thông báo tài khoản đã đóng vĩnh viễn, **tuyệt đối không hiển thị nút Khôi phục tài khoản giả**. | Đảm bảo tuân thủ nghiêm ngặt hợp đồng bảo mật; khôi phục tài khoản cần quy trình nghiệp vụ cấp bách riêng. |
| **DEP-07** | Mâu thuẫn: `UC-ME01` nói không cho tạo tin sau giờ bắt đầu; `MERCENARY API 2.1` cho phép tạo đến trước khi trận kết thúc. | Prototype bám sát theo `API 2.1`: Cho phép tạo tin tuyển cho đến trước giờ kết thúc trận, nhưng **chặn duyệt/ứng tuyển từ giờ bắt đầu**. | Đã ghi nhận mâu thuẫn để đội ngũ Product Owner chốt thống nhất spec trong phiên bản sau. |
| **DEP-08** | Các màn: Onboarding (`APP-02`), Menu tài khoản (`PROF-03`), Xem lại trước khi đăng (`MATCH-06`) là UX đề xuất. | Xây dựng giao diện tinh gọn, tái sử dụng component chuẩn của hệ thống, gắn nhãn `[PROPOSAL]` trong tài liệu bàn giao. | Lập trình viên Android có thể triển khai ngay mà không đòi hỏi thêm bất kỳ endpoint nào từ backend. |
| **DEP-09** | Chưa có nội dung văn bản pháp lý (Điều khoản dịch vụ, Chính sách quyền riêng tư) được phê duyệt. | Hiển thị hộp thoại placeholder có nhãn thông báo rõ ràng *"Nội dung văn bản đang được hoàn thiện theo quy định"*. | Tránh việc tự tạo các điều khoản giả mạo có thể gây rủi ro pháp lý cho dự án. |
| **DEP-10** | Cơ chế tải ảnh (Avatar, Sân bóng), Bản đồ Google Maps thật và Đăng xuất server chưa được chốt hạ tầng. | Mô phỏng lưu trữ ảnh bằng bộ sưu tập ảnh thể thao chuẩn, mô phỏng vị trí GPS bằng slider tọa độ, xóa session cục bộ khi đăng xuất. | Thiết kế Compose sẵn sàng các Composable ImagePicker và MapView để cắm SDK thật khi có hạ tầng. |
| **DEP-11** | Bảo vệ quyền riêng tư sân bóng đá lẻ (`AC-P05`) đối lập với việc danh mục sân bóng là công khai (`VEN-01`). | Kiểm soát chặt chẽ ở cấp độ dữ liệu: API tin tuyển đá lẻ chỉ trả về `district/ward`, không bao giờ nhúng `venueId` hay `address` vào payload public rồi chỉ dùng CSS để ẩn. | Giữ an toàn tuyệt đối cho các đội tuyển; tránh lộ địa điểm thi đấu cho người lạ khi chưa được duyệt tham gia. |
