package com.vexel.passport.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_history")
data class FamilyHistoryEntity(
    @PrimaryKey val id: String,
    val relationship: String,
    val condition: String,
    val notes: String = "",
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
