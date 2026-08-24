package com.vexel.passport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcedureDao {
    @Query("SELECT * FROM procedures ORDER BY date DESC, createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<ProcedureEntity>>

    @Query("SELECT * FROM procedures WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ProcedureEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(procedure: ProcedureEntity)

    @Update
    suspend fun update(procedure: ProcedureEntity)

    @Query("DELETE FROM procedures WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM procedures")
    suspend fun deleteAll()
}
