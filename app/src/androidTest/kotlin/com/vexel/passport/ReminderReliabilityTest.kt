package com.vexel.passport

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.vexel.passport.core.database.DatabaseProvider
import com.vexel.passport.core.database.HealthDatabase
import com.vexel.passport.core.datastore.PreferencesStore
import com.vexel.passport.core.files.LocalSecureFileStore
import com.vexel.passport.core.notifications.WorkManagerReminderScheduler
import com.vexel.passport.core.security.KeystorePinMaterialCipher
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies a scheduled reminder is durably persisted by WorkManager (the mechanism that
 * actually survives device reboot/doze, via WorkManager's own bundled boot receiver and
 * SQLite-backed work database) and that this app's reconcile() self-healing path
 * correctly re-derives scheduled work from Room after a simulated process restart.
 * No dedicated coverage existed for this before.
 */
@RunWith(AndroidJUnit4::class)
class ReminderReliabilityTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val scheduledIds = mutableListOf<String>()

    private fun newViewModel(database: HealthDatabase) = PassportViewModel(
        appContext = context,
        database = database,
        preferences = PreferencesStore(context),
        pinMaterialCipher = KeystorePinMaterialCipher(),
        secureFileStore = LocalSecureFileStore(context),
        reminderScheduler = WorkManagerReminderScheduler(context, database),
    )

    @Test
    fun scheduled_reminder_is_durably_enqueued_in_work_manager() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, HealthDatabase::class.java).build()
        try {
            val vm = newViewModel(database)
            val dueAt = System.currentTimeMillis() + 3_600_000
            vm.addReminder("Reboot survival check", "MEDICATION", "Synthetic reminder", dueAt, "ONCE").join()
            val reminder = withTimeout(10_000) { vm.reminders.first { it.isNotEmpty() } }.first { it.title == "Reboot survival check" }
            scheduledIds += reminder.id

            // This is WorkManager's own durable state, backed by its internal SQLite database.
            // WorkManager registers its own boot receiver (merged into the app manifest from the
            // work-runtime AAR) that reads exactly this state to reschedule pending work after a
            // device reboot, independent of any app-level code path.
            val workInfos = WorkManager.getInstance(context).getWorkInfosForUniqueWork(reminder.id).get()
            assertTrue("expected an enqueued WorkManager entry for the reminder", workInfos.isNotEmpty())
            assertEquals(WorkInfo.State.ENQUEUED, workInfos.first().state)
        } finally {
            database.close()
        }
    }

    @Test
    fun weekly_recurring_reminder_is_enqueued_as_periodic_work() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, HealthDatabase::class.java).build()
        try {
            val vm = newViewModel(database)
            val dueAt = System.currentTimeMillis() + 3_600_000
            vm.addReminder("Weekly check-in", "CUSTOM", "Synthetic reminder", dueAt, "WEEKLY").join()
            val reminder = withTimeout(10_000) { vm.reminders.first { it.isNotEmpty() } }.first { it.title == "Weekly check-in" }
            scheduledIds += reminder.id

            val workInfos = WorkManager.getInstance(context).getWorkInfosForUniqueWork(reminder.id).get()
            assertTrue("expected an enqueued WorkManager entry for the weekly reminder", workInfos.isNotEmpty())
            assertEquals(WorkInfo.State.ENQUEUED, workInfos.first().state)
            assertTrue("weekly reminders must be periodic work so they keep repeating", workInfos.first().periodicityInfo != null)
        } finally {
            database.close()
        }
    }

    @Test
    fun monthly_reminder_self_reschedules_to_a_calendar_correct_next_month() = runBlocking {
        // Fires almost immediately so the test doesn't need to wait a full month; verifies the
        // self-rescheduling path in ReminderWorker.doWork() advances dueAtEpochMillis by
        // exactly one calendar month (not a fixed 30-day duration) and keeps the reminder
        // SCHEDULED with a fresh one-time work request enqueued for the new date.
        val database = Room.inMemoryDatabaseBuilder(context, HealthDatabase::class.java).build()
        val reminderId = UUID.randomUUID().toString()
        try {
            val originalDueAt = System.currentTimeMillis() + 2_000
            val originalZoned = java.time.Instant.ofEpochMilli(originalDueAt).atZone(java.time.ZoneId.systemDefault())
            val now = System.currentTimeMillis()
            database.reminderDao().insert(
                com.vexel.passport.core.database.ReminderEntity(
                    id = reminderId,
                    title = "Monthly reminder",
                    type = "CUSTOM",
                    notes = "Synthetic",
                    dueAtEpochMillis = originalDueAt,
                    recurrence = "MONTHLY",
                    status = "SCHEDULED",
                    createdAtEpochMillis = now,
                    updatedAtEpochMillis = now,
                )
            )
            scheduledIds += reminderId
            WorkManagerReminderScheduler(context, database).schedule(reminderId, originalDueAt, "MONTHLY")

            val updated = withTimeout(30_000) {
                var current = database.reminderDao().find(reminderId)
                while (current?.dueAtEpochMillis == originalDueAt) {
                    kotlinx.coroutines.delay(500)
                    current = database.reminderDao().find(reminderId)
                }
                current
            }
            assertTrue("reminder must remain SCHEDULED after a monthly firing", updated?.status == "SCHEDULED")
            val newZoned = java.time.Instant.ofEpochMilli(updated!!.dueAtEpochMillis).atZone(java.time.ZoneId.systemDefault())
            assertEquals("next due date must be exactly one calendar month later", originalZoned.plusMonths(1), newZoned)

            val workInfos = WorkManager.getInstance(context).getWorkInfosForUniqueWork(reminderId).get()
            assertTrue("a fresh one-time work request must be enqueued for the next month", workInfos.isNotEmpty())
        } finally {
            database.reminderDao().delete(reminderId)
            database.close()
        }
    }

    @Test
    fun reconcile_after_simulated_restart_re_enqueues_scheduled_reminders() = runBlocking {
        val database = DatabaseProvider.create(context)
        val reminderId = UUID.randomUUID().toString()
        try {
            val dueAt = System.currentTimeMillis() + 3_600_000
            val now = System.currentTimeMillis()
            // Insert directly into the app's persistent Room database, bypassing schedule(), to
            // simulate process recreation: the reminder row survives, while WorkManager has no
            // work registered under this new unique id until reconcile() reads persistent state.
            database.reminderDao().insert(
                com.vexel.passport.core.database.ReminderEntity(
                    id = reminderId,
                    title = "Post-restart reminder",
                    type = "CUSTOM",
                    notes = "Synthetic",
                    dueAtEpochMillis = dueAt,
                    recurrence = "ONCE",
                    status = "SCHEDULED",
                    createdAtEpochMillis = now,
                    updatedAtEpochMillis = now,
                )
            )
            scheduledIds += reminderId

            assertTrue(
                "WorkManager must not already know about this reminder before reconcile()",
                WorkManager.getInstance(context).getWorkInfosForUniqueWork(reminderId).get().isEmpty(),
            )

            // A fresh scheduler instance, exactly as PassportViewModel.init constructs one on
            // every app process start (including the first launch after a device reboot).
            val freshScheduler = WorkManagerReminderScheduler(context, database)
            freshScheduler.reconcile()

            val workInfos = WorkManager.getInstance(context).getWorkInfosForUniqueWork(reminderId).get()
            assertTrue("reconcile() must re-enqueue scheduled reminders found in Room", workInfos.isNotEmpty())
            assertEquals(WorkInfo.State.ENQUEUED, workInfos.first().state)
        } finally {
            database.reminderDao().delete(reminderId)
            database.close()
        }
    }

    @org.junit.After
    fun cleanUp() {
        val workManager = WorkManager.getInstance(context)
        scheduledIds.forEach { id ->
            runCatching { workManager.cancelUniqueWork(id).result.get() }
        }
    }
}
