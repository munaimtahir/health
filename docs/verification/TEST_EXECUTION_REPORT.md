# Test execution report

Passed commands: `./gradlew clean`, `assembleDebug`, `assembleRelease`, `bundleRelease`, `test`, `lint`, `check`, `bash scripts/verify.sh`, `./verify_project.sh`, `./gradlew connectedCheck --no-daemon` (emulator-only after the later USB disconnect), and targeted model/database/files/app regressions through `4ba6cab`.

Connected app launch passed on TECNO CH6i Android 13 and Android 16 emulator. The current instrumentation suite contains an app launch assertion only; full end-to-end coverage is not yet implemented.

Final physical smoke installed the debug APK from `b767ce3`, completed onboarding, opened Home, Plan/Reminders, Records, and Profile, and inspected the expected controls. A later `4ba6cab` connected run could not complete on the TECNO because serial `08357252AE006901` disappeared; this is recorded as pending environmental verification, not a pass.
