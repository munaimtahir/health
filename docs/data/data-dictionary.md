# Data Dictionary

Status: Canonical. Purpose: define the conceptual health model.

Entities are UserProfile, Allergy, Diagnosis, Procedure, SymptomDefinition, SymptomEntry, HealthEvent, MedicalDocument, StoredFile, Medication, MedicationRegimen, Consultation, Reminder, Appointment, GeneratedReport, and BackupMetadata. Store timestamps consistently with UTC plus useful original offset. Active medication is derived from active regimen; dose changes end one regimen and create another.

