# ArenaFlow — Prototype Hoàn Chỉnh Bàn Giao UI

Bản prototype tương tác hoàn chỉnh của **ArenaFlow**, được xây dựng dựa trên đặc tả `docs/prototype/PROTOTYPE_HANDOFF.md` và hệ thống thiết kế Stitch nguồn `2789586751825382546`.

Mục tiêu chính: Cung cấp đầy đủ màn hình, sheet/dialog, luồng tương tác hai phía, mọi vai trò người dùng, các trạng thái vòng đời, tình huống lỗi và bộ công cụ kiểm thử trực quan để tham chiếu triển khai trực tiếp sang **Android Jetpack Compose**.

---

## 1. Cách chạy ứng dụng

Prototype được xây dựng bằng **HTML5, CSS Variables và Modular ES6** thuần túy, hoàn toàn không yêu cầu build pipeline (không cần webpack, vite, npm install hay gradle để xem prototype):

### Cách 1: Chạy qua Local HTTP Server (Khuyên dùng)
Từ thư mục gốc dự án:
```bash
# Sử dụng Python
python -m http.server 8000

# Hoặc sử dụng Node.js npx
npx serve docs/prototype/complete -p 8000
```
Sau đó mở trình duyệt tại: `http://localhost:8000/docs/prototype/complete/`

### Cách 2: Mở trực tiếp file `index.html`
- Nhấp đúp mở file `docs/prototype/complete/index.html` trên trình duyệt Chrome, Edge, Safari hoặc Firefox hỗ trợ ES6 Modules.

---

## 2. Cấu trúc thư mục mã nguồn

```
docs/prototype/complete/
├── index.html                 # Entry point chính chứa khung điện thoại & thanh điều hướng
├── README.md                  # Hướng dẫn chạy và tổng quan kiến trúc (file này)
├── decisions.md               # Decision log ghi nhận các quyết định kiến trúc & UX
├── css/
│   ├── tokens.css             # Design tokens: màu sắc, typography Be Vietnam Pro, spacing, bo góc
│   ├── components.css         # UI components chuẩn Compose: Button, Input, Chip, Badge, Card, Sheet
│   └── layout.css             # Khung điện thoại (360-430px), status bar, headers, dockable Demo Panel
└── js/
    ├── fixtures.js            # Dữ liệu mẫu khởi tạo: 8 nhân vật, đội, sân, kèo A/B, đơn, thông báo
    ├── store.js               # Reactive Central Store (window.AppStore) hỗ trợ pub/sub & localStorage
    ├── router.js              # Hash-based Router (window.AppRouter) hỗ trợ deep link, back stack & modal
    ├── demo-panel.js          # Bảng điều khiển Demo Control Center (đổi vai, đồng hồ, GPS, lỗi)
    └── views/
        ├── auth.js            # APP-01..02, AUTH-01..06, PROF-01..06 (Auth & Tài khoản)
        ├── team.js            # TEAM-01..14 (Đội bóng, thành viên, mời, chuyển quyền, giải tán)
        ├── venue.js           # VEN-01..07 (Danh sách, chi tiết, thêm/sửa sân, ảnh, báo cáo sân)
        ├── match.js           # MATCH-01..14 (Tìm kèo, tạo kèo A/B, yêu cầu ghép, hủy kèo)
        ├── pairing.js         # PAIR-01..08 (Thương lượng kèo B, thỏa thuận gói, revision)
        ├── mercenary.js       # MERC-01..12, EXT-01..05 (Tuyển người, ứng tuyển, trận ngoài)
        ├── chat-notif.js      # CHAT-01..05, NOTIF-01..03 (Tin nhắn hội thoại, catalog thông báo)
        ├── reputation.js      # REP-01..10 (Đánh giá sau trận, uy tín, báo cáo vi phạm, khiếu nại)
        └── admin.js           # ADM-01..09 (Khu vực quản trị hệ thống - dành riêng cho ADMIN)
```

---

## 3. Bộ điều khiển Demo (Demo Control Center)

