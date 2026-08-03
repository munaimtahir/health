# Database Design

Status: Canonical. Purpose: define Room preparation.

Room database version 2 is implemented with `profile` and `health_events` tables. Profile values and user-entered event records are local and archiveable; event kinds include symptoms, medications, records, reminders, and other health events. A non-destructive 1→2 migration preserves the original event rows. The broader conceptual model covers future document, consultation, report, and backup metadata; every future schema change requires a migration and migration test.
