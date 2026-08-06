package pk.vexel.healthpassport.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_events")
data class HealthEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val details: String = "",
    val kind: String = "OTHER",
    val effectiveAtEpochMillis: Long? = null,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long = createdAtEpochMillis,
    val status: String = "ACTIVE",
    val severity: Int? = null,
    val archived: Boolean = false,
    val durationMinutes: Int? = null,
    val startAtEpochMillis: Long? = null,
    val endAtEpochMillis: Long? = null,
    val ongoing: Boolean = false,
    val bodyLocation: String = "",
    val associatedSymptoms: String = "",
    val possibleTrigger: String = "",
    val relatedMedication: String = "",
    val imageAttachmentId: String? = null,
    val episodeId: String? = null,
)
