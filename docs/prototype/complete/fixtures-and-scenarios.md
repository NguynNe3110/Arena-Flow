# ArenaFlow Prototype — Dữ Liệu Mẫu & Kịch Bản Demo (Fixtures & Scenarios)

> Ngày lập: 18/11/2026 · Ngôn ngữ: Tiếng Việt · Phiên bản: 1.0-complete  
> Tài liệu thuộc bộ bàn giao UI Prototype ArenaFlow trong `docs/prototype/complete/`.

---

## 1. Hệ Thống Nhân Vật Demo (Character Personas & Roles)

Bộ dữ liệu mẫu cung cấp 8 nhân vật chuẩn hóa đại diện cho mọi vai trò và tình huống trong đặc tả ArenaFlow:

| STT | Tên Nhân Vật | ID Người Dùng (UUID) | Vai Trò & Thuộc Tính Cốt Lõi | Mục Đích Kiểm Thử Trong Demo |
|---|---|---|---|---|
| 1 | **User Mới** | `u-new-001` | Chưa hoàn thiện hồ sơ (`displayName` rỗng, chưa có vị trí/khu vực), chưa tham gia đội nào. | Kiểm thử Onboarding (APP-02), Đăng nhập SĐT/OTP, Phân luồng User mới bắt buộc sang PROF-02 (AC-P01). |
| 2 | **User Cá Nhân (Đá Lẻ)**| `u-solo-002` | Hồ sơ đầy đủ (Tiền vệ, Hải Châu), Điểm uy tín 100, không làm Captain, có 2 đơn ứng tuyển (1 Pending, 1 Approved). | Kiểm thử Tìm đá lẻ GPS (MERC-01), Xem tin ẩn sân (AC-P05), Rút đơn Approved bị trừ uy tín (AC-P06). |
| 3 | **Captain Alpha** | `u-cap-alpha-003` | Đội trưởng của **FC Alpha** (Sân 7, Bán chuyên). Có 1 kèo A đang OPEN với 2 request đến, 1 kèo B đang thương lượng. | Kiểm thử Duyệt đối thủ kèo A (AC-P03), Thương lượng gói B, Đăng tin tuyển người (MERC-08). |
| 4 | **Captain Beta** | `u-cap-beta-004` | Đội trưởng của **FC Beta** (Đối thủ truyền thống của Alpha). Đã nhận bài B của Alpha và đang ở trong phòng PAIR-03. | Kiểm thử Thương lượng 2 bên, Sửa chi phí làm xóa xác nhận (AC-P04), Đánh giá ẩn sau trận (AC-P07). |
| 5 | **Captain Gamma** | `u-cap-gamma-005` | Đội trưởng của **FC Gamma**. Đang cùng gửi yêu cầu vào Kèo A của Alpha và cũng đang thương lượng Kèo B của Alpha. | Kiểm thử Trạng thái bị thay thế (`SUPERSEDED`) khi Alpha chọn chốt với Beta thay vì Gamma. |
| 6 | **Captain Nhiều Đội**| `u-cap-multi-006` | Đội trưởng của cả **FC Alpha** lẫn **FC Rồng Vàng**. | Kiểm thử Bộ chọn đội đang thao tác (TEAM-14), Chặn tự đánh giá chính mình, Chặn xóa tài khoản (AC-P02). |
| 7 | **Member Thường** | `u-mem-007` | Thành viên chính thức trong đội FC Alpha (không giữ chức vụ quản lý). | Kiểm thử Màn hình thành viên (TEAM-04), Rời đội (TEAM-11), Không thấy nút quản trị đội trưởng. |
| 8 | **Quản Trị Viên (Admin)**| `u-admin-008` | Tài khoản có cờ quyền `role: "ADMIN"`. | Kiểm thử Cổng Admin (ADM-01..09), Giải quyết tranh chấp NO-SHOW (ADM-04/06), Kiểm duyệt sân và bộ môn. |

---

## 2. Dữ Liệu Đặc Biệt & Thực Thể Giới Hạn (Edge-Case Entities)

Nhằm phục vụ kiểm thử toàn diện các tình huống biên và ngoại lệ:

1. **Tài khoản người dùng đã bị xóa (`u-deleted-999`):**
   - Khi đăng nhập vào tài khoản này -> Hiển thị màn hình lỗi `AUTH-05` (Mã lỗi `AUTH_003`), giải thích tài khoản đã đóng vĩnh viễn, không có CTA khôi phục giả.
   - Khi hiển thị trong danh sách thành viên đội thường (`TEAM-06`) -> Tên bị ẩn thành *"Người dùng ArenaFlow"*.
   - Khi hiển thị trong màn hình Admin (`ADM-03`) -> Hiển thị tên thật *"Trần Văn Nam"* kèm cờ đỏ `[TÀI KHOẢN ĐÃ XÓA]`.
