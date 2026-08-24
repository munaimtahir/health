# Data Migration Report (v9 to v10)

## Overview
As part of the v1.1 product completion sprint, the Room database has been migrated from version 9 to 10. The goal of this migration was to support structured logging of additional medical history categories: Procedures/Surgeries, Hospitalisations, Vaccinations, Devices/Implants, and Family History, as well as adding detail attributes to medications and allergy records.

## Migration Specification
- **Room version change**: 9 → 10
- **Migration type**: Deterministic forward-only SQL migration (`MIGRATION_9_10`)
- **Schema changes**:
  - Created table `procedures`.
  - Created table `hospitalisations`.
  - Created table `vaccinations`.
  - Created table `devices`.
  - Created table `family_history`.
  - Altered `allergies` to add `allergyDate TEXT NOT NULL DEFAULT ''`.
  - Altered `medications` to add `formulation TEXT NOT NULL DEFAULT ''` and `prescriptionId TEXT DEFAULT NULL`.

## Migration Verification Results
- **Pre-migration row counts**: Tested using synthetic data injection of v9 tables.
- **Post-migration row counts**: Exactly matching pre-migration counts for all existing tables.
- **Mapped entities**:
  - `ConditionEntity` -> Mapped, no changes.
  - `AllergyEntity` -> Mapped, added default `allergyDate` (defaulting to empty string).
  - `MedicationEntity` -> Mapped, added default `formulation` (empty string) and `prescriptionId` (null).
  - `MeasurementEntity` -> Mapped, no changes.
- **Unmapped/New entities**:
  - `ProcedureEntity` -> Created empty.
  - `HospitalisationEntity` -> Created empty.
  - `VaccinationEntity` -> Created empty.
  - `DeviceEntity` -> Created empty.
  - `FamilyHistoryEntity` -> Created empty.
- **Attachment integrity**: Attachment references (including UUIDs mapped in `documents` table and `imageAttachmentId` fields) remain fully preserved.
- **Backup compatibility**: Decrypting and parsing legacy v1.0.5 backup files successfully maps existing structures while populating defaults for the new v1.1 fields.
- **Detected loss**: None.

## Final Verdict
**UNINTENTIONAL DATA LOSS: ZERO** (PASSED)
