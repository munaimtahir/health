package com.vexel.passport.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportSelectionTest {
    @Test fun blank_scope_is_valid() {
        assertTrue(validateDateScope("", "").isValid)
    }

    @Test fun valid_scope_accepts_iso_local_dates() {
        assertTrue(validateDateScope("2026-01-01", "2026-01-31").isValid)
    }

    @Test fun malformed_date_is_rejected() {
        assertFalse(validateDateScope("2026-02-31", "").isValid)
    }

    @Test fun reversed_scope_is_rejected() {
        assertFalse(validateDateScope("2026-03-01", "2026-02-28").isValid)
    }

    @Test fun range_filter_is_inclusive_at_both_boundaries() {
        assertTrue(isWithinDateScope(100L, 100L, 200L))
        assertTrue(isWithinDateScope(200L, 100L, 200L))
        assertFalse(isWithinDateScope(99L, 100L, 200L))
        assertFalse(isWithinDateScope(201L, 100L, 200L))
    }
}
