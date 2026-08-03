# Requirements Traceability Matrix

| Requirement ID | Requirement | Source | Sprint | Implementation | Tests | Status | Evidence |
|---|---|---|---:|---|---|---|---|
| F-001 | Fresh installation launches | handoff §23 | 0 | `MainActivity` | `AppLaunchTest` | TESTED | Physical install/launch 2026-08-04 |
| F-002 | First-launch onboarding | product requirements | 1 | `VexelHealthPassportApp.kt` | `AppLaunchTest` | TESTED | Physical UI dump showed onboarding |
| F-003 | Profile persists | product requirements | 1 | `ProfileDao`, `ProfileScreen` | JVM scaffold only | IN PROGRESS | No full device assertion yet |
| F-005 | Add symptom with optional severity | handoff §12 | 2 | `SymptomDraft`, `CaptureDialog` | `SymptomValidationTest` | TESTED | Targeted Gradle test passed |
| F-009 | Invalid severity rejected | handoff §12 | 2 | `SymptomDraft.validationErrors` | `SymptomValidationTest` | TESTED | Targeted Gradle test passed |
| F-016 | Chronological history | handoff §23 | 3 | `HealthEventDao.observeAll` | Deferred | IN PROGRESS | Device path not fully exercised |
| F-017 | Timeline search | handoff §23 | 3 | `TimelineScreen` | Deferred | IMPLEMENTED | Build passed |
| F-014 | Confirmed event deletion | handoff §23 | 2 | `HealthEventDao.delete`, timeline dialog | Deferred | IMPLEMENTED | Build passed |
| F-026 | PIN lock | handoff §23 | 5 | Not implemented | None | NOT STARTED | — |
| F-028 | Private document import | handoff §23 | 6 | `LocalSecureFileStore` | None | IN PROGRESS | Build passed; UI/metadata pending |
| F-030 | Scheduled reminders | handoff §23 | 7 | Contract only | None | NOT STARTED | — |
| F-032 | PDF report | handoff §23 | 8 | Not implemented | None | NOT STARTED | — |
| F-033 | Export | handoff §23 | 9 | Not implemented | None | NOT STARTED | — |
| F-034 | Backup | handoff §23 | 10 | Not implemented | None | NOT STARTED | — |
| F-036 | Delete all data | handoff §23 | 9 | Not implemented | None | NOT STARTED | — |
