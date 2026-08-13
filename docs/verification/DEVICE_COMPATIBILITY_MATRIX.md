# Device compatibility matrix

| Device | Android | Connected launch | Manual evidence |
|---|---:|---|---|
| TECNO CH6i physical (`08357252AE006901`) | 13 | PASS | Install, onboarding, main shell/profile inspected |
| Android_26_Test emulator (`emulator-5554`, `google_apis` x86_64) | 8.0 (API 26, the app's actual minSdk floor) | PASS | Two independent full `connectedCheck` runs, 6/6 then 7/7 instrumented tests passed, 0 failed |
| Android_16_Test emulator (`emulator-5556`, `google_apis_playstore` x86_64) | 16 (API 36, the app's actual compileSdk/targetSdk) | Manual only (not run through `connectedCheck` in this pass) | Fresh install, onboarding, Home/Records/Plan/Vault/Profile all visually inspected in light theme; launcher-icon capture retaken (`emulator-api36-launcher-icon.png`) |

API 26 and API 36 are now both tested — API 36 was the previous gap, and it surfaced a real defect (see below). Intermediate API levels between 26 and 36, and between 26 and the physical device's 13, remain untested; a real device or additional emulator images would be needed to close that fully.

Latest smoke on the debug APK (API 26 emulator): onboarding completed, Home and Plan/Reminders opened, Records showed the private vault empty state and import control, Profile showed JSON export, backup/restore, PDF report, and delete controls, a full backup/restore round trip (including tamper and wrong-password rejection) passed as a real instrumented test. No app `FATAL EXCEPTION` was observed in the inspected main log buffer.

**API 36 finding (DEF-006, FIXED):** on the Android 16 emulator, status bar icons (clock, battery, wifi) were invisible — white-on-white — on every main screen (Home, Records, Plan, Vault). Screens themselves rendered correctly; only the system status bar row was affected. Not reproduced on the physical Tecno device (Android 13) or the API 26 emulator, which predate Android's mandatory edge-to-edge enforcement (API 35+). Fixed by explicitly setting status/navigation-bar icon appearance from the app's own dark-theme state; re-verified on-device in both light and dark theme after the fix. Root cause and fix details are in `DEFECT_REGISTER.md` DEF-006.

Note: `visual-2026-08-09/physical-tecno-launcher-icon.png` was deleted — it was mislabeled evidence containing an unrelated Google search screenshot, not the app's launcher icon. It has not yet been recaptured on the physical device; `emulator-api36-launcher-icon.png` (correct, verified content) replaces it for the API 36 row only.
