package com.vexel.passport.core.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

private val SYMPTOM_DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter
    .ofPattern("uuuu-MM-dd HH:mm")
    .withResolverStyle(ResolverStyle.STRICT)

/** Parses a symptom draft's start/end text strictly; returns null for blank or unparsable input. */
fun parseSymptomDateTime(text: String): LocalDateTime? {
    if (text.isBlank()) return null
    return try {
        LocalDateTime.parse(text.trim(), SYMPTOM_DATE_TIME_FORMATTER)
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun isValidOrBlank(text: String): Boolean = text.isBlank() || parseSymptomDateTime(text) != null

data class SymptomDraft(
    val name: String,
    val severity: Int?,
    val notes: String,
    val startAtText: String = "",
    val endAtText: String = "",
    val durationMinutes: Int? = null,
    val ongoing: Boolean = false,
    val bodyLocation: String = "",
    val associatedSymptoms: String = "",
    val possibleTrigger: String = "",
    val relatedMedication: String = "",
    val episodeId: String = "",
)

fun SymptomDraft.validationErrors(): Map<String, String> = buildMap {
    if (name.isBlank()) put("name", "Enter a symptom")
    if (name.length > 120) put("name", "Use 120 characters or fewer")
    if (severity != null && severity !in 0..10) put("severity", "Severity must be between 0 and 10")
    if (notes.length > 4_000) put("notes", "Use 4,000 characters or fewer")
    if (startAtText.length > 32) put("startAt", "Use yyyy-MM-dd HH:mm or leave blank")
    else if (!isValidOrBlank(startAtText)) put("startAt", "Enter a valid date and time as yyyy-MM-dd HH:mm, or leave blank")
    if (endAtText.length > 32) put("endAt", "Use yyyy-MM-dd HH:mm or leave blank")
    else if (!isValidOrBlank(endAtText)) put("endAt", "Enter a valid date and time as yyyy-MM-dd HH:mm, or leave blank")
    if (durationMinutes != null && durationMinutes < 0) put("duration", "Duration cannot be negative")
    if (ongoing && endAtText.isNotBlank()) put("endAt", "Ongoing symptoms cannot have an end time")
    if (bodyLocation.length > 160) put("bodyLocation", "Use 160 characters or fewer")
    if (associatedSymptoms.length > 500) put("associatedSymptoms", "Use 500 characters or fewer")
    if (possibleTrigger.length > 500) put("possibleTrigger", "Use 500 characters or fewer")
    if (relatedMedication.length > 160) put("relatedMedication", "Use 160 characters or fewer")
    if (episodeId.length > 80) put("episodeId", "Use 80 characters or fewer")
}
