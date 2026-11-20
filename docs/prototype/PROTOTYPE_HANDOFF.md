# ArenaFlow — Hướng dẫn xây dựng prototype đầy đủ để bàn giao UI

> Ngày lập: 18/11/2026 · Ngôn ngữ: tiếng Việt · Đích triển khai sau prototype: Android Jetpack Compose.
> Tài liệu này là brief giao việc cho AI thực hiện prototype. Việc tạo tài liệu không có nghĩa prototype bên dưới đã được triển khai hoặc nghiệm thu.

## 1. Mục tiêu và cách dùng tài liệu

**Mục tiêu:** tạo prototype có độ hoàn thiện thị giác đủ để tham chiếu triển khai UI Android, đồng thời có tương tác và dữ liệu mẫu đủ để đi hết các hành trình người dùng, đội trưởng và admin trong phạm vi ArenaFlow hiện tại.

Không dừng ở các màn chính hoặc bộ ảnh rời. Phải thể hiện cả phía gửi và phía xử lý, trước/sau thao tác, quyền truy cập, trạng thái cuối, lỗi và đường quay lại.

Cách giao cho AI khác:

1. Cho AI quyền đọc repo này hoặc gửi kèm bộ nguồn trong mục 3; chỉ gửi prompt ngắn sẽ không đủ đặc tả.
2. Dùng prompt ở mục 2.
3. Yêu cầu thực hiện tuần tự các gói ở mục 13, cập nhật tiến độ trong thư mục đầu ra.
4. Nghiệm thu theo hành trình và tiêu chí ở mục 14–15, không theo số lượng ảnh.
5. Khi đạt nghiệm thu mới dùng bộ artifact ở mục 12 để triển khai UI Android.

### 1.1. Định nghĩa “đầy đủ”

- Bao phủ tất cả nhóm nghiệp vụ được liệt kê trong tài liệu này, kể cả admin và trận ngoài hệ thống.
- Một màn có thể dùng nhiều biến thể trạng thái/quyền. Không cần nhân bản layout chỉ để đủ số lượng.
- Sheet/dialog, bộ chọn, trạng thái tải/lỗi và màn toàn trang phải được phân loại rõ.
- Mọi hành động nhìn thấy đều có kết quả kiểm tra được hoặc giải thích vì sao không khả dụng.
- Có seed dữ liệu, đổi nhân vật, đồng hồ mô phỏng và reset để chạy lại các hành trình.
- Có thiết kế và hướng dẫn bàn giao đủ để lập trình viên không phải tự đoán nội dung, trạng thái, điều hướng hay quyền.
- Không yêu cầu backend thật, OAuth/SMS thật, GPS thật, thanh toán hay mã Compose production trong task này.

### 1.2. Phân loại yêu cầu

- **[SPEC]:** đã có trong bộ đặc tả; phải bám theo phiên bản hiện hành.
- **[USER]:** yêu cầu của chủ dự án trong task này: hoàn thiện prototype trước để làm UI nhanh.
- **[PROPOSAL]:** lựa chọn UX để prototype liền mạch; ghi trong decision log, không tự biến thành yêu cầu backend.
- **[DEPENDENCY]:** thiếu contract/dữ liệu hoặc có mâu thuẫn nguồn; vẫn mô phỏng có nhãn và ghi giới hạn.
- Mức P0/P1/P2 bên dưới là **thứ tự thực hiện**, không phải cho phép bỏ P1/P2 khi báo “prototype đầy đủ”.

## 2. Prompt giao việc — có thể sao chép

~~~text
Hãy đọc docs/prototype/PROTOTYPE_HANDOFF.md và thực hiện toàn bộ việc xây dựng prototype ArenaFlow theo tài liệu đó.

Mục tiêu là prototype đầy đủ để sau này triển khai UI Android Jetpack Compose nhanh, gồm màn hình, sheet/dialog, luồng tương tác, mọi vai trò, trạng thái và tình huống lỗi quan trọng. Không dừng ở kế hoạch, bộ ảnh hoặc chỉ happy path.

Trước khi làm:
- Đọc nguồn spec/API hiện hành và nguồn thiết kế được dẫn trong tài liệu.
- Kiểm kê hiện trạng, lập traceability matrix theo ID màn/hành trình và decision log.
- Dùng skill UI phù hợp với môi trường; trong repo này ưu tiên Impeccable khi thực hiện thiết kế.
- Giữ nguyên dự án Stitch nguồn 2789586751825382546 và các bản export gốc.
- Tạo bản làm việc mới tại docs/prototype/complete/; tái sử dụng ngôn ngữ thiết kế từ nguồn chính.
- Không sửa app Android, Gradle, backend hoặc repo docs/ArenaFlow-spec trong task prototype.

Thực hiện lần lượt các gói ở mục 13. Ghi tiến độ và phần còn thiếu sau mỗi gói rồi tiếp tục các phần không bị chặn. Dùng dữ liệu mẫu minh bạch cho contract chưa có; không tự tuyên bố backend đã hỗ trợ.

Bắt buộc có:
- Onboarding, đăng nhập/đăng ký OAuth hoặc OTP, hoàn thiện hồ sơ, phiên hết hạn, tài khoản đã xóa.
- Đội và lời mời; hai phía của kèo A/B; tuyển người và trận ngoài hệ thống.
- Chat, thông báo, địa điểm, sau trận, uy tín, report/tranh chấp và khu vực admin.
- Bộ điều khiển demo riêng: đổi nhân vật, trạng thái, thời gian, mô phỏng lỗi/đồng thời và reset.
- Screen inventory, flow/state map, tài liệu component/token, ảnh kiểm tra và QA report để bàn giao Compose.

Chạy, kiểm tra tương tác và kiểm tra trực quan bằng công cụ hiện có. Không báo hoàn tất khi chưa đi hết các hành trình bắt buộc. Nếu không thể kiểm tra một phần, ghi chính xác phần chưa kiểm tra và lý do; không thay kiểm tra thật bằng tuyên bố suy đoán.
~~~ 

## 3. Nguồn phải đọc và thứ tự ưu tiên

### 3.1. Nguồn thiết kế