2. **Đội bóng đã giải tán (`team-disbanded-888` - FC Cựu Binh):**
   - Trạng thái `DISBANDED`. Mở màn hình `TEAM-13`: Có banner xám thông báo giải tán, toàn bộ nút mời, sửa, tạo kèo bị vô hiệu hóa.
3. **Sân bóng ngừng hoạt động (`ven-inactive-777` - Sân Chuyên Việt Chi Nhánh 2):**
   - Trạng thái `INACTIVE`. Mở màn hình `VEN-07`: Có banner vàng cảnh báo; trong bộ chọn sân `VEN-01` và `PAIR-04` sân này bị làm mờ (disabled) và không thể bấm chọn.
4. **Đội bóng mới dưới 5 trận (`team-fresh-555`):**
   - Mới hoàn thành 3 trận -> Thẻ uy tín `REP-04` chỉ hiển thị Điểm uy tín (4.8 sao), **ẩn hoàn toàn Tỷ lệ hoàn thành trận (Completion Rate)** theo đúng quy định spec.

---

## 3. Hệ Thống Đồng Hồ Mô Phỏng (Demo Mock Clock)

Thời gian trong bản prototype được thiết kế tương đối dựa trên một mốc chuẩn `T0` (thời điểm người dùng mở ứng dụng). Bảng điều khiển Demo Panel cho phép dịch chuyển đồng hồ qua 8 trạng thái thời gian chính:

```
[Mốc chuẩn T0: 14:00 Hôm nay] ----------------------------------------------------->
    |---> Clock 1: T0          (Hiện tại: Kèo OPEN, đang tuyển người, đang thương lượng)
    |---> Clock 2: T0 + 2h     (Hết hạn bài đăng: Các kèo OPEN quá hạn tự chuyển EXPIRED)
    |---> Clock 3: T0 + 4h     (18:00: 1 giờ trước trận đấu 19:00 - Cảnh báo phạt khi hủy)
    |---> Clock 4: T0 + 5h     (19:00: Giờ bóng lăn - Khóa hoàn toàn nút hủy và sửa form)
    |---> Clock 5: T0 + 7h     (21:00: Hết trận - Kèo chuyển COMPLETED, mở đánh giá REP-01)
    |---> Clock 6: T0 + 31h    (Quá 24 giờ sau trận - Hết hạn khiếu nại tranh chấp NO-SHOW)
    |---> Clock 7: T0 + 31 ngày (Quá 30 ngày - Hết hạn lưu trữ thông báo NOTIF-01)
    |---> Clock 8: T0 + 91 ngày (Quá 90 ngày - Hết hạn lưu trữ tin nhắn CHAT-04: Read-only)
```

---

## 4. Mô Phỏng Quyền GPS & Bộ Kích Hoạt Lỗi Hệ Thống (Simulators)

### 4.1. Bộ Điều Khiển Vị Trí & GPS (Location Simulator)
- **Cấp quyền thành công (GPS Granted):** Tọa độ mô phỏng tại Trung tâm Thể thao Đà Nẵng (16.0544° N, 108.2022° E). Thanh trượt bán kính từ 1 đến 20 km (mặc định 3 km).
- **Từ chối quyền (GPS Denied):** Hệ thống tự động kích hoạt cơ chế fallback, sử dụng Phường/Quận đã khai báo trong Hồ sơ cá nhân (`PROF-02`).
- **Chưa có hồ sơ:** Hiển thị banner nhắc nhở hoàn thiện hồ sơ kèm nút dẫn tới `PROF-02`.

### 4.2. Bộ Kích Hoạt Lỗi Máy Chủ & Mạng (Error Simulator)
Trong Demo Panel có công tắc kích hoạt mã lỗi cho hành động tiếp theo:
- `200 OK`: Hoạt động bình thường.
- `400 Bad Request`: Kích hoạt lỗi kiểm tra tính hợp lệ dữ liệu (Validation errors hiển thị dưới từng field).
- `401 Unauthorized`: Kích hoạt phiên hết hạn, tự động bật Sheet cảnh báo `AUTH-06`.
- `403 Forbidden`: Giả lập hành vi mất quyền (Ví dụ: Member cố bấm nút của Captain).
- `404 Not Found`: Đối tượng đã bị xóa, kích hoạt luồng xử lý an toàn `NOTIF-02`.
- `409 Conflict`: Kích hoạt xung đột phiên bản gói thương lượng `PAIR-07` (Revision mismatch).
- `Network Timeout / Offline`: Kích hoạt trạng thái mất kết nối mạng kèm nút "Thử lại".

---

## 5. Danh Mục 10 Kịch Bản Demo Chuẩn Hóa (Scenario Presets SC-01..SC-10)

