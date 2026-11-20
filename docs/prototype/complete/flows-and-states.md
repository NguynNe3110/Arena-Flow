# ArenaFlow Prototype — Sơ Đồ Luồng & Chuyển Trạng Thái (Flows & State Transitions)

> Ngày lập: 18/11/2026 · Ngôn ngữ: Tiếng Việt · Phiên bản: 1.0-complete  
> Tài liệu thuộc bộ bàn giao UI Prototype ArenaFlow trong `docs/prototype/complete/`.

---

## 1. Cấu Trúc Điều Hướng Tổng Thể & Quy Tắc Phím Quay Lại (Navigation & Back Stack)

### 1.1. Sơ Đồ Điều Hướng Cấp Cao (High-Level Navigation Architecture)

```mermaid
flowchart TD
    Launch([Khởi động APP-01]) --> SessionCheck{Kiểm tra JWT?}
    SessionCheck -- Hết hạn/Chưa có --> OnboardingCheck{Mở lần đầu?}
    SessionCheck -- Còn hạn --> MainShell[Khu vực chính: MainShell]
    
    OnboardingCheck -- Đúng --> Onboarding[APP-02 Onboarding]
    OnboardingCheck -- Sai --> Login[AUTH-01 Chọn đăng nhập]
    Onboarding --> Login
    
    Login --> Phone[AUTH-02 Nhập SĐT]
    Phone --> OTP[AUTH-03 Nhập OTP]
    OTP --> PostAuthCheck{User mới? AC-P01}
    
    PostAuthCheck -- Đúng --> CompleteProfile[PROF-02 Hoàn thiện hồ sơ]
    PostAuthCheck -- Sai --> MainShell
    CompleteProfile --> MainShell

    subgraph MainShell [4 Tabs Điều Hướng Cốt Lõi + Profile]
        direction TB
        TabMatches[Tab Tìm kèo: MATCH-01]
        TabPlayers[Tab Đá lẻ: MERC-01]
        TabInbox[Tab Tin nhắn: CHAT-01]
        TabTeams[Tab Đội bóng: TEAM-01]
        ProfileEntry[Lối vào Profile: PROF-01]
    end

    TabMatches --> MatchDetail[MATCH-03 / MATCH-04 Chi tiết kèo]
    TabPlayers --> MercDetail[MERC-02 Chi tiết tin tuyển]
    TabInbox --> ChatRoom[CHAT-02 / CHAT-03 Phòng chat]
    TabTeams --> TeamDetail[TEAM-04 Chi tiết đội]
    ProfileEntry --> AdminPortal[ADM-01 Admin Portal - Chỉ ADMIN]
```

### 1.2. Ma Trận Xử Lý Phím Quay Lại (Back Button Behavior Matrix)

| Ngữ cảnh hiện tại | Hành động khi nhấn phím Back / Nút Quay lại | Hành vi dữ liệu & Giao diện |
|---|---|---|
| **Đang mở Modal Bottom Sheet hoặc Dialog** | **Đóng Sheet / Dialog trước tiên** | Không thay đổi màn hình bên dưới; giữ nguyên vị trí cuộn trang. |
| **Form đang nhập dở có thay đổi (`isDirty=true`)** | **Mở Dialog xác nhận hủy thay đổi** | Cảnh báo: "Bạn có thay đổi chưa lưu, bạn có chắc chắn muốn thoát?". Nếu chọn Có -> Hủy và Back. Nếu chọn Không -> Ở lại form. |
| **Chọn sân bóng từ form (`VEN-01`)** | **Quay lại Form tạo ban đầu (`MATCH-05` / `EXT-01`)** | **Bảo lưu 100% dữ liệu đã nhập trước đó** (ngày, giờ, chi phí, trình độ), tự động điền ID sân vừa chọn vào trường sân đấu. |
| **Phiên hết hạn khi đang thao tác (`AUTH-06`)** | **Đưa về màn hình đăng nhập, lưu `redirectUrl`** | Sau khi xác thực thành công -> Tự động điều hướng trở lại đúng màn hình/sheet đích ban đầu nếu còn hợp lệ. |
| **Chuyển tab dưới thanh Bottom Navigation** | **Bảo tồn trạng thái từng tab** | Mỗi tab duy trì back stack độc lập, giữ nguyên trạng thái cuộn (scroll position) và bộ lọc đang áp dụng. |
| **Khu vực Quản trị Admin (`ADM-01..09`)** | **Thoát ra màn hình Hồ sơ cá nhân (`PROF-01`)** | Có TopAppBar và Breadcrumb riêng; cấm user thường quay lại bằng phím cứng. |