- [README nguồn prototype](README.md).
- [Dự án Stitch nguồn ArenaFlow](https://stitch.google.com/u/4/projects/2789586751825382546?pli=1), ID 2789586751825382546.
- [Danh mục bản xuất chính](stitch/2789586751825382546/index.html).
- [Manifest screen ID / tên / metadata](stitch/2789586751825382546/manifest.json).
- [Quy định bản xuất](stitch/2789586751825382546/README.md).
- [Thư viện icon cục bộ](icons/README.md).
- [Prototype tương tác cũ](index.html): tham khảo các hành vi đã có; không dùng làm chuẩn thị giác ưu tiên.

**Giới hạn nguồn:** chỉ đọc Stitch gốc; không tạo/sửa/xóa màn, thay theme hoặc sắp xếp canvas nguồn. Thực hiện UI trên bản làm việc cục bộ. Nếu cần generation bằng Stitch, dùng dự án mới do người thực hiện sở hữu khi có khả năng và thẩm quyền phù hợp; lưu ID/URL riêng, không ghi đè nguồn chính.

Không dùng project 16710701957767846160, STITCH_PROMPT.md, stitch/original/ hoặc stitch-preview.html làm nguồn thiết kế chính: chúng thuộc thử nghiệm cũ.

### 3.2. Nguồn nghiệp vụ và contract

- [Tổng quan](../ArenaFlow-spec/business-spec/SYSTEM-001/Overview.md).
- [Use cases](../ArenaFlow-spec/business-spec/SYSTEM-001/Use_Cases.md).
- [Business rules](../ArenaFlow-spec/business-spec/SYSTEM-001/Business_Rules.md).
- [Acceptance criteria](../ArenaFlow-spec/business-spec/SYSTEM-001/Acceptance_Criteria.md).
- [USER-001: xác thực, hồ sơ, đội](../ArenaFlow-spec/api/USER-001/API_Spec.md).
- [USER-002: giải tán đội, chặn xóa tài khoản đội trưởng](../ArenaFlow-spec/api/USER-002/API_Spec.md).
- [MATCH-001: kèo A/B, yêu cầu, vòng đời, chat](../ArenaFlow-spec/api/MATCH-001/API_Spec.md).
- [MATCH-002: thương lượng và xác nhận gói](../ArenaFlow-spec/api/MATCH-002/API_Spec.md).
- [MERCENARY-001: tuyển người, đơn, trận ngoài, uy tín cá nhân](../ArenaFlow-spec/api/MERCENARY-001/API_Spec.md).
- [REPUTATION-001: đánh giá, report, tranh chấp, uy tín đội](../ArenaFlow-spec/api/REPUTATION-001/API_Spec.md).
- [VENUE-001: địa điểm và ảnh](../ArenaFlow-spec/api/VENUE-001/API_Spec.md).
- [NOTIFICATION-001: thông báo và điều hướng](../ArenaFlow-spec/api/NOTIFICATION-001/API_Spec.md).
- [Quy ước API](../ArenaFlow-spec/api/api-convention.md).

**Ưu tiên:** yêu cầu hiện tại của chủ dự án → quyết định bổ sung đã chốt/đặc tả chuyên biệt mới hơn → contract API hiện hành về dữ liệu/quyền/hành động → đặc tả tổng quan → thiết kế nguồn → prototype cũ.

Nếu hai nguồn mâu thuẫn, ghi cả hai trích dẫn vào decision log, nêu phương án mô phỏng và tác động tích hợp. Không lặng lẽ sửa spec hoặc tuyên bố đã giải quyết nghiệp vụ. Tài liệu này tóm tắt để giao việc; AI phải đọc lại contract trước khi chốt chi tiết từng form.

### 3.3. Baseline đã kiểm tra ngày 18/11/2026

| Nguồn | Hiện trạng | Ý nghĩa khi thực hiện |
|---|---|---|
| Prototype cũ | 8 view: home, detail, create, pair, people, chat, team, alerts | Có tương tác mẫu nhưng chưa đủ vòng đời/quyền/ngoại lệ |
| Stitch chính | 32 thiết kế đang hiển thị: 16 mobile + 16 desktop | Có login, phone, OTP, profile, edit/delete account, team và admin team |
| Bản xuất cục bộ | Xuất ngày 17/11/2026; giữ HTML và ảnh nguồn | Dùng manifest để map ID; không sửa file nguồn |
| App Android | Chưa phải phạm vi của task này | Bộ prototype hoàn tất sẽ được bàn giao để triển khai UI riêng |

Các family đã có trong Stitch: S1 login; S2a số điện thoại; S2b OTP; S3 hồ sơ; S4 sửa hồ sơ; S5 xóa tài khoản; S6 danh sách đội; S7 tạo đội; S8a đội; S8b đội giải tán; S9 mời; S10 chuyển quyền; S11 rời đội; S12 giải tán; S13 admin đội; S14 lịch sử thành viên.

Lưu ý: một số thiết kế mobile mang metadata DESKTOP; phân loại theo nội dung/tên và kích thước vùng mobile, không chỉ dựa vào metadata. Một số ảnh mô tả lỗi/loading; phải xây thêm trạng thái mặc định sử dụng được.

## 4. Ranh giới sản phẩm và quy tắc chung

### 4.1. Phải giữ nguyên

- ArenaFlow phục vụ tạo kèo, ghép đội và tuyển người cho bóng đá phong trào.
- Không đặt sân, quản lý vận hành sân, thu/giữ/hoàn tiền hoặc tạo checkout.
- Sân là địa điểm tham chiếu; chi phí/cọc là thỏa thuận thanh toán bên ngoài.
- Bản hiện tại có thông báo trong app; push FCM/APNs và đăng ký thiết bị làm sau.
- Chat không thay nút xác nhận gói thương lượng.
- Không có email/password trong auth hiện hành; không thêm form đăng ký mật khẩu/quên/đặt lại mật khẩu.
- Không tự thêm xin gia nhập đội, kick thành viên, hủy duyệt mercenary, rút Pairing, khôi phục đội giải tán hoặc khôi phục tài khoản nếu contract chưa hỗ trợ.
- Đội trưởng không phải role chọn vĩnh viễn khi onboarding; một user có quyền đội trưởng theo từng đội. Role ADMIN là quyền riêng.
- Danh sách đội toàn hệ thống không được mặc nhiên coi là “đội của tôi”.

### 4.2. Quy tắc trải nghiệm [PROPOSAL]

- Onboarding ngắn, có bỏ qua, chỉ tự hiện lần đầu; không bắt buộc slider dài.
- Hoàn thiện hồ sơ dùng lại form sửa hồ sơ, có nội dung phù hợp người mới.
- Không tự bắt buộc mọi field hồ sơ nếu contract cho tùy chọn; ghi quyết định “tiếp tục/bỏ qua” rõ ràng.
- Vị trí xin theo ngữ cảnh khi mở tìm đá lẻ; từ chối vẫn có đường dùng khu vực hồ sơ.
- Có lối vào Hồ sơ/Tài khoản từ avatar hoặc menu rõ ràng; không ép đổi bốn tab chính chỉ để thêm hồ sơ.
- Điều khoản/quyền riêng tư nếu trình bày phải có nhãn nội dung chờ cung cấp; không bịa văn bản pháp lý đã được phê duyệt.
- Mô phỏng OAuth, SMS, quyền hệ điều hành, map và upload được phép; không thu thông tin thật.

## 5. Cấu trúc điều hướng

~~~mermaid
flowchart TD
    Start["Khởi động / kiểm tra phiên"] --> Intro["Onboarding lần đầu"]
    Intro --> Auth["Đăng nhập / đăng ký"]
    Start --> Auth
    Auth --> Profile["Hoàn thiện hồ sơ nếu tài khoản mới"]
    Auth --> Shell["Khu vực chính"]
    Profile --> Shell
    Start --> Shell
    Shell --> Matches["Tìm kèo"]
    Shell --> Players["Đá lẻ"]
    Shell --> Inbox["Tin nhắn"]
    Shell --> Teams["Đội của tôi"]
    Shell --> Account["Hồ sơ / tài khoản"]
    Shell --> Alerts["Thông báo"]
    Matches --> MatchDetail["Chi tiết kèo theo vai trò / trạng thái"]
    MatchDetail --> Requests["Yêu cầu / thương lượng / gói"]
    MatchDetail --> Recruit["Tuyển người"]
    Players --> Application["Tin tuyển / đơn của tôi"]
    Teams --> TeamDetail["Chi tiết đội / thành viên / lịch kèo"]
    Alerts --> Target["Đối tượng liên quan / trạng thái không xem được"]
    Account --> Admin["Khu vực quản trị — chỉ ADMIN"]
~~~

Yêu cầu điều hướng:

- Giữ tab, filter và vị trí cuộn khi mở chi tiết rồi quay lại.
- Back đóng sheet trước; form có thay đổi chưa lưu phải xử lý rõ.
- Chọn/tạo sân từ form phải quay lại đúng form và giữ các giá trị đã nhập.
- Auth/session expiry phải có đường quay lại điểm đến còn hợp lệ sau xác thực.
- Đổi nhân vật demo không được giữ quyền của nhân vật trước.
- Nếu quyền/đối tượng thay đổi, cập nhật nút và hiển thị lý do; không để màn trắng.
- Khu vực admin có navigation và breadcrumb/back riêng, chỉ xuất hiện cho ADMIN.
- Chọn đội phải truyền đúng ngữ cảnh cho gửi yêu cầu, nhận kèo, tuyển người, hủy, chi phí và đánh giá; hỗ trợ người dùng làm đội trưởng nhiều đội.

## 6. Danh mục màn hình, sheet và trạng thái

Mỗi ID là đơn vị cần truy vết; không bắt buộc một ID = một file hoặc một layout độc lập. Có thể gộp các ID dùng chung layout nếu inventory ghi rõ biến thể và vẫn đi được toàn bộ hành vi.

Ký hiệu: **M** = màn; **S** = sheet/dialog; **V** = biến thể/trạng thái. “Nguồn” là thiết kế tái sử dụng hoặc domain phải đối chiếu.

### 6.1. Khởi đầu, xác thực và tài khoản — UC-U01..U03

| ID | Loại | Nội dung và hành động bắt buộc | Nguồn |
|---|---|---|---|
| APP-01 | V | Khởi động: phiên còn hạn, refresh thành công, cần đăng nhập, lỗi mạng có thử lại | USER-001 |
| APP-02 | M | Onboarding: giá trị tạo kèo/đá lẻ/uy tín, Bỏ qua, Bắt đầu | PROPOSAL |
| AUTH-01 | M | Chọn Google/Facebook/số điện thoại; giải thích tài khoản tự tạo lần đầu | Stitch S1 |
| AUTH-02 | M | Nhập SĐT, kiểm tra định dạng, gửi mã, quay lại | S2a |
| AUTH-03 | M | Nhập OTP, đổi số, gửi lại; default/loading/error/expired/success theo mô phỏng | S2b |
| AUTH-04 | V | Người mới chuyển sang PROF-02; người cũ về điểm đến phù hợp | UC-U01 |
| AUTH-05 | V | AUTH_003: tài khoản đã xóa, quay lại/chọn phương thức; không CTA khôi phục giả | USER-001 |
| AUTH-06 | S/V | Phiên hết hạn, tiếp tục đăng nhập; giữ điểm đến nếu còn hợp lệ | USER-001 |
| PROF-01 | M | Hồ sơ của tôi: tên/ảnh/tiểu sử/vị trí/ngày sinh/giới tính/khu vực, lối vào sửa và uy tín | S3 |
| PROF-02 | M | Sửa/hoàn thiện hồ sơ; chọn ảnh, vị trí, ngày sinh, giới tính, tỉnh/phường; lỗi từng field | S4 |
| PROF-03 | M/S | Menu tài khoản: hồ sơ, đăng xuất, xóa tài khoản; nội dung trợ giúp theo phạm vi đã chốt | PROPOSAL |
| PROF-04 | S | Cảnh báo và xác nhận xóa; thành công đưa về auth; lỗi giữ trạng thái hợp lý | S5, USER-002 |
| PROF-05 | M/S | Chặn xóa: liệt kê đội còn làm đội trưởng, đi chuyển quyền/giải tán, kiểm tra lại | USER-002 |
| PROF-06 | S | Xác nhận đăng xuất và kết quả; token/session chỉ mô phỏng cục bộ | PROPOSAL |

Fields PROF-02: displayName, avatarUrl, bio, preferredPosition, dateOfBirth, gender, wardId; không tự thêm username làm field đã được backend hỗ trợ. Ngày sinh nếu nhập phải đủ 13 tuổi; tên không chỉ khoảng trắng; vị trí có bốn giá trị theo spec. Bộ chọn ảnh là mô phỏng UX, không khẳng định đã có pipeline upload avatar production.

### 6.2. Đội và lời mời — UC-T01..T06, USER-002

| ID | Loại | Nội dung và hành động bắt buộc | Nguồn |
|---|---|---|---|
| TEAM-01 | M | Đội của tôi; chưa có đội, nhiều đội, vai trò trên mỗi đội; tạo đội/xem lời mời qua thông báo | DEPENDENCY |
| TEAM-02 | M | Danh sách đội toàn hệ thống; phân trang và trạng thái theo contract, không tự nhận có search tên | S6 |
| TEAM-03 | M | Tạo đội: các field USER-001, khu vực, tối đa ba nhãn trình độ | S7 |
| TEAM-04 | M | Chi tiết đội ACTIVE; biến thể captain/member/outsider; uy tín, thành viên, lịch kèo | S8a |
| TEAM-05 | M | Sửa đội; tái sử dụng form tạo, kiểm quyền và dữ liệu đã đổi | USER-001 |
| TEAM-06 | M/S | Danh sách thành viên; vai trò, dữ liệu user đã xóa theo endpoint thường | USER-001 |
| TEAM-07 | S | Mời thành viên; ID/tra cứu theo quyết định phụ thuộc; đã mời/đã là thành viên/không tìm thấy | S9 |
| TEAM-08 | M/S | Chi tiết lời mời từ thông báo: đội, Chấp nhận/Từ chối, kết quả đã xử lý/đội đã giải tán | UC-T03 |
| TEAM-09 | S | Chọn thành viên và chuyển quyền; giải thích chuyển tức thì, không có bên nhận từ chối | S10 |
| TEAM-10 | M/S | Lịch sử đội trưởng; hiển thị thời điểm, người theo contract | USER-001 |
| TEAM-11 | S | Thành viên rời đội; captain được hướng tới chuyển quyền/giải tán | S11 |
| TEAM-12 | S | Giải tán đội vĩnh viễn; cảnh báo, xác nhận, kết quả | S12 |
| TEAM-13 | M/V | Chi tiết đội INACTIVE/DISBANDED; lý do khi có dữ liệu; nút theo quyền/trạng thái | S8b, USER-002 |
| TEAM-14 | S | Chọn đội/phía đang thao tác; chỉ các đội đủ điều kiện; rỗng có hướng dẫn | Dùng chung |

Lời mời được vào từ thông báo in-app hiện hành; không bịa GET invitation detail hoặc endpoint “lời mời của tôi”. Dùng payload/fixture của thông báo và kết quả thao tác cho prototype; ghi rõ dữ liệu chi tiết nào còn phụ thuộc. Tên đội được trùng; người chơi có thể thuộc nhiều đội.

### 6.3. Khám phá, tạo và vòng đời kèo — UC-M01..M04, M08..M09

| ID | Loại | Nội dung và hành động bắt buộc | Nguồn |
|---|---|---|---|
| MATCH-01 | M | Tìm kèo; A/B, card có nhãn trạng thái, thời gian/khu vực/trình độ và dữ liệu đúng contract | Prototype cũ + MATCH-001 |
| MATCH-02 | S | Bộ lọc; chỉ các tiêu chí endpoint hỗ trợ, áp dụng/reset, trạng thái không có kết quả | MATCH-001 2.2 |
| MATCH-03 | M | Chi tiết kèo A; chủ kèo/đội gửi/đã ghép/người chỉ xem; sân/giờ/đội/chi phí | MATCH-001 |
| MATCH-04 | M | Chi tiết bài B trước nhận; hai danh sách sân/giờ, chọn đội và nhận kèo | MATCH-001/002 |
| MATCH-05 | M | Tạo A/B; chọn đội, bộ môn/định dạng/trình độ, sân/giờ, thời hạn; thêm/xóa lựa chọn B | UC-M01/M02 |
| MATCH-06 | M/S | Xem lại trước đăng: dữ liệu đã nhập, sửa phần tương ứng, đăng chống bấm lặp | PROPOSAL |
| MATCH-07 | M | Lịch/kèo của đội: sắp tới, kết thúc, hủy/hết hạn theo bộ lọc contract | MATCH-001 2.11 |
| MATCH-08 | M | Yêu cầu đội đã gửi; trạng thái, chi tiết và rút khi được phép | MATCH-001 2.12 |
| MATCH-09 | M | Chủ kèo xem yêu cầu đến; mở hồ sơ đội, chat, chấp nhận/từ chối | MATCH-001 2.7–2.9 |
| MATCH-10 | M/S | Chi tiết/xác nhận xử lý yêu cầu; pending/accepted/rejected/withdrawn/superseded/expired | MATCH-001 |
| MATCH-11 | S | Rút yêu cầu: pending không trừ; accepted tương đương hủy, lý do/mức trừ | MATCH-001 2.10 |
| MATCH-12 | S | Hủy kèo: expectedStatus, lý do, tác động uy tín; chặn nếu còn pending A hoặc trận bắt đầu | MATCH-001 2.5 |
| MATCH-13 | M/V | Kèo CONFIRMED, đang diễn ra, ARCHIVED, COMPLETED, CANCELLED, EXPIRED | Dùng lại chi tiết |
| MATCH-14 | M/S | Thỏa thuận chi phí A lúc cho phép sửa; khoản tự nhập, chia tiền/cọc theo schema | MATCH-001 2.4 |

Form tạo: giờ bắt đầu ít nhất một giờ sau hiện tại; kết thúc sau bắt đầu, thời lượng không quá 12 giờ; thời hạn bài đăng theo ràng buộc API. B có 1–5 địa điểm và 1–5 khung giờ độc lập, không phải tối đa năm cặp cố định. Sau tạo không cho sửa nội dung kèo như giờ/sân/trình độ; chi phí A có quy tắc sửa riêng khi OPEN.

### 6.4. Thương lượng B và gói — UC-M05..M07, UC-C01..C02

| ID | Loại | Nội dung và hành động bắt buộc | Nguồn |
|---|---|---|---|
| PAIR-01 | M | Danh sách thương lượng của kèo — chủ kèo; phân biệt từng đội và gói | MATCH-002 2.2 |
| PAIR-02 | M | Thương lượng của đội; trạng thái, kèo, đội đối phương | MATCH-002 2.3 |
| PAIR-03 | M | Chi tiết: ngữ cảnh hai đội, chat liên quan, gói riêng, xác nhận của từng bên | MATCH-002 2.4 |
| PAIR-04 | M/S | Chốt một sân + một khung giờ từ bài đăng; sân ngừng hoạt động không chọn được | MATCH-002 2.5 |
| PAIR-05 | M/S | Sửa chi phí, cọc, bên giữ bên ngoài, khoản tự nhập; validate theo schema | MATCH-002 2.6 |
| PAIR-06 | S/V | Xem toàn bộ gói trước xác nhận; một bên đã xác nhận/cả hai; cần phương án trước | MATCH-002 2.7 |
| PAIR-07 | V | Revision cũ/gói vừa đổi; tải lại, trình bày gói mới, người dùng tự xác nhận lại | PAIRING_006 |
| PAIR-08 | V | Đã ghép, bị thay thế, hết hạn, kèo bị hủy; khóa sửa, chat theo canSend | MATCH-002 |

Mỗi thương lượng có gói độc lập. Đổi phương án/chi phí thực sự làm tăng revision và xóa xác nhận hai bên; lưu đúng phương án không đổi với revision đúng thì giữ xác nhận. Chỉ bên thứ hai xác nhận mới hoàn tất; thương lượng khác bị SUPERSEDED. Không có nút rút Pairing hay chốt bằng nội dung tin nhắn.

### 6.5. Tuyển người, ứng tuyển và trận ngoài — UC-ME01..ME09

| ID | Loại | Nội dung và hành động bắt buộc | Nguồn |
|---|---|---|---|
| MERC-01 | M | Tìm đá lẻ: vị trí, GPS/bán kính 1–20 km mặc định 3; fallback khu vực hồ sơ | MERCENARY-001 2.3 |
| MERC-02 | M | Chi tiết tin: vị trí/số lượng/giờ/trình độ/khu vực/uy tín đội; chưa duyệt ẩn sân | 2.4 |
| MERC-03 | S | Xác nhận ứng tuyển; pending/success/duplicate/trùng giờ/thuộc đội của trận | 2.7 |
| MERC-04 | M | Đơn của tôi: pending/approved/rejected/withdrawn, trạng thái trận | 2.9 |
| MERC-05 | M | Chi tiết đơn; approved mới thấy thông tin địa điểm; lối chat, rút, đánh giá/report | 2.10 |
| MERC-06 | S | Rút pending hoặc approved; mức trừ dự kiến, chặn sau bắt đầu/kèo đã hủy | 2.13 |
| MERC-07 | M/S | Chọn trận tuyển: kèo đã ghép hoặc khai báo trận ngoài hệ thống | UC-ME01 |
| MERC-08 | M | Đăng tin: 1–4 vị trí, mỗi vị trí số lượng 1–10; mỗi vị trí thành tin riêng | 2.1 |
| MERC-09 | M | Tin tuyển của đội: open/filled/closed/expired và số được duyệt | 2.2 |
| MERC-10 | M | Ứng viên của tin: hồ sơ/uy tín, trạng thái, chat, duyệt/từ chối | 2.8 |
| MERC-11 | M/S | Chi tiết ứng viên + xác nhận duyệt/từ chối; mất slot hoặc xung đột giờ | 2.11/2.12 |
| MERC-12 | S | Sửa số lượng không dưới số đã duyệt, đóng tin và hệ quả với pending | 2.5/2.6 |
| EXT-01 | M | Khai báo trận ngoài: chọn sân, giờ, trình độ, đối thủ tùy chọn, vị trí/số lượng | 2.28 |
| EXT-02 | M | Danh sách/chi tiết trận ngoài của đội, tin tuyển và đơn liên quan | 2.29/2.30 |
| EXT-03 | M | Sửa trận ngoài; khóa sân/giờ/trình độ khi đã có người được duyệt | 2.31 |
| EXT-04 | S | Hủy trận ngoài trước bắt đầu, giải thích ảnh hưởng tin tuyển/đơn | 2.32 |
| EXT-05 | M/S | Thêm vị trí tuyển cho trận ngoài hiện có | 2.33 |

- Không hiện tên sân/địa chỉ/tọa độ hoặc link làm lộ địa điểm ở góc nhìn người chưa được duyệt. Góc nhìn đội trưởng và người đã duyệt phải theo response tương ứng.
- FILLED không duyệt thêm; các đơn pending còn lại vẫn giữ pending trừ khi có hành động làm chúng đổi trạng thái.
- Người rút approved có thể làm tin FILLED về OPEN; pending rút không trừ điểm.
- Không có “hủy duyệt” từ đội trưởng; slot/trùng giờ phải kiểm lại lúc duyệt.
- Không tự dùng vòng đời Match cho trận ngoài: external match có SCHEDULED/CANCELLED; thời gian quyết định trạng thái trải nghiệm sau trận.
- Việc tạo tin tuyển kèo đã ghép đang có khác biệt giữa UC-ME01 và API 2.1 về mốc bắt đầu; xử lý theo DEP-07 ở mục 11, không ghi chung rằng mọi thao tác tin đều bị chặn từ giờ bắt đầu.

### 6.6. Địa điểm — UC-V01..V06

| ID | Loại | Nội dung và hành động bắt buộc | Nguồn |
|---|---|---|---|
| VEN-01 | M/S | Tìm/chọn sân, phân trang, không có kết quả, quay lại form với sân đã chọn | VENUE-001 2.2 |
| VEN-02 | M | Chi tiết sân: tên/địa chỉ/khu vực/toạ độ/loại/liên hệ/ảnh theo response | 2.3 |
| VEN-03 | M | Thêm sân, vị trí mô phỏng và trường theo schema; có hiệu lực ngay | 2.1 |
| VEN-04 | M | Sửa sân: người tạo hoặc admin; lỗi/mất quyền/xung đột dữ liệu | 2.12 |
| VEN-05 | M/S | Xem ảnh, thêm ảnh, progress/error/retry, xóa và chọn ảnh chính theo quyền | 2.4–2.6/2.13 |
| VEN-06 | S/M | Report sai thông tin/đóng cửa, lý do, trạng thái gửi theo spec | 2.7 |
| VEN-07 | V | Sân INACTIVE và cảnh báo lâu không dùng khi response có; không chọn sân inactive cho gói mới | VENUE-001 |

Tối đa năm ảnh và đúng một ảnh chính khi có ảnh. Quyền thêm ảnh/đặt ảnh chính khác quyền sửa thông tin/xóa ảnh; đọc bảng quyền API, không gom tất cả thành “chỉ người tạo”. Upload chỉ mô phỏng, không gọi Cloudinary thật. Không thêm chức năng đặt sân, xem slot trống hay thanh toán.

### 6.7. Chat, thông báo — UC-N01..N04

| ID | Loại | Nội dung và hành động bắt buộc | Nguồn |
|---|---|---|---|
| CHAT-01 | M | Danh sách mọi hội thoại của tôi, ngữ cảnh kèo/đơn, tin cuối, chưa đọc | MATCH-001 2.20 |
| CHAT-02 | M | Chat hai đội từ request A hoặc Pairing B; đầu phòng, tác giả thật, thời gian, đã đọc | MATCH-001 |
| CHAT-03 | M/V | Chat đội trưởng–ứng viên; ngữ cảnh đơn, đi tới đơn/tin tuyển | MERCENARY-001 |
| CHAT-04 | V | Chỉ xem theo canSend; mất quyền, đang nối lại, gửi lỗi/thử lại, tải lịch sử cũ | Contract chat |
| CHAT-05 | S | Report giao tiếp tệ khi cuộc chat có tin; loại đã gửi không gửi trùng | REPUTATION/MERCENARY |
| NOTIF-01 | M | Thông báo của tôi, tất cả/chưa đọc, số chưa đọc, đọc một/tất cả, phân trang | NOTIFICATION-001 |
| NOTIF-02 | V | Mở đối tượng đích; đã đổi trạng thái/không tồn tại/mất quyền/ngoài thời hạn lưu | NOTIFICATION-001 4.3 |
| NOTIF-03 | V | Mẫu nội dung cho toàn bộ loại thông báo hiện hành; map tới đúng đích | NOTIFICATION-001 4.2 |

Không tạo một màn riêng cho mỗi loại thông báo. Tạo catalog mẫu và bảng map type → referenceType → màn đích cho toàn bộ loại hiện hành (baseline 35 loại). Không tạo thông báo chat giả nếu spec chỉ có tín hiệu chưa đọc ở Tin nhắn.

Chat dùng lại room theo contract khi gửi lại request; không mở room mới tùy tiện. CanSend không suy ra đơn giản từ “kèo hủy”: hai đội đã ghép và đơn approved có thể tiếp tục nhắn theo spec. Hết retention/mất quyền phải có trạng thái thích hợp.

### 6.8. Sau trận, uy tín và tranh chấp — UC-R01..R07, UC-ME06/ME08

| ID | Loại | Nội dung và hành động bắt buộc | Nguồn |
|---|---|---|---|
| REP-01 | M | Đánh giá đối phương: 1–5 sao nguyên, nhận xét, tỷ số tùy chọn hoặc Không đến | REPUTATION-001 2.1 |
| REP-02 | M/V | Xem đánh giá trận: given/received, chưa được xem, đã gửi, không sửa/xóa | 2.2 |
| REP-03 | M | Đội trưởng đánh giá từng mercenary; mercenary đánh giá đội; sao/Không đến theo quyền | MERCENARY 2.18/2.19 |
| REP-04 | M/S | Uy tín đội: score/rating/completion/badge, đánh giá từ mercenary hiển thị riêng | REPUTATION 2.9, MERCENARY 2.24 |
| REP-05 | M/S | Uy tín cá nhân của ứng viên/user theo contract, không giả định có public profile đầy đủ | MERCENARY 2.23 |
| REP-06 | M/S | Gửi report; đối tượng/loại/lý do; loại đã gửi/điều kiện sai hiển thị rõ | REPUTATION 2.3/2.10, MERCENARY 2.20 |
| REP-07 | M | Report đã gửi/nhận của đội hoặc cá nhân; phân biệt nguồn giữa đội và mercenary | Các endpoint report |
| REP-08 | M | Chi tiết report: mức ảnh hưởng được phép hiển thị, trạng thái, hạn tranh chấp | REPUTATION/MERCENARY |
| REP-09 | M/S | Tranh chấp NOSHOW trong 24h; bằng chứng văn bản, xác nhận gửi | REPUTATION 2.5 |
| REP-10 | V | Chờ admin, hết hạn tranh chấp, xác nhận/bác report, kết quả cuối | REPUTATION/MERCENARY |

Không tiết lộ đối phương đã/chưa đánh giá trước khi bên mình gửi. Không đến không tự tạo report bùng; sau đánh giá chỉ gợi ý thao tác report riêng. Không thêm sửa/xóa rating hoặc tranh chấp lại quyết định cuối. Tranh chấp hiện dùng bằng chứng văn bản; không bịa pipeline upload bằng chứng.

Score đội và cá nhân độc lập. Dưới năm trận chưa hiện completion rate; badge phải dựa vào dữ liệu/quy tắc, không cho user tự bật. Rating mercenary dành cho đội hiển thị riêng, không nhập chung sao trung bình đội.

### 6.9. Admin — khu vực riêng, phải hoàn tất trong bản đầy đủ

| ID | Loại | Nội dung và hành động bắt buộc | Nguồn |
|---|---|---|---|
| ADM-01 | M | Lối vào/bố cục admin, nhóm chức năng và số chờ nếu có dữ liệu; USER không truy cập | PROPOSAL bố cục |
| ADM-02 | M | Tìm/quản lý đội, lọc, chi tiết, tạm ngưng/kích hoạt lại và lý do | Stitch S13, USER-001 |
| ADM-03 | M | Lịch sử thành viên đội; tên thật + nhãn đã xóa đúng contract admin | S14, USER-002 |
| ADM-04 | M | Danh sách tranh chấp đội, trạng thái và chi tiết từ hai phía | REPUTATION 2.6/2.7 |
| ADM-05 | M | Danh sách/chi tiết tranh chấp mercenary, hàng chờ riêng | MERCENARY 2.25/2.26 |
| ADM-06 | S | Xác nhận đúng/bác report; ghi chú quyết định bắt buộc 1–500 ký tự | REPUTATION 2.8, MERCENARY 2.27 |
| ADM-07 | M | Danh sách report địa điểm, chi tiết sân/report, quyết định xử lý | VENUE-001 2.8/2.9 |
| ADM-08 | S | Ngừng hoạt động/kích hoạt lại sân theo quyền; không lẫn giải tán đội | VENUE-001 2.10/2.11 |
| ADM-09 | M/S | Danh sách bộ môn, bật/tắt, cảnh báo ảnh hưởng thao tác mới | MATCH-001 2.14/2.15 |

Không tự thêm dashboard doanh thu, thanh toán, quản lý đặt sân, phân quyền admin hoặc analytics chưa có spec. ADM-01 chỉ là điểm truy cập chức năng. Dữ liệu user đã xóa phải theo từng response: endpoint thường che tên; endpoint admin giữ tên thật kèm cờ; không tự bổ sung field ảnh mà API không có.

## 7. State matrix và hành vi toàn cục

### 7.1. Ma trận dùng chung

| Tình huống | Cách thể hiện tối thiểu | Kiểm tra |
|---|---|---|
| Đang tải lần đầu | Skeleton/progress có nghĩa, tránh nhảy bố cục | Có thể bật từ demo panel |
| Danh sách trống lần đầu | Giải thích + hành động phù hợp quyền | Ví dụ chưa có đội/đơn |
| Không có kết quả lọc | Tóm tắt filter, xóa filter/chỉnh điều kiện | Không nhầm lỗi mạng |
| Mạng/timeout | Thử lại, giữ form/nội dung đã nhập | Không báo thành công giả |
| Validation | Lỗi gần field, focus tới lỗi đầu, giữ giá trị đúng | Cả biên và dữ liệu thiếu |
| Đang gửi | Chống gửi lặp, phản hồi rõ | Double click không sinh bản ghi trùng |
| Thành công | Cập nhật dữ liệu ở màn trước/danh sách/badge liên quan | Không chỉ toast |
| Không tìm thấy | Giải thích, quay lại danh sách | Link/notification cũ |
| Không có quyền | Ẩn/khóa hành động + lý do theo ngữ cảnh | Chuyển quyền khi đang mở màn |
| Dữ liệu đổi đồng thời | Tải lại và cho người dùng quyết định tiếp | Không tự xác nhận gói revision mới |
| Thao tác có hậu quả | Xác nhận kèm lý do/mức ảnh hưởng thực tế | Hủy/rút/giải tán/xóa |
| Form chưa lưu | Back/cancel có cách giữ hoặc bỏ rõ ràng | Không mất dữ liệu ngoài ý muốn |
| Phân trang | Loading thêm, hết dữ liệu, lỗi tải thêm | Danh sách và lịch sử chat |
| Retention | Nội dung không còn truy cập/không còn trong danh sách theo contract | Chat 90 ngày, thông báo 30 ngày |

Không yêu cầu mọi màn hỗ trợ thao tác offline. Offline trong prototype là tình huống lỗi/trạng thái hiển thị, không đồng nghĩa có hàng đợi đồng bộ offline production.

### 7.2. State machine phải mô phỏng

- Match: OPEN → CONFIRMED → ARCHIVED → COMPLETED; OPEN → EXPIRED/CANCELLED; CONFIRMED → CANCELLED trước giờ bắt đầu.
- Request A: PENDING → ACCEPTED/REJECTED/WITHDRAWN/SUPERSEDED/EXPIRED; rút ACCEPTED hủy kèo theo contract.
- Pairing: NEGOTIATING → CONFIRMED/SUPERSEDED/EXPIRED; gói đổi trong NEGOTIATING xóa xác nhận.
- Team: ACTIVE/INACTIVE, DISBANDED kết thúc vĩnh viễn; captain transfer thay quyền tức thì.
- Tin tuyển: OPEN/FILLED/CLOSED/EXPIRED; số lượng và rút đơn làm OPEN ↔ FILLED khi đủ điều kiện.
- Đơn: PENDING → APPROVED/REJECTED/WITHDRAWN; APPROVED → WITHDRAWN khi được phép; không có revoke approve.
- Trận ngoài: SCHEDULED/CANCELLED; dùng mốc thời gian để xác định đang/sau trận.
- Report/tranh chấp: đúng bảng trạng thái API, không gom mọi AUTO_ACCEPTED thành đang chờ admin.
- Chat: canSend được tính/mô phỏng theo ngữ cảnh request/Pairing/application và quyền hiện tại.

Các bảng quyền §3 API là nguồn kiểm tra chi tiết. Đừng dùng một biến isCaptain toàn app hay một biến confirmed chung cho mọi kèo.

## 8. Dữ liệu mẫu và bộ điều khiển demo

### 8.1. Bộ dữ liệu tối thiểu

| Fixture | Nội dung cần có |
|---|---|
| User mới | Chưa hoàn thiện hồ sơ, chưa có đội; phương thức Google/Facebook/Phone |
| User cá nhân | Hồ sơ/khu vực đầy đủ, không có quyền captain, có đơn pending/approved và lịch sử |
| Captain Alpha | Đội A, thành viên có thể nhận chuyển quyền, nhiều request/Pairing/tin tuyển |
| Captain Beta | Phía đối phương; có thể gửi/nhận, xác nhận, hủy, đánh giá |
| Captain Gamma | Cạnh tranh ghép cùng kèo để demo SUPERSEDED |
| Captain nhiều đội | Chọn đúng đội; có thể là captain cả hai phía nhưng bị chặn đánh giá chính mình theo spec |
| Member | Xem đội/rời đội, không có nút quản trị captain |
| Admin | Tất cả hàng chờ, bộ môn, venue và đội; không kế thừa quyền captain tùy tiện |
| Dữ liệu đặc biệt | User đã xóa; đội inactive/disbanded; sân inactive; đội/cá nhân dưới năm trận |
| Kèo/đơn | Mỗi trạng thái chính; nhiều bản ghi để thử phân trang, slot cuối/trùng giờ |
| Report | Bốn loại; NOSHOW còn hạn/hết hạn/disputed/resolved |
| Notification | Catalog toàn bộ type hiện hành, link hợp lệ và link không còn truy cập |

Dùng UUID hợp lệ ở dữ liệu đại diện contract. Có thể dùng alias dễ đọc trong panel nhưng giữ quan hệ ID nhất quán. Dữ liệu địa điểm/người dùng là minh họa; không gọi số thật hoặc gửi tin ra ngoài.

### 8.2. Demo panel — công cụ kiểm tra, tách khỏi sản phẩm

- Chuyển nhân vật/đội/phía đang mô phỏng.
- Chọn scenario preset; reset riêng scenario hoặc toàn bộ seed.
- Đồng hồ mô phỏng: trước hạn, hết hạn bài, trước trận, sát giờ, đang đá, sau trận, quá hạn tranh chấp/retention.
- GPS được cấp/từ chối/chưa có khu vực.
- Lần thao tác tiếp theo lỗi mạng/validation/403/404/409 theo tình huống phù hợp.
- Mô phỏng bên kia gửi yêu cầu, duyệt, đổi gói, xác nhận và ghép với đội khác.
- Mô phỏng notification/tin nhắn mới và cập nhật chưa đọc.
- Hiển thị tên scenario và trạng thái đang dùng; có cách ẩn panel khi chụp màn.

Không đặt nút “Mô phỏng đối phương xác nhận” như một nút sản phẩm trong màn thương lượng. Đưa nó vào panel kiểm tra.

### 8.3. Nguyên tắc lưu trữ

Dùng một store dữ liệu mẫu có quan hệ giữa user/team/match/request/pairing/recruitment/application/conversation/report/notification. Nếu dùng localStorage, version key và có reset; không lưu dữ liệu thật. Chuyển nhân vật trong cùng dataset phải thấy kết quả phía kia vừa thao tác. Seed dùng mốc thời gian tương đối với đồng hồ demo để bản prototype không tự hết hạn sau vài ngày.

## 9. Yêu cầu thị giác và khả năng bàn giao Compose

- Dùng Be Vietnam Pro và token màu/bo góc từ thiết kế nguồn chính; customColor nguồn là #16A34A nhưng nhiều component dùng token primary khác, cần trích token thực tế. Không tự thay toàn bộ bằng màu xanh từ prototype cũ.
- Giữ cấu trúc/thành phần nhận diện nguồn, bổ sung component còn thiếu cùng hệ thống; không thiết kế lại thương hiệu.
- Mobile chính khoảng 390 px; kiểm tra tối thiểu 360, 390 và 430 px, màn thấp và form khi bàn phím xuất hiện.
- Desktop dùng để tham khảo và khu vực admin thích hợp; không phải yêu cầu xây lại đủ mọi màn web desktop song song với mobile.
- Mục tiêu chạm tối thiểu tương đương 48 dp; không sao chép nguyên các nút 44 px từ HTML nguồn khi không phù hợp Android.
- Text tiếng Việt không cắt dấu, số tiền/giờ/tên đội dài không phá layout; không dùng màu làm tín hiệu duy nhất.
- Focus/label/error/disabled/loading cần có; keyboard tab được, contrast dễ đọc ngoài trời.
- Mô tả safe area/insets, bottom bar, nội dung cuộn và hành vi khi IME mở để chuyển Compose rõ ràng.
- Form, chip, card, status badge, action bar và dialog có các trạng thái chuẩn; không tùy biến mỗi màn một kiểu.
- Animation phục vụ chuyển trạng thái, có reduced motion khi công nghệ cho phép.
- Không đưa schema/API/revision/debug ID vào giao diện người dùng nếu họ không cần để quyết định; giữ chi tiết này trong tài liệu/panel demo.

Bàn giao mỗi component: tên, mục đích, anatomy, token, variant, state, nội dung/giới hạn, hành vi, accessibility và các màn sử dụng. Với Compose, ghi gợi ý screen state, event và navigation argument; không cần viết mã production hoặc đổi kiến trúc app hiện tại.

## 10. Cách xây prototype

**Mặc định đề xuất:** HTML/CSS/JavaScript có cấu trúc trong docs/prototype/complete/, thuận tiện mở và dùng dữ liệu cục bộ. Nếu có toolchain đang dùng được, tái sử dụng khi có lợi; không tự dựng framework/build system lớn chỉ để trình diễn.

- Một entry point, route/deep link có thể mở trực tiếp; nêu cách chạy (mở file hay server local).
- Tách component, route, fixtures, state transitions và scenario để dễ sửa.
- Không chỉ dùng ảnh chụp Stitch làm nút giả; màn cần thiết phải có UI tương tác thực.
- Có dữ liệu mẫu rõ ràng nhưng không lặp nhãn kỹ thuật trên mọi card.
- Không gọi API production hoặc gửi dữ liệu ra dịch vụ thật.
- Map endpoint là tài liệu chuẩn bị tích hợp, không yêu cầu kết nối trong task này.
- Khi môi trường có skill Impeccable, đọc shared selection rules/SKILL.md trước phần thiết kế và áp dụng workflow phù hợp.
- Làm việc trong thư mục prototype mới; giữ app Kotlin, Gradle, backend, repo spec lồng và export nguồn nguyên vẹn.

## 11. Phụ thuộc, mâu thuẫn và các quyết định cần ghi

| ID | Hiện trạng đã biết | Cách xử lý trong prototype | Tác động bàn giao UI/API |
|---|---|---|---|
| DEP-01 | USER-001 chưa có “đội của tôi”; GET /teams trả mọi đội | Fixture có quan hệ thành viên/captain; TEAM-01/14 hoạt động | Cần contract trước tích hợp thật; không lọc giả trên toàn bộ danh sách phân trang |
| DEP-02 | Mời chỉ nhận userId; chưa có tra cứu username/user | Mô phỏng chọn người từ fixture; ghi lựa chọn UX vào decision log | Không tuyên bố tìm username có API; không tự thêm field username |
| DEP-03 | Chưa có danh mục tỉnh/phường | Bộ chọn từ fixture có ID và quan hệ tỉnh–phường | Cần nguồn/endpoint danh mục; không tự gửi tên thay wardId |
| DEP-04 | Lời mời chỉ biết qua notification, không hết hạn; notification 30 ngày | TEAM-08 vào từ NOTIF-01; thể hiện giới hạn đã chấp nhận | Không giả định có inbox lời mời hoặc endpoint detail; mở rộng USER-003 là việc sau |
| DEP-05 | Auth OAuth/OTP API hiện là mock | Seed thành công/thất bại/gửi lại/hết hạn rõ là demo | Expiry thật, resend cooldown/rate limit, SMS/provider integration cần contract bảo mật riêng |
| DEP-06 | Tổng quan nói có thể khôi phục tài khoản sau này; API AUTH_003 chưa cho khôi phục | AUTH-05 chặn, không làm CTA khôi phục hoạt động | Khôi phục account chưa thuộc contract hiện hành |
| DEP-07 | UC-ME01 exception nói không tạo tin sau bắt đầu; MERCENARY API 2.1 cho tạo đến trước kết thúc, nhưng duyệt/ứng tuyển chặn từ bắt đầu | Với mô phỏng tích hợp, theo API 2.1 và §3 hiện hành; ghi xung đột trong log | Chủ dự án cần thống nhất spec; không tự sửa tài liệu nguồn |
| DEP-08 | Onboarding, menu tài khoản, review trước đăng là UX bổ sung | Dựng gọn, tái dùng component, đánh dấu PROPOSAL | Không suy ra endpoint mới hoặc field bắt buộc |
| DEP-09 | Chưa có nội dung pháp lý/trợ giúp đã phê duyệt | Chỉ placeholder có nhãn nếu cần; không chặn core flow bằng trang chưa có | Bổ sung nội dung trước phát hành |
| DEP-10 | Upload avatar, map/location picker thật, logout server chưa được chốt trong phạm vi đã đọc | Mô phỏng UX/local session; đọc lại contract nếu nguồn được bổ sung | Không tự hứa backend/storage có hỗ trợ |
| DEP-11 | Sân của tin đá lẻ bị bảo vệ nhưng địa điểm có luồng danh mục riêng | Kiểm mọi đường từ tin/đơn/chat/preview; không lộ quan hệ với sân trước duyệt | Không nhúng venue chính xác vào payload public rồi chỉ ẩn CSS |

Danh sách này không chặn việc làm giao diện bằng dữ liệu mẫu. Nếu phát hiện khoảng trống mới, thêm DEP-ID, trích nguồn và tiếp tục phần độc lập. Với lựa chọn làm thay đổi nghiệp vụ quan trọng, mô tả phương án và phần bị ảnh hưởng thay vì tự đoán.

## 12. Artifact bắt buộc bàn giao

Mặc định đặt tất cả trong docs/prototype/complete/. Tên file có thể điều chỉnh nếu có lý do; README phải liên kết được đến từng artifact.

| Artifact | Nội dung bắt buộc |
|---|---|
| index.html + source cần thiết | Prototype chạy được, route, component, store, fixtures và demo panel |
| README.md | Cách chạy, reset, đổi nhân vật, scenario, giới hạn, nguồn và trạng thái hoàn tất |
| screen-inventory.md | Toàn bộ ID mục 6 → loại → route/state → vai trò → source → API/UC → tiến độ → ảnh |
| flows-and-states.md | Sơ đồ hành trình, bảng chuyển trạng thái/quyền, Back và điều hướng sau hành động |
| design-system.md | Token thực tế, typography, spacing, component/variant/state và icon mapping |
| ui-handoff.md | Từng màn/component: state, event, field/error, action, navigation args, insets/IME, quyền, dữ liệu cần cho Compose |
| fixtures-and-scenarios.md | Nhân vật, dataset, quan hệ, đồng hồ, thao tác reset và cách tái hiện |
| api-and-dependencies.md | Màn/action → endpoint hiện có; dependency/mock; không bịa contract |
| decisions.md | DEC-ID, nguồn, quyết định/giả định/đề xuất/câu hỏi còn mở và tác động |
| progress.md | Checklist từng gói/ID; đã thiết kế/đã tương tác/đã kiểm tra/đang thiếu |
| qa-report.md | Các hành trình đã chạy, viewport/nhân vật/state, expected/actual, lỗi còn lại và giới hạn kiểm tra |
| screenshots/ + chỉ mục | Ảnh bản làm việc cho các layout chuẩn và trạng thái quan trọng; chú thích ID/scenario/viewport |
| stitch-working-manifest.json | Chỉ khi có project Stitch làm việc mới: ID, URL, screen mapping; không sửa manifest nguồn |

Một mô tả màn trong ui-handoff phải có ít nhất:
- ID + tên + loại + route + entry points + exit points.
- Vai trò/ngữ cảnh đội; điều kiện xem/ghi.
- Field hiển thị và form, mặc định/giới hạn/validation.
- Primary/secondary/destructive actions, sự kiện và kết quả.
- Loading/empty/error/success/read-only/conflict áp dụng.
- Nguồn thiết kế/component, ảnh tham chiếu, API/UC.
- State cần lưu khi Back/chuyển tab/đổi cấu hình; hành vi bàn phím và cuộn.
- Dependency/đề xuất chưa chốt.

## 13. Gói thực hiện và điểm kiểm tra

| Gói | Thứ tự | Phạm vi | Điều kiện kết thúc gói |
|---|---|---|---|
| WP-01 | P0 | Đọc nguồn, inventory, navigation, token/component nền, seed/demo panel | Có route map và dữ liệu đổi nhân vật; ghi toàn bộ ID, không chỉ các màn sẽ làm đầu |
| WP-02 | P0 | APP/AUTH/PROF + TEAM | Đi được người mới, người cũ, lời mời hai phía, chuyển quyền, xóa bị chặn |
| WP-03 | P0 | VEN chọn/thêm/chi tiết, MATCH và PAIR, cost | Đi trọn A/B từ đăng đến ghép; conflict/rút/hủy đúng state |
| WP-04 | P0 | MERC/EXT + chat/notification cho các hành trình trên | Hai phía tuyển người hoạt động, bảo vệ sân, đổi trạng thái nhất quán |
| WP-05 | P1 | REP, các chức năng VEN còn lại, CHAT/NOTIF đầy đủ và retention | Sau trận/report/tranh chấp, toàn bộ notification type có đích, venue đúng quyền |
| WP-06 | P2 | ADM | Mọi nhóm admin có happy/error path; không truy cập bằng USER |
| WP-07 | Bắt buộc cuối | Chạy hành trình, rà thị giác/accessibility, hoàn tất UI handoff và QA | Đạt mục 14–15; không còn ID bắt buộc bị bỏ trống |

Sau mỗi gói: cập nhật inventory/progress, chạy scenario liên quan, chụp các layout mới và ghi lỗi. Không đợi đến cuối mới phát hiện state store không hỗ trợ hai phía. Có thể ghép gói khi thuận tiện nhưng không bỏ phạm vi.

## 14. Hành trình nghiệm thu bắt buộc

Mỗi hàng phải có cách mở/reset, nhân vật, bước thực hiện, kết quả mong đợi, kết quả thực tế và bằng chứng trong QA report. Cùng một layout có thể phục vụ nhiều hành trình.

| ID | Hành trình | Kết quả cần kiểm tra |
|---|---|---|
| J-01 | Mở mới → onboarding → phone → OTP → hoàn thiện hồ sơ | User mới tới màn chính; user cũ không bị bắt tạo lại hồ sơ |
| J-02 | Google/Facebook thành công, hủy, lỗi; OTP đổi số/gửi lại/lỗi/expired | Trạng thái tách biệt, không khóa nút vĩnh viễn hoặc báo thành công giả |
| J-03 | Phiên hết hạn khi đang mở đích từ thông báo | Đăng nhập lại về đích hợp lệ; mất quyền có giải thích |
| J-04 | Chưa có đội → tạo → sửa → mời → đổi nhân vật → chấp nhận/từ chối | Hai phía và số thành viên/quyền cập nhật đúng |
| J-05 | Chuyển đội trưởng → member rời → đội inactive/disbanded | Quyền thay ngay; không có chấp nhận chuyển quyền giả |
| J-06 | Xóa account của captain nhiều đội/duy nhất | Bị chặn, xử lý từng đội, thử lại xóa; auth không khôi phục giả |
| J-07 | Tạo kèo A → bên khác gửi → chat → chủ duyệt một trong nhiều request | Một accepted, các pending khác superseded; room đúng trạng thái |
| J-08 | A: từ chối/rút pending/gửi lại khi hợp lệ/rút accepted | Điều kiện gửi lại và mức ảnh hưởng đúng; rút accepted hủy kèo |
| J-09 | B: tạo 1–5 sân + 1–5 giờ → nhiều đội nhận → sửa gói → hai bên xác nhận | Hai danh sách độc lập; mỗi Pairing có gói riêng; đúng một bên thắng |
| J-10 | B: bên kia đổi revision ngay trước xác nhận hoặc ghép với đội khác | Tải gói mới, không tự confirm; hiển thị conflict/superseded |
| J-11 | Hủy A OPEN có pending, B OPEN có Pairing, CONFIRMED ở các mốc thời gian | Điều kiện/hậu quả khác nhau; sau bắt đầu chặn theo spec |
| J-12 | Tìm đá lẻ có GPS/từ chối/chưa có ward/không có kết quả | Mặc định 3 km, phạm vi 1–20; fallback đúng và có đường hoàn thiện hồ sơ |
| J-13 | Ứng tuyển → pending chat → captain xem uy tín/duyệt → user mở đơn | Trước duyệt ẩn sân, sau duyệt có sân; không lộ qua preview/chat header/link |
| J-14 | Slot cuối, trùng giờ, thành viên đội, từ chối, rút pending/approved | Không duyệt quá slot; không revoke approved; rút mở lại tin khi phù hợp |
| J-15 | Captain tạo tin/sửa số lượng/đóng → thêm trận ngoài/sửa/hủy/thêm vị trí | Chặn field đúng điều kiện; trạng thái tin/đơn cập nhật |
| J-16 | Từ form kèo → tìm/thêm sân → quay lại → ảnh/report sân | Giữ form; giới hạn ảnh/quyền đúng; không có booking |
| J-17 | Chat unread/read, gửi lỗi/retry, room read-only, reconnect/retention | Không mất ngữ cảnh hoặc gửi được khi canSend=false |
| J-18 | Notification đọc một/tất cả, catalog type → đích, đích đã đổi/mất quyền | Count cập nhật; không giả định target luôn xem được |
| J-19 | Sau trận: tỷ số/sao hoặc không đến → xem given/received | Chưa gửi không biết bên kia đã đánh giá; không sửa/xóa đã gửi |
| J-20 | Hai phía mercenary đánh giá/report; xem uy tín cá nhân/đội | Sao/điểm/tỷ lệ/badge không bị trộn sai |
| J-21 | Report các loại → NOSHOW tranh chấp còn/hết 24h → admin quyết định | Chỉ NOSHOW tranh chấp; văn bản, ghi chú bắt buộc; quyết định cuối |
| J-22 | Admin đội, lịch sử user xóa, venue report/inactive/reactivate, sports | Đúng quyền và che/hiện dữ liệu theo endpoint, USER không truy cập |
| J-23 | 360/390/430 px, màn thấp, tên dài, form bàn phím, Back/chuyển tab | Không che CTA/cắt dấu, giữ dữ liệu/scroll, target chạm phù hợp |
| J-24 | Reset → đổi nhân vật → đồng hồ qua hạn/trận/retention | Dataset nhất quán; không còn tác động scenario trước |

### 14.1. Acceptance criteria dạng Given/When/Then

- **AC-P01 [AUTH]:** Given user mới xác thực thành công, When tiếp tục, Then mở form hoàn thiện hồ sơ dùng lại PROF-02; user cũ về điểm đến phù hợp.
- **AC-P02 [TEAM]:** Given user còn là captain của ít nhất một đội chưa giải tán, When xóa tài khoản, Then bị chặn và có đường xử lý từng đội.
- **AC-P03 [MATCH-A]:** Given nhiều request pending, When chủ kèo chấp nhận một request, Then một request accepted, kèo confirmed và các request khác superseded.
- **AC-P04 [PAIRING]:** Given đã có xác nhận, When gói thực sự thay đổi, Then xác nhận hai bên bị xóa; revision cũ không được tự gửi lại với revision mới.
- **AC-P05 [PRIVACY]:** Given đơn đá lẻ chưa approved, When mở mọi phần của tin/đơn/preview, Then không thấy tên/địa chỉ/tọa độ sân hoặc link tiết lộ chúng.
- **AC-P06 [MERCENARY]:** Given đơn approved rút hợp lệ làm thiếu người, When xác nhận rút, Then điểm cá nhân và tin tuyển cập nhật theo spec; pending rút không bị trừ.
- **AC-P07 [RATING]:** Given bên mình chưa đánh giá, When xem đánh giá trận, Then không suy ra đối phương đã/chưa đánh giá từ nội dung UI.
- **AC-P08 [DISPUTE]:** Given report không phải NOSHOW hoặc đã quá hạn, When mở chi tiết, Then không có hành động tranh chấp khả dụng.
- **AC-P09 [NOTIFICATION]:** Given thông báo trỏ tới đối tượng không còn quyền xem, When mở, Then có giải thích và đường quay lại thay vì màn trống.
- **AC-P10 [DEMO]:** Given reset một scenario, When chuyển qua hai phía và thực hiện hành động, Then cả hai quan sát cùng dữ liệu và state transition nhất quán.

## 15. Definition of Done và cách báo cáo

Chỉ đánh dấu “prototype đầy đủ” khi:

- [ ] Toàn bộ ID mục 6 có route/state hoặc ghi rõ dùng chung layout nào; không có mục bắt buộc bị bỏ vì P1/P2.
- [ ] Tất cả J-01..J-24 đã chạy; lỗi còn lại được phân loại và không làm hỏng hành trình/quyền/riêng tư/trạng thái.
- [ ] API/UC traceability có nguồn thật; dependency và proposal tách khỏi chức năng có contract.
- [ ] Mọi button/link hiển thị có hành vi hợp lệ; không dùng toast đơn thuần thay xử lý dữ liệu.
- [ ] Hai phía cùng dữ liệu; reset/đồng hồ/đổi nhân vật hoạt động.
- [ ] Form có validation và giữ input sau lỗi; cập nhật trạng thái không chỉ ở màn hiện tại.
- [ ] Đã rà trực quan các layout chuẩn và các state quan trọng ở viewport mục 9.
- [ ] Có đầy đủ artifact mục 12, mở/chạy theo README được.
- [ ] Không sửa source Stitch/export/app/spec hoặc gửi dữ liệu thật.
- [ ] Phần nào chưa kiểm tra do giới hạn công cụ được ghi cụ thể; không gắn nhãn “đã kiểm thử” cho phần đó.

Báo cáo cuối cho chủ dự án:
1. Link mở prototype và cách chạy/reset.
2. Inventory tổng số màn/sheet/state đã làm, không cộng bản desktop hoặc ảnh trùng thành tính năng mới.
3. Hành trình đã kiểm tra và bằng chứng.
4. Dependency/quyết định cần chốt trước tích hợp backend/UI thật.
5. Link ui-handoff/design-system để bắt đầu Compose.
6. Nếu còn việc bắt buộc chưa xong, nói rõ “chưa hoàn tất” và liệt kê ID; không dùng “xong bản đầy đủ, phần còn lại để sau”.

## 16. Prompt tiếp tục khi đổi AI hoặc hết phiên

~~~text
Tiếp tục prototype ArenaFlow theo docs/prototype/PROTOTYPE_HANDOFF.md.
Đọc docs/prototype/complete/README.md, progress.md, screen-inventory.md, decisions.md và qa-report.md trước.
Kiểm tra artifact hiện có, không tạo lại nguồn Stitch hoặc làm lại phần đã đạt nghiệm thu.
Hoàn thành các ID/hành trình còn thiếu theo thứ tự gói, cập nhật tiến độ và chạy kiểm tra.
Giữ nguyên mọi ranh giới và dependency trong brief; báo rõ phần đã/chưa kiểm tra.
~~~ 
