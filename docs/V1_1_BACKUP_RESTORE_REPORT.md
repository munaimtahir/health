# V1.1 Backup & Restore Report

## Backup Format & Compatibility
The Vexel Health Passport backup file is a Zip archive containing:
- `data.json`: An export of all database tables serialized into JSON.
- `manifest.json`: Verification manifest containing encryption format, hashes, and item counts.
- `documents/`: Folder storing actual binary content of imported medical documents and media.

The entire archive is encrypted using PBKDF2-HMAC-SHA256 key derivation and AES-GCM encryption with a password supplied by the user.

## Updates in v1.1 Backup & Restore
- Serialization of the new tables: `procedures`, `hospitalisations`, `vaccinations`, `devices`, `familyHistory`.
- Updates to existing JSON models:
  - `medications` array entries include `formulation` and `prescriptionId`.
  - `allergyRecords` array entries include `allergyDate`.
- Graceful default loading when restoring from v1.0.5 databases:
  - If `formulation`, `prescriptionId`, or `allergyDate` are missing from json entries, default values (`""` and `null`) are safely used.
  - Missing tables (procedures, hospitalisations, vaccinations, devices, familyHistory) are skipped and initialized as empty lists rather than causing parsing failures.

## Test Verification
- **v1.0.5 Backup Restore Test**: Created a mock database matching the v1.0.5 schema, exported a backup, upgraded the app, and restored it. Status: PASSED (all elements preserved, defaults correctly filled).
- **v1.1 Backup & Restore Test**: Created complete v1.1 profiles containing procedures, lab reports, vaccinations, and device implants, backed up, wiped the database, and restored successfully. Status: PASSED (100% database recovery and attachment matching).
- **Security Protections Test**: Incorrect passwords correctly throw `AEADBadTagException` and show standard incorrect password dialogs without failing or corrupting database state. Status: PASSED.
