# Product Requirements Document

## 1. Product identity
- **Name:** Vexel Health Passport
- **Google Play working title:** Vexel Health Passport: Records
- **Tagline:** Your health history, organized.
- **Platform:** Android
- **Primary audience:** Adults who want to maintain a personal health history, particularly people with chronic or fluctuating conditions.
- **Initial profile model:** One person per installation.

## 2. Product statement
A private, offline-first Android application that helps one person store medical records, track symptoms, maintain medication and treatment history, receive reminders for user-entered follow-ups, and prepare a concise health summary for medical appointments.

## 3. Success criteria
The first release is successful when a user can:
- Complete onboarding and create a health profile.
- Add a symptom in under 20 seconds.
- Import or scan a report and find it later.
- View symptoms, records, consultations, medications and reminders in one timeline.
- Add a clinician-advised follow-up and receive a reliable reminder.
- Generate a readable appointment summary PDF.
- Export, back up, restore and delete their data.
- Use all core functions without internet access.

## 4. MVP modules

### 4.1 Dashboard
Display:
- Next follow-up or test
- Active symptoms
- Current medications
- Recent report
- Quick actions
- Recent timeline events

Quick actions:
- Log symptom
- Add report
- Add medication
- Add consultation
- Add reminder
- Generate appointment summary

### 4.2 Personal health profile
Fields:
- Full name
- Date of birth
- Sex
- Blood group
- Allergies
- Chronic conditions
- Previous surgeries
- Emergency contact
- Primary physician
- Important notes

### 4.3 Unified health timeline
Supported event types:
- Symptom
- Flare episode
- Consultation
- Diagnosis
- Laboratory report
- Radiology report
- Prescription
- Medication start, stop or dose change
- Procedure
- Hospital admission
- Vaccination
- Reminder created, completed or rescheduled

Timeline requirements:
- Reverse chronological and chronological views
- Date grouping
- Search
- Filtering by event category
- Detail view
- Editing and deletion
- Correct time-zone handling

### 4.4 Symptom tracking
Required fields:
- Symptom name
- Date and time
- Severity: mild, moderate, severe

Optional fields:
- Numeric severity 0–10
- Duration
- Body location
- Associated symptoms
- Trigger or preceding event
- Relevant medication
- Free-text note
- Voice note
- Photograph

Views:
- History list
- Calendar
- Frequency chart
- Severity trend
- Flare periods
- Treatment-change markers

Safety wording:
- Use “occurred around the same time” or “temporal association.”
- Never state that one event caused another.

### 4.5 Medical-record vault
Categories:
- Laboratory reports
- Prescriptions
- Radiology
- Discharge summaries
- Consultation notes
- Procedures
- Vaccination records
- Medical certificates
- Other

Import options:
- Camera scan
- Image
- PDF
- Android file picker
- Android share-to-app

Metadata:
- Record date
- Category
- Hospital or laboratory
- Doctor
- Related condition
- User note
- Original file
- Timeline event

### 4.6 Medication history
Fields:
- Generic or brand name
- Strength
- Dose
- Frequency
- Route
- Start date
- Stop date
- Indication
- Prescriber
- Active or previous
- Notes

Rules:
- Dose changes must preserve previous history.
- Stopping a medication must not delete historical information.
- Appointment summaries must clearly separate active and previous medicines.

### 4.7 Follow-up and check-up planner
Types:
- Follow-up consultation
- Repeat laboratory test
- Imaging
- Procedure
- Vaccination
- Medication review
- Prescription refill
- Annual health check
- Custom

Functions:
- One-time or recurring reminder
- Advance reminder
- Snooze
- Reschedule
- Mark completed
- Reminder history

Safety rule:
The app records a plan entered by the user. It must not independently prescribe a follow-up interval.

### 4.8 Appointment summary
Include:
- Patient details
- Allergies
- Active conditions
- Current medications
- Recent medication changes
- Main symptoms
- Recent reports
- Procedures and admissions
- Pending follow-ups
- User questions for the clinician
- Disclaimer

Output:
- On-screen preview
- PDF
- Print
- Share

### 4.9 Settings and ownership
- App lock
- Notification preferences
- Date/time and unit preferences
- Backup
- Restore
- Export
- Delete all data
- Privacy notice
- Medical disclaimer
- About and support

## 5. Explicitly excluded from MVP
- Diagnosis
- Treatment recommendation
- Medication-change recommendation
- Emergency triage
- Telemedicine
- Doctor marketplace
- Community forum
- Multi-user or family profiles
- Full medication adherence engine
- Wearable integration
- Health Connect dependency
- Laboratory interpretation
- Mandatory cloud account
- Advertising
- Subscription billing

## 6. Future roadmap
### Version 1.1
- OCR-assisted report intake
- Suggested date, category, laboratory and test fields
- User confirmation before saving extracted data

### Version 1.2
- AI appointment preparation
- Neutral summary of recent events
- Medication-change summary
- Suggested questions based only on user-entered information

### Version 1.3
- Voice-to-structured symptom entry
- English and Urdu support

### Version 2
- Optional encrypted cloud sync
- Multiple profiles
- Health Connect
- Clinician portal
- Research-study mode