| Mã | Tên Kịch Bản | Nhân Vật Khởi Tạo | Mô Tả Tình Huống Cần Kiểm Thử | Hành Trình Bao Phủ |
|---|---|---|---|---|
| **SC-01** | Khởi đầu & Đăng ký lần đầu | User Mới (`u-new-001`) | Trải nghiệm Onboarding, đăng nhập bằng OTP và hoàn thiện hồ sơ bắt buộc theo AC-P01. | J-01, J-02 |
| **SC-02** | Quản trị Đội & Chặn xóa Captain | Captain Alpha (`u-cap-alpha-003`) | Xem đội, mời thành viên mới, thử xóa tài khoản bị chặn (AC-P02), chuyển quyền tức thì. | J-04, J-05, J-06 |
| **SC-03** | Duyệt kèo A & Thay thế đối thủ | Captain Alpha (`u-cap-alpha-003`) | Duyệt 1 yêu cầu của Beta; kiểm tra kèo Confirmed và yêu cầu của Gamma thành Superseded (AC-P03).| J-07, J-08 |
| **SC-04** | Kèo B: Sửa gói & Xung đột Revision | Captain Alpha & Captain Beta | Đổi qua lại 2 bên trong phòng PAIR-03: Sửa chi phí làm xóa xác nhận (AC-P04), hiện cảnh báo PAIR-07. | J-09, J-10 |
| **SC-05** | Đá lẻ: Ẩn sân & Rút Approved | User Cá Nhân (`u-solo-002`) | Xem tin tuyển (ẩn sân AC-P05), Captain duyệt đơn (hiện sân), rút đơn bị phạt 10 điểm (AC-P06). | J-12, J-13, J-14 |
| **SC-06** | Chọn Sân bóng & Bảo lưu Form | Captain Alpha (`u-cap-alpha-003`) | Nhập form tạo kèo dở dang, nhảy sang VEN-01 tìm sân, quay lại giữ 100% dữ liệu đã nhập. | J-16 |
| **SC-07** | Chat chỉ đọc & 35 Loại Thông Báo | User Cá Nhân (`u-solo-002`) | Xem phòng chat hết hạn (canSend=false), mở catalog 35 notification và xử lý link hỏng (AC-P09). | J-17, J-18 |
| **SC-08** | Đánh giá ẩn & Tranh chấp 24h | Captain Alpha & Captain Beta | Trận đấu kết thúc: Đánh giá ẩn (AC-P07), Alpha tố cáo NO-SHOW, Beta khiếu nại trong 24h (AC-P08). | J-19, J-20, J-21 |
| **SC-09** | Quản trị viên xử lý Khiếu nại | Admin (`u-admin-008`) | Vào cổng ADM-01, mở hàng đợi tranh chấp, nhập quyết định (bắt buộc 1-500 ký tự) để giải quyết. | J-22 |
| **SC-10** | Đa kích thước & Khôi phục gốc | Tất cả nhân vật | Kiểm tra co giãn 360/390/430px, xoay ngang dọc và bấm nút Khôi phục dữ liệu gốc (Reset Store). | J-23, J-24 |

---

## 6. Cơ Chế Lưu Trữ & Khôi Phục Dữ Liệu (Store & Reset Mechanism)

### 6.1. Cấu Trúc Central Store (`window.AppStore`)
Ứng dụng sử dụng mô hình Central Reactive Store được lưu trữ đồng bộ vào trình duyệt:
- Khóa lưu trữ cục bộ: `localStorage.getItem("arenaflow_prototype_store_v1")`.
- Cấu trúc dữ liệu bao gồm các bảng quan hệ: `users`, `teams`, `memberships`, `matches`, `matchRequests`, `pairings`, `mercenaryPosts`, `applications`, `venues`, `chats`, `notifications`, `reports`, `disputes`.
- Cơ chế Pub-Sub (Publish-Subscribe): Mọi hành động cập nhật state đều phát sự kiện `notifyListeners()`, tự động render lại toàn bộ các view đang hiển thị mà không cần tải lại trang.

### 6.2. Cơ Chế Reset Dữ Liệu Hai Cấp Độ
Trong Demo Panel cung cấp 2 nút khôi phục:
1. **Reset Kịch Bản Hiện Tại (Reset Scenario):** Đưa dữ liệu của riêng kịch bản đang chọn (ví dụ: reset phòng thương lượng PAIR-03 về revision 1) mà không làm mất các thay đổi ở các module khác.
2. **Khôi Phục Gốc Toàn Bộ (Full System Reset):** Xóa trắng `localStorage` và nạp lại toàn bộ bộ hạt giống `seedFixtures` ban đầu. Đưa hệ thống về trạng thái nguyên bản 100%.
