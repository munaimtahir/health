package pk.vexel.healthpassport.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun ten_thousand_entries_are_summarized_without_losing_counts() {
        val events = (0 until 10_000).map { index ->
            TrendEvent(if (index % 2 == 0) "Headache" else "Nausea", "SYMPTOM", index % 11)
        }
        val startedAt = System.nanoTime()
        val summary = summarizeSymptoms(events)
        val elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000

        assertEquals(10_000, summary.totalEntries)
        assertEquals(5_000, summary.symptomCounts["Headache"])
        assertEquals(5_000, summary.symptomCounts["Nausea"])
        assertTrue("10,000-entry summary took ${elapsedMillis}ms", elapsedMillis < 5_000)
    }
}
