# ArenaFlow Prototype — Hệ Thống Thiết Kế & Hướng Dẫn Bàn Giao UI (Design System)

> Ngày lập: 18/11/2026 · Ngôn ngữ: Tiếng Việt · Phiên bản: 1.0-complete  
> Trích xuất trực tiếp từ dự án Stitch nguồn: **ID 2789586751825382546** (ArenaFlow)  
> Đích triển khai thực tế: **Android Jetpack Compose (Material 3)**.

---

## 1. Triết Lý Thiết Kế & Nguyên Tắc Cốt Lõi

1. **Tinh thần Thể thao & Năng động:** Ngôn ngữ thiết kế lấy cảm hứng từ mặt cỏ sân bóng tự nhiên kết hợp phong cách hiện đại, sử dụng tone xanh lá sẫm tự nhiên (`#3d6943`) làm chủ đạo và xanh cỏ tươi (`#16A34A`) làm điểm nhấn hành động.
2. **Minh bạch & Dễ nắm bắt:** Mọi thông tin về chi phí, sân đấu, thời gian, uy tín đối thủ đều được trực quan hóa qua thẻ (Cards) và huy hiệu trạng thái (Status Badges).
3. **Thân thiện ngoài trời (High Contrast Outdoor Usability):** Độ tương phản văn bản đạt chuẩn WCAG AA trên nền sáng (`#f8faf3` và `#ffffff`), hạn chế bóng đổ mờ nhạt, ưu tiên đường viền phân tách rõ ràng (`#acb4a9`).
4. **Tiêu chuẩn thao tác ngón tay cái (Touch Target >= 48dp):** Toàn bộ các vùng chạm (Buttons, Chips, List items, Icon buttons) trên Android đều phải có kích thước tối thiểu **48 x 48 dp** theo chuẩn tiếp cận Material Design 3.

---

## 2. Hệ Thống Kiểu Chữ (Typography System)

ArenaFlow sử dụng duy nhất font chữ **Be Vietnam Pro** cho toàn bộ ứng dụng nhằm đảm bảo khả năng hiển thị tiếng Việt hoàn hảo, không lỗi dấu, cân đối ở mọi kích thước màn hình.

### 2.1. Bảng Thông Số Typography & Ánh Xạ Jetpack Compose

| Tên Style Material 3 | Font Weight | Kích thước (sp) | Chiều cao dòng (Line Height) | Letter Spacing | Ánh xạ Jetpack Compose `TextStyle` | Ứng dụng thực tế |
|---|---|---|---|---|---|---|
| **Display Large** | Bold (700) | 32 sp | 40 sp | -0.25 sp | `MaterialTheme.typography.displayLarge` | Tiêu đề lớn màn hình Onboarding (APP-02) |
| **Headline Large** | Bold (700) | 24 sp | 32 sp | 0 sp | `MaterialTheme.typography.headlineLarge` | Tiêu đề các trang chính (Auth, Chi tiết Kèo) |
| **Headline Medium**| SemiBold (600)| 20 sp | 28 sp | 0 sp | `MaterialTheme.typography.headlineMedium`| Tiêu đề nhóm nội dung, Tiêu đề Bottom Sheet |
| **Title Large** | SemiBold (600)| 18 sp | 24 sp | 0 sp | `MaterialTheme.typography.titleLarge` | Tên Đội bóng, Tên Sân bóng, Tên đối thủ |
| **Title Medium** | Medium (500) | 16 sp | 24 sp | +0.15 sp | `MaterialTheme.typography.titleMedium` | Tiêu đề thẻ Card, Tên vị trí tuyển |
| **Body Large** | Regular (400) | 16 sp | 24 sp | +0.5 sp | `MaterialTheme.typography.bodyLarge` | Văn bản nội dung form, mô tả chi tiết, tin nhắn |
| **Body Medium** | Regular (400) | 14 sp | 20 sp | +0.25 sp | `MaterialTheme.typography.bodyMedium` | Nhãn form (Form labels), Nội dung thông báo |
| **Body Small** | Regular (400) | 12 sp | 16 sp | +0.4 sp | `MaterialTheme.typography.bodySmall` | Thời gian gửi, Ghi chú phụ, Chú thích cọc |
| **Label Large** | SemiBold (600)| 14 sp | 20 sp | +0.1 sp | `MaterialTheme.typography.labelLarge` | Chữ trên nút bấm (Primary/Secondary Buttons) |
| **Label Medium** | Medium (500) | 12 sp | 16 sp | +0.5 sp | `MaterialTheme.typography.labelMedium` | Chữ trên Status Chips, Filter Pills, Tab items |
| **Label Small** | Medium (500) | 11 sp | 16 sp | +0.5 sp | `MaterialTheme.typography.labelSmall` | Badge số lượng tin nhắn, Huy hiệu cấp độ |

