# ArenaFlow — Nguyên Mẫu 16 Màn Hình Chuẩn (Stitch 16710701957767846160)

## Nguồn thiết kế UI Android đã chốt

Theo lựa chọn chính thức của chủ dự án ngày 21/11/2026:
- **[docs/prototype/index.html](index.html) là chuẩn thị giác cho toàn bộ UI Android ArenaFlow.**
- Nguyên mẫu tích hợp đầy đủ **16 màn hình Auth, Hồ sơ & Đội bóng** từ dự án Google Stitch **[16710701957767846160](https://stitch.google.com/u/4/projects/16710701957767846160?pli=1)**.
- 16 màn hình này khớp hoàn toàn 1:1 với 16 ảnh thiết kế tham chiếu tại `docs/prototype/images/` và 16 file HTML nguyên bản tại `docs/prototype/stitch/16710701957767846160/screens/`.

Bảng đăng ký và ánh xạ chi tiết được lưu trữ tại **[docs/prototype/stitch/16710701957767846160/mapping_16_screens.json](stitch/16710701957767846160/mapping_16_screens.json)**.

---

## Danh mục 16 Màn Hình Chuẩn Hóa

| Mã | Tên Màn Hình | Ảnh Tham Chiếu (`images/`) | File Nguồn Stitch (`screens/`) | Mô Tả & Nghiệp Vụ |
| :--- | :--- | :--- | :--- | :--- |
| **S1** | **Đăng nhập** | `login.png` | `9321802030056360865.html` | Hero header xanh đậm `#14532D`, logo ArenaFlow, pitch line SVG, các nút Google, Facebook, Số điện thoại |
| **S2a** | **Đăng nhập SĐT (bước 1)** | `loginbyphone.png` | `2595995208035978610.html` | Nhập số điện thoại, chọn mã vùng, điều khoản sử dụng |
| **S2b** | **Nhập mã OTP (bước 2)** | `nhập mã xác thực .png` | `16400415370137070312.html` | Nhập 6 chữ số OTP, đếm ngược thời gian, gửi lại mã xác thực |
| **S3** | **Hồ sơ cá nhân** | `hồ sơ cá nhân.png` | `6529945131439634299.html` | Thông tin cầu thủ Nguyễn Văn A, vị trí sở trường, điểm uy tín cá nhân, danh sách đội, nút sửa hồ sơ & xóa TK |
| **S4** | **Sửa hồ sơ** | `sửa hồ sơ.png` | `15248344246426653022.html` | Đổi ảnh đại diện, họ tên, số điện thoại, chọn vị trí (Thủ môn/Hậu vệ/Tiền vệ/Tiền đạo), chân thuận, tiểu sử |
| **S5** | **Xóa tài khoản** | `xóa tài khoản.png` | `9103943821149371607.html` | Cảnh báo hệ quả không thể hoàn tác, điều kiện bàn giao quyền đội trưởng trước khi xóa, nút xác nhận xóa |
| **S6** | **Danh sách đội** | `danh-sách đội.png` | `15751446206081935958.html` | Tìm kiếm đội bóng, lời mời gia nhập, danh sách đội tham gia (FC Sao Mai, Cầu Giấy United), nút tạo đội |
| **S7** | **Tạo đội** | `tạo đội.png` | `4941702336417157845.html` | Nhập tên đội, slogan, avatar, khu vực hoạt động, phong cách thi đấu, tiêu chí rèn luyện |
| **S8a** | **Chi tiết đội** | `chi tiết đội.png` | `1225597043044174334.html` | Hồ sơ FC Sao Mai, huy hiệu đội trưởng, tabs thành viên (5 người) và lịch sử đội trưởng, nút mời thành viên |
| **S8b** | **Chi tiết đội đã giải tán** | `chi tiết đội đã giải tán.png` | `15854622351192451723.html` | Đội bóng FC Sấm Sét ở trạng thái đã giải tán, nhật ký ban cán sự và lịch sử lưu trữ chỉ đọc |
| **S9** | **Mời thành viên (Sheet)** | `mời thành viên dạng bottom sheet.png` | `6653989197064971013.html` | Bottom sheet tìm kiếm người dùng theo SĐT/ID, sao chép liên kết mời thành viên vào FC Sao Mai |
| **S10** | **Chuyển quyền đội trưởng** | `chuyển quyền đội trưởng.png` | `3357117809017039066.html` | Danh sách thành viên đội, chọn người kế nhiệm làm đội trưởng, cảnh báo mất quyền quản trị ngay lập tức |
| **S11** | **Rời đội (Sheet)** | `rời đội dạng bottom sheet.png` | `5773400381173959441.html` | Bottom sheet xác nhận rời đội bóng FC Sao Mai, quy định bàn giao trước khi rời |
| **S12** | **Giải tán đội (Sheet)** | `giải tán đội dạng bottom sheet.png` | `16418524991311438402.html` | Bottom sheet cảnh báo đỏ xác nhận giải tán vĩnh viễn FC Sao Mai, chuyển toàn bộ dữ liệu sang lưu trữ |
| **S13** | **Đội trưởng quản lý đội** | `đội trưởng quản lý đội.png` | `10716029106654971628.html` | Màn quản trị đội bóng: danh sách đội quản lý, khoá đội, xem lịch sử thành viên, điều hướng phân quyền |
| **S14** | **Lịch sử thành viên đội** | `đội trưởng lịch sử thành viên đội.png` | `12681439629219777266.html` | Nhật ký thành viên FC Sấm Sét: ngày tham gia, ngày rời đội, vai trò đội trưởng/thành viên |

---

## Hệ Thống Thiết Kế Chuẩn Hóa

- **Typography**: Google Fonts `Be Vietnam Pro` (weights 400, 500, 600, 700).
- **Bộ icon**: Google `Material Symbols Outlined`.
- **Màu sắc chủ đạo**:
  - `Header Deep Green`: `#14532D` (Xanh sân bóng đậm kết hợp họa tiết đường kẻ sân cỏ bóng đá SVG).
  - `primary`: `#3D6943`
  - `primary-container`: `#BDEFBF` / `#2F5B36`
  - `surface` & `background`: `#F8FAF3`
  - `surface-container`: `#EAF0E6`
  - `surface-container-lowest`: `#FFFFFF`
  - `on-surface`: `#2D342C`
  - `outline`: `#757D73`
  - `outline-variant`: `#ACB4A9` / `#E5E7EB`
  - `error`: `#A73B21` / `#6E1400`
- **Khung nhìn chuẩn di động**: Giả lập cố định 390px (Mobile Web 390px target).
- **Họa tiết sân bóng**: SVG kẻ vạch sân thi đấu mờ (`field-lines`) đặc trưng cho nhận diện thương hiệu ArenaFlow.

---

## Trải Nghiệm Tương Tác & Điều Hướng

Mở **[docs/prototype/index.html](index.html)** trong trình duyệt (hoặc Live Server / Open in Browser):

1. **Thanh chuyển màn hình nhanh (Screen Switcher Bar)**:
   - Cố định ở đầu trang, chứa đủ 16 nút tương ứng với 16 màn hình (`S1` đến `S14`).
   - Màn hình đang kích hoạt được đánh dấu nổi bật bằng badge trắng viền xanh và đồng bộ hash URL (`#S1`...`#S14`).
2. **Luồng điều hướng tự nhiên từng bước**:
   - `S1 (Đăng nhập)` ➔ Bấm *"Tiếp tục bằng số điện thoại"* ➔ `S2a`.
   - `S2a (Nhập SĐT)` ➔ Bấm *"Tiếp tục"* ➔ `S2b`.
   - `S2b (Nhập OTP)` ➔ Bấm *"Xác nhận & Đăng nhập"* ➔ `S3 (Hồ sơ cá nhân)`.
   - `S3 (Hồ sơ)` ➔ Bấm *"Sửa hồ sơ"* ➔ `S4`; hoặc bấm *"Xoá tài khoản"* ➔ `S5`; hoặc bấm Menu ➔ `S6 (Danh sách đội)`.
   - `S6 (Danh sách đội)` ➔ Bấm *"+ Tạo đội"* ➔ `S7`; bấm vào thẻ *"FC Sao Mai"* ➔ `S8a (Chi tiết đội)`; bấm vào *"FC Sấm Sét"* ➔ `S8b (Đã giải tán)`.
   - `S8a (Chi tiết đội)` ➔ Khối thao tác nhanh tích hợp mở trực tiếp `S9 (Mời TV)`, `S10 (Chuyển quyền)`, `S11 (Rời đội)`, `S12 (Giải tán)`, `S13 (Quản trị)`.
   - `S13 (Quản trị)` ➔ Bấm *"Lịch sử thành viên"* ➔ `S14`.
   - Mọi màn hình con đều có nút mũi tên quay lại (`arrow_back`) và nút "Huỷ" đưa về màn hình trước đó một cách mượt mà.

---

## Kiểm Thử & Xác Minh Tự Động

File kiểm thử `docs/prototype/verify.cjs` đã được cập nhật kiểm tra tự động toàn diện:
```bash
node docs/prototype/verify.cjs
```

Kết quả:
- **16/16 màn hình** render thành công với đầy đủ mã đánh dấu và nội dung từ Stitch.
- Liên kết font **Be Vietnam Pro**, màu **#14532D**, **Tailwind CSS**, và **Material Symbols Outlined** được kiểm tra nguyên vẹn.
- Thanh chọn 16 màn hình (`btn-tab-S1` đến `btn-tab-S14`) hoạt động trơn tru.
- Toàn bộ luồng chuyển màn hình từng bước được mô phỏng và xác nhận hợp lệ.