---

## 2. Các Máy Trạng Thái Cốt Lõi (Core State Machines)

### 2.1. Vòng Đời Kèo Đấu (Match Lifecycle State Machine)

Kèo đấu trải qua các trạng thái từ khi tạo đến khi kết thúc hoặc bị hủy:

```mermaid
stateDiagram-v2
    [*] --> OPEN: Tạo bài kèo A hoặc B (MATCH-05)
    
    OPEN --> CONFIRMED: Kèo A duyệt yêu cầu (AC-P03) / Kèo B 2 bên chốt gói (AC-P04)
    OPEN --> EXPIRED: Hết hạn bài đăng (trước giờ bắt đầu 2h)
    OPEN --> CANCELLED: Chủ kèo tự hủy (MATCH-12)
    
    CONFIRMED --> IN_PROGRESS: Đến giờ bắt đầu trận đấu
    CONFIRMED --> CANCELLED: Hủy trước giờ bắt đầu (Bị phạt uy tín)
    
    IN_PROGRESS --> COMPLETED: Hết giờ thi đấu trận bóng
    COMPLETED --> ARCHIVED: Sau khi cả 2 bên hoàn tất đánh giá (REP-01/02)
    
    CANCELLED --> [*]
    EXPIRED --> [*]
    ARCHIVED --> [*]
```

*Quy tắc nghiệp vụ cốt lõi:*
- **Chặn hủy sau giờ bắt đầu:** Khi trận đấu đã đến giờ bóng lăn, không bên nào được quyền bấm Hủy kèo.
- **Hủy có phạt uy tín:** Hủy kèo khi đang `CONFIRMED` sẽ kích hoạt tự động trừ điểm uy tín đội bóng (-15 điểm).
- **Trạng thái COMPLETED:** Mở cổng đánh giá đối phương (REP-01).

---

### 2.2. Vòng Đời Yêu Cầu Giao Lưu Kèo A (Match-A Request State Machine)

```mermaid
stateDiagram-v2
    [*] --> PENDING: Đội đối thủ gửi yêu cầu (MATCH-03)
    
    PENDING --> ACCEPTED: Chủ kèo A chấp nhận (MATCH-09/10) [AC-P03]
    PENDING --> REJECTED: Chủ kèo A từ chối (MATCH-09)
    PENDING --> WITHDRAWN: Đội gửi tự nguyện rút (MATCH-11) (Không phạt điểm)
    PENDING --> SUPERSEDED: Chủ kèo chấp nhận 1 đội khác -> Đội này bị thay thế
    PENDING --> EXPIRED: Kèo A hết hạn hoặc bị hủy
    
    ACCEPTED --> WITHDRAWN_CANCELLED: Rút sau khi đã Accepted (Tương đương hủy kèo, Phạt uy tín)
    
    ACCEPTED --> [*]
    REJECTED --> [*]
    WITHDRAWN --> [*]
    SUPERSEDED --> [*]
    EXPIRED --> [*]
    WITHDRAWN_CANCELLED --> [*]
```

*Tiêu chí Acceptance Criteria AC-P03:*
> Khi Chủ kèo A chấp nhận một yêu cầu PENDING bất kỳ, yêu cầu đó chuyển thành `ACCEPTED`, kèo đấu chuyển sang `CONFIRMED`, và toàn bộ các yêu cầu PENDING khác của kèo này lập tức chuyển thành `SUPERSEDED`.

---

### 2.3. Vòng Đời Thương Lượng Kèo B & Chốt Gói (Pairing B Negotiation State Machine)

Mỗi đội nhận kèo B đều có một phòng thương lượng độc lập với Chủ kèo:

