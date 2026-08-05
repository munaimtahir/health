# Completion ledger

| Item | Evidence | Status |
|---|---|---|
| Onboarding/profile | Compose, DataStore/profile Room, physical onboarding smoke | TESTED PARTIAL |
| Symptoms/history/search/delete | Validation tests, Room and timeline UI | TESTED PARTIAL |
| Medication history | Room v3, migration, validation tests, capture/current display | TESTED PARTIAL |
| Trends | Neutral symptom count/frequency/average severity summary and tests | TESTED PARTIAL |
| PIN/biometric | PBKDF2 tests, Keystore cipher, lifecycle gate | TESTED PARTIAL |
| Document vault | SAF import, Room metadata, private storage/hash, metadata edit, replacement, FileProvider open/share/delete; connected replacement test | TESTED PARTIAL |
| Reminders/notifications | Room records, WorkManager one-time/daily scheduling, permission/channel/tap path, persisted schedule reconciliation | TESTED PARTIAL |
| PDF reports | Native paginated/date-ranged PDF save with user-data disclaimer and secure share action | TESTED PARTIAL |
| Export | Versioned JSON and readable text via SAF with optional date range | TESTED PARTIAL |
| Backup/restore | Versioned ZIP with manifest, data/document hash validation, document binaries and transactional restore | TESTED PARTIAL |
| Delete all data | Room/files/preferences with confirmation; full device deletion evidence pending | TESTED PARTIAL |
| Accessibility/security/privacy/safety | Partial review only | INCOMPLETE |
| Release AAB | Signed local AAB built and verified; Play Console upload is a manual owner action | TESTED |
