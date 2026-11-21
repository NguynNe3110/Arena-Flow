# ArenaFlow — Kế hoạch hoàn thiện UI Android theo prototype index.html

Ngày lập: 21/11/2026. Trạng thái: **PLAN — chưa triển khai theo kế hoạch mới**.
Dành cho AI/Android developer tiếp nhận công việc. Ngôn ngữ sản phẩm: tiếng Việt.
Baseline khảo sát: `feat/modularize-features`, commit `c0997ba`.
Bảng truy vết bắt buộc: [screen-coverage.md](screen-coverage.md).

## 1. Mục tiêu và quyết định đã chốt

1. Sửa các màn đã có để khớp **[docs/prototype/index.html](../prototype/index.html)**.
2. Hoàn thiện luồng bắt đầu, splash, onboarding, auth, hồ sơ và toàn bộ các nhóm màn phụ còn thiếu, thay vì chỉ dừng ở các tab chính.
3. Dùng cùng ngôn ngữ giao diện của index.html cho các màn chưa có thiết kế trong file đó. Không lấy layout hoặc theme Stitch thay thế.
4. Bao phủ **101 ID chức năng/màn/sheet/biến thể** trong inventory nghiệp vụ; đây không phải 101 màn độc lập hoặc 101 file Compose.
5. Giao việc thành **32 đợt, mỗi đợt 2 màn/destination**, với sheet, dialog và biến thể đi cùng màn chủ. Component và form được tái sử dụng.
6. Branch **nối tiếp nhau** theo lựa chọn của chủ dự án. Xong mỗi cặp: kiểm tra, commit, push branch; ghi rõ base và commit thực tế.
7. Giữ kiến trúc module, Koin, ViewModel/UDF, repository/use case và các test kiến trúc đang có.

Phạm vi phiên viết tài liệu: lập kế hoạch và làm rõ nguồn tham chiếu; không coi đây là yêu cầu bắt đầu sửa code Android ngay trong phiên này.

## 2. Nguồn đúng và thứ tự giải quyết mâu thuẫn

| Nội dung cần quyết định | Nguồn |
| --- | --- |
| Hình thức, bố cục, copy và nhịp giao diện của 8 view gốc | [prototype/index.html](../prototype/index.html) — lựa chọn mới của chủ dự án |
| Bổ sung màn ngoài 8 view | Kế thừa tokens/components từ index.html, dựa vào công việc và quyền trong spec |
| Field, endpoint, lỗi, enum, trạng thái và quyền | API chuyên biệt hiện hành dưới [ArenaFlow-spec/api](../ArenaFlow-spec/api/) và quyết định sửa đổi mới nhất |
| Danh mục chức năng cần bao phủ | Mục 6 của [PROTOTYPE_HANDOFF.md](../prototype/PROTOTYPE_HANDOFF.md), đối chiếu API |
| Các bước còn thiếu/fixtures để tham khảo | Tài liệu dưới prototype/complete, sau khi kiểm lại với API |
| Lịch sử các đợt UI cũ | [progress.md](progress.md); trạng thái “Completed” cũ không chứng minh đúng giao diện index.html |

**Không dùng stitch-preview.html, export Stitch, complete/css hoặc complete/design-system.md làm chuẩn thị giác cho đợt mới.**
Không sửa bản HTML nguồn để làm nó giống app hiện có. Không khôi phục file người dùng đã xóa chỉ để chạy một bản prototype khác.

Các file handoff cũ có những chỗ sai endpoint và mã lỗi. Không sao chép bảng API đó thẳng vào Retrofit. Ví dụ đã kiểm tra:

| Vấn đề | Theo contract gốc hiện hành |
| --- | --- |
| Gửi/xác minh OTP | `POST /auth/send-otp`, `POST /auth/verify-otp`, body `phone` / `otp` |
| Hồ sơ | `GET /profile`, `PATCH /profile`, `DELETE /profile` |
| Xóa tài khoản còn đội trưởng | `422 USER_003`, không mặc định là `409 USER_004` |
| Lịch sử/gửi chat | `/conversations/{conversationId}/messages`; inbox `GET /profile/conversations` |
| Thông báo | `/profile/notifications`, `/unread-count`, thao tác đọc theo NOTIFICATION-001 |
| Gói kèo B | `PUT /pairings/{pairingId}/proposal`, `.../cost-agreement`, confirm theo revision |
| Đá lẻ | Họ endpoint `/mercenary-recruitments`, `/mercenary-applications`; không dùng đường dẫn `/mercenary/posts` do handoff mô tả |
| Danh mục địa giới / đội của tôi / tìm người mời | Kiểm tra khả năng thực tế trước khi tích hợp; không tự thêm query hoặc endpoint |

Các đường dẫn trên nằm dưới `/api/v1`. Cần đọc lại phần request/response của từng endpoint ở đợt thực hiện, không chỉ dựa vào bảng tóm tắt này.

## 3. Hiện trạng và các lệch cần sửa

Khảo sát từ mã nguồn tại baseline; chưa phải biên bản nghiệm thu bằng ảnh mới của mọi màn.

| Khu vực | Hiện trạng | Việc phải làm |
| --- | --- | --- |
| MatchList / MatchCard | Có UI và bộ lọc nhưng theo complete/Stitch; thiếu hero, ngày và anatomy card của index | Sửa ở đợt 01 |
| MatchDetail | Có dữ liệu chi tiết nhưng dùng VS và nhiều section card | Đưa về tiêu đề đội, các dòng ngày/giờ/sân/chi phí theo index; giữ biến thể A/B và contract |
| MatchCreate / MatchReview | Đã có, hình thức nhiều card/radio/chips | Form gọn như create; review kế thừa cùng hệ |
| PairingRoom | Có UI/repository mẫu; mã phòng, banner và chat nhúng khác index | Sửa layout, bổ sung list và đường vào thật từ kèo B |
| MercenaryList | Có UI/ứng tuyển mẫu; GPS banner/slider/chips khác index | Sửa theo people, hoàn thiện chi tiết và hai phía đơn |
| TeamDetail / TeamCreate | Có UI mẫu, thống kê và các tab; chưa đủ quản trị vòng đời đội | Sửa home đội và bổ sung các màn quản lý |
| Tin nhắn | Tab đang là placeholder; chat nhúng không thay thế inbox/phòng chat dùng chung | Đợt 09 |
| Thông báo | Thiếu destination và chuông dùng chung được nối tới màn | Đợt 10 |
| Splash / auth / profile | Activity có system splash; chưa có launch gate và auth graph hoàn chỉnh trong navigation | Đợt 02–05 |
| Venue / trận ngoài / sau trận / admin | Chưa có đủ destination và hành trình tích hợp | Đợt 16–32 |

