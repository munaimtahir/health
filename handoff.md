# Vexel Health Passport — Engineering Handoff

## Mission

Continue building this Android application from the current repository state. Implement the remaining features, verify them on the user’s connected physical Android device through ADB, repair failures, update documentation, and report only work that was actually completed.

Do not stop at plans, mock screens, or placeholders. Do not disable tests, suppress failures, use real health data, or claim device verification without executing it.

## Repository and identity

Repository: `/home/munaim/srv/apps/health`

- Public name: `Vexel Health Passport`
- Application ID: `com.vexel.passport`
- Tagline: `Your health history, organized.`
- Minimum SDK: 26
- Compile/target SDK: 36
- Kotlin, Java 17, Gradle Kotlin DSL
- Compose, Material 3, Room, DataStore, Hilt, Coroutines/Flow, WorkManager
- Single-user, offline-first, local-storage oriented

Read `AGENTS.md`, `README.md`, `CLAUDE.md`, `docs/README.md`, all applicable canonical documents under `docs/`, `docs/delivery/final-build-report.md`, and `docs/delivery/parked-blockers.md` before editing. Preserve `docs/archive/original-ai-development-pack/`.

## Current state

Latest implementation commit:

```text
4e53e53 feat: add persisted offline MVP foundation
```

Implemented foundation:

- Compose shell with Home, Timeline, Records, Plan, and Profile destinations
- First-run onboarding and local medical-safety disclaimer acknowledgement
- Persisted profile fields
- Room-backed health events with archiveable chronological timeline
- Generic capture for symptoms, medications, records, reminders, and other events
- DataStore onboarding/theme persistence
- Hilt dependency injection
- Material 3 light/dark themes
- Room v1→v2 non-destructive migration
- CI and verification script

Previously passing commands:

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew test
./gradlew lint
bash scripts/verify.sh
```

## Physical ADB verification

The user will provide a physical Android device. Start with:

```bash
adb devices -l
```

Require one device in `device`/authorized state. If it is missing or unauthorized, record the exact state and do not claim runtime verification passed.

Build, install, and launch:

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am force-stop com.vexel.passport
adb shell monkey -p com.vexel.passport 1
```

Use synthetic data only. Verify through ADB/manual interaction:

- onboarding and disclaimer persistence
- profile creation/editing
- symptoms and symptom history
- medications and treatment history
- record import, metadata, preview, and export
- timeline ordering, archive, and deletion confirmation
- reminder creation, notification, snooze, completion, and rescheduling
- app lock, biometric/PIN behavior, and resume locking
- theme switching
- rotation and process-death recovery
- PDF report generation and secure sharing
- backup, restore, export, and deletion

## Remaining functional blockers

### Application security

Implement biometric authentication, PIN fallback where feasible, lock-on-resume, timeout settings, safe process-death handling, no health-data visibility before unlock, denial/cancellation/unavailable handling, and Keystore-backed security abstractions. Keep auth logic outside Compose UI. Add unit and instrumented security tests.

### Secure medical-record vault

Implement Storage Access Framework PDF/image import, share-to-app handling, document metadata, private app storage, original-file preservation, SHA-256 hashes, safe filenames, temporary-file cleanup, secure preview/open/export/share URIs, archive/delete policy, unsupported-file handling, and low-storage handling. Do not request broad storage access.

### Structured medication history

Implement medication name, generic/brand name, strength, dose, unit, route, frequency, start/stop dates, status, indication, physician, and notes. Preserve dose-change history, stop history, and restart treatment periods. Generate accurate medication timeline events and current/previous lists.

### Complete symptom tracking

Implement predefined/custom/favourite symptoms, optional severity and 0–10 scale, start/end time, duration, ongoing status, body location, associated symptoms, user-entered possible trigger, related medication, notes, optional image, flare episodes, history, calendar/timeline views, and neutral trend charts. Never diagnose, infer causation, or convert missing severity to zero.

### Reminders and notifications

Implement user-entered follow-up, laboratory, imaging, procedure, vaccination, medication-review, refill, annual-check-up, and custom reminders. Support one-time/recurring schedules, advance notice, snooze, reschedule, completed/missed states, history, notification settings, WorkManager scheduling, restart recovery, timezone changes, permission denial, and duplicate prevention. Never independently determine medical intervals or imply medical advice.

### Appointment preparation and PDF reports

Implement date-range/section selection and reports containing selected profile, allergies, conditions, medications, medication changes, symptoms, consultations, reports, procedures, pending reminders, physician questions, and user notes. Generate readable multi-page PDFs with wrapping, page numbers, generation date, selected range, medical disclaimer, and user-entered-data notice. Add preview, save, print, secure share, and a timeline event. Never expose internal paths or unsupported interpretation.

### Backup, restore, export, and deletion

Implement local backup containing database and managed documents, version/metadata, integrity and SHA-256 validation, Keystore-backed encryption where feasible, restore preview, clean-install restore, compatibility validation, duplicate prevention, corrupt-backup rejection, rollback-safe failed restore, structured JSON export, human-readable export, document export, complete deletion, and selective deletion where defined. Add migration, corruption, restore, rollback, duplicate, and deletion tests.

### Accessibility, performance, and release hardening

Add semantic labels, content descriptions, large touch targets, dynamic text support, light/dark contrast checks, loading/empty/error/confirmation states, rotation/process-death state preservation, 10,000-event timeline performance testing, low-storage and permission-denial handling, R8/ProGuard review, dependency review, permission review, privacy/Data Safety alignment, release checklist, rollback plan, and known limitations. Remove misleading placeholder text.

## Required workflow

For each feature group:

```text
Review requirements → design → implement → add tests → run gate
→ capture failures → repair root causes → rerun regression checks
→ update docs → commit → continue
```

Do not use destructive migrations, in-memory production storage, fake release behavior, or unsupported medical claims.

## Required quality gates

Run and record exact results:

```bash
./gradlew clean
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew bundleRelease
./gradlew test
./gradlew lint
./gradlew check
bash scripts/verify.sh
```

With the physical device authorized:

```bash
./gradlew connectedCheck
```

Install the final debug APK and verify the flows listed above. Use logcat only after confirming no sensitive data is emitted.

## Documentation requirements

Keep these current and truthful:

```text
docs/delivery/build-progress.md
docs/delivery/quality-gate-results.md
docs/delivery/parked-blockers.md
docs/delivery/final-build-report.md
docs/architecture/database-design.md
docs/architecture/file-storage-design.md
docs/architecture/reminder-architecture.md
docs/architecture/backup-and-restore.md
docs/privacy/data-handling.md
docs/privacy/permissions-register.md
docs/privacy/privacy-policy-draft.md
docs/testing/test-matrix.md
docs/release/play-store-readiness.md
```

## Final report

Report implemented features, commands actually executed, physical ADB device state, runtime verification evidence, APK/AAB paths, tests, commits, remaining blockers, exact next actions, and the full report path.

Use only one final status:

```text
COMPLETE — READY FOR CLOSED TESTING
COMPLETE EXCEPT FOR PARKED EXTERNAL RELEASE ITEMS
FUNCTIONALLY COMPLETE — ENVIRONMENTAL VERIFICATION PENDING
INCOMPLETE — CRITICAL BLOCKERS REMAIN
```

Do not mark the project complete while any functional blocker remains.
