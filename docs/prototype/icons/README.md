# ArenaFlow — SVG đã lọc trùng

Nguồn: 32 màn mobile/desktop của prototype Stitch **2789586751825382546**.

Bộ hiện tại có **44 SVG: 43 icon + 1 họa tiết sân bóng**, giảm từ 108 file. Đã xóa 64 bản trùng hoặc tương đương về ý nghĩa.

## Quy tắc chọn

- Một icon đại diện cho mỗi ý nghĩa, dùng chung mobile và desktop.
- Ưu tiên bản có stroke-linecap/stroke-linejoin bo tròn hoặc hình học bo tròn trong prototype. Không tự vẽ lại icon.
- Khi không có bản bo tròn tương đương, giữ bản nguồn hiện có.
- Gộp outlined/filled; gộp biến thể bóng đá, bolt/flash_on, star/stars, huy chương/cúp/phần thưởng, lịch và điện thoại.
- Giữ riêng các chức năng khác nhau: info, warning, error; shield, shield_check, shield_person; person, person_add, person_off.
- Họa tiết sân bóng dùng một bản desktop đầy đủ làm đại diện.
- Google giữ màu gốc; Facebook giữ bản đơn sắc dùng currentColor.

## Sử dụng

Mở [index.html](index.html) để xem và tìm icon. Tìm bằng tên cũ cũng trả về icon thay thế.
Các file SVG còn lại nằm trong `svg/inline/`, `svg/material/` và `svg/decorations/`.
Một số tên file đại diện còn hậu tố v2 do giữ nguyên đường dẫn của bản được chọn; không còn các bản trùng trong thư mục.

[manifest.json](manifest.json) liệt kê asset hiện tại, các alias và ánh xạ file đã xóa sang file thay thế.
`originalVariants` chỉ lưu metadata nguồn của bộ cũ, không đại diện cho các file còn tồn tại.
HTML/ảnh prototype gốc không thay đổi và vẫn dùng để đối chiếu thiết kế.

Material Symbols dùng giấy phép Apache 2.0 trong [LICENSE-material-symbols.txt](LICENSE-material-symbols.txt).
Logo và SVG nhúng được trích từ prototype, không thuộc giấy phép Material Symbols.

SVG độc lập, không cần Tailwind hay icon font. Spinner là hình tĩnh.
Để dùng trong Android, import SVG cần thiết qua Vector Asset trong Android Studio.