Code hiện nằm trong `:feature:match`, `:feature:pairing`, `:feature:mercenary`, `:feature:team` và `:feature:base`.
Các module album/favourite/settings cũ không được coi là màn sản phẩm đã hoàn thiện chỉ vì còn tồn tại.

## 4. Chuẩn giao diện phải khóa trước khi mở rộng

| Token / thành phần | Chuẩn index.html |
| --- | --- |
| Primary / onPrimary | `#146B46` / trắng |
| Chữ chính / phụ | `#172E27` / `#52675D` |
| Nền app / card | `#FAFBF7` / `#FFFFFF` |
| Viền / nút phụ | `#DCE3D8` / `#E7EEE6` |
| Chip active | nền `#172E27`, chữ trắng |
| Badge xanh | nền `#DFEFDF`, chữ `#155B39` |
| Badge amber | nền `#FFF0CE`, chữ `#6B4A08` |
| Notice / nav active | `#F1F0DF` / `#E5EEE1` |
| Bo góc | Button 12; card 14; input 10; badge 7; avatar tròn |
| Typography gốc | system-ui; brand 23px; h2 30px; h3 19px; small 13px |
| Khoảng cách gốc | page 24px; card 18px; nhóm nội dung 14px; chip gap 8px |
| Touch target | tối thiểu 48dp; giữ scale chữ, semantics và insets Android |

Thông số CSS là điểm xuất phát khi map sang dp/sp ở cùng chiều rộng logic. Không đổi px sang dp bằng mật độ pixel vật lý của ảnh. Đối chiếu line-height, font metrics và xuống dòng bằng render thực tế. Mặc định khảo sát font hệ thống Android tương ứng với system-ui; không mặc định Be Vietnam Pro là yêu cầu của nguồn này.

Các thành phần cần dùng chung: BrandHeader, AppPage, PageHeading, Primary/SecondaryButton, LabeledField, ChoiceField, FilterChips, MatchSummaryCard, KeyValueRow, InlineNotice, StatusBadge, TeamAvatar, BottomNavigation, ConfirmationDialog và UnavailableContent.

- Header giữ ArenaFlow. và chuông; entry hồ sơ/tài khoản qua avatar/menu phụ rõ ràng [PROPOSAL], không thêm tab thứ năm chỉ để chứa hồ sơ.
- Tìm kèo: khu vực + nhãn dữ liệu mẫu nhỏ → “Tìm đối thủ. Ra sân thôi.” → mô tả → CTA tạo kèo toàn chiều ngang → 3 chip loại → ngày → card.
- Card: badge loại + định dạng → tên/uy tín/sao và avatar → giờ → khu vực/trình độ → nút toàn chiều ngang.
- Chi tiết: tên đội là tiêu đề lớn, các dòng phân cách; không tự thay bằng khối VS/card dày.
- Màn mới không có trong index: dùng heading, field, notice và action cùng hệ; không tự thêm bento, ảnh trang trí, bảng chỉ số hoặc theme riêng.
- Không đưa aside, bóng đổ khung điện thoại hay chrome trình duyệt vào Android.
- Chữ “backend”, HTTP code, mã contract và bộ điều khiển demo đặt trong tài liệu/debug, không rải vào product flow. Mô phỏng chỉ cần nhãn dữ liệu mẫu rõ và ngắn.
- Phiên so sánh chuẩn là light theme. Dark theme cần màu có tương phản riêng và test sau, không tự nhận index có bản dark.

## 5. Luồng khởi động, auth và tài khoản

### 5.1. Điều hướng khởi động

~~~mermaid
flowchart TD
    A[System splash + LaunchGate] --> B{Phiên có dùng được?}
    B -->|Có| S[Main shell]
    B -->|Cần refresh| R[Refresh một lần qua session coordinator]
    R -->|Thành công| S
    R -->|Refresh không hợp lệ| C{Đã xem onboarding?}
    B -->|Chưa có phiên| C
    C -->|Chưa| O[Onboarding có Bỏ qua]
    C -->|Rồi| L[Chọn cách đăng nhập]
    O --> L
    L -->|Google / Facebook| X[OAuth theo mode tích hợp]
    L -->|Điện thoại| P[Nhập số điện thoại]
    P --> T[Nhập OTP]
    X --> U{isNewUser?}
    T --> U
    U -->|Có| F[Hoàn thiện hồ sơ]
    U -->|Không| D[Đích chờ hợp lệ hoặc Main shell]
    F --> D
    R -->|Mạng / server tạm lỗi| E[Trạng thái lỗi có Thử lại]
    X -->|AUTH_003| Z[Tài khoản đã xóa]
    T -->|AUTH_003| Z
    R -->|AUTH_003| Z
    Z --> L
~~~

Đây là sơ đồ UX, không thay thế phân loại mã lỗi từ server. Không xóa phiên chỉ vì mất mạng hoặc server tạm lỗi.

### 5.2. Splash và onboarding

