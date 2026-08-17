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

    @Test
    fun malformed_start_date_is_rejected_not_silently_accepted() {
        val draft = SymptomDraft("Headache", 5, "", startAtText = "not-a-date")
        assertTrue(draft.validationErrors().containsKey("startAt"))
    }

    @Test
    fun calendar_impossible_date_is_rejected() {
        val draft = SymptomDraft("Headache", 5, "", startAtText = "2026-02-30 10:00")
        assertTrue(draft.validationErrors().containsKey("startAt"))
    }

    @Test
    fun malformed_end_date_is_rejected_not_silently_accepted() {
        val draft = SymptomDraft("Headache", 5, "", endAtText = "13:99 whenever")
        assertTrue(draft.validationErrors().containsKey("endAt"))
    }

    @Test
    fun blank_start_and_end_dates_are_not_errors() {
        val draft = SymptomDraft("Headache", 5, "", startAtText = "", endAtText = "")
        assertTrue(draft.validationErrors()["startAt"] == null)
        assertTrue(draft.validationErrors()["endAt"] == null)
    }

    @Test
    fun well_formed_start_date_parses_to_the_expected_instant() {
        val parsed = parseSymptomDateTime("2026-08-06 10:15")
        assertTrue(parsed != null)
        assertTrue(parsed?.year == 2026 && parsed.monthValue == 8 && parsed.dayOfMonth == 6 && parsed.hour == 10 && parsed.minute == 15)
    }
}
