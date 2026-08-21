package com.vexel.passport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthEventDao {
    @Query("SELECT * FROM health_events WHERE archived = 0 ORDER BY COALESCE(effectiveAtEpochMillis, createdAtEpochMillis) DESC")
    fun observeAll(): Flow<List<HealthEventEntity>>

    @Insert
    suspend fun insert(event: HealthEventEntity)

    @Update
    suspend fun update(event: HealthEventEntity)

    @Query("UPDATE health_events SET archived = 1, updatedAtEpochMillis = :updatedAt WHERE id = :id")
    suspend fun archive(id: String, updatedAt: Long)

    @Query("UPDATE health_events SET archived = 0, updatedAtEpochMillis = :updatedAt WHERE id = :id")
    suspend fun unarchive(id: String, updatedAt: Long)

    @Query("DELETE FROM health_events WHERE id = :id")
    suspend fun delete(id: String)
    @Query("DELETE FROM health_events")
    suspend fun deleteAll()
}
