package com.vexel.passport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AllergyDao {
    @Query("SELECT * FROM allergies ORDER BY CASE WHEN status = 'ACTIVE' THEN 0 ELSE 1 END, allergen COLLATE NOCASE")
    fun observeAll(): Flow<List<AllergyEntity>>

    @Query("SELECT * FROM allergies WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AllergyEntity?

    @Insert
    suspend fun insert(allergy: AllergyEntity)

    @androidx.room.Update
    suspend fun update(allergy: AllergyEntity)

    @Query("DELETE FROM allergies WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM allergies")
    suspend fun deleteAll()
}
