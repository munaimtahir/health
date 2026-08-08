package com.vexel.passport.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SymptomEpisodesTest {
    @Test
    fun groups_only_exact_user_entered_episode_ids() {
        val summaries = summarizeSymptomEpisodes(
            listOf(
                EpisodeEvent("Headache", "flare-a", 100),
                EpisodeEvent("Nausea", " flare-a ", 200, ongoing = true),
                EpisodeEvent("Headache", "flare-b", 300),
                EpisodeEvent("Back pain", null, 400),
            ),
        )

        assertEquals(2, summaries.size)
        val first = summaries.first { it.episodeId == "flare-a" }
        assertEquals(listOf("Headache", "Nausea"), first.symptomNames)
        assertEquals(2, first.entryCount)
        assertEquals(100L, first.firstOccurredAtEpochMillis)
        assertEquals(200L, first.lastOccurredAtEpochMillis)
        assertTrue(first.hasOngoingEntry)
    }

    @Test
    fun missing_episode_ids_are_not_inferred_or_grouped() {
        val summaries = summarizeSymptomEpisodes(
            listOf(EpisodeEvent("Headache", null, 100), EpisodeEvent("Headache", "", 200)),
        )

        assertTrue(summaries.isEmpty())
        assertFalse(summaries.any { it.symptomNames.contains("Headache") })
    }
}
