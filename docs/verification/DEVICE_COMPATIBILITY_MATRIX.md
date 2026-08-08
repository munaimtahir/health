# Device compatibility matrix

| Device | Android | Connected launch | Manual evidence |
|---|---:|---|---|
| TECNO CH6i physical (`08357252AE006901`) | 13 | PASS | Install, onboarding, main shell/profile inspected |
| Android_26_Test emulator (`emulator-5554`, `google_apis` x86_64) | 8.0 (API 26, the app's actual minSdk floor) | PASS | Two independent full `connectedCheck` runs, 6/6 then 7/7 instrumented tests passed, 0 failed |

API 26 is now tested — it was the previous gap. Intermediate API levels between 26 and the physical device's 13 remain untested; a real device or additional emulator images would be needed to close that fully.

Latest smoke on the debug APK (API 26 emulator): onboarding completed, Home and Plan/Reminders opened, Records showed the private vault empty state and import control, Profile showed JSON export, backup/restore, PDF report, and delete controls, a full backup/restore round trip (including tamper and wrong-password rejection) passed as a real instrumented test. No app `FATAL EXCEPTION` was observed in the inspected main log buffer.
