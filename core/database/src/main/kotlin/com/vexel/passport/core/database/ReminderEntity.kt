package com.vexel.passport.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String = "CUSTOM",
    val notes: String = "",
    val dueAtEpochMillis: Long,
    val recurrence: String = "ONCE",
    val status: String = "SCHEDULED",
    val snoozeUntilEpochMillis: Long? = null,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
