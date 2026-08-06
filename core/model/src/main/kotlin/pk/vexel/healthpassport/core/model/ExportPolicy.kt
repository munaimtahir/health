package pk.vexel.healthpassport.core.model

enum class ExportFormat(val mimeType: String, val extension: String) {
    JSON("application/json", "json"),
    READABLE("text/plain", "txt"),
    PDF("application/pdf", "pdf"),
}

data class ExportShareDescriptor(
    val mimeType: String,
    val extension: String,
    val sensitiveDataWarning: String,
)

fun exportShareDescriptor(format: ExportFormat): ExportShareDescriptor = ExportShareDescriptor(
    mimeType = format.mimeType,
    extension = format.extension,
    sensitiveDataWarning = "This file may contain sensitive health information. Share it only with people you trust.",
)

fun hasSelectedReportSection(
    includeProfile: Boolean,
    includeEvents: Boolean,
    includeMedications: Boolean,
    includeDocuments: Boolean,
    includeReminders: Boolean,
): Boolean = includeProfile || includeEvents || includeMedications || includeDocuments || includeReminders
