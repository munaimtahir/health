package com.vexel.passport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccinationDao {
    @Query("SELECT * FROM vaccinations ORDER BY date DESC, createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<VaccinationEntity>>

    @Query("SELECT * FROM vaccinations WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): VaccinationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vaccination: VaccinationEntity)

    @Update
    suspend fun update(vaccination: VaccinationEntity)

    @Query("DELETE FROM vaccinations WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM vaccinations")
    suspend fun deleteAll()
}
