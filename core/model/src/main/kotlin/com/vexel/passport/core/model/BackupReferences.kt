package com.vexel.passport.core.model

data class RestoredSymptomReferences(
    val imageAttachmentId: String?,
    val episodeId: String?,
)

/** Remaps managed attachment IDs while preserving the user's explicit episode identifier. */
fun remapRestoredSymptomReferences(
    oldImageAttachmentId: String?,
    episodeId: String?,
    restoredDocumentIds: Map<String, String>,
): RestoredSymptomReferences = RestoredSymptomReferences(
    imageAttachmentId = oldImageAttachmentId?.takeIf { it.isNotBlank() }?.let(restoredDocumentIds::get),
    episodeId = episodeId?.trim()?.takeIf { it.isNotBlank() },
)
