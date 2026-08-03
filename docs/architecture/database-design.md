# Database Design

Status: Canonical. Purpose: define Room preparation.

Room database version 1 is scaffolded with provider, constants, converter, DAO, and entity packages. The conceptual model covers profile, allergy, diagnosis/condition, procedure, symptom, health event, medical document, medication/regimen, consultation, reminder, appointment, report, and backup metadata. Every future schema change requires a non-destructive migration and migration test.

