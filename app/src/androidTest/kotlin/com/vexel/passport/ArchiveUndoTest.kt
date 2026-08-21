package com.vexel.passport

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vexel.passport.core.database.HealthDatabase
import com.vexel.passport.core.datastore.PreferencesStore
import com.vexel.passport.core.files.LocalSecureFileStore
import com.vexel.passport.core.notifications.ReminderScheduler
import com.vexel.passport.core.security.KeystorePinMaterialCipher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Archiving a timeline event is a fully reversible soft-delete (the row stays, only an
 * `archived` flag flips), so it is a case Phase 4 explicitly calls out for an Undo action --
 * unlike permanent document deletion, which must never offer a false Undo. Verifies the
 * unarchive() round trip and that PassportViewModel.archive() emits the archived event for
 * the UI's Undo Snackbar to consume.
 */
@RunWith(AndroidJUnit4::class)
class ArchiveUndoTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun archived_event_can_be_restored_and_reappears_in_the_active_list() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, HealthDatabase::class.java).build()
        try {
            val vm = PassportViewModel(
                appContext = context,
                database = database,
                preferences = PreferencesStore(context),
                pinMaterialCipher = KeystorePinMaterialCipher(),
                secureFileStore = LocalSecureFileStore(context),
                reminderScheduler = object : ReminderScheduler {
                    override suspend fun schedule(id: String, dueAtEpochMillis: Long, recurrence: String) = Unit
                    override suspend fun cancel(id: String) = Unit
                    override suspend fun reconcile() = Unit
                },
            )
            vm.addEvent("OTHER", "Synthetic archive-undo record", "Synthetic test note").join()
            val event = withTimeout(10_000) { vm.events.first { it.isNotEmpty() } }.first { it.title == "Synthetic archive-undo record" }

            vm.archive(event).join()
            withTimeout(10_000) { vm.events.first { events -> events.none { it.id == event.id } } }

            val emitted = withTimeout(10_000) { vm.archivedEvents.first() }
            assertEquals("archivedEvents must emit the exact event that was archived", event.id, emitted.id)

            vm.unarchive(event).join()
            val restored = withTimeout(10_000) { vm.events.first { events -> events.any { it.id == event.id } } }
            assertTrue("unarchived event must be active again", restored.any { it.id == event.id && it.archived.not() })
        } finally {
            database.close()
        }
    }
}
