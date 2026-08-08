package com.vexel.passport.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BackupReferencesTest {
    @Test
    fun restore_remaps_attachment_without_losing_episode_reference() {
        val result = remapRestoredSymptomReferences("old-image", " flare-a ", mapOf("old-image" to "new-image"))

        assertEquals("new-image", result.imageAttachmentId)
        assertEquals("flare-a", result.episodeId)
    }

    @Test
    fun missing_attachment_mapping_is_not_recreated_as_an_external_reference() {
        val result = remapRestoredSymptomReferences("missing-image", "flare-a", emptyMap())

        assertEquals(null, result.imageAttachmentId)
        assertEquals("flare-a", result.episodeId)
    }
}
