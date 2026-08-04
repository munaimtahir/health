package pk.vexel.healthpassport.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrendSummaryTest {
    @Test fun missingSeverityIsExcludedFromAverage() {
        val summary = summarizeSymptoms(listOf(TrendEvent("Headache", "SYMPTOM", 8), TrendEvent("Headache", "SYMPTOM")))
        assertEquals(8.0, summary.averageRecordedSeverity!!, 0.01)
    }

    @Test fun noSymptomsProducesNeutralEmptySummary() {
        val summary = summarizeSymptoms(listOf(TrendEvent("Medication", "MEDICATION")))
        assertEquals(0, summary.totalEntries)
        assertNull(summary.averageRecordedSeverity)
    }
}
