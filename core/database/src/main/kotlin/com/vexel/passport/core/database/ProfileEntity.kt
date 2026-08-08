package com.vexel.passport.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val dateOfBirth: String = "",
    val bloodGroup: String = "",
    val allergies: String = "",
    val conditions: String = "",
    val emergencyContact: String = "",
    val updatedAtEpochMillis: Long = 0L,
)
