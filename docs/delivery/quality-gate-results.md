# Quality gate results

| Check | Result | Evidence |
|---|---|---|
| Debug compile/package | PASSED | `./gradlew assembleDebug` |
| Unit tests | PASSED | `./gradlew test` |
| Android lint | PASSED | `./gradlew lint` |
| Release APK | PASSED | `./gradlew assembleRelease` |
| Instrumented tests | NOT RUN | `adb devices` reported no connected device |
| Clean final suite | PENDING | Run after final review |

The compileSdk 36 warning is emitted because AGP 8.7.3 was tested through API 35; it is a toolchain warning, not a lint failure.
