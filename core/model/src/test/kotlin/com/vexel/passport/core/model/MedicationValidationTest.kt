package com.vexel.passport.core.model

import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationValidationTest {
    @Test fun blankNameIsRejected() {
        assertEquals("Medication name is required", MedicationDraft("").validationErrors()["name"])
    }

    @Test fun validMedicationIsAccepted() {
        assertTrue(MedicationDraft("Amoxicillin", strength = "500 mg", dose = "1 capsule").validationErrors().isEmpty())
    }

    @Test fun structuredMedicationFieldsAreValidated() {
        val errors = MedicationDraft("Medicine", status = "UNKNOWN", startDate = "x".repeat(33)).validationErrors()
        assertTrue(errors.containsKey("status"))
        assertTrue(errors.containsKey("startDate"))
    }

    @Test fun malformed_start_date_is_rejected_not_silently_accepted() {
        val errors = MedicationDraft("Medicine", startDate = "not-a-date").validationErrors()
        assertTrue(errors.containsKey("startDate"))
    }

    @Test fun calendar_impossible_stop_date_is_rejected() {
        val errors = MedicationDraft("Medicine", stopDate = "2026-13-40").validationErrors()
        assertTrue(errors.containsKey("stopDate"))
    }

    @Test fun blank_dates_are_not_errors() {
        val errors = MedicationDraft("Medicine", startDate = "", stopDate = "").validationErrors()
        assertTrue(errors["startDate"] == null)
        assertTrue(errors["stopDate"] == null)
    }

    @Test fun well_formed_date_parses() {
        val parsed = parseMedicationDate("2026-08-06")
        assertTrue(parsed != null)
        assertTrue(parsed?.year == 2026 && parsed.monthValue == 8 && parsed.dayOfMonth == 6)
    }
}
