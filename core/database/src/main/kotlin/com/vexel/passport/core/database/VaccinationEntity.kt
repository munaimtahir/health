package com.vexel.passport.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vaccinations")
data class VaccinationEntity(
    @PrimaryKey val id: String,
    val vaccineName: String,
    val dose: String = "",
    val date: String = "",
    val provider: String = "",
    val lotNumber: String = "",
    val nextDueDate: String = "",
    val linkedDocumentId: String? = null,
    val notes: String = "",
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
