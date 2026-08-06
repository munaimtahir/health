# Final build report

## Project identity

- Repository: `health`
- Application: Vexel Health Passport
- Application ID: `pk.vexel.healthpassport`
- Version: `0.1.0` / version code `1`
- Minimum SDK: 26
- Target/compile SDK: 36

## Sprint results

- Sprint 0: PASSED — repository audit, documentation archive, Gradle normalization, CI and verification foundation.
- Sprint 1: PASSED — Compose shell, Hilt, Room/DataStore infrastructure, onboarding/profile/event flows and connected settings coverage.
- Sprints 2–12: PASSED FOR INTERNAL TESTING — feature implementation, regression tests, release artifacts, full repository gates, and physical-device connected checks pass.

The latest implementation and verification history is on `main`; see `git log` and `docs/verification/FINAL_HANDOVER_REPORT.md`.

## Verification

- `./verify_project.sh`: passed, including clean, assembleDebug, assembleRelease, bundleRelease, unit tests, lint, check, repository verification, and connected checks.
- `./gradlew :app:connectedDebugAndroidTest --no-daemon`: passed with 6 tests on the physical TECNO CH6i (Android 13).
- Debug artifact: `app/build/outputs/apk/debug/app-debug.apk` after the final build.
- Signed release APK: `app/build/outputs/apk/release/app-release.apk`.
- Signed release AAB: `app/build/outputs/bundle/release/app-release.aab`.

## Known limitations

The current build is suitable for controlled internal testing. Play Console submission/publication remains a manual owner action. The connected TECNO has fingerprint hardware but no enrolled biometric, so successful biometric unlock is not claimed.

## Parked blockers

Play Console access and upload remain a manual owner action. Local release signing is configured outside the repository and the generated release APK/AAB is signed.

## Final status

`COMPLETE WITH EXTERNAL PUBLICATION BLOCKERS`
