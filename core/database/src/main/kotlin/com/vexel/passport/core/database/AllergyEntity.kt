package com.vexel.passport.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "allergies")
data class AllergyEntity(
    @PrimaryKey val id: String,
    val allergen: String,
    val category: String = "OTHER",
    val reaction: String = "",
    val severity: String = "",
    val notes: String = "",
    val status: String = "ACTIVE",
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
