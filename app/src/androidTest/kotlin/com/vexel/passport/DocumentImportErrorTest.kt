package com.vexel.passport

import android.net.Uri
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vexel.passport.core.database.HealthDatabase
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

/**
 * Documents larger than the 50 MiB limit and unsupported MIME types are rejected by
 * SecureFileStore with an IllegalArgumentException from require(). PassportViewModel.
 * importDocument() runs that call inside viewModelScope.launch with no caller to catch
 * it, so before the fix in commit history around this test, either failure would crash
 * the app with an uncaught exception instead of surfacing anything to the user.
 */
@RunWith(AndroidJUnit4::class)
class DocumentImportErrorTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun oversized_document_import_surfaces_a_safe_error_instead_of_crashing() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, HealthDatabase::class.java).build()
        val fileStore = LocalSecureFileStore(context)
        val oversizedFile = File(context.cacheDir, "oversized-${UUID.randomUUID()}.pdf")
        try {
            // One byte over the 50 MiB limit; written sparse-ish via RandomAccessFile to avoid
            // holding 50+ MiB in memory during the test itself.
            java.io.RandomAccessFile(oversizedFile, "rw").use { it.setLength(50L * 1024 * 1024 + 1) }

            val viewModel = PassportViewModel(
                appContext = context,
                database = database,
                preferences = PreferencesStore(context),
                pinMaterialCipher = KeystorePinMaterialCipher(),
                secureFileStore = fileStore,
                reminderScheduler = NoOpReminderScheduler,
            )
            viewModel.importDocument(Uri.fromFile(oversizedFile), "Oversized", "OTHER", "", "").join()

            val error = withTimeout(10_000) { viewModel.operationError.first { it != null } }
            assertTrue("error message must not be blank", error!!.isNotBlank())
            assertTrue("error must not leak the raw exception type", !error.contains("IllegalArgumentException"))
            assertTrue("no document should have been persisted", withTimeout(5_000) { viewModel.documents.first() }.isEmpty())
        } finally {
            oversizedFile.delete()
            database.close()
        }
    }

    private object NoOpReminderScheduler : ReminderScheduler {
        override suspend fun schedule(id: String, dueAtEpochMillis: Long, recurrence: String) = Unit
        override suspend fun cancel(id: String) = Unit
        override suspend fun reconcile() = Unit
    }
}
