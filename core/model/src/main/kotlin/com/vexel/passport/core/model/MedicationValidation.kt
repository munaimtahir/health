package com.vexel.passport.core.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

private val MEDICATION_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter
    .ofPattern("uuuu-MM-dd")
    .withResolverStyle(ResolverStyle.STRICT)

/** Parses a medication start/stop date strictly; returns null for blank or unparsable input. */
fun parseMedicationDate(text: String): LocalDate? {
    if (text.isBlank()) return null
    return try {
        LocalDate.parse(text.trim(), MEDICATION_DATE_FORMATTER)
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun isValidDateOrBlank(text: String): Boolean = text.isBlank() || parseMedicationDate(text) != null

data class MedicationDraft(
    val name: String,
    val genericName: String = "",
    val strength: String = "",
    val dose: String = "",
    val unit: String = "",
    val route: String = "",
    val frequency: String = "",
    val startDate: String = "",
    val stopDate: String = "",
    val status: String = "CURRENT",
    val indication: String = "",
    val physician: String = "",
    val notes: String = "",
    val formulation: String = "",
    val prescriptionId: String? = null,
)

fun MedicationDraft.validationErrors(): Map<String, String> = buildMap {
    if (name.isBlank()) put("name", "Medication name is required")
    if (name.length > 160) put("name", "Medication name must be 160 characters or fewer")
    if (genericName.length > 160) put("genericName", "Generic or brand name must be 160 characters or fewer")
    if (strength.length > 80) put("strength", "Strength must be 80 characters or fewer")
    if (dose.length > 80) put("dose", "Dose must be 80 characters or fewer")
    if (unit.length > 40) put("unit", "Unit must be 40 characters or fewer")
    if (route.length > 80) put("route", "Route must be 80 characters or fewer")
    if (frequency.length > 120) put("frequency", "Frequency must be 120 characters or fewer")
    if (startDate.length > 32) put("startDate", "Start date must be 32 characters or fewer")
    else if (!isValidDateOrBlank(startDate)) put("startDate", "Enter a valid date as yyyy-MM-dd, or leave blank")
    if (stopDate.length > 32) put("stopDate", "Stop date must be 32 characters or fewer")
    else if (!isValidDateOrBlank(stopDate)) put("stopDate", "Enter a valid date as yyyy-MM-dd, or leave blank")
    if (status !in setOf("CURRENT", "STOPPED")) put("status", "Choose current or stopped")
    if (indication.length > 200) put("indication", "Indication must be 200 characters or fewer")
    if (physician.length > 160) put("physician", "Physician must be 160 characters or fewer")
    if (notes.length > 4000) put("notes", "Notes must be 4,000 characters or fewer")
    if (formulation.length > 80) put("formulation", "Formulation must be 80 characters or fewer")
    if ((prescriptionId?.length ?: 0) > 80) put("prescriptionId", "Prescription ID must be 80 characters or fewer")
}
