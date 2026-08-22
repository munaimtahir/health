package com.vexel.passport.feature.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vexel.passport.core.database.HealthDatabase
import com.vexel.passport.core.database.ReminderEntity
import com.vexel.passport.core.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val database: HealthDatabase,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    val reminders = database.reminderDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addReminder(title: String, type: String, notes: String, dueAtEpochMillis: Long, recurrence: String) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val reminder = ReminderEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            type = type.trim().ifBlank { "CUSTOM" },
            notes = notes.trim(),
            dueAtEpochMillis = dueAtEpochMillis,
            recurrence = recurrence,
            status = "SCHEDULED",
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now
        )
        database.reminderDao().insert(reminder)
        reminderScheduler.schedule(reminder.id, reminder.dueAtEpochMillis, reminder.recurrence)
    }

    fun completeReminder(reminder: ReminderEntity) = viewModelScope.launch {
        reminderScheduler.cancel(reminder.id)
        database.reminderDao().setStatus(reminder.id, "COMPLETED", System.currentTimeMillis())
    }

    fun deleteReminder(reminder: ReminderEntity) = viewModelScope.launch {
        reminderScheduler.cancel(reminder.id)
        database.reminderDao().delete(reminder.id)
    }

    fun updateReminder(reminder: ReminderEntity, title: String, type: String, notes: String, dueAtEpochMillis: Long, recurrence: String) = viewModelScope.launch {
        reminderScheduler.cancel(reminder.id)
        val updated = reminder.copy(
            title = title.trim(),
            type = type.trim().ifBlank { "CUSTOM" },
            notes = notes.trim(),
            dueAtEpochMillis = dueAtEpochMillis,
            recurrence = recurrence,
            status = "SCHEDULED",
            snoozeUntilEpochMillis = null,
            updatedAtEpochMillis = System.currentTimeMillis()
        )
        database.reminderDao().update(updated)
        reminderScheduler.schedule(updated.id, updated.dueAtEpochMillis, updated.recurrence)
    }

    fun snoozeReminder(reminder: ReminderEntity) = viewModelScope.launch {
        val dueAt = System.currentTimeMillis() + 60 * 60 * 1000L
        database.reminderDao().reschedule(reminder.id, dueAt, System.currentTimeMillis())
        reminderScheduler.schedule(reminder.id, dueAt, reminder.recurrence)
    }
}
