package com.vexel.passport.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val type: String = "OTHER",
    val name: String,
    val manufacturer: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val implantationDate: String = "",
    val hospital: String = "",
    val notes: String = "",
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
