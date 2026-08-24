package com.vexel.passport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY implantationDate DESC, createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: DeviceEntity)

    @Update
    suspend fun update(device: DeviceEntity)

    @Query("DELETE FROM devices WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM devices")
    suspend fun deleteAll()
}
