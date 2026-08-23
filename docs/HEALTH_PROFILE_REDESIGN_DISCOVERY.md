# Health profile redesign discovery

## Baseline

The repository is a multi-module Kotlin/Compose Android application. Navigation is owned by `app`; Room persistence is in `core/database`; local file preservation is in `core/files`; profile, dashboard, timeline, vault, reports, backup/restore, PIN/biometric lock, reminders, and symptom attachment flows already exist. The feature directories are present, but several real screens remain composed through the app shell and shared view models.

Baseline on 23 August 2026: `./gradlew assembleDebug test lint` passed. Existing connected-device evidence is recorded under `docs/verification/evidence/`. The repository was clean at the start of this redesign slice.

## Product gap found

The source experience was still symptom-led: the dashboard said “Record how you feel,” and primary navigation used “Home / Records / Plan / Vault / Profile.” The database had a free-text profile, generic health events, medications, documents, and reminders, but no structured condition, allergy, or measurement tables.

## Implemented slice

Room version 9 adds additive, migration-safe entities for conditions, allergies, and structured measurements. New records emit a corresponding `health_events` row so the existing chronological timeline remains the derived activity surface. The dashboard now presents “My Health,” a health summary, and recent tracking cards. Primary labels are now “Health” and “Timeline.”

## Compatibility and risks

The 8→9 migration only creates new tables and does not rewrite or delete legacy rows. Existing symptoms, medication records, documents, exports, encrypted backups, and lock behavior remain on their existing paths. The current slice does not yet provide dedicated entry dialogs for every new structured entity, so the product is not a complete mega-sprint release.

## Remaining implementation areas

Universal capture, dedicated conditions/allergies/measurement forms, full vault taxonomy, global search across every table, expanded report/backup fields, and migration/UI/device coverage remain engineering work. They must not be described as complete until implemented and tested.
