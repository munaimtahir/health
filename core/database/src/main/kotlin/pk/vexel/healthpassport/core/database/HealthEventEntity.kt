package pk.vexel.healthpassport.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_events")
data class HealthEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAtEpochMillis: Long,
)

