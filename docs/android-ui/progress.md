# Android UI implementation

Source: docs/prototype/complete, with current API contracts taking precedence over prototype shortcuts.
Base: main at 6c1601b. Branches are cumulative; each delivery contains two full-screen layouts and their associated sheets/variants.
Existing staged prototype icons belong to the user and are excluded from UI commits.

## Direction contract

THESIS: Implement the supplied ArenaFlow prototype as native task-focused Compose screens.
OWN-WORLD: Preserve the existing Be Vietnam Pro typography, forest-green Material roles, pale green surfaces, 8dp controls and 12dp outlined cards.
STORY: Players scan open matches, distinguish fixed venue from negotiable venue/time, inspect details and select the team acting in a request.
FIRST VIEWPORT: List title, mock-data disclosure, filters, type chips and match cards; detail presents status, two teams and venue/time before cost agreement. Native system and Back navigation remain available.
FORM: seed: none (pinned). User: “dựa vào prototype build hoàn thiện các màn hình”.
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance.
The established design authority is docs/prototype/complete/design-system.md; this extension preserves it. No new design system or raster assets are introduced.

## Deliveries

| Batch | Branch | Screens | State |
| --- | --- | --- | --- |
| 01 | ui/01-match-list-detail | MATCH-01 list; shared MATCH-03/04 detail | Implemented; verification and review in progress |
| 02 | ui/02-match-create-review | MATCH-05 create; MATCH-06 review | Pending |
| Later | cumulative branches | Authentication/profile, teams, negotiation, players, venues, chat, notifications, reputation, admin from the prototype inventory | Pending |

## Batch 01 scope and limitations

- Koin, repository/use-case boundaries and lifecycle-aware state collection are preserved.
- Detail has its own endpoint-shaped DTO, mapper, repository method and mock fixture. API summary data is not used to fabricate missing detail fields.
- Filter sheet supports type, format and available skill labels; reset and empty results work. Geographic/time filters and “my team” depend on subsequent team/location work.
- Both fixed-venue A and multi-venue/time B use the same detail layout.
- Request/team picker and withdrawal are explicitly local UI demonstrations. They do not contact the backend. B cannot be withdrawn through the A request action.
- Cost agreement follows MATCH-001: no invented cost on the list; A has no deposit; B's cost is agreed in a later negotiation flow.
- Authentication, role-based host management, actual request APIs and full B negotiation remain for subsequent deliveries.
- Other existing bottom tabs still open starter-project screens until their own deliveries.

## Verification

- Debug APK assembles; 8 match unit tests and 9 base unit tests pass.
- Repository test checks list/detail time consistency, costs, B options and missing-ID classification.
- ViewModel tests cover retry, request restoration, duplicate submit, closed-match guard and blocking pairing withdrawal.
- Pixel_4 emulator / Android 10: list to detail, displayed costs, team picker, send request, pending state, withdrawal confirmation, back, format filter, empty state and reset exercised.
- Native screenshots: .impeccable/review/01-list-phone.png and 01-detail-phone.png.
- Compose Preview failed to load a newly compiled helper class; runtime APK was installed and checked instead.
- Tablet, dark mode and font scaling are not yet verified. No web-only detector is used on native Compose.
