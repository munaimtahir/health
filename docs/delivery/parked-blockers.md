# Parked blockers

## BLK-001 — Physical device or emulator unavailable

- Sprint: 0/11
- Issue: `adb devices` returned no device, so connected tests and visual/runtime verification were not executable.
- Completed: JVM tests, compile, packaging, lint, Room KSP, and manifest checks.
- Remains: `./gradlew connectedCheck` and manual onboarding/timeline verification.
- Safe assumption: local JVM and static gates are authoritative for this environment.
- Recommended resolution: connect an API 26+ emulator/device and run `./gradlew connectedCheck`.
- Release effect: blocks environmental verification only.

## BLK-002 — Core MVP workflows still need implementation

- Sprints: 2–12
- Issue: biometric/PIN lock, secure binary document vault, WorkManager notification scheduling, PDF report generation, encrypted backup/restore, export/deletion, and release hardening are not yet implemented.
- Completed: local onboarding acknowledgement, profile, event capture, Room timeline, theme persistence, migration, test/lint foundation.
- Safe assumption: no placeholder is represented as a completed production capability.
- Recommended resolution: continue implementation in the listed sprint order before closed testing.
- Release effect: blocks functional MVP release.
