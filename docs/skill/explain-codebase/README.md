# Giải thích codebase — bản dùng chung

Chép **cả thư mục `explain-codebase`** sang dự án hoặc máy khác. Giữ `SKILL.md` và thư mục `references/` cạnh nhau để AI đọc được các hướng dẫn liên quan. Nội dung giải thích viết bằng Markdown, không phụ thuộc API, tài khoản, model hay plugin riêng của Codex.

## Cá nhân hóa theo kinh nghiệm học của bạn

Gói này có [hồ sơ học tập](references/learner-profile.md), cập nhật theo thông tin bạn xác nhận ngày 2026-11-04: còn vướng cú pháp/API/nền tảng; đã tự viết phần lớn Customer/v2/v3 và đúng project `Prodech/adroi/fahion`; hiện follow `android-showcase`. Skill đọc hồ sơ để chọn ví dụ và độ sâu giải thích, không coi mọi kỹ thuật có trong repo là kiến thức bạn đã nắm chắc.

Bạn vẫn chỉ cần yêu cầu giải thích như trước. Khi tiến bộ hoặc thấy cách giảng chưa phù hợp, có thể nói cụ thể, chẳng hạn “lambda tôi đã hiểu, hãy tập trung vào Flow” hoặc “tôi chưa phân biệt truyền callback và gọi hàm”. Phản hồi mới ưu tiên hơn hồ sơ; yêu cầu AI cập nhật hồ sơ nếu muốn lưu cho lần sau.

Các đường dẫn project cũ trong hồ sơ dùng để tham khảo khi truy cập được. Khi chuyển máy hoặc IDE, không cần chép toàn bộ project học cũ. Nếu chia sẻ skill cho người khác, hãy sửa nội dung hồ sơ theo người nhận; nội dung hồ sơ cá nhân nằm trong cả thư mục và file ZIP.

## Cách dùng khi AI trong IDE đọc được file dự án

Đặt thư mục này trong dự án, ví dụ `skills/explain-codebase/`, rồi gửi:

```text
Đọc skills/explain-codebase/SKILL.md và làm theo hướng dẫn trong đó.
Đọc các tài liệu trong references/ được skill chỉ định khi phù hợp với stack.
Sau đó giải thích codebase hiện tại chi tiết cho tôi bằng tiếng Việt.
Tôi biết code cơ bản nhưng chưa biết bắt đầu đọc dự án này từ đâu.
```

Nếu đặt thư mục ở vị trí khác, sửa đường dẫn trong prompt. Khi IDE hỗ trợ đính kèm file vào chat, có thể đính kèm `SKILL.md`; AI vẫn cần truy cập các file trong `references/` và mã nguồn dự án.

AI cần khả năng tìm và đọc code trong workspace để khảo sát. Nếu chỉ có chat không truy cập file, cung cấp nội dung skill, tài liệu stack liên quan và mã nguồn cần giải thích; AI chỉ có thể kết luận trong phạm vi mã được cung cấp.

## Cách để công cụ nhận diện như một skill

Với công cụ hỗ trợ Agent Skills, chép thư mục vào vị trí công cụ đó nhận diện. Các đường dẫn dưới đây tính từ root dự án:

| Công cụ | Đường dẫn tới file chính sau khi chép | Tài liệu chính thức |
| --- | --- | --- |
| Cursor | `.cursor/skills/explain-codebase/SKILL.md` | [Cursor Skills](https://cursor.com/help/customization/skills) |
| GitHub Copilot trong VS Code | `.github/skills/explain-codebase/SKILL.md` | [VS Code Agent Skills](https://code.visualstudio.com/docs/agent-customization/agent-skills) |
| Codex | `.agents/skills/explain-codebase/SKILL.md` | [OpenAI Build skills](https://learn.chatgpt.com/docs/build-skills) |

Vị trí và cách gọi phụ thuộc phiên bản, agent và cấu hình IDE. Chỉ cần chọn vị trí của công cụ đang dùng. Nếu skill chưa được nhận diện, dùng cách yêu cầu AI đọc file trực tiếp ở trên.

`agents/openai.yaml` là metadata tùy chọn cho giao diện Codex. Phần hướng dẫn thực hiện nằm trong `SKILL.md` và `references/`; công cụ khác có thể bỏ qua metadata này. Cú pháp `$explain-codebase` dùng trong prompt mẫu của Codex không phải cú pháp chung cho mọi IDE.

## Các file trong gói

```text
explain-codebase/
├── SKILL.md                 # Quy trình khảo sát và giảng giải
├── README.md                # Hướng dẫn mang sang công cụ khác
├── references/
│   ├── walkthrough.md       # Đi sâu một tính năng, file hoặc hàm
│   ├── learner-profile.md   # Mốc kinh nghiệm và cách giải thích phù hợp
│   ├── kotlin-android.md    # Kotlin, Android, KMP
│   ├── flutter.md           # Dart, Flutter
│   ├── react-native.md      # JavaScript, TypeScript, React Native, Expo
│   └── swift.md             # Swift, SwiftUI, UIKit
└── agents/
    └── openai.yaml          # Metadata tùy chọn cho Codex
```

Stack khác vẫn dùng quy trình chung trong `SKILL.md`. AI chỉ đọc tài liệu stack cần thiết, không cần nạp tất cả file cùng lúc.

## Một số yêu cầu có thể dùng lại

```text
Áp dụng skill ở skills/explain-codebase/SKILL.md để giải thích luồng đăng nhập,
từ lúc người dùng bấm nút đến khi giao diện nhận kết quả.
```

```text
Áp dụng skill ở skills/explain-codebase/SKILL.md để giải thích file đang mở
từng đoạn, gồm nơi gọi, dữ liệu đầu vào, state thay đổi và nơi dùng kết quả.
```

```text
Áp dụng skill ở skills/explain-codebase/SKILL.md để giải thích dự án Flutter này
cho một người đã biết Kotlin. Khi so sánh, làm rõ khác biệt về state và lifecycle.
```