```mermaid
stateDiagram-v2
    [*] --> NEGOTIATING: Đội nhận bài B (MATCH-04) mở phòng (PAIR-03)
    
    state NEGOTIATING {
        [*] --> Draft: Chọn sân/giờ ban đầu
        Draft --> ConfirmedA: Chủ kèo bấm xác nhận gói
        Draft --> ConfirmedB: Đội khách bấm xác nhận gói
        
        ConfirmedA --> Draft: Đối phương sửa sân/chi phí (Xóa xác nhận, Revision++) [AC-P04]
        ConfirmedB --> Draft: Đối phương sửa sân/chi phí (Xóa xác nhận, Revision++) [AC-P04]
        
        ConfirmedA --> BothConfirmed: Đội khách bấm xác nhận gói
        ConfirmedB --> BothConfirmed: Chủ kèo bấm xác nhận gói
    }
    
    BothConfirmed --> CONFIRMED: Ghép thành công! Kèo chuyển sang CONFIRMED
    
    NEGOTIATING --> SUPERSEDED: Chủ kèo chốt gói với 1 đội khác trong phòng khác
    NEGOTIATING --> CANCELLED: Chủ kèo hủy bài B hoặc Đội khách rời phòng
    NEGOTIATING --> EXPIRED: Hết hạn thời gian bài đăng B
```

*Tiêu chí Acceptance Criteria AC-P04:*
> Nếu một bên đã bấm Xác nhận gói, bất kỳ thao tác chỉnh sửa phương án Sân/Giờ (`PAIR-04`) hoặc Chi phí (`PAIR-05`) từ phía đối phương sẽ lập tức làm tăng `revision` gói, hiển thị cảnh báo `PAIR-07` và **xóa toàn bộ xác nhận của cả hai bên**, buộc người dùng phải xem lại và tự tay bấm xác nhận lại.

---

### 2.4. Vòng Đời Đơn Ứng Tuyển & Tuyển Người Đá Lẻ (Mercenary State Machine)

#### a) Vòng đời Tin Tuyển của Đội (Recruitment Post):
```mermaid
stateDiagram-v2
    [*] --> OPEN: Đăng tin tuyển vị trí (MERC-08)
    OPEN --> FILLED: Đã duyệt đủ số lượng ứng viên (approvedCount == quantity)
    FILLED --> OPEN: Có ứng viên đã duyệt rút đơn (MERC-06) làm thiếu người [AC-P06]
    OPEN --> CLOSED: Đội trưởng chủ động đóng tin sớm (MERC-12)
    OPEN --> EXPIRED: Đến giờ bắt đầu trận đấu
    FILLED --> EXPIRED: Đến giờ bắt đầu trận đấu
```

#### b) Vòng đời Đơn Ứng Tuyển (Application):
```mermaid
stateDiagram-v2
    [*] --> PENDING: Ứng viên nộp đơn (MERC-03)
    
    PENDING --> APPROVED: Đội trưởng duyệt đơn (MERC-11) (Mở khóa tên sân AC-P05)
    PENDING --> REJECTED: Đội trưởng từ chối đơn
    PENDING --> WITHDRAWN: Ứng viên tự rút đơn (MERC-06) (0 điểm phạt)
    
    APPROVED --> WITHDRAWN_PENALTY: Ứng viên rút đơn sau khi duyệt (-10 điểm uy tín AC-P06)
    
    APPROVED --> COMPLETED: Trận đấu kết thúc -> Mở đánh giá 2 chiều (REP-03)
```

*Tiêu chí Acceptance Criteria AC-P05 & AC-P06:*
- **AC-P05 [PRIVACY]:** Khi đơn ở trạng thái `PENDING`, tuyệt đối không hiển thị tên sân, số nhà hoặc tọa độ trong `MERC-02`, `MERC-04` hoặc header phòng chat `CHAT-03`. Chỉ sau khi `APPROVED`, các trường thông tin này mới được mở khóa.
- **AC-P06 [WITHDRAWAL]:** Rút đơn lúc `PENDING` không bị trừ uy tín. Rút đơn khi đã `APPROVED` tự động trừ 10 điểm uy tín cá nhân và làm tin tuyển từ `FILLED` trở lại `OPEN`.

---

### 2.5. Vòng Đời Đội Bóng & Quyền Đội Trưởng (Team Lifecycle)

