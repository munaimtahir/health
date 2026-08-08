package com.vexel.passport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationChangeDao {
    @Query("SELECT * FROM medication_changes ORDER BY changedAtEpochMillis DESC")
    fun observeAll(): Flow<List<MedicationChangeEntity>>

    @Query("SELECT * FROM medication_changes ORDER BY changedAtEpochMillis DESC")
    suspend fun findAll(): List<MedicationChangeEntity>
    @Query("SELECT * FROM medication_changes WHERE medicationId = :medicationId ORDER BY changedAtEpochMillis DESC")
    fun observeForMedication(medicationId: String): Flow<List<MedicationChangeEntity>>

    @Insert suspend fun insert(change: MedicationChangeEntity)
    @Query("DELETE FROM medication_changes") suspend fun deleteAll()
}
