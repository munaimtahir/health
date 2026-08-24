package com.vexel.passport.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hospitalisations")
data class HospitalisationEntity(
    @PrimaryKey val id: String,
    val admissionDate: String = "",
    val dischargeDate: String = "",
    val hospital: String = "",
    val reason: String = "",
    val diagnosis: String = "",
    val notes: String = "",
    val linkedDocumentId: String? = null,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
