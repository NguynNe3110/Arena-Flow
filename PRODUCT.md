# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Amateur football players, team captains, solo mercenary players, and system administrators in Vietnam (primarily urban areas such as Ho Chi Minh City) who organize, join, negotiate, and review casual 5/7/11-a-side matches.

## Product Purpose

ArenaFlow connects amateur football teams and solo players to discover open matches, negotiate flexible venue/time packages, recruit missing positions, communicate per match/application, and build transparent team & personal reputation scores.

## Positioning

Unlike booking platforms, ArenaFlow does not book pitches, collect, hold, or refund pitch fees/deposits. Cost splits and deposits are external agreements between teams, paired with structured Match A (fixed venue) and Match B (multi-venue/time negotiation with revision locks), blind post-match ratings, and privacy-guarded mercenary recruitment (hiding exact pitch details until approved).

## Operating Context

Mobile-first Vietnamese interface (`Be Vietnam Pro` typography, Material 3 on Android, 48dp touch targets, edge-to-edge safe area insets, light-mode primary surface `#F8FAF3` with Deep Pitch Green `#14532D` & Primary `#3D6943`). Currently backed by a reactive in-memory mock store & fixtures synchronized with the interactive prototype (`docs/prototype/index.html`) while real REST/STOMP backend integration is pending.

## Capabilities and Constraints

- 101 functional screen/sheet/state IDs across 13 domains (`APP`, `AUTH`, `PROF`, `TEAM`, `MATCH`, `PAIR`, `MERC`, `EXT`, `VEN`, `CHAT`, `NOTIF`, `REP`, `ADM`).
- Multi-team captain acting-team context (`TEAM-14`).
- Privacy guard (`AC-P05`): mercenary venue name, address, and coordinates hidden prior to `APPROVED` application.
- Blind rating (`AC-P07`): post-match rating does not reveal opponent submission status until both submit or current side submits.
- Completion rate hidden until `matchCount >= 5`.
- Deleted accounts masked as `"Người dùng đã xóa"` in user-facing views and shown with real name + `(Đã xóa)` flag in Admin views.

## Brand Commitments

- App name: `ArenaFlow`
- Deep Pitch Green `#14532D` with soccer pitch-line motif on brand hero headers.
- Primary `#3D6943`, Surface `#F8FAF3`, Card `#FFFFFF`, Border `#DDE5D9`.
- Font: `Be Vietnam Pro` (bundled in `:feature:base`).

## Evidence on Hand

- Interactive prototype & fixtures: `docs/prototype/index.html`, `docs/prototype/js/store.js`, `docs/prototype/images/`.
- API & business specs: `docs/ArenaFlow-spec/`.
- Real OAuth/SMS/REST/STOMP backend endpoints are not yet connected; demo mode uses reactive in-memory fixtures.

## Product Principles

1. **Task clarity over decoration**: every screen leads with the match, team, or action at hand.
2. **Enforce business invariants in domain/data**: privacy masking, blind rating, revision resets, and role guards are enforced in domain/data, not just hidden in UI.
3. **Zero dead ends**: every deleted target, 403/404/409 error, or empty state provides clear explanation and safe Back/Home recovery.
4. **Preserve in-progress drafts**: picking a venue or switching tabs never discards form input.

## Accessibility & Inclusion

- Minimum 48×48dp touch targets on all interactive controls.
- Scalable `sp` typography tested with system font scaling and IME/keyboard padding on forms.
