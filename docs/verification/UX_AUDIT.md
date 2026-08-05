# UX audit

| Issue | Principle | Change | Evidence | Status |
| --- | --- | --- | --- | --- |
| Primary destinations used inconsistent terminology (`Timeline` and `Records`) | Recognition over recall | Standardized navigation to Home, Records, Plan, Vault, Profile | `vexel-graphical-home-physical.png` | Implemented |
| Home exposed several equal-weight actions and dense summaries | Cognitive load reduction | Added one dominant “Log symptom” action and concise summary cards | `vexel-graphical-home-physical.png` | Implemented |
| Screens relied heavily on default Material surfaces | Consistency and visual hierarchy | Added controlled Vexel palette and explicit surface colors | `Theme.kt`, `Components.kt` | Implemented |
| Empty states were plain text or ambiguous | Error prevention | Added reusable actionable empty-state component | `Components.kt`, Records/Vault/Plan screens | Implemented |
| Plan offered no clear separation between current and historical reminders | Recognition over recall | Added Upcoming and History filter chips | `VexelHealthPassportApp.kt` | Implemented |
| Profile was a long ungrouped action list | Hick’s law | Grouped profile, tools, appearance/security, and privacy/data cards | physical Profile evidence | Implemented |
| Onboarding presented setup without enough hierarchy | Emotional safety | Added step indicator, concise safety explanation, explicit acknowledgement, and disabled Continue until acknowledged | `vexel-graphical-profile-physical.png` | Implemented |

Remaining review items are recorded in `VISUAL_QA_REPORT.md` and are not marked complete until the final device and accessibility checks run.
