# Quality gate results

| Check | Result | Evidence |
|---|---|---|
| Debug compile/package | PASSED | `./gradlew assembleDebug` |
| Unit tests | PASSED | `./gradlew test` |
| Android lint | PASSED | `./gradlew lint` |
| Release APK | PASSED | `./gradlew assembleRelease` |
| Instrumented tests | PASSED | `./verify_project.sh`; app launch passed on TECNO CH6i and Android 16 emulator |
| Clean final suite | PASSED at `a1ec5ae` | `./verify_project.sh` completed all scripted stages, including six connected app tests on the physical TECNO |

Additional executed checks:

- `./gradlew :core:model:test :core:database:compileDebugKotlin :core:files:compileDebugKotlin :app:assembleDebug --no-daemon`: passed after current changes.
- `./verify_project.sh` at commit `a1ec5ae`: all stages passed; logs are in `docs/verification/evidence/`.
- Physical TECNO CH6i: debug APK installed and onboarding/main-shell launch exercised with synthetic input.
- The final gate used the authorized physical TECNO device. An Android 15 emulator run is historical evidence, not part of the latest gate.

The compileSdk 36 warning is emitted because AGP 8.7.3 was tested through API 35; it is a toolchain warning, not a lint failure.

Release signing verification: `app-release.apk` passed `apksigner verify --verbose`; `app-release.aab` passed `jarsigner -verify`. The local keystore is outside the repository and is not committed.

## Repair-programme baseline (2026-08-17)

The Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`) was missing from git — `./verify_project.sh` failed immediately with `./gradlew: No such file or directory`. This was a real, reproducible build break, not an environment artifact; it was regenerated with `gradle wrapper --gradle-version 8.13` (matching the version already pinned in `gradle-wrapper.properties`) and committed.

`./verify_project.sh` was then re-run to completion, including the Phase 1 fixes described below (Room-instance dedup in reminder reconciliation, real `NavHost` navigation):

| Stage | Result |
|---|---|
| clean | PASSED |
| assembleDebug | PASSED |
| assembleRelease (unsigned — no `vexelRelease*` properties set in this environment) | PASSED |
| bundleRelease (unsigned) | PASSED |
| unit tests (`./gradlew test`) | PASSED |
| lint | PASSED |
| check | PASSED |
| repository/privacy/boundary verification (`scripts/verify.sh` + `verify_boundaries.sh`) | PASSED |
| connectedCheck | NOT RUN — no ADB device/emulator attached in this environment. Deferred until the app is cloned onto a device with ADB access (owner-confirmed plan). |

Full logs: `docs/verification/evidence/*.log` (updated by this run).
