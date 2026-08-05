# Screen redesign specification

| Screen | Primary purpose | Implemented states and hierarchy |
| --- | --- | --- |
| Onboarding | Establish trust and acknowledgement | Step label, app purpose, safety disclaimer, explicit acknowledgement, disabled/enabled Continue |
| Home | Show next useful action | Welcome, Log symptom CTA, privacy note, symptom/medication summaries, empty-safe cards |
| Records | Review longitudinal entries | Search, filter, date grouping, empty/no-result states, archive and delete actions |
| Plan | Manage scheduled actions | Upcoming/History filters, schedule metadata, completion, snooze, edit, delete, empty states |
| Vault | Organize private documents | Import, metadata, type/status pills, open/edit/delete, missing/empty states |
| Profile | Manage personal data and tools | Grouped profile, reports/data tools, appearance/security, privacy/data |
| Capture and edit dialogs | Enter structured data safely | Scrollable forms, validation, explicit save/cancel, confirmation for destructive actions |

The implementation is intentionally a refinement of the existing functional flows; no unsupported clinical interpretation or new health data field was introduced.