- Dùng Android system splash/Core SplashScreen hiện có. LaunchGate là logic/session state, không tạo SplashActivity thứ hai hoặc dùng delay cố định để kéo dài splash.
- Có thể giữ system splash trong thời gian kiểm tra cục bộ ngắn. Nếu refresh lâu/lỗi, hiển thị launch state thật có loading/retry; không giữ splash treo vô hạn.
- Đọc session/refresh ngoài main thread; hủy công việc khi lifecycle yêu cầu, ngăn điều hướng lặp.
- Không render Home rồi mới chuyển Login gây nháy nội dung đã đăng nhập.
- Onboarding gọn, có thể 3 nội dung giá trị trong một destination, Bỏ qua/Bắt đầu; lưu cờ đã xem.
- Đợt 02 chỉ hoàn thiện hai destination đầu; **bật auth gate làm đường vào sản phẩm khi đợt 04 đã nối đủ flow**. Không báo auth end-to-end xong ở đợt 02.

### 5.3. Google/Facebook, số điện thoại và OTP

Contract USER-001 hiện là **mock auth**, không xác minh OAuth và không gửi SMS:
- `oauth` nhận `provider`, `oauthToken`, `email`; không tự thêm email/password, quên mật khẩu hoặc đăng ký password.
- `send-otp` nhận `phone` theo `^0\d{9}$`; `verify-otp` nhận `phone` và `otp` 6 chữ số.
- Nếu UI hỗ trợ nhập +84, normalize về phone bắt đầu 0 trước khi gọi contract, kiểm tra và test rõ; không gửi nguyên phoneNumber/code từ handoff.
- Send trả `expiresIn` (ví dụ 300 giây); tính countdown bằng deadline, không reset khi rotate/background. Resend cooldown là quyết định UI riêng nếu chưa có contract, không gán bừa expiresIn là cooldown server.
- Mock backend chấp nhận mọi mã 6 chữ số. Invalid/expired OTP và hủy provider được test bằng fake/debug scenario; không báo backend hiện đã kiểm mã/SMS thật.
- Sáu ô OTP có thể là một text input có visual slots để paste, xóa, IME và TalkBack hoạt động đúng.
- Disable submit đang xử lý; tránh gửi lại do recomposition hoặc bấm liên tiếp; cho sửa số và quay lại.
- Chưa gắn Firebase Auth/Credential Manager/provider SDK thật chỉ để vẽ nút. Auth thật cần integration task riêng với cấu hình và backend tương ứng.

### 5.4. Sau đăng nhập và hoàn thiện hồ sơ

- `isNewUser=true` mở PROF-02; người cũ về đích hợp lệ hoặc shell. Đăng ký tài khoản là nhánh kết quả của auth, không thêm màn đăng ký mật khẩu.
- Form có displayName, avatarUrl, bio, preferredPosition, dateOfBirth, gender, wardId.
- Theo `PATCH /profile`, field đều tùy chọn; nếu gửi tên không được trắng, DOB đủ 13 tuổi và trong quá khứ; preferredPosition dùng nhãn tiếng Việt theo API; wardId là UUID phường/xã.
- Onboarding hồ sơ cho phép bỏ qua thông tin tùy chọn/tiếp tục với dữ liệu profile hợp lệ. Không tự khóa người dùng cho tới khi điền mọi field.
- Avatar picker/upload và master data khu vực là phụ thuộc cần kiểm tra; fake phải tách khỏi real repository. Không tự tạo URL ảnh “đã upload”.
- Giữ draft khi đi chọn khu vực/ảnh và khi app tái tạo.

### 5.5. Session, đăng xuất và tài khoản đã xóa

- Một session coordinator điều phối refresh cho nhiều request 401 đồng thời, retry request gốc có giới hạn; không mở nhiều Login/dialog và không refresh vòng lặp.
- Lưu access/refresh qua abstraction lưu trữ phù hợp, không log token; cờ onboarding/preferences qua DataStore. Tách dữ liệu theo user.
- Chỉ lưu pending destination có kiểu + ID hợp lệ; sau auth tải lại và kiểm quyền/đối tượng, fallback rõ nếu đã xóa. Không tự mở URL tùy ý.
- Logout xóa session và state/cache của user, hủy request/subscription cũ, đưa về Login và không Back vào nội dung bảo vệ. Không bịa `POST /auth/logout` khi contract chưa có.
- `AUTH_003`: xóa phiên, giải thích tài khoản đã xóa, chọn tài khoản khác; không có CTA khôi phục.
- Xóa tài khoản thành công `204` → xóa phiên ngay. `422 USER_003` → liệt kê/đưa tới đội còn làm đội trưởng, kể cả INACTIVE; chuyển quyền hoặc giải tán trước. Cách lấy danh sách đội là dependency nếu contract chưa cung cấp, không khẳng định lỗi trả sẵn danh sách.
- Role tài khoản USER/ADMIN khác quyền CAPTAIN/MEMBER theo từng đội. Không có lựa chọn “đội trưởng vĩnh viễn” lúc đăng nhập.

## 6. Bản đồ điều hướng sản phẩm đầy đủ

- **Tìm kèo:** khám phá → detail A/B → chọn đội → gửi/nhận → yêu cầu hoặc thương lượng → chat → trận đã ghép → sau trận.
- **Đá lẻ:** khám phá → tin tuyển → ứng tuyển → đơn của tôi → chi tiết đơn → chat/sân sau duyệt → đánh giá hoặc rút đơn.
- **Tin nhắn:** inbox → phòng TEAM_MATCH/MERCENARY_APPLICATION → đối tượng liên quan; phòng không còn quyền có recovery rõ.
- **Đội của tôi:** danh sách/hub → chi tiết đội → thành viên/lời mời/chuyển quyền/lịch sử/lịch đấu → tạo kèo/tuyển người/trận ngoài.
- **Avatar/menu phụ:** hồ sơ → sửa/hoàn thiện → uy tín → reports → tài khoản; ADMIN thấy lối vào quản trị.
- **Chuông header:** thông báo → đánh dấu đọc và resolve referenceType/referenceId → kiểm quyền → đích hoặc nội dung không khả dụng.
- **Địa điểm:** picker dùng chung → detail/create/edit/photos/report → trả kết quả đúng caller; không mở thành tab thứ năm.
- **Admin:** dashboard → đội/lịch sử thành viên/tranh chấp đội/tranh chấp đá lẻ/báo cáo sân/bộ môn → xem xét/ra quyết định.
- Chi tiết hệ thống Back: đóng dialog/sheet trước; form dirty có xác nhận; về tab giữ filter/scroll; giữ tab stack; không tạo nhiều bản shell.
- Chỉ khai báo mỗi destination một chỗ phù hợp trong graph; PairingRoom dùng chung, không đăng ký trùng ở các nested graph rồi để route thiếu đường vào.
- Entry hồ sơ, địa điểm, lịch sử, inbox và trạng thái lỗi mới là mở rộng UX [PROPOSAL] cùng hệ index; không mặc nhiên xem HTML có sẵn layout cho tất cả.

