# ArenaFlow Prototype — Decision Log

Tài liệu ghi nhận toàn bộ các quyết định kiến trúc, lựa chọn UX [PROPOSAL] và phương án xử lý phụ thuộc/mâu thuẫn [DEPENDENCY] trong quá trình xây dựng prototype hoàn chỉnh tại `docs/prototype/complete/`.

---

## 1. Quyết định Kiến trúc & Nền tảng (WP-01)

### DEC-01: Công nghệ thuần HTML5, CSS Tokens và ES6 Modules
- **Bối cảnh:** Cần một prototype có thể chạy trực tiếp bằng cách mở file `index.html` trong trình duyệt hoặc qua bất kỳ local static server nào (`http-server`, `python -m http.server`, VSCode Live Server) mà không bắt buộc cài đặt Node build toolchain hay bundler nặng nề.
- **Quyết định:** Sử dụng HTML5 semantic, CSS Variables (Design Tokens) và Native ES6 Modules (`import`/`export`).
- **Nguồn:** `PROTOTYPE_HANDOFF.md` §10.
- **Tác động:** Không có bước compile, nhà phát triển và QA có thể inspect code và sửa trực tiếp; chuyển giao sang Android Jetpack Compose dễ dàng vì tokens và components được chuẩn hóa 1-1.

### DEC-02: Viewport trung tâm 390px và Demo Control Center
- **Bối cảnh:** Đích đến của dự án là Android Jetpack Compose. Cần hiển thị đúng tỷ lệ điện thoại di động với các kích thước chuẩn (360px, 390px, 430px), đồng thời có bộ công cụ kiểm thử cho QA/người xem.
- **Quyết định:** Thiết kế mobile frame giả lập với status bar, header sticky, bottom navigation bar 64px, safe area, và home indicator. Đi kèm là một bảng điều khiển nổi (Demo Panel) có thể thu gọn (dock/collapse) thành floating action button (FAB) để không che khuất màn hình khi chụp ảnh nghiệm thu.
- **Nguồn:** `PROTOTYPE_HANDOFF.md` §8.2, §9.

### DEC-03: Reactive Central Store (`window.AppStore`)
- **Bối cảnh:** Người dùng cần kiểm thử hai phía (ví dụ Captain Alpha tạo kèo, đổi sang Captain Beta gửi yêu cầu hoặc chốt gói thương lượng). Chuyển nhân vật phải thấy ngay kết quả của phía bên kia vừa thao tác.
- **Quyết định:** Xây dựng store tập trung theo mô hình pub/sub, lưu trữ toàn bộ entities (users, teams, matches, requests, pairings, recruitments, applications, chats, notifications, reports, reviews) trong bộ nhớ, tự động đồng bộ hóa xuống `localStorage` với version key `arenaflow_prototype_v1`, có nút Reset toàn diện.
- **Nguồn:** `PROTOTYPE_HANDOFF.md` §8.3.

### DEC-04: Deep-linking Router & Overlay Management (`window.AppRouter`)
- **Bối cảnh:** Cần hỗ trợ điều hướng route, lưu lịch sử back stack, deep links dạng hash (`#/match-detail?id=...`), mở modal bottom sheet và center dialog, đồng thời không bao giờ để xảy ra màn hình trắng khi link trỏ tới đối tượng không còn quyền xem hoặc không tồn tại (AC-P09).
- **Quyết định:** Router quản lý view canvas và 2 primitives overlay (Bottom Sheet & Center Dialog). Khi người dùng nhấn nút Back của điện thoại hoặc header, router ưu tiên đóng sheet/dialog trước khi pop route. Nếu route không tìm thấy, router render fallback thông báo thân thiện kèm nút quay về.
- **Nguồn:** `PROTOTYPE_HANDOFF.md` §5, §14.1 (AC-P09).

---

## 2. Xử lý Phụ thuộc & Mâu thuẫn Spec (DEP-01..DEP-11)

