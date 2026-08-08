package com.vexel.passport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY status ASC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<MedicationEntity>>

    @Insert suspend fun insert(medication: MedicationEntity)
    @Update suspend fun update(medication: MedicationEntity)
    @Query("DELETE FROM medications WHERE id = :id") suspend fun delete(id: String)
    @Query("DELETE FROM medications") suspend fun deleteAll()
}
