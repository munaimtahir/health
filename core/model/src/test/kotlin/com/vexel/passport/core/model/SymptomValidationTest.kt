package com.vexel.passport.core.model

import org.junit.Assert.assertTrue
import org.junit.Test

class SymptomValidationTest {
    @Test
    fun valid_draft_has_no_errors() {
        assertTrue(SymptomDraft("Headache", 5, "Occurred after a long day").validationErrors().isEmpty())
    }

    @Test
    fun severity_outside_zero_to_ten_is_rejected() {
        assertTrue(SymptomDraft("Headache", 11, "").validationErrors().containsKey("severity"))
    }

    @Test
    fun missing_name_is_rejected_without_inventing_severity() {
        val draft = SymptomDraft("", null, "")
        assertTrue(draft.validationErrors().containsKey("name"))
        assertTrue(draft.validationErrors()["severity"] == null)
    }

    @Test
    fun ongoing_symptom_cannot_have_an_end_time() {
        assertTrue(SymptomDraft("Pain", 4, "", ongoing = true, endAtText = "2026-08-06 10:00").validationErrors().containsKey("endAt"))
    }
}