```mermaid
stateDiagram-v2
    [*] --> ACTIVE: Tạo đội bóng thành công (TEAM-03)
    
    ACTIVE --> TRANSFER_CAPTAIN: Đội trưởng chuyển quyền (TEAM-09)
    TRANSFER_CAPTAIN --> ACTIVE: Chuyển giao tức thì, không cần bên nhận duyệt
    
    ACTIVE --> DISBANDED: Đội trưởng giải tán đội (TEAM-12)
    DISBANDED --> [*]: Hủy toàn bộ kèo, khóa vĩnh viễn (TEAM-13)
```

*Tiêu chí Acceptance Criteria AC-P02:*
> Nếu người dùng còn là Captain của ít nhất một đội bóng đang ở trạng thái `ACTIVE`, khi thực hiện hành vi Xóa tài khoản (`PROF-04`), hệ thống lập tức chặn lại và mở màn hình `PROF-05`, liệt kê các đội bóng liên quan và hướng dẫn chuyển quyền hoặc giải tán đội trước khi được xóa tài khoản.

---

### 2.6. Vòng Đời Báo Cáo Vi Phạm & Tranh Chấp (Report & Dispute State Machine)

```mermaid
stateDiagram-v2
    [*] --> SUBMITTED: Gửi báo cáo vi phạm (REP-06)
    
    state SUBMITTED {
        [*] --> CheckType
        CheckType --> NonNoShow: Vi phạm Bạo lực / Gian lận / Thái độ
        CheckType --> NoShow: Vi phạm Không đến (NO-SHOW)
    }
    
    NonNoShow --> AUTO_ACCEPTED: Không có cơ chế tranh chấp -> Trừ uy tín trực tiếp
    
    NoShow --> PENDING_DISPUTE: Đếm ngược 24 giờ cho bên bị tố cáo
    
    PENDING_DISPUTE --> EXPIRED_DISPUTE: Quá 24h không gửi khiếu nại -> Tự động xác nhận phạt
    PENDING_DISPUTE --> IN_REVIEW: Bên bị tố cáo gửi đơn tranh chấp kèm bằng chứng (REP-09) [AC-P08]
    
    IN_REVIEW --> RESOLVED_UPHELD: Admin ra phán quyết: Bác đơn khiếu nại (Giữ phạt) (ADM-06)
    IN_REVIEW --> RESOLVED_DISMISSED: Admin ra phán quyết: Chấp thuận khiếu nại (Hủy phạt) (ADM-06)
```

*Tiêu chí Acceptance Criteria AC-P07 & AC-P08:*
- **AC-P07 [BLIND RATING]:** Khi một bên vào xem kết quả đánh giá trận đấu (`REP-02`), nếu bên mình chưa gửi đánh giá (`REP-01`), giao diện hoàn toàn che giấu trạng thái nộp bài của đối phương. Chỉ khi cả hai bên đã nộp xong mới hiển thị công khai kết quả.
- **AC-P08 [DISPUTE LIMIT]:** Quyền gửi đơn tranh chấp (`REP-09`) **chỉ áp dụng duy nhất cho loại báo cáo NO-SHOW** và phải gửi trong vòng 24 giờ kể từ thời điểm báo cáo được lập. Mọi loại báo cáo khác đều bị khóa nút tranh chấp.

---

## 3. Sơ Đồ Chi Tiết 24 Hành Trình Nghiệm Thu (User Journeys J-01..J-24)

### 3.1. Luồng Xác Thực, Onboarding & Tài Khoản (J-01, J-02, J-03)

