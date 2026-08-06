package pk.vexel.healthpassport.core.model

/** Minimal user-entered episode grouping; it does not infer a medical relationship. */
data class EpisodeEvent(
    val title: String,
    val episodeId: String?,
    val occurredAtEpochMillis: Long,
    val ongoing: Boolean = false,
)

data class SymptomEpisodeSummary(
    val episodeId: String,
    val symptomNames: List<String>,
    val entryCount: Int,
    val firstOccurredAtEpochMillis: Long,
    val lastOccurredAtEpochMillis: Long,
    val hasOngoingEntry: Boolean,
)

/**
 * Groups only entries that share the exact user-entered episode ID. Entries without an ID are
 * intentionally excluded so the app never infers that separate symptoms belong together.
 */
fun summarizeSymptomEpisodes(events: List<EpisodeEvent>): List<SymptomEpisodeSummary> =
    events.asSequence()
        .filter { !it.episodeId.isNullOrBlank() }
        .groupBy { it.episodeId!!.trim() }
        .map { (episodeId, entries) ->
            SymptomEpisodeSummary(
                episodeId = episodeId,
                symptomNames = entries.map { it.title.trim() }.filter { it.isNotBlank() }.distinct().sorted(),
                entryCount = entries.size,
                firstOccurredAtEpochMillis = entries.minOf { it.occurredAtEpochMillis },
                lastOccurredAtEpochMillis = entries.maxOf { it.occurredAtEpochMillis },
                hasOngoingEntry = entries.any { it.ongoing },
            )
        }
        .sortedWith(compareByDescending<SymptomEpisodeSummary> { it.lastOccurredAtEpochMillis }.thenBy { it.episodeId })
