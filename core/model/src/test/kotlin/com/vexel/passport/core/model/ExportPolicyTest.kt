package com.vexel.passport.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportPolicyTest {
    @Test
    fun share_descriptor_uses_safe_format_mime_and_extension() {
        val descriptor = exportShareDescriptor(ExportFormat.PDF)

        assertEquals("application/pdf", descriptor.mimeType)
        assertEquals("pdf", descriptor.extension)
        assertTrue(descriptor.sensitiveDataWarning.contains("sensitive health information"))
    }

    @Test
    fun report_requires_at_least_one_selected_section() {
        assertFalse(hasSelectedReportSection(false, false, false, false, false))
        assertTrue(hasSelectedReportSection(false, true, false, false, false))
    }
}
