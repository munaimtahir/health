# Sprint Plan and Quality Gates

## Governing rule
The agent must not start the next sprint until all current-sprint acceptance criteria and quality gates pass. Failed gates require correction, retesting and documented evidence.

## Sprint 0 — Product constitution
Deliver:
- Repository
- PRD
- Architecture decision records
- Feature boundaries
- Data dictionary
- Threat model
- Design tokens
- CI skeleton

Gate:
- Every MVP feature has acceptance criteria.
- Diagnostic and treatment functionality is explicitly excluded.
- No unresolved core architectural decision.

## Sprint 1 — Foundation
Deliver:
- Kotlin/Compose project
- Modular structure
- Hilt
- Navigation
- Room
- DataStore
- Design system
- CI
- Test framework

Gate:
- Debug and release builds compile.
- App launches offline.
- Database create/reopen test passes.
- Navigation smoke tests pass.
- No critical lint/static-analysis issue.

## Sprint 2 — Onboarding and profile
Deliver:
- Onboarding
- Privacy and disclaimer
- Personal profile
- Allergies
- Conditions
- Surgery history
- App lock

Gate:
- Profile create/edit/export/delete works.
- App lock survives restart.
- No sensitive data in logs.
- Accessibility labels complete.

## Sprint 3 — Timeline
Deliver:
- Unified event projection
- Timeline list
- Filters
- Search
- Detail/edit/delete

Gate:
- Correct ordering across event types.
- No duplicate event after edit.
- Responsive with 10,000 events.
- Time-zone and daylight-saving tests pass.

## Sprint 4 — Symptoms
Deliver:
- Symptom library
- Fast entry
- Expanded entry
- History
- Calendar
- Trends
- Flares

Gate:
- Common symptom entered in under 20 seconds during usability test.
- Charts reconcile with raw data.
- Missing values are not treated as zero.
- No diagnostic or causal wording.

## Sprint 5 — Records
Deliver:
- Camera scan
- PDF/image import
- Metadata
- Search/filter
- Secure viewer
- Share-to-app
- Timeline link

Gate:
- Original bytes preserved.
- File survives restart.
- Unsupported formats handled safely.
- Duplicate file handling verified.
- App cannot browse unrelated files.

## Sprint 6 — Medication history
Deliver:
- Medication list
- Regimens
- Dose changes
- Start/stop
- Timeline integration

Gate:
- Dose change preserves prior history.
- Current list is accurate.
- Stopping a medicine does not delete it.
- Appointment report displays correct active regimen.

## Sprint 7 — Plan and reminders
Deliver:
- Follow-up/test/check-up reminder
- Recurrence
- Snooze
- Reschedule
- Completion history

Gate:
- Reminder survives reboot.
- No duplicate notifications.
- Time-zone change handled.
- Denied notification permission does not crash.
- Language states reminders are user-entered plans.

## Sprint 8 — Appointment summary and ownership
Deliver:
- Report builder
- PDF
- Print/share
- Backup
- Restore
- Structured export
- Delete all data

Gate:
- PDF matches source data.
- Active allergy and medication checks pass.
- Clean-install restore succeeds.
- Restore does not duplicate records.
- Exported files open externally.

## Sprint 9 — Release hardening
Deliver:
- Accessibility review
- Dark mode
- Adaptive layout
- Performance
- Migration suite
- Security review
- Privacy policy
- Play declarations
- Closed-test build
- Store assets

Gate:
- All automated tests pass.
- No critical/high security issue.
- Release AAB builds.
- API 36 target verified.
- Backup disaster test passes.
- Multiple Android-version smoke tests pass.
- Application works offline.
- Policy declarations match actual behavior.
