# Health data migration report

## Result

The redesign slice uses an additive Room 8→9 migration. Existing symptom-centric data is not transformed, duplicated, or deleted. This preserves deterministic legacy IDs and attachment references while allowing new structured records to coexist.

## Evidence

- `DatabaseProvider.MIGRATION_8_9` creates `conditions`, `allergies`, and `measurements` only.
- `HealthDatabase` schema version is 9 and Room schema export regenerated successfully.
- `./gradlew :app:assembleDebug :core:database:test :core:model:test` passed after the change.

## Remaining verification

An on-device migration test that opens a version-8 fixture and asserts all new tables after upgrade still needs to be added. JSON export and encrypted backup/restore now include the new tables; a dedicated round-trip assertion for those fields remains open.
