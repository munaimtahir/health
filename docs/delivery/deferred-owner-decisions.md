# Deferred owner decisions

Items parked for owner input rather than resolved unilaterally, per the repair-programme execution rules. Nothing here blocks the engineering work that doesn't depend on it — that work continues in parallel.

## DOD-001 — Device/emulator verification

- **Decision or input required:** Run `connectedCheck` and manual runtime smoke tests on a physical device or emulator.
- **Affected phase/task:** Phase 0 gate, Phase 6 final gate, all "connected" test suites (`BackupRestoreTest`, `ReminderReliabilityTest`, `ReminderSystemAcceptanceTest`, `NavigationAccessibilityTest`, `LargeDataPerformanceTest`, `PrivacyRuntimeTest`).
- **Safe options:**
  1. Continue all JVM/static-gate work now, defer device verification until a device is available.
  2. Halt engineering until a device is attached.
- **Recommended option:** (1) — confirmed by owner. This environment currently reports `adb devices` empty. The owner has stated the plan is to clone the finished repository onto a device with ADB access and run the device checkup separately, after development work here completes.
- **Work already completed:** All JVM/static gates (clean, assembleDebug, assembleRelease (unsigned), bundleRelease (unsigned), unit tests, lint, check, privacy/boundary verification) pass as of the Phase 0/1 commits.
- **Blocks internal release:** Yes, for the final "connected test suite on every available compatible device/emulator" gate and the manual runtime smoke test — both are owner-scheduled for after this development pass.
- **Action required at check-in:** Attach a device/emulator, run `./gradlew connectedCheck`, and run the Phase 6 manual smoke-test checklist.

## DOD-002 — Release signing credentials

- **Decision or input required:** Provide `vexelReleaseStoreFile`/`vexelReleaseStorePassword`/`vexelReleaseKeyAlias`/`vexelReleaseKeyPassword` Gradle properties (or a keystore) to produce a signed release APK/AAB.
- **Affected phase/task:** Phase 6 release artifacts.
- **Safe options:** Continue producing unsigned release builds (proves the release variant compiles and packages correctly) and leave signing as an owner-only step, vs. blocking on a signing credential that isn't available in this environment.
- **Recommended option:** Continue unsigned — confirmed by owner ("skip the signed release too, just work with assembleDebug"). `assembleRelease`/`bundleRelease` remain green as an unsigned build to prove the release variant is not broken.
- **Work already completed:** Unsigned `assembleRelease` and `bundleRelease` both pass.
- **Blocks internal release:** Only blocks a *signed, distributable* release artifact — not internal engineering completion.
- **Action required at check-in:** Owner decides whether/when to supply signing credentials outside the repository.

## DOD-003 — Play Console / external publication

- **Decision or input required:** Play Console access, data-safety questionnaire submission, production rollout.
- **Affected phase/task:** Phase 6.
- **Safe options:** N/A — this is an owner-only account action; no safe engineering substitute exists.
- **Recommended option:** Treat as external/environmental, not an engineering blocker.
- **Work already completed:** N/A.
- **Blocks internal release:** No (internal release is distinct from Play publication).
- **Action required at check-in:** Owner-only; no engineering action possible from this session.