---

## 3. Hệ Thống Màu Sắc Trích Xuất Từ Stitch (Color Tokens)

Trích xuất trực tiếp từ `manifest.json` của Stitch Project `2789586751825382546`:

### 3.1. Bảng Token Màu Chi Tiết (Material 3 Tonal Spot)

| Tên Token (CSS / Compose) | Mã Hex | Ý nghĩa sử dụng trong giao diện | Thành phần áp dụng |
|---|---|---|---|
| `primary` | `#3d6943` | Màu thương hiệu chủ đạo (Forest Green) | Nút bấm chính (CTA), Header active, Icon active |
| `primary_dim` | `#315c38` | Màu nhấn tối hơn của primary | Trạng thái Pressed / Hover của Primary button |
| `primary_container` | `#bdefbf` | Vùng chứa màu thương hiệu dịu nhẹ | Nền chip đang chọn, Nền tin nhắn của tôi, Badge active |
| `on_primary` | `#e9ffe6` | Màu chữ/icon trên nền primary | Chữ trên nút bấm chính, Icon trên nền xanh đậm |
| `on_primary_container` | `#2f5b36` | Màu chữ trên nền primary_container | Chữ trong chip đã chọn, Icon trạng thái thành công |
| `custom_brand_accent` | `#16A34A` | Màu xanh lá tươi bóng đá phong trào | Điểm uy tín cao, Sao đánh giá tích cực, Live badge |
| `brand_dark` | `#15803d` | Màu xanh nhấn bóng đá đậm | Viền thẻ đang active, Nút bắt đầu ngay |
| `brand_light` | `#dcfce7` | Màu nền xanh tươi rất nhạt | Nền badge Đã chốt kèo, Đã duyệt đơn |
| `surface` | `#f8faf3` | Màu nền màn hình chung (Light Green-White)| Nền toàn bộ Scaffold màn hình di động |
| `surface_container_lowest`| `#ffffff`| Màu nền trắng tinh khiết | Nền thẻ Card nội dung, Nền Modal Bottom Sheet |
| `surface_container_low` | `#f1f5ec` | Màu nền xám xanh nhạt cấp 1 | Nền ô input, Nền container phụ, Nền chip chưa chọn |
| `surface_container` | `#eaf0e6` | Màu nền container cấp 2 | Nền thanh TopAppBar, Nền thanh BottomBar |
| `surface_container_high`| `#e4eae0` | Màu nền container cấp 3 | Nền trạng thái Hover, Nền Divider dày |
| `on_surface` | `#2d342c` | Màu chữ văn bản chính (Đen lục đậm) | Tiêu đề màn hình, Văn bản chính, Tên người dùng |
| `on_surface_variant` | `#596158` | Màu chữ văn bản phụ (Xám lục) | Nhãn phụ, Thời gian, Placeholder input |
| `outline` | `#757d73` | Đường viền các thành phần chính | Viền nút Outlined, Viền ô Input khi chưa focus |
| `outline_variant` | `#acb4a9` | Đường viền phân cách mờ | Đường Divider giữa các danh sách, Viền thẻ Card |
| `error` | `#a73b21` | Màu cảnh báo lỗi & hành động hủy | Viền báo lỗi input, Nút Hủy kèo, Rời đội, Báo cáo |
| `error_container` | `#fd795a` | Vùng chứa cảnh báo lỗi | Nền cảnh báo lỗi khẩn cấp, Banner kỷ luật |
| `on_error` | `#fff7f6` | Chữ trên nền màu lỗi | Chữ nút Xóa tài khoản, Giải tán đội |
| `on_error_container` | `#6e1400` | Chữ trên nền error_container | Chữ cảnh báo phạt uy tín khi hủy kèo |
| `secondary` | `#526451` | Màu phụ trung tính (Sage Green) | Icon phụ, Chữ bổ sung thông tin |
| `secondary_container` | `#d4e8d1` | Vùng chứa phụ | Nền badge vai trò Thành viên, Bộ lọc thứ cấp |
| `tertiary` | `#5c6330` | Màu bổ sung (Olive Green) | Điểm nhấn thông tin tỷ số, Chi phí kèo đấu |
| `tertiary_container` | `#f4fcba` | Nền vùng chứa bổ sung vàng lục nhạt | Nền badge trạng thái Đang thương lượng |

