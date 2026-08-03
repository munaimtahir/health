# QA and Test Strategy

## Unit tests
Cover:
- Symptom severity and duration logic
- Medication-regimen transitions
- Timeline ordering and filtering
- Reminder recurrence calculations
- Appointment-summary composition
- Backup manifest generation
- Duplicate file detection
- Date/time conversion

## Database tests
- Entity CRUD
- Foreign-key behavior
- Transaction integrity
- Every migration path
- Large dataset performance
- Restore into clean database
- Restore conflict prevention

## UI tests
- Onboarding
- Create/edit profile
- Log symptom fast path
- Import record
- Add medication and dose change
- Create and complete reminder
- Generate appointment summary
- Export and delete data
- Biometric/PIN lock flow

## Device and lifecycle tests
- App process killed during edit
- Rotation and configuration change
- Low-storage condition
- File permission revoked
- Notification permission denied
- Device reboot
- Time-zone change
- Date/time manually changed
- App upgraded with existing data
- Backup restored on another supported Android version

## Performance targets
- Cold start: reasonable on lower-midrange hardware
- Timeline with 10,000 events: interactive without visible blocking
- Record list with 2,000 files: searchable and filterable
- PDF generation: progress feedback for large reports
- No main-thread file or database blocking

## Security tests
- Sensitive log inspection
- Screenshot protection option
- Rooted-device behavior documented, not overclaimed
- App-lock bypass attempts
- Backup tampering detection
- Corrupt-file handling
- Path traversal protection
- MIME confusion tests

## Release test matrix
- API 26
- API 29
- API 31
- API 33 physical TECNO CH6i
- API 34
- API 35
- API 36

## Defect severity
- Critical: data loss, privacy exposure, incorrect medication/allergy report, app-lock bypass
- High: failed restore, repeated notification, inaccessible imported report, corrupted timeline
- Medium: misleading chart, broken filter, layout/accessibility defect
- Low: cosmetic inconsistency

No release is permitted with unresolved critical or high defects.
