# Quality gate results

| Check | Result | Evidence |
|---|---|---|
| Debug compile/package | PASSED | `./gradlew assembleDebug` |
| Unit tests | PASSED | `./gradlew test` |
| Android lint | PASSED | `./gradlew lint` |
| Release APK | PASSED | `./gradlew assembleRelease` |
| Instrumented tests | PENDING | Authorized emulator and physical TECNO CH6i are now attached; final connected gate has not yet been rerun after current changes |
| Clean final suite | PENDING | Run after remaining feature implementation |

Additional executed checks:

- `./gradlew :core:model:test :core:security:test :app:assembleDebug`: passed after current changes.
- Physical TECNO CH6i: debug APK installed and onboarding/main-shell launch exercised with synthetic input.
- Emulator `emulator-5554`: install succeeded; a transient System UI not-responding dialog prevented a clean launch UX assertion.

The compileSdk 36 warning is emitted because AGP 8.7.3 was tested through API 35; it is a toolchain warning, not a lint failure.
