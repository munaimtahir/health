package com.vexel.passport

import android.net.Uri
import android.os.SystemClock
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vexel.passport.core.database.HealthDatabase
import com.vexel.passport.core.database.HealthEventEntity
import com.vexel.passport.core.datastore.PreferencesStore
import com.vexel.passport.core.files.LocalSecureFileStore
import com.vexel.passport.core.notifications.ReminderScheduler
import com.vexel.passport.core.security.KeystorePinMaterialCipher
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LargeDataPerformanceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun large_export_pdf_and_encrypted_backup_complete_within_device_budget() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, HealthDatabase::class.java).build()
        val pdfFile = File(context.cacheDir, "large-report-${UUID.randomUUID()}.pdf")
        val backupFile = File(context.cacheDir, "large-backup-${UUID.randomUUID()}.vexel")
        try {
            val baseTime = System.currentTimeMillis() - LARGE_EVENT_COUNT * 60_000L
            repeat(LARGE_EVENT_COUNT) { index ->
                val eventTime = baseTime + index * 60_000L
                database.healthEventDao().insert(
                    HealthEventEntity(
                        id = UUID.randomUUID().toString(),
                        title = "Synthetic symptom ${index % 20}",
                        details = "Synthetic performance entry $index with bounded non-clinical text",
                        kind = "SYMPTOM",
                        effectiveAtEpochMillis = eventTime,
                        createdAtEpochMillis = eventTime,
                        updatedAtEpochMillis = eventTime,
                        severity = index % 11,
                    )
                )
            }

            val viewModel = PassportViewModel(
                appContext = context,
                database = database,
                preferences = PreferencesStore(context),
                pinMaterialCipher = KeystorePinMaterialCipher(),
                secureFileStore = LocalSecureFileStore(context),
                reminderScheduler = NoOpReminderScheduler,
            )
            withTimeout(30_000) { viewModel.events.first { it.size == LARGE_EVENT_COUNT } }

            val exportStarted = SystemClock.elapsedRealtime()
            val export = viewModel.exportJson()
            val exportDuration = SystemClock.elapsedRealtime() - exportStarted
            assertTrue(export.contains("Synthetic symptom"))
            assertTrue("large JSON export took ${exportDuration}ms", exportDuration < OPERATION_BUDGET_MILLIS)

            val pdfStarted = SystemClock.elapsedRealtime()
            viewModel.createPdfReport(
                uri = Uri.fromFile(pdfFile),
                includeProfile = false,
                includeEvents = true,
                includeMedications = false,
                includeDocuments = false,
                includeReminders = false,
            ).join()
            val pdfDuration = SystemClock.elapsedRealtime() - pdfStarted
            assertTrue(pdfFile.exists() && pdfFile.length() > 0)
            assertTrue("large PDF generation took ${pdfDuration}ms", pdfDuration < OPERATION_BUDGET_MILLIS)

            val backupStarted = SystemClock.elapsedRealtime()
            viewModel.createBackup(Uri.fromFile(backupFile), "synthetic-performance-password").join()
            val backupDuration = SystemClock.elapsedRealtime() - backupStarted
            assertTrue(backupFile.exists() && backupFile.length() > 0)
            assertTrue("large encrypted backup took ${backupDuration}ms", backupDuration < OPERATION_BUDGET_MILLIS)
        } finally {
            pdfFile.delete()
            backupFile.delete()
            database.close()
        }
    }

    private object NoOpReminderScheduler : ReminderScheduler {
        override suspend fun schedule(id: String, dueAtEpochMillis: Long, recurrence: String) = Unit
        override suspend fun cancel(id: String) = Unit
        override suspend fun reconcile() = Unit
    }

    private companion object {
        const val LARGE_EVENT_COUNT = 1_500
        const val OPERATION_BUDGET_MILLIS = 30_000L
    }
}
