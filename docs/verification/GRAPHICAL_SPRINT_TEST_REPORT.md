# Graphical sprint test report

## Targeted verification completed

```text
./gradlew :core:designsystem:compileDebugKotlin :app:compileDebugKotlin
./gradlew :app:assembleDebug :app:testDebugUnitTest --no-daemon
adb -s 08357252AE006901 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s 08357252AE006901 shell monkey -p pk.vexel.healthpassport 1
adb -s 08357252AE006901 shell uiautomator dump /sdcard/vexel.xml
```

These targeted checks passed during the graphical changes. The final report will add the complete clean/build/test/lint/check/connected evidence after the remaining refinements.

Subsequent targeted checks also passed after the continued engineering work:

```text
./gradlew :app:testDebugUnitTest :app:assembleDebug --no-daemon
./gradlew :app:connectedDebugAndroidTest --no-daemon
```

The connected suite ran two tests on the TECNO Android 13 device and two tests on the Android 15 emulator, including private document replacement/hash verification.

The release gate was rerun after structured medication fields, date-range export, and secure report/document sharing changes:

```text
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:bundleRelease --no-daemon
```

Result: `BUILD SUCCESSFUL`.
