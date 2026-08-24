package com.vexel.passport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HospitalisationDao {
    @Query("SELECT * FROM hospitalisations ORDER BY admissionDate DESC, createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<HospitalisationEntity>>

    @Query("SELECT * FROM hospitalisations WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): HospitalisationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hospitalisation: HospitalisationEntity)

    @Update
    suspend fun update(hospitalisation: HospitalisationEntity)

    @Query("DELETE FROM hospitalisations WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM hospitalisations")
    suspend fun deleteAll()
}
