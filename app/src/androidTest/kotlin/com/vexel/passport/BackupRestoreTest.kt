package com.vexel.passport

import android.net.Uri
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vexel.passport.core.database.DocumentEntity
import com.vexel.passport.core.database.HealthDatabase
import com.vexel.passport.core.database.ProfileEntity
import com.vexel.passport.core.datastore.PreferencesStore
import com.vexel.passport.core.files.LocalSecureFileStore
import com.vexel.passport.core.model.MedicationDraft
import com.vexel.passport.core.model.SymptomDraft
import com.vexel.passport.core.notifications.WorkManagerReminderScheduler
import com.vexel.passport.core.security.BackupCrypto
import com.vexel.passport.core.security.KeystorePinMaterialCipher
import java.io.ByteArrayInputStream
import java.io.File
import java.security.MessageDigest
import java.util.UUID
import javax.crypto.AEADBadTagException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the real create-backup / restore-backup path used by [PassportViewModel]
 * against on-device Room + Keystore + file-store implementations, simulating a
 * clean-install restore. No dedicated coverage existed for this flow before.
 */
@RunWith(AndroidJUnit4::class)
class BackupRestoreTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val fileStore = LocalSecureFileStore(context)
    private val backupFile = File(context.cacheDir, "backup-restore-test-${UUID.randomUUID()}.vexel")
    private val password = "correct-horse-battery-staple"
    private val createdDocumentIds = mutableListOf<String>()
    private val scheduledReminderIds = mutableListOf<String>()

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes).joinToString("") { "%02x".format(it) }

    private fun newViewModel(database: HealthDatabase) = PassportViewModel(
        database = database,
        preferences = PreferencesStore(context),
        pinMaterialCipher = KeystorePinMaterialCipher(),
        secureFileStore = fileStore,
        reminderScheduler = WorkManagerReminderScheduler(context),
    )

    @After
    fun cleanUp() {
        backupFile.delete()
        createdDocumentIds.forEach { runCatching { fileStore.delete(it) } }
        val scheduler = WorkManagerReminderScheduler(context)
        runBlocking { scheduledReminderIds.forEach { runCatching { scheduler.cancel(it) } } }
    }

    @Test
    fun backup_then_restore_on_fresh_install_preserves_all_data() = runBlocking {
        val sourceDatabase = Room.inMemoryDatabaseBuilder(context, HealthDatabase::class.java).build()
        val documentBytes = "synthetic document contents ${UUID.randomUUID()}".toByteArray(Charsets.UTF_8)
        try {
            val sourceVm = newViewModel(sourceDatabase)

            // Seed one of each entity type through the same code paths the app UI uses.
            sourceVm.saveProfile(ProfileEntity(name = "Test Patient", dateOfBirth = "1990-01-01", bloodGroup = "O+", allergies = "Penicillin", conditions = "Asthma", emergencyContact = "555-0100")).join()
            withTimeout(10_000) { sourceVm.profile.first { it?.name == "Test Patient" } }

            sourceVm.addSymptom(context, SymptomDraft(name = "Headache", severity = 5, notes = "Synthetic test note")).join()
            withTimeout(10_000) { sourceVm.events.first { it.isNotEmpty() } }

            sourceVm.addMedication(MedicationDraft(name = "Test Med", strength = "10mg", dose = "1", unit = "tablet", frequency = "daily", status = "CURRENT")).join()
            withTimeout(10_000) { sourceVm.medications.first { it.isNotEmpty() } }
            withTimeout(10_000) { sourceVm.medicationChanges.first { it.isNotEmpty() } }

            val preserved = fileStore.preserveOriginal(ByteArrayInputStream(documentBytes), "application/pdf", "synthetic.pdf")
            createdDocumentIds += preserved.id
            sourceDatabase.documentDao().insert(DocumentEntity(preserved.id, "Synthetic report", "LAB", "2026-01-01", "Synthetic test document", "synthetic.pdf", preserved.mimeType, preserved.byteCount, preserved.sha256, System.currentTimeMillis()))
            withTimeout(10_000) { sourceVm.documents.first { it.isNotEmpty() } }

            val dueAt = System.currentTimeMillis() + 3_600_000
            sourceVm.addReminder("Refill prescription", "MEDICATION", "Synthetic reminder", dueAt, "ONCE").join()
            val seededReminder = withTimeout(10_000) { sourceVm.reminders.first { it.isNotEmpty() } }.first()
            scheduledReminderIds += seededReminder.id

            // Create the encrypted backup.
            val backupUri = Uri.fromFile(backupFile)
            sourceVm.createBackup(context, backupUri, password).join()
            assertTrue("backup file should be written", backupFile.exists() && backupFile.length() > 0)
            val backupBytes = backupFile.readBytes()
            assertTrue("backup must be encrypted", BackupCrypto.isEncrypted(backupBytes))

            // Wrong password must be rejected, not silently accepted.
            assertThrows(AEADBadTagException::class.java) {
                BackupCrypto.decrypt(backupBytes, "totally-wrong-password".toCharArray())
            }

            // A tampered backup must fail integrity verification, not restore corrupted data.
            val tampered = backupBytes.copyOf()
            tampered[tampered.size - 1] = (tampered[tampered.size - 1].toInt() xor 0xFF).toByte()
            assertThrows(AEADBadTagException::class.java) {
                BackupCrypto.decrypt(tampered, password.toCharArray())
            }

            // Restore into a fresh database + fresh document store, simulating a clean install.
            val targetDatabase = Room.inMemoryDatabaseBuilder(context, HealthDatabase::class.java).build()
            try {
                val targetVm = newViewModel(targetDatabase)
                targetVm.restoreBackup(context, backupUri, password).join()

                val restoredProfile = withTimeout(10_000) { targetVm.profile.first { it != null } }
                assertEquals("Test Patient", restoredProfile?.name)
                assertEquals("Penicillin", restoredProfile?.allergies)
                assertEquals("Asthma", restoredProfile?.conditions)

                val restoredEvents = withTimeout(10_000) { targetVm.events.first { it.isNotEmpty() } }
                assertTrue(restoredEvents.any { it.title == "Headache" && it.kind == "SYMPTOM" })

                val restoredMeds = withTimeout(10_000) { targetVm.medications.first { it.isNotEmpty() } }
                assertTrue(restoredMeds.any { it.name == "Test Med" && it.strength == "10mg" })

                val restoredChanges = withTimeout(10_000) { targetVm.medicationChanges.first { it.isNotEmpty() } }
                assertTrue(restoredChanges.any { it.changeType == "STARTED" })

                val restoredDocs = withTimeout(10_000) { targetVm.documents.first { it.isNotEmpty() } }
                val restoredDoc = restoredDocs.first()
                assertEquals("byte-for-byte integrity must survive backup+restore", sha256(documentBytes), restoredDoc.sha256)
                assertNotEquals("restored document must get a fresh id, not reuse the source id", preserved.id, restoredDoc.id)
                createdDocumentIds += restoredDoc.id
                fileStore.open(restoredDoc.id).use { assertEquals(documentBytes.toList(), it.readBytes().toList()) }

                val restoredReminders = withTimeout(10_000) { targetVm.reminders.first { it.isNotEmpty() } }
                val restoredReminder = restoredReminders.first { it.title == "Refill prescription" }
                assertEquals(dueAt, restoredReminder.dueAtEpochMillis)
                scheduledReminderIds += restoredReminder.id
            } finally {
                targetDatabase.close()
            }
        } finally {
            sourceDatabase.close()
        }
    }
}
