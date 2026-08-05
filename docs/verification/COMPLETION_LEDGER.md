# Completion ledger

| Item | Evidence | Status |
|---|---|---|
| Onboarding/profile | Compose, DataStore/profile Room, physical onboarding smoke | TESTED PARTIAL |
| Symptoms/history/search/delete | Validation tests, Room and timeline UI | TESTED PARTIAL |
| Medication history | Room v3, migration, validation tests, capture/current display | TESTED PARTIAL |
| Trends | Neutral symptom count/frequency/average severity summary and tests | TESTED PARTIAL |
| PIN/biometric | PBKDF2 tests, Keystore cipher, lifecycle gate | TESTED PARTIAL |
| Document vault | SAF import, Room metadata, private storage/hash, FileProvider open/delete | TESTED PARTIAL |
| Reminders/notifications | Room records, WorkManager one-time/daily scheduling, permission/channel/tap path | TESTED PARTIAL |
| PDF reports | Native paginated PDF save with user-data disclaimer | TESTED PARTIAL |
| Export | Versioned JSON via SAF | TESTED PARTIAL |
| Backup/restore | Versioned ZIP with document binaries and hash validation | TESTED PARTIAL |
| Delete all data | Room/files/preferences with confirmation | DEVICE FLOW PENDING |
| Accessibility/security/privacy/safety | Partial review only | INCOMPLETE |
| Release AAB | Signed local AAB built and verified; Play Console upload is a manual owner action | TESTED |
