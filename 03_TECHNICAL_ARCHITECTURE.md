# Technical Architecture

## Technology stack
- Kotlin
- Jetpack Compose
- Material 3
- Feature-based modular architecture
- MVVM with unidirectional data flow
- Room
- DataStore
- Hilt
- Navigation Compose
- WorkManager
- Android Storage Access Framework
- CameraX or Android document scanner
- Android Keystore
- Native PDF generation
- JUnit, Robolectric where appropriate, instrumented tests and Compose UI tests

## Platform targets
- Target SDK: API 36
- Minimum SDK: API 26
- Primary physical test device: TECNO CH6i, Android 13

## Architectural layers
### UI layer
- Compose screens
- ViewModels
- Immutable UI state
- User actions/events

### Domain layer
Use cases for:
- Add/edit/delete symptom
- Add/edit/delete record
- Add/update medication
- Build timeline
- Schedule reminder
- Generate appointment summary
- Backup/restore/export

### Data layer
Repositories:
- ProfileRepository
- TimelineRepository
- SymptomRepository
- RecordRepository
- MedicationRepository
- ReminderRepository
- AppointmentReportRepository
- BackupRepository

Data sources:
- Room local database
- Encrypted local file store
- DataStore preferences
- Android notification scheduler

## Suggested module structure
```text
app/
core/common/
core/database/
core/designsystem/
core/domain/
core/files/
core/notifications/
core/security/
core/testing/
feature/onboarding/
feature/dashboard/
feature/profile/
feature/timeline/
feature/symptoms/
feature/records/
feature/medications/
feature/reminders/
feature/appointments/
feature/reports/
feature/settings/
```

## Offline-first rules
- Local database is the source of truth.
- No core feature requires internet access.
- UI reads from local observable streams.
- Writes are committed locally before UI success is shown.
- Future cloud sync must be optional and conflict-aware.

## File storage
- Copy imported files into app-controlled storage.
- Preserve original bytes.
- Store metadata in Room.
- Use generated opaque file identifiers.
- Never store sensitive file names in logs.
- Validate MIME type and extension.
- Handle missing/corrupted files gracefully.

## Database migrations
- Every schema change requires a tested migration.
- Maintain migration tests from every supported prior release.
- Never use destructive migration in production.
- Backup/restore format must include schema version.

## Reminder architecture
- Store reminder definition in Room.
- Use WorkManager for resilient background scheduling.
- Reconcile scheduled work after reboot, app update and time-zone change.
- Use unique work names to prevent duplicates.

## PDF architecture
- Build reports from immutable domain models.
- Unit test report composition independently from rendering.
- Include generated timestamp and disclaimer.
- Ensure active allergies and current medications cannot be silently omitted.

## Error handling
- Define typed domain errors.
- Show recoverable, nontechnical messages.
- Never expose stack traces or protected paths.
- Record only privacy-safe diagnostics.
