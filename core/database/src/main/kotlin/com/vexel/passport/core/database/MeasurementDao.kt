package com.vexel.passport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements ORDER BY recordedAtEpochMillis DESC")
    fun observeAll(): Flow<List<MeasurementEntity>>
    @Query("SELECT * FROM measurements WHERE type = :type ORDER BY recordedAtEpochMillis DESC LIMIT :limit")
    fun observeLatest(type: String, limit: Int = 30): Flow<List<MeasurementEntity>>
    @Insert suspend fun insert(measurement: MeasurementEntity)
    @Query("DELETE FROM measurements") suspend fun deleteAll()
}