### 3.2. Mã Nguồn Cấu Hình Compose ColorScheme

```kotlin
val ArenaFlowLightColorScheme = lightColorScheme(
    primary = Color(0xFF3D6943),
    onPrimary = Color(0xFFE9FFE6),
    primaryContainer = Color(0xFFBDEFBF),
    onPrimaryContainer = Color(0xFF2F5B36),
    secondary = Color(0xFF526451),
    onSecondary = Color(0xFFEAFFE7),
    secondaryContainer = Color(0xFFD4E8D1),
    onSecondaryContainer = Color(0xFF445645),
    tertiary = Color(0xFF5C6330),
    onTertiary = Color(0xFFF6FFBC),
    tertiaryContainer = Color(0xFFF4FCBA),
    onTertiaryContainer = Color(0xFF5A612E),
    error = Color(0xFFA73B21),
    onError = Color(0xFFFFF7F6),
    errorContainer = Color(0xFFFD795A),
    onErrorContainer = Color(0xFF6E1400),
    background = Color(0xFFF8FAF3),
    onBackground = Color(0xFF2D342C),
    surface = Color(0xFFF8FAF3),
    onSurface = Color(0xFF2D342C),
    surfaceVariant = Color(0xFFDDE5D9),
    onSurfaceVariant = Color(0xFF596158),
    outline = Color(0xFF757D73),
    outlineVariant = Color(0xFFACB4A9)
)
```

---

## 4. Hệ Thống Khoảng Cách & Bo Góc (Spacing & Elevation)

### 4.1. Thang Đo Khoảng Cách (Spacing Scale - Bội số của 4dp)

- `space_4` (4dp): Khoảng cách giữa icon và text trong badge/chip.
- `space_8` (8dp): Khoảng cách giữa các chip, padding trong của input nhỏ.
- `space_12` (12dp): Khoảng cách giữa các dòng trong thẻ card, margin item nhỏ.
- `space_16` (16dp): **Khoảng cách tiêu chuẩn (Standard Page Margin)** cách mép màn hình.
- `space_20` (20dp): Khoảng cách giữa các khối nội dung form.
- `space_24` (24dp): Khoảng cách giữa các section lớn trong trang.
- `space_32` (32dp): Khoảng cách tiêu đề trang với nội dung phía dưới.
- `space_48` (48dp): Chiều cao tối thiểu vùng chạm ngón tay.

### 4.2. Độ Bo Góc (Corner Radii - Chuẩn Stitch `ROUND_EIGHT`)

- **Bán kính mặc định (`RoundedCornerShape(8.dp)`):** Áp dụng cho Buttons, Input fields, Badges, Chips.
- **Bán kính thẻ nội dung (`RoundedCornerShape(12.dp)`):** Áp dụng cho Match Card, Team Card, Player Card.
- **Bán kính Bottom Sheet (`RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)`):** Áp dụng cho mọi Modal Bottom Sheet.
- **Bán kính tròn tuyệt đối (`CircleShape` / 9999dp):** Áp dụng cho Avatar cầu thủ/đội bóng, Nút điều hướng vòng tròn (Icon button tròn).

---

## 5. Quy Chuẩn Vùng Chạm & Khả Năng Tiếp Cận (Accessibility & Touch Targets >= 48dp)

Theo yêu cầu nghiêm ngặt của dự án tại Mục 9 và Mục 14:
1. **Quy tắc 48dp bất biến:** Mọi nút bấm, icon button, tab bar item, checkbox, radio và chip đều phải đảm bảo kích thước khung chạm (touch target) tối thiểu **48 x 48 dp**.
   - Trường hợp visual chip nhỏ hơn (ví dụ cao 32dp), bắt buộc phải bọc trong vùng mở rộng:
     ```kotlin
     Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
     ```
