# Decision Log

Status: Canonical. Purpose: record material decisions and resolved contradictions.

| ID | Decision | Rationale | Status |
|---|---|---|---|
| D-001 | Public name is **Vexel Health Passport**; application ID and namespace are `com.vexel.passport`. | Fixed project identity overrides working Play-title wording in the source pack. | Approved |
| D-002 | Scaffold targets SDK 36 when supported by the installed toolchain; minimum SDK is 26. | The prompt requires latest stable supported by the toolchain; API 36 is installed. | Approved |
| D-003 | Initial scaffold is shell-only; production feature flows remain deferred to Sprint 1 onward. | Scope protection prevents fake health functionality. | Approved |
| D-004 | Original numbered documents are archived verbatim before canonical consolidation. | Source material must remain recoverable and auditable. | Approved |
| D-005 | Feature modules do not depend on one another; navigation is owned by `app`. | Prevents circular dependencies and preserves boundaries. | Approved |
| D-006 | The first executable MVP slice stores user-entered profile and health events locally through Room/DataStore; binary documents, authentication, scheduling, reports, and backup remain separate delivery items. | Keeps current behavior honest and avoids presenting placeholder clinical or security behavior as complete. | Approved |
| D-007 | Gradle wrapper is upgraded to 8.9 to match AGP 8.7.3. | The previous 8.7 wrapper could not configure the project. | Approved |
| D-008 | `CLAUDE.md`/`AGENTS.md` previously implied the 11 `feature/*` modules contain real screen implementations that `app` composes. In fact all 11 are 5-line placeholder files (`/** X feature placeholder. */`); every real screen and the shared `PassportViewModel` live in `app/src/main/kotlin/com/vexel/passport/VexelHealthPassportApp.kt`. Docs now describe this as the current, intentional state rather than claiming modularization that was never done. Extracting the monolith into the placeholder modules would require splitting the shared ViewModel per feature and redefining navigation contracts — treated as a separate, future initiative, not undertaken here. | A blind large-scale refactor of a single shared ViewModel touching every entity carries high regression risk; documenting reality accurately is safer than silently claiming or silently attempting modularization. | Approved |
