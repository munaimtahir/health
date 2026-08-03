package pk.vexel.healthpassport.core.model

data class SymptomDraft(
    val name: String,
    val severity: Int?,
    val notes: String,
)

fun SymptomDraft.validationErrors(): Map<String, String> = buildMap {
    if (name.isBlank()) put("name", "Enter a symptom")
    if (name.length > 120) put("name", "Use 120 characters or fewer")
    if (severity != null && severity !in 0..10) put("severity", "Severity must be between 0 and 10")
    if (notes.length > 4_000) put("notes", "Use 4,000 characters or fewer")
}
