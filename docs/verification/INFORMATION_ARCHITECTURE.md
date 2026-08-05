# Information architecture

The application uses five primary destinations, kept visible in the Material 3 navigation bar:

| Destination | Purpose | Secondary flows |
| --- | --- | --- |
| Home | Dashboard and fastest route to logging | Symptom and medication capture |
| Records | Longitudinal health-event history | Search, filters, archive, deletion |
| Plan | Upcoming and historical reminders | Create, edit, complete, snooze |
| Vault | Private imported documents | Import, open, metadata, delete |
| Profile | Personal information and administration | Reports, export, backup/restore, security, appearance, deletion |

The mapping is implemented in `app/src/main/kotlin/pk/vexel/healthpassport/VexelHealthPassportApp.kt`. Administrative tools are intentionally kept under Profile so they do not compete with routine health-record tasks.
