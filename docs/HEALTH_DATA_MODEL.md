# Health data model

## Structured entities implemented

- `ConditionEntity`: name, active/resolved/historical status, diagnosis/resolution dates, notes, treating doctor, tags.
- `AllergyEntity`: allergen, category, reaction, severity, notes, and active status.
- `MeasurementEntity`: extensible type, primary/optional secondary value, unit, context, timestamp, and notes.

Each entity uses a stable UUID and creation/update timestamps. `MeasurementEntity` supports blood pressure as a pair and future trackers such as glucose, temperature, weight, pulse, and SpO₂ without forcing unrelated columns into the legacy symptom table.

## Migration

Room database version 9 creates the three new tables in `MIGRATION_8_9`. No existing table is altered, and no legacy symptom or attachment row is copied or removed. New structured records also create a timeline event for chronological discovery.
