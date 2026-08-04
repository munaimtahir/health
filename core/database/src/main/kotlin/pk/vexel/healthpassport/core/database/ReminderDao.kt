package pk.vexel.healthpassport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY dueAtEpochMillis ASC") fun observeAll(): Flow<List<ReminderEntity>>
    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1") suspend fun find(id: String): ReminderEntity?
    @Insert suspend fun insert(reminder: ReminderEntity)
    @Update suspend fun update(reminder: ReminderEntity)
    @Query("UPDATE reminders SET status = :status, updatedAtEpochMillis = :updatedAt WHERE id = :id") suspend fun setStatus(id: String, status: String, updatedAt: Long)
    @Query("DELETE FROM reminders WHERE id = :id") suspend fun delete(id: String)
    @Query("DELETE FROM reminders") suspend fun deleteAll()
}
