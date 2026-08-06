# Backup and Restore

Status: Canonical. Purpose: define ownership safeguards.

Backup is opt-in, user-controlled, integrity-checked, and includes schema/app version, database, files, required preferences, and checksums. Restore validates before replacement, avoids duplicates, and fails safely. The current backup envelope uses PBKDF2-HMAC-SHA256 key derivation and AES-GCM with per-backup random salt and IV; the password is entered for each operation and never persisted. This is password-based portable encryption, not a claim of Android Keystore portability across installations.
