package com.vexel.passport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConditionDao {
    @Query("SELECT * FROM conditions ORDER BY CASE WHEN status = 'ACTIVE' THEN 0 ELSE 1 END, name COLLATE NOCASE")
    fun observeAll(): Flow<List<ConditionEntity>>

    @Query("SELECT * FROM conditions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ConditionEntity?

    @Insert
    suspend fun insert(condition: ConditionEntity)

    @androidx.room.Update
    suspend fun update(condition: ConditionEntity)

    @Query("DELETE FROM conditions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM conditions")
    suspend fun deleteAll()
}