Bảng điều khiển gắn bên phải khung điện thoại (hoặc thu nhỏ thành nút nổi `🛠 Demo Panel` ở góc dưới):

1. **Chuyển nhân vật (Character Switcher):**
   - **User mới:** Chưa hoàn thiện hồ sơ, chưa có đội (thử nghiệm luồng onboarding và form đăng ký).
   - **User cá nhân:** Đá lẻ, có đơn ứng tuyển pending/approved, kiểm tra bảo mật ẩn địa chỉ sân (AC-P05).
   - **Captain Alpha:** Đội trưởng Bình Minh FC (chủ kèo A, quản trị thành viên, giải tán/chuyển quyền).
   - **Captain Beta:** Đội trưởng Sài Gòn United (đối tác nhận kèo A, chủ bài đăng kèo B).
   - **Captain Gamma:** Đội trưởng Thảo Điền FC (đối thủ cạnh tranh ghép kèo B để demo trạng thái SUPERSEDED).
   - **Captain nhiều đội:** Quản lý cùng lúc Rồng Vàng FC và Phượng Hoàng FC; chọn ngữ cảnh đội thao tác.
   - **Member:** Thành viên đội, kiểm tra phân quyền (không có nút quản trị, chỉ có quyền rời đội).
   - **Admin:** Quản trị viên hệ thống (mở khóa cổng quản trị `/admin`, duyệt tranh chấp, kiểm duyệt sân/đội).
2. **Đồng hồ mô phỏng (Mock Clock):**
   - Thiết lập thời gian tương đối: Hiện tại (18/11), Sát giờ đá (-30m), Đang diễn ra, Sau trận (+2h), Hết hạn khiếu nại NOSHOW (>24h), Quá hạn lưu trữ retention (>90 ngày).
3. **Mô phỏng GPS & Vị trí:**
   - Cấp quyền (tìm đá lẻ bán kính 3km) / Từ chối quyền (fallback về khu vực hồ sơ cá nhân).
4. **Giả lập lỗi hệ thống:**
   - Mô phỏng lần thao tác tiếp theo bị lỗi: `403 Forbidden`, `404 Not Found`, `409 Conflict/Revision`, hoặc `Mất kết nối mạng (Offline)`.
5. **Thao tác nhanh phía đối phương (Counter-party Actions):**
   - Mô phỏng đội khác gửi yêu cầu ghép kèo.
   - Mô phỏng đội khác thay đổi gói thương lượng B (tăng revision, xóa xác nhận hai bên).
   - Mô phỏng đội khác xác nhận gói B.
   - Mô phỏng đội thứ ba chốt kèo thành công khiến thương lượng của bạn bị `SUPERSEDED`.
6. **Làm mới dữ liệu (Clean Reset):**
   - Nút `🔄 Reset toàn bộ dữ liệu mẫu` xóa sạch `localStorage` và khôi phục trạng thái ban đầu chỉ với 1 click.

---

## 4. Danh mục tài liệu bàn giao liên kết

- [Screen Inventory](screen-inventory.md): Bảng tra cứu toàn bộ màn hình, sheet, dialog và trạng thái.
- [Flows & State Matrix](flows-and-states.md): Sơ đồ luồng, ma trận trạng thái và quy tắc nút Back.
- [Design System & Tokens](design-system.md): Bảng mã màu, kiểu chữ, khoảng cách, anatomy component.
- [UI Handoff to Compose](ui-handoff.md): Hướng dẫn chi tiết triển khai sang Jetpack Compose.
- [Fixtures & Scenarios](fixtures-and-scenarios.md): Đặc tả cấu trúc dữ liệu mẫu và các kịch bản kiểm thử.
- [API & Dependencies](api-and-dependencies.md): Ánh xạ contract API và ghi chú các phần phụ thuộc mock.
- [Decision Log](decisions.md): Ghi nhận toàn bộ quyết định kiến trúc và lựa chọn UX.
- [Progress Tracking](progress.md): Bảng theo dõi tiến độ từng gói công việc WP-01 đến WP-07.
- [QA Report](qa-report.md): Báo cáo kiểm thử 24 hành trình nghiệm thu J-01..J-24.