## 7. Module và cách giữ kiến trúc

| Phạm vi | Module/owner dự kiến |
| --- | --- |
| Launch, root graph, shell, route guard, composition root | app |
| Auth và session domain/data | feature:auth mới; service dùng chung có owner rõ, không import presentation qua feature |
| Hồ sơ/tài khoản | feature:profile mới |
| Kèo/yêu cầu/lịch đấu | feature:match hiện có |
| Gói thương lượng | feature:pairing hiện có |
| Đá lẻ, tuyển người, ứng viên, trận ngoài | feature:mercenary hiện có; tách external chỉ khi có nhu cầu cụ thể |
| Đội/thành viên/lời mời | feature:team hiện có |
| Inbox/chat/realtime conversation | feature:chat mới |
| Thông báo/resolver | feature:notification mới |
| Sân/picker/ảnh/report | feature:venue mới |
| Uy tín/đánh giá/report/tranh chấp | feature:reputation mới |
| Các destination quản trị | feature:admin mới, UI guard + API guard |
| Tokens, controls, errors, result và nền tảng dùng chung | feature:base hiện có |

Tên/module mới là đề xuất triển khai, không phải yêu cầu tạo rỗng ngay toàn bộ. Tạo khi đến đợt sử dụng; cập nhật Koin, convention plugins và Konsist tương ứng.
Giữ nguyên dependency rules hiện có. Tái sử dụng qua base hoặc domain contract, không cho các feature import ViewModel/Composable của nhau.
Tuyệt đối không trộn state của request ghép kèo với state pairing hoặc application; UI effect điều hướng dùng ID từ kết quả thao tác.
Model immutable, StateFlow + collectAsStateWithLifecycle; side effect không chạy trực tiếp trong recomposition.
Sử dụng Navigation Compose type-safe đang có; không tự migrate Navigation 3/đổi DI chỉ để chỉnh UI.

## 8. Thứ tự 32 đợt, mỗi đợt 2 màn

Bảng dưới là kế hoạch mới. **Mọi đợt đang Pending**; chữ “Sửa” chỉ nói có code nền, không có nghĩa đã nghiệm thu.
Các nhãn P0/P1 ở đây chỉ xác định thứ tự, không cho phép bỏ phần sau khi báo hoàn thiện.

- Đợt 01: sửa chuẩn thị giác trước để khóa ngôn ngữ giao diện.
- Đợt 02–05: ưu tiên splash/auth/profile để có hành trình vào app đầy đủ.
- Đợt 06–10: nối vòng lặp chính và thay placeholder chat/thông báo.
- Đợt 11–18: quản lý đội/kèo/địa điểm đầy đủ.
- Đợt 19–24: hai phía đá lẻ và trận ngoài.
- Đợt 25–28: sau trận/uy tín/reports.
- Đợt 29–32: admin và kiểm tra liên thông cuối.

