# Database Design

Status: Canonical. Purpose: define Room preparation.

Room database version 9 is implemented with legacy profile/events, medications, documents, reminders, and additive structured `conditions`, `allergies`, and `measurements` tables. A non-destructive 8→9 migration creates only the new tables and preserves all original event and attachment rows. Every future schema change requires a migration and migration test.
