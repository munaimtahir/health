package pk.vexel.healthpassport.core.database

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthEventDao {
    @Query("SELECT * FROM health_events ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<HealthEventEntity>>
}