```mermaid
sequenceDiagram
    autonumber
    actor User as Người dùng
    participant App as App Shell (APP-01)
    participant Auth as Auth & OTP (AUTH-01..03)
    participant Profile as Hồ sơ (PROF-01..02)
    participant Store as AppStore (State)

    Note over User, App: Hành trình J-01: Mở mới -> Onboarding -> OTP -> Hoàn thiện hồ sơ
    User->>App: Mở ứng dụng lần đầu
    App->>User: Hiển thị Onboarding (APP-02)
    User->>App: Bấm "Bắt đầu ngay"
    App->>Auth: Mở AUTH-01 (Chọn Google/FB/SĐT)
    User->>Auth: Nhập số điện thoại (AUTH-02)
    Auth->>User: Gửi mã OTP, mở AUTH-03
    User->>Auth: Nhập OTP "123456"
    Auth->>Store: Xác thực thành công (isNewUser=true)
    Store->>Profile: Chuyển hướng tới PROF-02 (AC-P01)
    User->>Profile: Nhập Tên, Vị trí, Ngày sinh, Phường/Xã
    User->>Profile: Bấm "Lưu hồ sơ"
    Profile->>Store: Cập nhật thông tin người dùng
    Store->>App: Điều hướng vào Màn hình chính (MainShell)
```

---

### 3.2. Luồng Quản Trị Đội Bóng & Chuyển Quyền (J-04, J-05, J-06)

```mermaid
sequenceDiagram
    autonumber
    actor Capt as Captain Alpha
    actor Mem as Thành viên Beta
    participant Team as Quản trị Đội (TEAM-01..14)
    participant Notif as Thông báo (NOTIF-01)
    participant Store as AppStore

    Note over Capt, Mem: Hành trình J-04 & J-05: Mời thành viên -> Phản hồi -> Chuyển quyền
    Capt->>Team: Tạo đội mới "FC Chiến Binh" (TEAM-03)
    Team->>Store: Lưu đội mới, vai trò CAPTAIN
    Capt->>Team: Mở Sheet Mời thành viên (TEAM-07)
    Capt->>Team: Chọn gửi lời mời cho Member Beta
    Store->>Notif: Tạo thông báo TEAM_INVITATION cho Beta
    
    Mem->>Notif: Mở thông báo lời mời (TEAM-08)
    Mem->>Team: Bấm "Chấp nhận lời mời"
    Team->>Store: Thêm Beta vào danh sách thành viên đội (TEAM-06)
    
    Capt->>Team: Mở Sheet Chuyển quyền đội trưởng (TEAM-09)
    Capt->>Team: Chọn Member Beta làm Đội trưởng mới
    Team->>Store: Cập nhật Beta thành CAPTAIN, Alpha thành MEMBER (Tức thì)
    Capt->>Team: Giao diện Alpha tự động cập nhật mất nút quản trị
```

---

### 3.3. Luồng Tạo Kèo, Ghép Đội & Xung Đột Revision (J-07, J-09, J-10)

```mermaid
sequenceDiagram
    autonumber
    actor Host as Captain Alpha (Chủ kèo)
    actor Guest as Captain Beta (Đối thủ)
    participant Match as Kèo & Pairing (MATCH/PAIR)
    participant Store as AppStore

    Note over Host, Guest: Hành trình J-09 & J-10: Kèo B -> Thương lượng -> Sửa gói -> Xóa confirm
    Host->>Match: Tạo kèo B (MATCH-05) với 2 sân và 2 khung giờ
    Guest->>Match: Xem chi tiết kèo B (MATCH-04), bấm "Nhận kèo"
    Match->>Store: Khởi tạo phòng thương lượng riêng (PAIR-03)
    
    Host->>Match: Vào phòng PAIR-03, chọn Sân 1 + Khung giờ 19:00 (PAIR-04)
    Host->>Match: Bấm "Xác nhận gói" (PAIR-06) -> Host đã xác nhận
    
    Note over Guest: Lúc này Guest sửa chi phí tiền sân
    Guest->>Match: Mở Sheet Sửa chi phí (PAIR-05), sửa chia tiền 60-40
    Guest->>Match: Bấm "Lưu chi phí mới"
    Match->>Store: Tăng revision (rev=2), XÓA XÁC NHẬN CỦA HOST (AC-P04)
    Store->>Host: Hiển thị cảnh báo Xung đột gói PAIR-07 (Revision vừa đổi)
    Host->>Match: Xem lại gói mới và bấm "Xác nhận lại"
    Guest->>Match: Bấm "Xác nhận gói"
    Match->>Store: Cả 2 bên đều xác nhận -> Kèo B chuyển sang CONFIRMED!
```

---

### 3.4. Luồng Tuyển Người Đá Lẻ & Bảo Vệ Riêng Tư Địa Điểm (J-12, J-13, J-14)