2. **Không phụ thuộc đơn thuần vào màu sắc:**
   - Trạng thái Lỗi / Thành công / Cảnh báo phải luôn đi kèm **Icon nhận diện** và **Văn bản giải thích** (Text Label).
3. **Độ tương phản chuẩn ngoài trời:**
   - Chữ chính (`#2d342c`) trên nền surface (`#f8faf3`): Tỷ lệ tương phản **11.4:1** (vượt chuẩn AAA 7:1).
   - Nút chính (`#3d6943`) với chữ trắng/xanh nhạt (`#e9ffe6`): Tỷ lệ tương phản **5.8:1** (đạt chuẩn AA).
4. **Hỗ trợ Screen Reader & TalkBack:**
   - Mọi `IconButton` đều phải cung cấp tham số `contentDescription` rõ nghĩa bằng tiếng Việt (ví dụ: `"Đóng hộp thoại"`, `"Quay lại trang trước"`, `"Gửi tin nhắn"`).

---

## 6. Đặc Tả Chi Tiết Thành Phần Giao Diện (Component Anatomy)

### 6.1. Nút Bấm (Buttons)

Mọi Button đều có chiều cao chuẩn **48dp**, bo góc **8dp**, phông chữ `Be Vietnam Pro SemiBold 14sp`.

1. **Primary Button (Nút chính - CTA):**
   - *Mục đích:* Dùng cho hành động quan trọng nhất trên màn hình ("Đăng nhập", "Tạo bài đăng", "Xác nhận gói", "Duyệt ứng viên").
   - *Nền:* `#3d6943` | *Chữ:* `#e9ffe6`.
   - *Trạng thái:*
     - Default: Nền `#3d6943`.
     - Pressed: Nền `#315c38`.
     - Disabled: Nền `#dde5d9`, chữ `#757d73`.
     - Loading: Hiện `CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White)`.
2. **Secondary / Outlined Button (Nút phụ):**
   - *Mục đích:* "Lưu nháp", "Sửa bài", "Xem trước", "Hủy bỏ".
   - *Nền:* Trong suốt hoặc `#f1f5ec` | *Viền:* 1dp `#757d73` | *Chữ:* `#2d342c`.
3. **Destructive Button (Nút có hậu quả nghiêm trọng):**
   - *Mục đích:* "Hủy kèo", "Rời đội", "Giải tán đội", "Xóa tài khoản".
   - *Nền:* `#a73b21` hoặc nền nhạt `#fff5f3` viền đỏ | *Chữ:* `#ffffff` hoặc `#a73b21`.

### 6.2. Ô Nhập Liệu & Biểu Mẫu (Input Fields & Forms)

- **Chiều cao tiêu chuẩn:** 52dp (đảm bảo touch target >= 48dp).
- **Bo góc:** 8dp (`ROUND_EIGHT`).
- **Nền:** `#ffffff` hoặc `#f1f5ec`.
- **Đường viền:**
  - Chưa chọn (Default): 1dp `#acb4a9`.
  - Đang nhập (Focused): 2dp `#3d6943`.
  - Báo lỗi (Error): 2dp `#a73b21`.
- **Thông báo lỗi (Error message):** Hiển thị ngay sát mép dưới ô nhập liệu, màu đỏ `#a73b21`, font `BodySmall 12sp`, kèm icon cảnh báo nhỏ.
- **Xử lý bàn phím ảo (IME Handling):** Khi mở bàn phím, giao diện tự động cuộn đến trường đang nhập liệu (`Modifier.imePadding().bringIntoViewRequester()`).

### 6.3. Thẻ Nội Dung (Cards)

1. **Match Card (Thẻ Kèo Đấu - MATCH-01):**
   - Nền trắng `#ffffff`, viền 1dp `#dde5d9`, bo góc 12dp, padding 16dp.
   - Header: Tên đội chủ kèo, Logo nhỏ (36dp), Badge trạng thái (OPEN/CONFIRMED).
   - Body: Icon lịch + Giờ bắt đầu, Icon ghim + Tên sân/khu vực, Tag định dạng sân (Sân 7, Bán chuyên).
   - Footer: Thỏa thuận chi phí (VD: "Chia đôi tiền sân"), nút hành động "Bắt kèo".
