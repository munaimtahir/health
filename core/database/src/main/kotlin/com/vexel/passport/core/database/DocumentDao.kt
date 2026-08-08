package com.vexel.passport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE archived = 0 ORDER BY COALESCE(documentDate, '') DESC, createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<DocumentEntity>>

    @Insert suspend fun insert(document: DocumentEntity)
    @androidx.room.Update suspend fun update(document: DocumentEntity)
    @Query("UPDATE documents SET archived = 1 WHERE id = :id") suspend fun archive(id: String)
    @Query("DELETE FROM documents WHERE id = :id") suspend fun delete(id: String)
    @Query("DELETE FROM documents") suspend fun deleteAll()
}
