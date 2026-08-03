# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository state

This repository currently contains **no application code** — only a numbered pack of planning/specification documents for an Android app called **Vexel Health Passport**. `docs/` is an empty placeholder directory. There is no build system, no source tree, and no tests yet. If asked to build the app, treat the numbered documents below as the spec to implement against, following the sprint order in `05_SPRINT_PLAN_AND_GATES.md`.

## Document pack (read in this order)

1. `00_README.md` — product overview and recommended reading order
2. `01_PRODUCT_REQUIREMENTS.md` — MVP modules, fields, and what's explicitly excluded
3. `02_UX_SCREEN_SPEC.md` — screen-by-screen UX spec
4. `03_TECHNICAL_ARCHITECTURE.md` — stack, layers, module structure
5. `04_DATA_MODEL.md` — entities and data-integrity rules
6. `05_SPRINT_PLAN_AND_GATES.md` — sprint-by-sprint deliverables and gates (governs build order)
7. `06_QA_TEST_STRATEGY.md` — required test coverage and severity definitions
8. `07_SECURITY_PRIVACY.md` — privacy/security requirements and disclaimer wording
9. `08_PLAY_STORE_RELEASE_CHECKLIST.md` — release checklist
10. `10_MASTER_AI_AGENT_PROMPT.md` — the controlling prompt for an AI coding agent building this app; **read this before writing any code**

`manifest.json` lists the pack files; `09_BRAND_AND_NAME.md` and `11_MARKET_POSITIONING_SUMMARY.md` are reference/marketing material.

## Non-negotiable product boundary

Vexel Health Passport is a health-record organization and symptom-logging app. It must **never**:
- Diagnose disease
- Recommend treatment or medication changes
- Independently determine/prescribe follow-up intervals
- Interpret laboratory reports as a clinician would
- Perform emergency triage
- Claim causation between events (use "occurred around the same time" / "temporal association," never "caused by")

This boundary overrides feature requests that conflict with it. When source documents conflict, resolve in this order: safety/privacy > product requirements > sprint acceptance gates > architecture > UX preferences.

## Planned architecture (once implementation starts)

- **Stack:** Kotlin, Jetpack Compose, Material 3, Hilt, Room, DataStore, Navigation Compose, WorkManager, Android Storage Access Framework, CameraX/Android document scanner, Android Keystore-backed encryption, native PDF generation.
- **Pattern:** Feature-based modular architecture, MVVM with unidirectional data flow, separated UI/domain/data layers, repositories + explicit use cases.
- **Target SDK 36, min SDK 26.** Primary physical test device: TECNO CH6i (Android 13).
- **Offline-first:** local Room database is the source of truth; no core feature may require network access; writes commit locally before UI reports success.
- Suggested module layout (`app/`, `core/{common,database,designsystem,domain,files,notifications,security,testing}/`, `feature/{onboarding,dashboard,profile,timeline,symptoms,records,medications,reminders,appointments,reports,settings}/`) — see `03_TECHNICAL_ARCHITECTURE.md` for full details.
- **Data integrity rules** (from `04_DATA_MODEL.md`): dates stored in UTC with local offset retained where useful; a medication dose change ends the prior regimen and creates a new one rather than mutating it (history must never be destroyed); timeline events link to source entities instead of copying mutable data; every schema change requires a tested migration — destructive migrations are forbidden in production.
- **File handling:** imported reports/prescriptions are copied into app-controlled private storage with original bytes preserved, opaque generated file identifiers, and validated MIME/extension — never log sensitive file names or expose unrestricted file-provider paths.
- **Reminders:** defined in Room, scheduled via WorkManager with unique work names, and reconciled after reboot, app update, and time-zone change to avoid duplication.

## Build/execution protocol for an AI agent implementing this app

Per `10_MASTER_AI_AGENT_PROMPT.md`, work strictly sprint by sprint per `05_SPRINT_PLAN_AND_GATES.md`; do not start the next sprint until the current sprint's acceptance gate passes. At the end of each sprint: run formatting, lint, unit tests, database tests, and relevant UI/instrumented tests; build debug and release variants; verify acceptance criteria against the gate; do a separate reviewer pass for security, data integrity, accessibility, and scope compliance. Do not introduce cloud services, analytics, ads, or billing. Do not implement AI/OCR features in the MVP — extension points only (future AI/OCR must be optional, on-device-by-default with explicit consent for external processing, present extracted data as suggestions requiring user confirmation, and never overwrite original documents).

## Quality priorities (in order)

No data loss > no privacy exposure > accurate allergy/medication reporting > reliable backup/restore > reliable reminders > low-burden symptom entry > accessibility > performance > visual polish.
