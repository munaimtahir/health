package com.vexel.passport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyHistoryDao {
    @Query("SELECT * FROM family_history ORDER BY relationship COLLATE NOCASE, condition COLLATE NOCASE")
    fun observeAll(): Flow<List<FamilyHistoryEntity>>

    @Query("SELECT * FROM family_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FamilyHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(familyHistory: FamilyHistoryEntity)

    @Update
    suspend fun update(familyHistory: FamilyHistoryEntity)

    @Query("DELETE FROM family_history WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM family_history")
    suspend fun deleteAll()
}