| Đợt | Branch dự kiến | Màn 1 | Màn 2 | Loại công việc | Điều kiện chính để nghiệm thu |
| --- | --- | --- | --- | --- | --- |
| 01 | `ui/index-01-match-list-detail` | Tìm kèo | Chi tiết kèo A/B | Sửa | Theme/header/bottom bar và 2 màn khớp index.html; lọc ngày/loại; giữ state khi Back. |
| 02 | `ui/index-02-launch-onboarding` | Splash / kiểm tra phiên | Onboarding | Mới | Không chớp Home trước kiểm tra phiên; onboarding có Bỏ qua; bước mở đầu auth được kiểm tra cùng đợt 04. |
| 03 | `ui/index-03-login-phone` | Chọn cách đăng nhập | Nhập số điện thoại | Mới | Google/Facebook/điện thoại; chuẩn hóa phone đúng contract; xử lý gửi mã, hủy OAuth và lỗi mạng. |
| 04 | `ui/index-04-otp-profile-form` | Xác thực OTP | Hoàn thiện / sửa hồ sơ | Mới | Đăng ký và đăng nhập chung; isNewUser; bộ đếm theo expiresIn; nối Launch → Auth → Shell; khôi phục đích hợp lệ. |
| 05 | `ui/index-05-profile-account` | Hồ sơ cá nhân | Tài khoản / cài đặt | Mới | Đăng xuất, xóa và chặn xóa khi còn làm đội trưởng; phiên hết hạn/tài khoản đã xóa; entry từ shell. |
| 06 | `ui/index-06-match-create-review` | Tạo kèo A/B | Xem lại trước đăng | Sửa | Form theo index.html; bản nháp giữ khi Back; validation thời gian/options; chống đăng trùng. |
| 07 | `ui/index-07-pairing-room-list` | Phòng thương lượng | Danh sách thương lượng | Sửa + mới | Danh sách có biến thể chủ kèo/đội tham gia; chọn sân/giờ, chi phí, revision, xác nhận hai bên và trạng thái cuối. |
| 08 | `ui/index-08-mercenary-team-home` | Khám phá đá lẻ | Trang đội của tôi / chi tiết đội | Sửa | Hai màn chính đúng bố cục index.html; đội ACTIVE/INACTIVE/DISBANDED và vai trò; giữ đường vào chức năng mở rộng. |
| 09 | `ui/index-09-chat-inbox-room` | Hộp thư hội thoại | Phòng chat dùng chung | Mới | TEAM_MATCH và MERCENARY_APPLICATION; nguồn REST; read-only/canSend, lỗi gửi và reconnect; thay placeholder. |
| 10 | `ui/index-10-notifications-target` | Trung tâm thông báo | Nội dung không khả dụng | Mới | Chuông header; all/unread, đọc tất cả, referenceType/id; đích xóa/mất quyền; catalog là dữ liệu kiểm thử. |
| 11 | `ui/index-11-team-lists` | Danh sách đội của tôi | Khám phá đội hệ thống | Mới | Tách tư cách thành viên khỏi danh sách toàn hệ thống; hỗ trợ nhiều đội và bộ chọn đội thao tác. |
| 12 | `ui/index-12-team-create-edit` | Tạo đội | Sửa đội | Sửa + mới | Tái sử dụng form; validate tên/khu vực/trình độ và phân quyền; đồng bộ danh sách/chi tiết sau lưu. |
| 13 | `ui/index-13-team-members-invitation` | Thành viên đội | Phản hồi lời mời | Mới | Sheet mời/chuyển quyền/rời/giải tán; đúng userId và quyền; thông báo mở đúng lời mời. |
| 14 | `ui/index-14-team-history-schedule` | Lịch sử đội trưởng | Lịch thi đấu của đội | Mới | Sắp tới/kết thúc/hủy-quá hạn; từ đội vào kèo; tên tài khoản đã xóa theo contract. |
| 15 | `ui/index-15-match-requests` | Yêu cầu bắt kèo đã gửi | Yêu cầu bắt kèo nhận được | Mới | Chi tiết/duyệt/từ chối/rút; chọn đúng đội; hủy kèo, expectedStatus, chi phí A và cạnh tranh đồng thời. |
| 16 | `ui/index-16-venue-picker-detail` | Tìm / chọn sân | Chi tiết sân | Mới | Trả kết quả đúng form và giữ draft; sân ngừng hoạt động không chọn mới; trạng thái thiếu ảnh/bản đồ. |
| 17 | `ui/index-17-venue-create-edit` | Thêm sân | Sửa sân | Mới | Quyền người tạo/admin; validation; sân tạo xong chọn ngay vào form đang dở. |
| 18 | `ui/index-18-venue-photos-report` | Thư viện ảnh sân | Báo cáo thông tin sân | Mới | Thêm/xóa/đặt ảnh chính theo contract; lỗi upload/retry; báo cáo có kết quả, không submit giả. |
| 19 | `ui/index-19-mercenary-detail-applications` | Chi tiết tin đá lẻ | Đơn ứng tuyển của tôi | Mới | Ứng tuyển qua sheet; kiểm tra trùng/đội mình/giờ; ẩn sân trước duyệt ở cả payload và UI. |
| 20 | `ui/index-20-application-team-posts` | Chi tiết đơn ứng tuyển | Tin tuyển của đội | Mới | APPROVED mới hiện sân; rút đơn theo trạng thái; sửa số lượng/đóng tin và cập nhật slot. |
| 21 | `ui/index-21-recruitment-select-create` | Chọn trận cần tuyển | Đăng tin tuyển | Mới | Chọn kèo đã ghép hoặc trận ngoài; tạo từng vị trí đúng endpoint; partial failure không đăng trùng. |
| 22 | `ui/index-22-applicants-review` | Danh sách ứng viên | Chi tiết / duyệt ứng viên | Mới | Ứng viên và chủ tuyển hai phía; duyệt/từ chối, đủ slot/xung đột lịch, không có hủy duyệt giả. |
| 23 | `ui/index-23-external-list-detail` | Danh sách trận ngoài | Chi tiết trận ngoài | Mới | Đội đúng ngữ cảnh; liên kết tin tuyển và ứng viên; hiển thị trạng thái trận. |
| 24 | `ui/index-24-external-create-edit` | Khai báo trận ngoài | Sửa trận ngoài | Mới | Khóa sân/giờ khi đã có người được duyệt; hủy trận qua dialog; thêm vị trí qua sheet. |
| 25 | `ui/index-25-match-rating-results` | Đánh giá đối thủ | Kết quả đánh giá trận | Mới | Blind rating; sao/nội dung/tỷ số nếu contract có; một lần gửi; chưa đủ điều kiện/hết hạn. |
| 26 | `ui/index-26-mercenary-rating-team-reputation` | Đánh giá đá lẻ hai chiều | Uy tín đội | Mới | Người đá lẻ chấm đội và đội chấm người; chỉ hiện completion rate khi đủ số trận; tách uy tín cá nhân. |
| 27 | `ui/index-27-player-reputation-reports` | Uy tín cá nhân | Báo cáo đã gửi / nhận | Mới | Danh sách đúng quyền và đúng loại đội/cá nhân; phân trang/empty; mở chi tiết report. |
| 28 | `ui/index-28-report-create-detail` | Gửi báo cáo vi phạm | Chi tiết báo cáo / tranh chấp | Mới | Loại vi phạm theo enum gốc; khiếu nại NO-SHOW còn hạn; kết quả xử lý và quyền của hai bên. |
| 29 | `ui/index-29-admin-home-teams` | Trang quản trị | Quản lý đội | Mới | Route guard ADMIN; đình chỉ/kích hoạt theo trạng thái hợp lệ; lý do; không phục hồi đội giải tán. |
| 30 | `ui/index-30-admin-members-team-disputes` | Lịch sử thành viên quản trị | Tranh chấp giữa các đội | Mới | Tên tài khoản xóa theo API admin; hàng chờ, phân trang và đích xem xét tranh chấp. |
| 31 | `ui/index-31-admin-player-disputes-venue-reports` | Tranh chấp đá lẻ | Báo cáo sân | Mới | Hai hàng chờ; chốt report sân/bật-tắt sân qua dialog; lỗi quyền và trạng thái đã xử lý. |
| 32 | `ui/index-32-admin-sports-dispute-review` | Danh mục bộ môn | Xem xét / xử lý tranh chấp | Mới | Dùng biến thể tranh chấp đội/đá lẻ; quyết định + ghi chú; bật-tắt bộ môn; nối toàn bộ luồng admin. |

