# Backup and Restore

Status: Canonical. Purpose: define ownership safeguards.

Backup is opt-in, user-controlled, integrity-checked, and includes schema/app version, database, files, required preferences, and checksums. Restore validates before replacement, avoids duplicates, and fails safely. Encryption and key management require maintained platform-backed implementations; custom cryptography is prohibited.

