package pk.vexel.healthpassport.core.model

data class MedicationDraft(
    val name: String,
    val strength: String = "",
    val dose: String = "",
    val route: String = "",
    val frequency: String = "",
    val notes: String = "",
)

fun MedicationDraft.validationErrors(): Map<String, String> = buildMap {
    if (name.isBlank()) put("name", "Medication name is required")
    if (name.length > 160) put("name", "Medication name must be 160 characters or fewer")
    if (strength.length > 80) put("strength", "Strength must be 80 characters or fewer")
    if (dose.length > 80) put("dose", "Dose must be 80 characters or fewer")
    if (route.length > 80) put("route", "Route must be 80 characters or fewer")
    if (frequency.length > 120) put("frequency", "Frequency must be 120 characters or fewer")
    if (notes.length > 4000) put("notes", "Notes must be 4,000 characters or fewer")
}
