package com.vexel.passport.core.model

data class ConditionDraft(
    val name: String,
    val status: String = "ACTIVE",
    val diagnosisDate: String = "",
    val resolvedDate: String = "",
    val notes: String = "",
    val treatingDoctor: String = "",
    val tags: String = "",
)

data class AllergyDraft(
    val allergen: String,
    val category: String = "OTHER",
    val reaction: String = "",
    val severity: String = "MILD",
    val notes: String = "",
    val status: String = "ACTIVE",
    val allergyDate: String = "",
)

data class ProcedureDraft(
    val name: String,
    val date: String = "",
    val hospital: String = "",
    val doctor: String = "",
    val indication: String = "",
    val notes: String = "",
    val linkedDocumentId: String? = null,
)

data class HospitalisationDraft(
    val admissionDate: String = "",
    val dischargeDate: String = "",
    val hospital: String = "",
    val reason: String = "",
    val diagnosis: String = "",
    val notes: String = "",
    val linkedDocumentId: String? = null,
)

data class VaccinationDraft(
    val vaccineName: String,
    val dose: String = "",
    val date: String = "",
    val provider: String = "",
    val lotNumber: String = "",
    val nextDueDate: String = "",
    val linkedDocumentId: String? = null,
    val notes: String = "",
)

data class DeviceDraft(
    val type: String = "OTHER",
    val name: String,
    val manufacturer: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val implantationDate: String = "",
    val hospital: String = "",
    val notes: String = "",
)

data class FamilyHistoryDraft(
    val relationship: String,
    val condition: String,
    val notes: String = "",
)
