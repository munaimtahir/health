# Decision Log

Status: Canonical. Purpose: record material decisions and resolved contradictions.

| ID | Decision | Rationale | Status |
|---|---|---|---|
| D-001 | Public name is **Vexel Health Passport**; application ID and namespace are `pk.vexel.healthpassport`. | Fixed project identity overrides working Play-title wording in the source pack. | Approved |
| D-002 | Scaffold targets SDK 36 when supported by the installed toolchain; minimum SDK is 26. | The prompt requires latest stable supported by the toolchain; API 36 is installed. | Approved |
| D-003 | Initial scaffold is shell-only; production feature flows remain deferred to Sprint 1 onward. | Scope protection prevents fake health functionality. | Approved |
| D-004 | Original numbered documents are archived verbatim before canonical consolidation. | Source material must remain recoverable and auditable. | Approved |
| D-005 | Feature modules do not depend on one another; navigation is owned by `app`. | Prevents circular dependencies and preserves boundaries. | Approved |

