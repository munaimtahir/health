package com.vexel.passport.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey val id: String,
    val type: String,
    val primaryValue: Double,
    val secondaryValue: Double? = null,
    val unit: String,
    val context: String = "",
    val recordedAtEpochMillis: Long,
    val notes: String = "",
)