Quy ước tách màn:
- List thương lượng có hai scope host/participant nhưng tái sử dụng một destination/layout.
- OTP mới/cũ và ProfileForm onboarding/edit là biến thể, không tạo màn chỉ để tăng số lượng.
- Chat có layout room dùng chung theo conversation context. Catalog thông báo là fixtures/test, không phải tab sản phẩm.
- EXT-02 được tách thành list và detail vì hai tác vụ riêng.
- ApplicantReview, VenueReport, AdminDisputeReview và UnavailableContent được chọn làm destination toàn trang [PROPOSAL]; xác nhận quyết định vẫn dùng dialog khi phù hợp.
- Sheet/dialog/biến thể được truy vết trong [screen-coverage.md](screen-coverage.md); không dùng một dialog để thay cho màn thứ hai của một đợt.
- Một số entry cần màn ở đợt sau (ví dụ giải tán đội trong flow chặn xóa). Ghi dependency, không bật nút không có kết quả hoặc báo flow đó xong sớm. Re-test liên thông khi dependency hoàn tất.

## 9. Các ràng buộc nghiệp vụ xuyên màn

1. ArenaFlow không đặt sân/thu/giữ/hoàn tiền; chi phí và cọc là thỏa thuận bên ngoài.
2. Cọc chỉ ở gói B theo contract; không tự đưa deposit vào kèo A.
3. Chi tiết A: request accepted → match confirmed → các pending khác superseded. Chỉ server/fake repository chuyển state, không patch riêng một nhãn UI.
4. B: đổi sân/giờ/chi phí tăng revision và reset xác nhận; hai bên phải xác nhận cùng revision; 409 buộc tải lại. Không có rút pairing nếu contract không hỗ trợ.
5. UI hiển thị hành động dựa vào người dùng, teamId, vai trò và trạng thái mới nhất. Các thao tác ghi vẫn phải xử lý server từ chối sau khi màn mở.
6. Hủy/rút dùng expectedStatus và lỗi xung đột theo spec; không tính mức phạt bằng một nhãn cứng.
7. Đá lẻ: trước approved không lấy/hiện venueId, tên sân, địa chỉ chi tiết hoặc tọa độ từ nguồn dữ liệu bị hạn chế rồi chỉ che bằng UI. Public catalog sân không đồng nghĩa công khai sân của một tin tuyển.
8. Rút đơn approved cảnh báo -10 điểm cá nhân theo contract; pending không phạt. Khôi phục slot/tin tuyển và xử lý giờ bắt đầu.
9. Không thêm “hủy duyệt ứng viên”, xin vào đội, kick thành viên, phục hồi đội/tài khoản hay endpoint tìm người chưa có contract.
10. Trận ngoài đã có approved applicant khóa sửa sân/giờ; hủy phải phản ánh các tin và đơn liên quan.
11. Blind rating không tiết lộ đối phương đã gửi/chưa gửi khi bên mình chưa gửi. Completion rate chỉ hiện khi đủ 5 trận.
12. NO-SHOW dispute có hạn 24 giờ theo server; loại khác không tự thêm nút tranh chấp. Enum dùng nguyên văn contract chuyên biệt.
13. Tài khoản đã xóa: UI thường dùng tên che do server trả; admin dùng tên/cờ tương ứng admin API.
14. Chat dùng REST làm nguồn chính; subscribe STOMP theo conversation và user queue, không dùng STOMP SEND để gửi tin.
15. Reconnect phải bù lịch sử và deduplicate message ID; token/user/role đổi phải hủy subscription cũ. canSend không suy ra chỉ từ trạng thái “đã kết thúc”: hỏi header phòng hiện tại và retention.
16. Thông báo: danh mục và resolver theo NOTIFICATION-001; chưa có push điện thoại thật trong phạm vi contract hiện tại.
17. Ngày/giờ dùng timezone nhất quán. Fixtures dùng clock cố định ở debug/test; runtime thật không khóa vào 21/11/2026.

## 10. Phụ thuộc và chế độ dữ liệu

Mỗi màn ghi riêng ba mức: **UI**, **flow/navigation**, **API thật**. Không gộp thành một cột “Done”.

| Phụ thuộc | Việc phải kiểm tra / cách tiến hành |
| --- | --- |
| OAuth/SMS thật | Hiện backend mock; hoàn thiện UI/flow bằng fake có nhãn. Không báo xác thực provider thật |
| Đội của tôi / quyền từng đội | Xác minh endpoint/membership thực tế; không giả định GET teams là my teams. Fake có nhiều đội và quyền khác nhau |
| Tìm người mời | Contract mời bằng userId; không suy ra userId từ tên/SĐT. Dùng bộ chọn fixture trong demo nếu thiếu search |
| Master data khu vực | ID phường/xã đúng kiểu; mapping profile/venue; không gửi số nguyên fixture thay UUID |
| Upload/avatar/map | Kiểm tra API hạ tầng hiện có. Venue có flow xin signature/lưu ảnh/set-primary; không sao chép /photos từ handoff |
| Realtime | Đọc MATCH-001 §1.7, NOTIFICATION-001 §1.4 và [ADR-0014](../ArenaFlow-spec/decisions/0014-realtime-chat-stomp-websocket.md), kể cả sửa đổi mới nhất |
| Các endpoint chuyên biệt admin/report | Đội và đá lẻ khác scope; kiểm từng endpoint và body, không dùng một endpoint tổng quát do prototype tưởng tượng |
| Nội dung pháp lý/hỗ trợ | Cần nội dung chủ dự án cung cấp; không viết văn bản giả rồi trình bày như đã phê duyệt |
| Các màn chưa có trong index | Thiết kế theo cùng tokens/control, lưu decision và ảnh nghiệm thu; không lấy template Stitch làm shortcut |

