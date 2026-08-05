package pk.vexel.healthpassport.core.model

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
}
