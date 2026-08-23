package com.vexel.passport.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conditions")
data class ConditionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val status: String = "ACTIVE",
    val diagnosisDate: String = "",
    val resolvedDate: String = "",
    val notes: String = "",
    val treatingDoctor: String = "",
    val tags: String = "",
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