UI/flow có thể tiếp tục bằng fake khi backend chưa sẵn sàng. Trạng thái API thật phải ghi **Blocked + dependency cụ thể** nếu chưa tích hợp/kiểm được. Không đánh dấu toàn sản phẩm production-ready khi còn dùng mock auth hoặc thiếu API.

## 11. Quy trình Git và bàn giao từng cặp

1. Đọc trạng thái working tree/index trước thao tác. Không stage/khôi phục/xóa thay đổi của người dùng.
2. Tại lúc lập plan, nền module mới là `c0997ba` trên `feat/modularize-features`. Trước đợt 01 kiểm tra `main` đã chứa commit này chưa. Nếu có, lấy main đã cập nhật; nếu chưa, giữ nền module hiện hành và ghi rõ base. Không checkout main cũ làm mất việc modularize.
3. Nhánh đầu: `ui/index-01-match-list-detail`. Nhánh sau tạo từ commit đã kiểm tra của nhánh trước theo bảng. Không tái sử dụng/ghi đè các branch ui/01... cũ.
4. Kiểm tra remote hiện tại trước push. Push branch mới với upstream; không force push hoặc tự merge vào main.
5. Chỉ commit đường dẫn của cặp màn và hạ tầng/test/docs liên quan. Trong baseline có deletion được stage của `docs/prototype/complete/index.html`; đó là thay đổi người dùng, không đưa vào commit UI nếu chưa được giao.
6. Sau kiểm tra, commit và push cặp màn; ghi branch, base SHA, commit SHA, hai màn, ảnh, test và phần API còn mock.
7. Chỉ tạo branch kế tiếp sau khi đợt trước đã có checkpoint và trạng thái push rõ ràng. Nếu push bị chặn bởi mạng/quyền/approval, giữ local commit, báo nguyên nhân thực tế; không tuyên bố đã push.
8. Mỗi đợt có file QA ngắn hoặc hàng trong progress mới. Giữ log lịch sử cũ, không sửa “Completed” thành “đúng index” khi chưa đối chiếu lại.
9. Chỉ gỡ module starter sau khi không còn route/dependency, build và Konsist xác nhận; không xóa hàng loạt ngay trong đợt chỉnh màu.

## 12. Kiểm tra và bằng chứng bắt buộc

### 12.1. So sánh giao diện

- Với 8 view gốc: chạy **đúng root prototype/index.html**, chụp từng view ở 390px và thêm 360/430px khi kiểm responsive.
- Android: chụp màn có chiều rộng logic tương đương, cùng fixtures, clock, role và state; loại trừ system bars khi so vùng nội dung.
- Xem ảnh nguồn và app cạnh nhau: header/hero, thứ tự nội dung, line break, padding, card, nút, màu, icon, border/radius. Không so bằng trí nhớ.
- Màn ngoài 8 view: ảnh tham chiếu là màn cùng loại đã khóa theo index + mô tả anatomy; không gọi là “giống pixel prototype” khi nguồn không có màn.
- Kiểm ở font mặc định và font scale 1.3; bàn phím không che submit; danh sách/form cuộn được; touch target ≥48dp; TalkBack có nhãn.
- Test light trước. Tablet/expanded width dùng khoảng đọc và navigation phù hợp; không kéo card phone rộng toàn màn. Không đưa vào báo cáo đã test tablet/dark nếu chưa chạy.
- Capture phải mở kiểm tra, không ảnh đen/stale/sai route; đợi transition ổn định. Compose Preview hỗ trợ, không thay kiểm tra native runtime.
- Không yêu cầu web detector chạy trên Kotlin. Test và bằng chứng đánh giá native theo nền tảng Android.

### 12.2. Kiểm tra code

- Chạy assembleDebug, unit tests của các module thay đổi và các kiểm tra kiến trúc Konsist hiện có; lint/check theo cấu hình repo.
- Test logic có rủi ro: auth routing/refresh/logout; vai trò theo đội; mapper/contract; draft validation; revision; duyệt đồng thời; visibility sân; blind rating; notification resolver.
- Kiểm API thật với môi trường có quyền khi khả dụng. UI fake pass không thay thế integration pass.
- Form đang dở, rotate/process recreation, chuyển tab/quay lại, double tap, offline/retry/403/404/409 phải có kết quả rõ. Không tự động retry thao tác ghi gây nhân bản.
- Mỗi nút nhìn thấy có hành vi, điều hướng hoặc trạng thái không khả dụng có lý do. Không còn callback rỗng/placeholder trong luồng đã báo hoàn thiện.

### 12.3. Hành trình nghiệm thu toàn bộ

