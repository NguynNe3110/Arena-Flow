# Nguồn GitHub được tích hợp và tra cứu

Khảo sát ngày 2026-11-12. Phân biệt phương pháp đã tích hợp vào gói này với skill kỹ thuật chỉ được liên kết để tra theo nhu cầu. Không cài các skill tutor gốc thành các skill hoạt động song song; `kotlin-mentor` là nơi điều phối. Không sao chép nguyên văn SKILL.md hoặc chạy script từ các repo. Nội dung mới là bản tổng hợp riêng theo hồ sơ người học, các nguồn dưới đây được ghi để kiểm tra xuất xứ và quyết định điều chỉnh.

## Phương pháp dạy đã tích hợp

### Jane Street — socratic-tutor

- Repo: https://github.com/janestreet/agent_skills
- File đã đọc: https://github.com/janestreet/agent_skills/blob/7c6715c29548a3b39d4a9545c698ee4fd17a7213/socratic-tutor/SKILL.md
- Commit khảo sát: `7c6715c29548a3b39d4a9545c698ee4fd17a7213` (nhánh master).
- License: MIT, Jane Street Group, LLC; https://github.com/janestreet/agent_skills/blob/7c6715c29548a3b39d4a9545c698ee4fd17a7213/LICENSE.md
- Giá trị chọn dùng: câu hỏi suy luận theo khái niệm, thử biến thể, khả năng tự nhớ ở một lần khác và ôn lại kiến thức cũ.
- Điều chỉnh: không bắt chẩn đoán xong mới được giảng; không dùng thang 0–5/phần trăm tổng; không giữ đáp án khi người học cần giải thích trực tiếp; không mặc định gây áp lực hoặc đặt kỳ thi cuối khóa. Hai tình huống độc lập là tiêu chí ghi tự lực ổn định, không là cổng cấm học tiếp.
- Tích hợp tại: [teaching-playbook.md](teaching-playbook.md), hướng dẫn bằng chứng trong SKILL.md và progress.md.

### Kevin Perez — TuTor

- Repo: https://github.com/kevinnio/tutor
- File đã đọc: https://github.com/kevinnio/tutor/blob/f0ad1ee514a034b720a4af4a36b5767f473a5994/SKILL.md
- Commit khảo sát: `f0ad1ee514a034b720a4af4a36b5767f473a5994` (master), metadata skill ghi version 0.6.
- License: MIT; https://github.com/kevinnio/tutor/blob/f0ad1ee514a034b720a4af4a36b5767f473a5994/LICENSE
- Giá trị chọn dùng: bước thực hành rõ, tiêu chí thành công, kiểm tra bài làm thay vì chỉ nghe “done”, giải thích lệnh và cờ chưa quen.
- Điều chỉnh: không cấm code block, không buộc AI từ chối yêu cầu viết giúp, không áp danh sách tool dành riêng cho agent khác. Nếu chỉ có tự báo thì lưu tự báo, không giữ người học ở một bước vô thời hạn. Giữ quyền nhận ví dụ mẫu/đáp án và quyền đổi mục tiêu hiện tại.
- Tích hợp tại: [teaching-playbook.md](teaching-playbook.md), bài mobile chia theo hành vi trong [mobile-practice.md](mobile-practice.md).

## Nguồn kỹ thuật mobile đã xem

### Android — android/skills

- Repo chính thức: https://github.com/android/skills
- Commit khảo sát: `5815d27fda564aa9f07ebdcb5e57ed3e8d904e0d` (main).
- Đã đọc trực tiếp hai skill:
  - Testing: https://github.com/android/skills/blob/5815d27fda564aa9f07ebdcb5e57ed3e8d904e0d/testing/testing-setup/SKILL.md
  - Compose performance: https://github.com/android/skills/blob/5815d27fda564aa9f07ebdcb5e57ed3e8d904e0d/jetpack-compose/jetpack-compose-performance/SKILL.md
- License: Apache-2.0; https://github.com/android/skills/blob/5815d27fda564aa9f07ebdcb5e57ed3e8d904e0d/LICENSE.txt
- Vai trò trong mentor: bản đồ chọn đúng tác vụ và cách biến một vấn đề thành bài học có bằng chứng. Chưa đọc hết các reference mà hai skill này liên kết; phải đọc reference liên quan trước khi áp một phát hiện/fix cụ thể. Không nhập toàn bộ quy tắc/version hoặc tự động hóa cài đặt của upstream vào khóa học.
- Navigation và adaptive chỉ được xem ở mức danh mục, chưa được thẩm định sâu. Các kỹ thuật này cần đúng phiên bản API, nhu cầu sản phẩm và quyền sửa project.

### Kotlin — kotlin-agent-skills

- Repo chính thức: https://github.com/Kotlin/kotlin-agent-skills
- Commit khảo sát: `087f5689c1934135ed58718cdf524525e452b7e9` (main).
- Đã xem README và danh mục SKILL.md, chưa đọc sâu từng skill. Nội dung hiện tại có nhiều tác vụ tooling, migration và backend; không được mô tả như khóa mentor Android có sẵn.
- License: Apache-2.0; https://github.com/Kotlin/kotlin-agent-skills/blob/087f5689c1934135ed58718cdf524525e452b7e9/LICENSE
- Vai trò: điểm tra cứu khi build/toolchain thật sự chặn việc học hoặc khi được giao một migration cụ thể; chưa cài hay kích hoạt các skill đó.

## Khi cập nhật nguồn

Các commit là mốc đã xem, không khẳng định phiên bản mới nhất vĩnh viễn. Khi cần áp dụng kỹ thuật từ upstream, kiểm tra file và reference hiện hành, đối chiếu với phiên bản project. Chỉ tích hợp thay đổi có ích cho bài học; giữ hồ sơ và tiến độ cá nhân. Không dùng star count, tên repo hoặc danh sách keyword làm bằng chứng chất lượng sư phạm hay năng lực người học.
