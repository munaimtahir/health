# UX and Screen Specification

## Design principles
1. Usable during pain, fatigue or brain fog.
2. Core actions require few taps.
3. Medical information is visually calm and readable.
4. The timeline is the central organizing model.
5. The app never appears to diagnose or prescribe.
6. Offline status and backup status are transparent.

## Navigation
Bottom navigation:
- Home
- Timeline
- Records
- Plan
- Profile

Central add action:
- Log symptom
- Add report
- Add medication
- Add consultation
- Add reminder

## Screen 1 — Onboarding
Pages:
1. Product purpose
2. Privacy and local-first storage
3. Medical-use boundary
4. Optional biometric/PIN lock
5. Create health profile

Acceptance requirements:
- No account required.
- No more than five onboarding pages.
- User can review privacy and disclaimer later.

## Screen 2 — Home dashboard
Cards:
- Today
- Next due item
- Quick symptom log
- Active medication summary
- Recent records
- Recent health events

Empty state:
“Start by adding a report, symptom or follow-up.”

## Screen 3 — Add symptom
Fast path:
1. Choose favourite or recent symptom.
2. Choose severity.
3. Confirm current date/time.
4. Save.

Expanded path:
- Duration
- Location
- Associations
- Medication
- Note
- Attachment

Target time:
- Common entry: under 20 seconds.

## Screen 4 — Timeline
Components:
- Search bar
- Filter chips
- Date groups
- Event cards
- Expandable details
- Add button

Event cards must show:
- Event icon
- Type
- Short title
- Date/time
- One key detail

## Screen 5 — Records
Tabs or filters:
- All
- Labs
- Prescriptions
- Imaging
- Discharge
- Other

Functions:
- Scan
- Import
- View
- Edit metadata
- Link to condition
- Share/export
- Delete with confirmation

## Screen 6 — Medication
Sections:
- Current
- Previous

Medication detail:
- Name
- Strength and dose
- Frequency
- Indication
- Start and stop history
- Dose-change timeline

## Screen 7 — Plan
Tabs:
- Upcoming
- Completed
- Overdue

Reminder card:
- Item
- Due date/time
- Source of plan: user-entered
- Snooze
- Reschedule
- Complete

## Screen 8 — Appointment summary
Steps:
1. Select date range.
2. Select sections.
3. Review summary.
4. Add questions.
5. Generate PDF.

Report hierarchy:
- Critical identifiers and allergies first
- Current medications
- Main symptoms and changes
- Recent records
- Pending actions

## Screen 9 — Profile
- Demographics
- Allergies
- Conditions
- Surgical history
- Emergency information
- Primary clinician

## Screen 10 — Settings
- App lock
- Notifications
- Backup and restore
- Export
- Delete data
- Appearance
- Privacy
- Disclaimer
- Support

## Accessibility
- Support large font sizes.
- Minimum touch targets of 48 dp.
- Content descriptions for interactive icons.
- Do not rely only on colour.
- Support dark mode.
- Maintain readable contrast.
- Use plain language.
