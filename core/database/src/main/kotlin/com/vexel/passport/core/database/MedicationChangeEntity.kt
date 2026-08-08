package com.vexel.passport.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medication_changes")
data class MedicationChangeEntity(
    @PrimaryKey val id: String,
    val medicationId: String,
    val changedAtEpochMillis: Long,
    val changeType: String,
    val strength: String = "",
    val dose: String = "",
    val unit: String = "",
    val frequency: String = "",
    val status: String = "CURRENT",
    val notes: String = "",
)