| Mã | Hành trình phải đi được |
| --- | --- |
| QA-01 | Cài mới → splash → onboarding skip/finish → Login → phone → OTP → profile → shell |
| QA-02 | Người cũ có token → shell; token hết hạn → refresh → shell; không chớp Login/Home sai |
| QA-03 | OAuth mock success/cancel/failure; phone invalid; resend/countdown qua rotate/background |
| QA-04 | Nhiều request 401 → một refresh; refresh hết hạn → Login một lần; về đích cũ nếu còn quyền |
| QA-05 | Logout → user khác; Back không vào phiên cũ; cache, acting team và realtime không lẫn user |
| QA-06 | Xóa tài khoản cá nhân; đội trưởng bị chặn → chuyển quyền/giải tán → thử lại; AUTH_003 không có khôi phục |
| QA-07 | Home đúng index → lọc ngày/loại → detail → Back giữ filter/scroll; empty/error/retry |
| QA-08 | Tạo A/B → picker sân/create sân → quay lại đủ draft → review → post một lần |
| QA-09 | Đội gửi request A → mở chat → chủ kèo duyệt → kèo confirmed và request khác superseded |
| QA-10 | Rút pending/accepted, hủy OPEN/CONFIRMED, expectedStatus thay đổi đồng thời; mức phạt/lý do đúng |
| QA-11 | Nhận B → proposal/cost → một bên confirm → sửa gói reset cả hai → hai bên confirm cùng revision |
| QA-12 | B bị đội thứ ba chốt / expired / cancelled / revision mismatch; nút và recovery đúng |
| QA-13 | Nhiều đội → chọn đúng actingTeam → quyền khác nhau khi gửi/nhận/tuyển/đánh giá |
| QA-14 | Tạo/sửa đội, mời, nhận/từ chối lời mời, chuyển quyền, rời, giải tán và lịch sử |
| QA-15 | Danh sách toàn hệ thống không bị hiển thị thành đội của tôi; USER thường không vào Admin |
| QA-16 | Đá lẻ từ chối GPS → fallback khu vực; bán kính mặc định 3km; chưa duyệt không lộ sân |
| QA-17 | Ứng tuyển → chủ tuyển duyệt/từ chối → ứng viên xem đơn; approved mới thấy sân/chat; slot đầy |
| QA-18 | Rút approved → cảnh báo phạt → slot mở lại; trùng giờ/đơn trùng/đội mình bị chặn đúng |
| QA-19 | Trận ngoài create/edit/list/detail → thêm vị trí → có approved khóa sân/giờ → cancel đồng bộ tin/đơn |
| QA-20 | Venue search/create/edit/images/report; inactive không chọn mới; lỗi upload và retry không nhân ảnh |
| QA-21 | Inbox → chat đội/đá lẻ → gửi lỗi/retry → reconnect bù tin/dedup → chỉ đọc/mất quyền |
| QA-22 | Chuông/badge → all/unread → mark read → mọi referenceType; đích bị xóa và session expired |
| QA-23 | Sau trận → đánh giá hai chiều → blind rating; đội dưới 5 trận không có completion rate |
| QA-24 | Report → NO-SHOW dispute trước/sau 24h → Admin resolve → kết quả/điểm theo server |
| QA-25 | Admin quản lý đội/sân/bộ môn, lịch sử tên đã xóa; report/dispute đã xử lý và quyền bị thu hồi |
| QA-26 | Tất cả 101 ID có route/màn chủ, state, entry, ảnh/test và trạng thái UI/flow/API riêng |

## 13. Điều kiện kết thúc và artifact bàn giao

Một cặp được nghiệm thu khi: đúng hình thức nguồn đã chọn, hai màn có hành vi kiểm tra được, sheet/state thuộc phạm vi có đủ, build/check liên quan pass, ảnh hợp lệ và branch/commit/push được ghi thật.
Cặp có UI mock chỉ được đánh dấu **UI/flow accepted**, cột API không được đánh dấu Integrated.

Toàn bộ kế hoạch chỉ hoàn tất khi:
- [ ] 101 ID trong screen-coverage có chủ sở hữu, entry và bằng chứng; không bỏ trạng thái/sheet vì không là màn chính.
- [ ] 8 view gốc đúng ngôn ngữ giao diện của root index.html.
- [ ] Splash/auth/profile/session nối từ app start; không cần mở route thủ công để demo.
- [ ] Hai phía đội trưởng/người chơi/admin và các hành trình QA-01..26 đi được.
- [ ] Không còn placeholder/callback rỗng trong phạm vi hoàn thành.
- [ ] Các dependency/API mock được ghi riêng; không nhận production-ready khi còn thiếu tích hợp.
- [ ] Các checkpoint theo cặp đã push hoặc được báo chính xác là bị chặn.
- [ ] Có bộ ảnh so sánh, báo cáo test, route/state map và hướng dẫn chuyển mock sang API.
- [ ] Tài liệu nguồn mới không còn bị README/progress cũ định hướng nhầm.

Artifact cần cập nhật qua từng đợt:
- Plan này và screen-coverage: trạng thái thực tế, route/màn chủ và link evidence.
- `docs/android-ui/batches/index-NN.md`: base/commit/push, 2 màn, endpoint/mock, test, ảnh, dependency.
- `docs/android-ui/qa/index/`: ảnh prototype/native theo viewport/state, so sánh và báo cáo QA.
- `docs/android-ui/design-index.md`: tokens/components thực sự triển khai sau đợt 01; không sao chép lại theme Stitch.
- Fixtures/debug scenario cho vai trò, clock và lỗi; không đưa demo panel vào sản phẩm release.

## 14. Prompt bàn giao cho AI thực hiện

> Đọc docs/android-ui/IMPLEMENTATION_PLAN.md và docs/android-ui/screen-coverage.md.
> Mục tiêu: hoàn thiện Android ArenaFlow, gồm sửa UI theo docs/prototype/index.html và bổ sung splash/onboarding/auth/profile cùng toàn bộ màn phụ trong 101 ID.
> Root index.html là chuẩn thị giác đã được người dùng chọn. Prototype/complete chỉ là checklist chức năng cần kiểm lại với contract gốc; không dùng complete, stitch-preview hoặc export Stitch làm theme/layout.
> Kiểm tra branch/base hiện tại và giữ các thay đổi người dùng; kế thừa modularization có c0997ba hoặc bản mới hơn.
> Làm lần lượt 32 đợt trong plan, mỗi đợt 2 màn và các sheet/state liên quan. Mỗi cặp hoàn tất kiểm tra thì commit/push branch nối tiếp, ghi base, SHA và trạng thái push thật.
> Giữ Kotlin/Compose, Koin, UDF/repository và Navigation type-safe hiện có. Không đổi stack hoặc sửa spec/backend ngoài phạm vi.
> Từ đợt 02–05 phải hoàn thiện splash → onboarding → auth → profile → shell và session lifecycle. Backend auth hiện mock; không nhận đã có OAuth/SMS thật.
> Đối chiếu ảnh 8 view nguồn và native ở cùng viewport/fixtures; màn mới kế thừa cùng hệ. Kiểm tra mọi CTA, Back, draft, role, error/empty/loading và các hành trình QA.
> Cập nhật UI/flow/API riêng; không đánh dấu Done chỉ vì compile hoặc có file Compose. Tiếp tục các việc không phụ thuộc phần đang bị chặn và báo rõ dependency thực tế.
