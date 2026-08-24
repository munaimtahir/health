package com.vexel.passport.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "procedures")
data class ProcedureEntity(
    @PrimaryKey val id: String,
    val name: String,
    val date: String = "",
    val hospital: String = "",
    val doctor: String = "",
    val indication: String = "",
    val notes: String = "",
    val linkedDocumentId: String? = null,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