| Mã DEC/DEP | Vấn đề / Hiện trạng | Phương án mô phỏng trong Prototype | Tác động bàn giao UI / Backend |
|---|---|---|---|
| **DEP-01** | USER-001 chưa có endpoint “đội của tôi”; `GET /teams` trả toàn bộ đội hệ thống. | Fixture lưu trữ quan hệ `teamMembers` và cờ `captainId`. Store cung cấp `getMyTeams()` và `getMyCaptainedTeams()`. | Cần backend bổ sung endpoint hoặc query param lọc đội của user trước khi tích hợp thật. |
| **DEP-02** | Mời thành viên chỉ nhận `userId`; chưa có endpoint tra cứu username. | Mô phỏng bộ chọn thành viên từ fixture người dùng khả dụng. Không tự bịa field username. | Bàn giao ghi rõ UX chọn từ danh bạ/ID mô phỏng. |
| **DEP-03** | Chưa có danh mục tỉnh/phường toàn quốc. | Fixture cung cấp bảng danh mục 4 phường đại diện tại TP. Thủ Đức và Quận 1 có ID chuẩn (`ward-ak`, `ward-bt`, ...). | Cần endpoint danh mục địa chính trước khi tích hợp production. |
| **DEP-04** | Lời mời chỉ nhận qua notification in-app, notification lưu 30 ngày. | Màn `TEAM-08` được mở trực tiếp từ danh sách thông báo `NOTIF-01`. | Không giả định có inbox lời mời riêng. |
| **DEP-05** | Auth OAuth và OTP hiện là mock service. | Demo panel và view auth cho phép nhập OTP mẫu, mô phỏng các trạng thái loading, expired, error, resend cooldown. | Cần contract SMS Gateway / Google Sign-In SDK thật cho Android. |
| **DEP-06** | Spec tổng quan nhắc đến khả năng khôi phục tài khoản, nhưng API `AUTH_003` chưa hỗ trợ. | Giao diện hiển thị thông báo tài khoản đã xóa theo đúng lỗi `AUTH_003`; không cung cấp nút khôi phục giả. | Tuân thủ nghiêm ngặt AC-P02 và ranh giới sản phẩm §4.1. |
| **DEP-07** | UC-ME01 nói không tạo tin sau giờ bắt đầu; MERCENARY API 2.1 cho tạo đến trước kết thúc trận. | Áp dụng theo API 2.1: Cho phép tạo tin tuyển cho đến trước giờ kết thúc; chặn duyệt/ứng tuyển khi trận đã bắt đầu. | Ghi nhận trong spec để backend và product owner đồng bộ. |
| **DEP-08** | Onboarding, menu tài khoản, xem lại trước đăng là các luồng UX bổ sung. | Thiết kế ngắn gọn, gắn nhãn `[PROPOSAL]` trong tài liệu bàn giao; không suy đoán thêm endpoint mới. | Đội phát triển Compose có thể bật/tắt theo cấu hình. |
| **DEP-09** | Chưa có văn bản điều khoản pháp lý và chính sách bảo mật đã phê duyệt. | Đặt nhãn "Văn bản mẫu chờ cung cấp nội dung chính thức", không bịa điều khoản giả. | Bổ sung nội dung trước khi phát hành lên Google Play. |
| **DEP-10** | Upload ảnh thật (Cloudinary) và GPS tracking thật chưa nằm trong phạm vi prototype. | Mô phỏng chọn ảnh từ danh mục ảnh mẫu và toggle GPS (Granted/Denied/Prompt) trong Demo Panel. | Android Compose sẽ tích hợp Image Picker và FusedLocationProviderClient. |
| **DEP-11** | Bảo mật địa điểm sân trong tin đá lẻ (AC-P05). | Sân bóng được ẩn hoàn toàn khỏi UI và payload phía client của ứng viên chưa được duyệt (trạng thái PENDING); chỉ hiển thị khu vực quận/huyện. Sau khi được APPROVED mới mở địa chỉ chính xác. | Backend bắt buộc phải che trường địa điểm ở cấp độ API response cho ứng viên chưa duyệt. |
