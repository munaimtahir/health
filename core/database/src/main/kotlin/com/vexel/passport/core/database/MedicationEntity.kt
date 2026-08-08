package com.vexel.passport.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val genericName: String = "",
    val strength: String = "",
    val dose: String = "",
    val unit: String = "",
    val route: String = "",
    val frequency: String = "",
    val startDate: String = "",
    val stopDate: String = "",
    val status: String = "CURRENT",
    val indication: String = "",
    val physician: String = "",
    val notes: String = "",
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
