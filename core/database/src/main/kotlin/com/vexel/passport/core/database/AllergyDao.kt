package com.vexel.passport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AllergyDao {
    @Query("SELECT * FROM allergies ORDER BY CASE WHEN status = 'ACTIVE' THEN 0 ELSE 1 END, allergen COLLATE NOCASE")
    fun observeAll(): Flow<List<AllergyEntity>>
    @Insert suspend fun insert(allergy: AllergyEntity)
    @Query("DELETE FROM allergies") suspend fun deleteAll()
}
