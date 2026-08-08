package com.vexel.passport.core.model

data class TrendEvent(val title: String, val kind: String, val severity: Int? = null)

data class SymptomTrendSummary(
    val totalEntries: Int,
    val averageRecordedSeverity: Double?,
    val mostFrequentSymptom: String?,
    val symptomCounts: Map<String, Int>,
)

fun summarizeSymptoms(events: List<TrendEvent>): SymptomTrendSummary {
    val symptoms = events.filter { it.kind == "SYMPTOM" }
    val counts = symptoms.groupingBy { it.title.trim() }.eachCount().toSortedMap()
    val severities = symptoms.mapNotNull { it.severity }
    return SymptomTrendSummary(symptoms.size, severities.takeIf { it.isNotEmpty() }?.average(), counts.maxWithOrNull(compareBy<Map.Entry<String, Int>> { it.value }.thenBy { it.key })?.key, counts)
}
