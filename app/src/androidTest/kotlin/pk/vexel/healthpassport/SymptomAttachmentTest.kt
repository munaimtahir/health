package pk.vexel.healthpassport

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.ByteArrayInputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import pk.vexel.healthpassport.core.files.LocalSecureFileStore

@RunWith(AndroidJUnit4::class)
class SymptomAttachmentTest {
    @Test
    fun private_png_attachment_is_preserved_and_removed() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = LocalSecureFileStore(context)
        val pngFixture = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a)
        val attachment = store.preserveOriginal(ByteArrayInputStream(pngFixture), "image/png", "symptom.png")
        try {
            assertEquals("image/png", attachment.mimeType)
            store.open(attachment.id).use { assertArrayEquals(pngFixture, it.readBytes()) }
        } finally {
            assertEquals(true, store.delete(attachment.id))
        }
    }
}
