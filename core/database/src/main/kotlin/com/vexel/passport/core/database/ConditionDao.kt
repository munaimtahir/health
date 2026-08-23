package com.vexel.passport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConditionDao {
    @Query("SELECT * FROM conditions ORDER BY CASE WHEN status = 'ACTIVE' THEN 0 ELSE 1 END, name COLLATE NOCASE")
    fun observeAll(): Flow<List<ConditionEntity>>
    @Insert suspend fun insert(condition: ConditionEntity)
    @Query("DELETE FROM conditions") suspend fun deleteAll()
}