2. **Mercenary Post Card (Thẻ Tuyển Đá Lẻ - MERC-01):**
   - Header: Vị trí tuyển (GK/DF/MF/FW) với màu chip riêng biệt, số lượng cần tuyển.
   - Body: Giờ thi đấu, Khoảng cách GPS (VD: "Cách bạn 2.4 km") hoặc Quận/Huyện. Tuyệt đối **không hiện tên sân** khi chưa duyệt (AC-P05).
   - Footer: Điểm uy tín đội tuyển, nút "Nộp đơn".

### 6.4. Huy Hiệu Trạng Thái & Bộ Lọc (Badges & Chips)

| Trạng thái | Nền (Background) | Chữ & Viền (Text & Border) | Icon đi kèm |
|---|---|---|---|
| **OPEN (Đang mở tìm đối)** | `#dcfce7` (Xanh tươi nhạt) | `#15803d` (Xanh tươi đậm) | Chấm tròn xanh lá `●` |
| **CONFIRMED (Đã chốt kèo)**| `#bdefbf` (Xanh lá container)| `#2f5b36` (Xanh lục sẫm) | Dấu tích hoàn tất `✓` |
| **PENDING (Chờ duyệt)** | `#fef3c7` (Vàng hổ phách nhạt)| `#b45309` (Nâu vàng) | Đồng hồ cát `⏳` |
| **REJECTED (Từ chối)** | `#fee2e2` (Đỏ nhạt) | `#b91c1c` (Đỏ sẫm) | Dấu chéo `✕` |
| **SUPERSEDED (Bị thay thế)**| `#f3f4f6` (Xám nhạt) | `#6b7280` (Xám tro) | Mũi tên chuyển hướng `⤳` |
| **EXPIRED (Quá hạn)** | `#e5e7eb` (Xám viền) | `#4b5563` (Xám đậm) | Đồng hồ báo thức `⏰` |

### 6.5. Modal Bottom Sheet & Dialogs

- **Cấu trúc Bottom Sheet:**
  - Thanh kéo trên cùng (Drag handle): Rộng 36dp, cao 4dp, bo tròn, màu `#acb4a9`.
  - Header: Tiêu đề Sheet (`TitleLarge 18sp Bold`), nút đóng `✕` (touch target 48dp).
  - Nội dung cuộn: Tự co giãn theo nội dung, giới hạn tối đa 85% chiều cao màn hình.
  - Vùng hành động chân sheet (Action Footer): Cố định dưới đáy với safe area padding, gồm 2 nút (Nút chính và Nút hủy).
- **Hành vi phím Back:** Nhấn Back luôn ưu tiên đóng Sheet/Dialog hiện tại trước khi thoát màn hình.

---

## 7. Bảng Ánh Xạ Biểu Tượng Hệ Thống (Iconography Mapping)

Dựa trên bộ Material Symbols và thư viện icon cục bộ:

| Khái niệm giao diện | Tên Material Symbol | Ý nghĩa chức năng |
|---|---|---|
| Kèo đấu bóng đá | `sports_soccer` | Tab Tìm kèo, Thể thao bóng đá |
| Người đá lẻ | `person_search` | Tab Đá lẻ, Tìm kiếm cầu thủ |
| Hộp thư tin nhắn | `chat_bubble_outline` | Tab Tin nhắn, Mở chat giao lưu |
| Đội bóng của tôi | `groups` | Tab Đội bóng, Danh sách thành viên |
| Hồ sơ cá nhân | `account_circle` | Màn hình Profile, Avatar mặc định |
| Địa điểm sân bóng | `location_on` | Chọn sân, Bản đồ, Địa chỉ |
| Thời gian thi đấu | `schedule` | Khung giờ đá, Đếm ngược hết hạn |
| Trọng tài / Thẻ phạt | `sports` | Báo cáo vi phạm, Tranh chấp |
| Uy tín sao vàng | `star` | Chấm sao đánh giá trận đấu (1-5 sao) |
| Cảnh báo vi phạm | `warning` | Báo cáo NO-SHOW, Cảnh báo hủy kèo |
| Cài đặt quản trị | `admin_panel_settings` | Khu vực Admin Portal (ADM-01) |
| Chuyển giao quyền lực | `swap_horiz` | Chuyển quyền Captain (TEAM-09) |