```mermaid
sequenceDiagram
    autonumber
    actor Merc as Cầu thủ đá lẻ
    actor Capt as Captain Alpha
    participant UI as Giao diện Đá lẻ (MERC-01..12)
    participant Store as AppStore

    Note over Merc, Capt: Hành trình J-13 & J-14: Bảo vệ địa điểm trước duyệt & Rút đơn
    Capt->>UI: Đăng tin tuyển 1 Tiền vệ (MERC-08) tại Sân Chuyên Việt
    Merc->>UI: Tìm kiếm đá lẻ bằng GPS bán kính 3km (MERC-01)
    Merc->>UI: Mở chi tiết tin tuyển (MERC-02)
    Note over Merc, UI: AC-P05: UI chỉ hiện "Quận Hải Châu", ẨN TÊN SÂN CHUYÊN VIỆT
    Merc->>UI: Bấm "Nộp đơn ứng tuyển" (MERC-03) -> Đơn PENDING
    
    Capt->>UI: Mở danh sách ứng viên (MERC-10), xem điểm uy tín của Merc
    Capt->>UI: Bấm "Duyệt ứng viên" (MERC-11) -> Đơn APPROVED
    
    Merc->>UI: Mở Chi tiết đơn (MERC-05)
    Note over Merc, UI: Đã APPROVED: UI MỞ KHÓA hiện rõ "Sân bóng Chuyên Việt, Số 2 Huỳnh Thúc Kháng"
    
    Note over Merc: Merc có việc bận đột xuất muốn rút đơn
    Merc->>UI: Bấm "Rút đơn ứng tuyển" (MERC-06)
    UI->>Merc: Cảnh báo rút Approved sẽ bị trừ 10 điểm uy tín cá nhân (AC-P06)
    Merc->>UI: Xác nhận rút đơn
    UI->>Store: Trừ 10 điểm uy tín, Đổi đơn sang WITHDRAWN, Tin tuyển mở lại 1 slot (FILLED -> OPEN)
```

---

### 3.5. Luồng Sau Trận, Đánh Giá Ẩn & Tranh Chấp 24h (J-19, J-21)

```mermaid
sequenceDiagram
    autonumber
    actor CaptA as Captain Alpha
    actor CaptB as Captain Beta
    actor Admin as Quản trị viên (ADM)
    participant Rep as Uy tín & Báo cáo (REP-01..10)
    participant Store as AppStore

    Note over CaptA, CaptB: Hành trình J-19: Đánh giá ẩn (Blind Rating)
    CaptA->>Rep: Vào màn Đánh giá trận đấu (REP-01)
    CaptA->>Rep: Chấm 5 sao cho Đội Beta, gửi nhận xét tích cực
    
    CaptB->>Rep: Vào xem Đánh giá trận (REP-02)
    Note over CaptB, Rep: AC-P07: Giao diện che giấu hoàn toàn, CaptB không biết CaptA đã đánh giá hay chưa
    CaptB->>Rep: CaptB nộp đánh giá của mình (3 sao)
    Note over CaptA, CaptB: Cả 2 bên đã nộp xong -> UI công khai kết quả đánh giá cả 2 phía!
    
    Note over CaptA, Admin: Hành trình J-21: Báo cáo NO-SHOW & Tranh chấp 24h
    CaptA->>Rep: Gửi báo cáo Đội Beta "Không đến thi đấu" (NO-SHOW) (REP-06)
    Rep->>Store: Khởi tạo báo cáo vi phạm, đếm ngược 24 giờ
    CaptB->>Rep: Nhận thông báo bị tố cáo, mở Chi tiết báo cáo (REP-08)
    CaptB->>Rep: Bấm "Khiếu nại tranh chấp" (REP-09) [AC-P08], nhập bằng chứng văn bản
    
    Admin->>Rep: Mở Hàng đợi tranh chấp đội (ADM-04)
    Admin->>Rep: Xem đối chất giữa Alpha và Beta, mở Sheet Quyết định (ADM-06)
    Admin->>Rep: Nhập lý do (250 ký tự), bấm "Chấp thuận khiếu nại" -> Hủy báo cáo vi phạm!
```
