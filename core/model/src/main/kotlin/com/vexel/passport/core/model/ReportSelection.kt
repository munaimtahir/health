package com.vexel.passport.core.model

import java.text.SimpleDateFormat
import java.util.Locale

/** Shared validation for report and export date scopes. Empty values mean no boundary. */
data class DateScopeValidation(
    val fromValid: Boolean,
    val toValid: Boolean,
    val ordered: Boolean,
) {
    val isValid: Boolean get() = fromValid && toValid && ordered
}

fun validateDateScope(from: String, to: String): DateScopeValidation {
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
    fun parse(value: String) = if (value.isBlank()) null else runCatching { parser.parse(value) }.getOrNull()
    val fromDate = parse(from)
    val toDate = parse(to)
    val fromValid = from.isBlank() || fromDate != null
    val toValid = to.isBlank() || toDate != null
    val ordered = fromDate == null || toDate == null || fromDate.time <= toDate.time
    return DateScopeValidation(fromValid, toValid, ordered)
}

fun isWithinDateScope(epochMillis: Long?, fromEpochMillis: Long?, toEpochMillis: Long?): Boolean {
    return epochMillis == null ||
        (fromEpochMillis == null || epochMillis >= fromEpochMillis) &&
        (toEpochMillis == null || epochMillis <= toEpochMillis)
}
