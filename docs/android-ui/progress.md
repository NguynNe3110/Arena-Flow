# Android UI & Architecture Implementation Progress

> **Updated 25/11/2026:** Completed full ArenaFlow Android implementation across `:feature:base`, all 11 feature modules (`auth`, `profile`, `team`, `venue`, `match`, `pairing`, `mercenary`, `chat`, `notification`, `reputation`, `admin`), and `:app` shell.

## Architecture & Variant Separation
- **Clean Architecture + Koin + Konsist**: All feature modules live under `com.igorwojda.showcase.feature.<name>.*` with strict `data` / `domain` / `presentation` layers, `BaseViewModel`, alphabetical single-`invoke` `UseCase`s, Detekt, and Spotless formatting.
- **ArenaFlowSpec REST Contracts**: All Retrofit interfaces, DTOs, envelopes (`ApiEnvelope`, `PaginatedEnvelope`, `CursorListDto`), Vietnam local `YYYY-MM-DDTHH:mm:ss` datetimes, `204` handling, and domain error codes (`AppError.DomainError`) match `docs/ArenaFlow-spec/api/**`.
- **Debug vs Release Build Separation**:
  - `src/debug/` contains `ArenaInMemoryStore`, `ArenaSeedFixtures`, `DebugSessionRepositoryImpl`, `Mock*RetrofitService` implementations across all 11 features, and `DebugDemoControlOverlay` (persona switcher, simulated API errors, reset demo fixtures).
  - `src/release/` contains `ReleaseSessionRepositoryImpl`, pure `Retrofit.create(...)` bindings across all 11 features, and a no-op `DebugDemoControlOverlay`, with zero mock data or fixtures.

## Module & Screen Status
| Module | Screens & Coverage | Debug Mock | Release Retrofit | Unit Tests |
| --- | --- | --- | --- | --- |
| `:feature:base` | Theme, shared components, Session, API envelopes | `ArenaInMemoryStore` | `ReleaseSessionRepositoryImpl` | 15 passing |
| `:feature:auth` | Onboarding, LoginMethod, PhoneInput, OtpVerify (`APP-02`, `AUTH-01..06`) | `MockAuthRetrofitService` | `AuthRetrofitService` | 13 passing |
| `:feature:profile` | MyProfile, ProfileEdit, AccountMenu (`PROF-01..06`) | `MockProfileRetrofitService` | `ProfileRetrofitService` | 16 passing |
| `:feature:team` | TeamHub, TeamDetail, TeamForm, TeamManage, TeamHistory, TeamInvitation (`TEAM-01..14`) | `MockTeam*RetrofitService` | `Team*RetrofitService` | 22 passing |
| `:feature:venue` | VenuePicker, VenueDetail, VenueForm, VenuePhotos (`VEN-01..07`) | `MockVenue*RetrofitService` | `Venue*RetrofitService` | 20 passing |
| `:feature:match` | MatchFeed, MatchDetail, MatchCreate, MatchReview (`MATCH-01..14`) | `MockMatch*RetrofitService` | `Match*RetrofitService` | 21 passing |
| `:feature:pairing` | PairingRoom (`PAIR-01..08`) | `MockPairingRetrofitService` | `PairingRetrofitService` | 13 passing |
| `:feature:mercenary` | MercenaryList, MercenaryDetail, MercenaryCreate (`MERC-01..12`) | `MockMercenary*RetrofitService` | `Mercenary*RetrofitService` | 19 passing |
| `:feature:chat` | ChatInbox, ChatRoom (`CHAT-01..05`) | `MockChatRetrofitService` | `ChatRetrofitService` | 14 passing |
| `:feature:notification` | NotificationList (`NOTIF-01..03`) | `MockNotificationRetrofitService` | `NotificationRetrofitService` | 10 passing |
| `:feature:reputation` | PostMatchReport, ReputationHistory (`REP-01..10`) | `MockReputation*RetrofitService` | `Reputation*RetrofitService` | 12 passing |
| `:feature:admin` | AdminConsole (`ADM-01..09`) | `MockAdmin*RetrofitService` | `Admin*RetrofitService` | 19 passing |
| `:app` | 5-tab BottomNav (`Tìm kèo`, `Đá lẻ`, `Đội bóng`, `Tin nhắn`, `Hồ sơ`), Auth Gate, Demo Overlay | `DebugDemoControlOverlay` | No-op overlay | Konsist + Debug/Release builds |
