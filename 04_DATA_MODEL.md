# Data Model

## Core entities

### UserProfile
- id
- fullName
- dateOfBirth
- sex
- bloodGroup
- emergencyContactName
- emergencyContactPhone
- primaryPhysician
- importantNotes
- createdAt
- updatedAt

### Allergy
- id
- profileId
- substance
- reaction
- severity
- status
- notes
- createdAt
- updatedAt

### Condition
- id
- profileId
- name
- diagnosisDate
- status
- clinician
- notes
- createdAt
- updatedAt

### SurgeryProcedure
- id
- profileId
- name
- date
- facility
- clinician
- notes
- createdAt
- updatedAt

### SymptomDefinition
- id
- canonicalName
- userLabel
- favourite
- archived
- createdAt
- updatedAt

### SymptomEvent
- id
- symptomDefinitionId
- startDateTime
- endDateTime
- severityCategory
- severityNumeric
- bodyLocation
- triggerText
- associatedSymptomsText
- relevantMedicationId
- note
- attachmentId
- sourceType
- createdAt
- updatedAt

### FlareEpisode
- id
- title
- startDateTime
- endDateTime
- severity
- notes
- createdAt
- updatedAt

### MedicalRecord
- id
- category
- title
- recordDate
- facility
- clinician
- relatedConditionId
- userNote
- fileId
- createdAt
- updatedAt

### StoredFile
- id
- internalPath
- originalDisplayName
- mimeType
- byteSize
- checksum
- encrypted
- createdAt

### Medication
- id
- genericName
- brandName
- strength
- route
- indication
- prescriber
- notes
- createdAt
- updatedAt

### MedicationRegimen
- id
- medicationId
- dose
- frequency
- startDate
- endDate
- active
- reasonForChange
- createdAt
- updatedAt

### Consultation
- id
- dateTime
- clinician
- facility
- specialty
- reason
- assessmentSummary
- planSummary
- relatedRecordId
- createdAt
- updatedAt

### Reminder
- id
- type
- title
- dueDateTime
- recurrenceRule
- advanceNoticeMinutes
- status
- sourceDescription
- completedAt
- createdAt
- updatedAt

### AppointmentQuestion
- id
- text
- resolved
- relatedAppointmentDate
- createdAt
- updatedAt

### TimelineEvent
Prefer a derived database view or domain projection rather than duplicated mutable clinical data.
Fields:
- eventId
- eventType
- eventDateTime
- title
- subtitle
- sourceEntityId

## Data integrity rules
- All dates stored in UTC with original local offset where useful.
- Soft-delete only where needed for recovery; permanent deletion must be available.
- Current medications are derived from active regimens.
- A dose change ends the prior regimen and creates a new regimen.
- Timeline events link to source entities rather than copying mutable data.
- Stored file checksums support duplicate detection.
- Backup includes database, files, preferences required for restore and manifest metadata.
