# Device compatibility matrix

| Device | Android | Connected launch | Manual evidence |
|---|---:|---|---|
| TECNO CH6i physical (`08357252AE006901`) | 13 | PASS | Install, onboarding, main shell/profile inspected |
| Android 16 emulator (`emulator-5554`) | 16 | PASS | Connected launch; earlier transient System UI dialog noted |

API 26 and intermediate versions remain untested in this environment.

Latest smoke on the final debug APK: onboarding completed, Home and Plan/Reminders opened, Records showed the private vault empty state and import control, Profile showed JSON export, backup/restore, PDF report, and delete controls. No app `FATAL EXCEPTION` was observed in the inspected main log buffer.
