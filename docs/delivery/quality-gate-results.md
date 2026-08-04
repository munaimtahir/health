# Quality gate results

| Check | Result | Evidence |
|---|---|---|
| Debug compile/package | PASSED | `./gradlew assembleDebug` |
| Unit tests | PASSED | `./gradlew test` |
| Android lint | PASSED | `./gradlew lint` |
| Release APK | PASSED | `./gradlew assembleRelease` |
| Instrumented tests | PASSED | `./verify_project.sh`; app launch passed on TECNO CH6i and Android 16 emulator |
| Clean final suite | PASSED for baseline commit | `./verify_project.sh` completed all scripted stages before the latest delete-all change; targeted regression passed after it |

Additional executed checks:

- `./gradlew :core:model:test :core:database:compileDebugKotlin :core:files:compileDebugKotlin :app:assembleDebug --no-daemon`: passed after current changes.
- `./verify_project.sh`: all stages passed; logs are in `docs/verification/evidence/`.
- Physical TECNO CH6i: debug APK installed and onboarding/main-shell launch exercised with synthetic input.
- Emulator `emulator-5554`: connected launch test passed; an earlier manual launch showed a transient System UI dialog, recorded as environmental.

The compileSdk 36 warning is emitted because AGP 8.7.3 was tested through API 35; it is a toolchain warning, not a lint failure.
