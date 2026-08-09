package com.vexel.passport

import android.app.NotificationManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkManager
import com.vexel.passport.core.database.DatabaseProvider
import com.vexel.passport.core.database.ReminderEntity
import com.vexel.passport.core.notifications.WorkManagerReminderScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Host-driven reboot/doze acceptance phases. Each method is intentionally gated by an
 * instrumentation argument so the normal connected suite skips it. The host prepares a reminder,
 * performs the actual OS transition, then starts the matching verification method.
 */
@RunWith(AndroidJUnit4::class)
class ReminderSystemAcceptanceTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test
    fun prepare_reboot_reminder() = runBlocking {
        assumePhase("prepare-reboot")
        prepare(REBOOT_REMINDER_ID, dueInMillis = 45_000)
    }

    @Test
    fun verify_reboot_reminder_fires() = runBlocking {
        assumePhase("verify-reboot")
        verifyAndClean(REBOOT_REMINDER_ID)
    }

    @Test
    fun prepare_doze_reminder() = runBlocking {
        assumePhase("prepare-doze")
        prepare(DOZE_REMINDER_ID, dueInMillis = 15_000)
    }

    @Test
    fun verify_doze_reminder_fires() = runBlocking {
        assumePhase("verify-doze")
        verifyAndClean(DOZE_REMINDER_ID)
    }

    private suspend fun prepare(id: String, dueInMillis: Long) {
        val database = DatabaseProvider.create(context)
        try {
            WorkManager.getInstance(context).cancelUniqueWork(id).result.get()
            database.reminderDao().delete(id)
            val now = System.currentTimeMillis()
            val dueAt = now + dueInMillis
            database.reminderDao().insert(
                ReminderEntity(
                    id = id,
                    title = "Synthetic system reliability reminder",
                    type = "CUSTOM",
                    notes = "Synthetic acceptance fixture",
                    dueAtEpochMillis = dueAt,
                    recurrence = "ONCE",
                    status = "SCHEDULED",
                    createdAtEpochMillis = now,
                    updatedAtEpochMillis = now,
                )
            )
            WorkManagerReminderScheduler(context).schedule(id, dueAt, "ONCE")
            val work = WorkManager.getInstance(context).getWorkInfosForUniqueWork(id).get()
            assertTrue("expected durable work for $id", work.isNotEmpty())
        } finally {
            database.close()
        }
    }

    private suspend fun verifyAndClean(id: String) {
        val database = DatabaseProvider.create(context)
        try {
            withTimeout(120_000) {
                while (database.reminderDao().find(id)?.status != "MISSED") delay(1_000)
            }
            assertEquals("MISSED", database.reminderDao().find(id)?.status)
            val notifications = context.getSystemService(NotificationManager::class.java).activeNotifications
            assertTrue(
                "expected the user-visible notification after the system transition",
                notifications.any { it.id == id.hashCode() },
            )
        } finally {
            context.getSystemService(NotificationManager::class.java).cancel(id.hashCode())
            WorkManager.getInstance(context).cancelUniqueWork(id).result.get()
            database.reminderDao().delete(id)
            database.close()
        }
    }

    private fun assumePhase(expected: String) {
        assumeTrue(InstrumentationRegistry.getArguments().getString("vexel.reminder.phase") == expected)
    }

    private companion object {
        const val REBOOT_REMINDER_ID = "vexel-system-reboot-acceptance"
        const val DOZE_REMINDER_ID = "vexel-system-doze-acceptance"
    }
}
