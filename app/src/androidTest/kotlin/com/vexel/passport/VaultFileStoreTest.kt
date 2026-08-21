package com.vexel.passport

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

import com.vexel.passport.core.files.LocalSecureFileStore

@RunWith(AndroidJUnit4::class)
class VaultFileStoreTest {
    @Test
    fun replacing_document_preserves_id_and_updates_bytes_and_hash() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = LocalSecureFileStore(context)
        val original = "original document".toByteArray(StandardCharsets.UTF_8)
        val replacement = "replacement document".toByteArray(StandardCharsets.UTF_8)
        val created = store.preserveOriginal(ByteArrayInputStream(original), "application/pdf", "original.pdf")
        try {
            val updated = store.replaceOriginal(created.id, ByteArrayInputStream(replacement), "image/png", "replacement.png")
            assertEquals(created.id, updated.id)
            assertEquals(replacement.size.toLong(), updated.byteCount)
            assertEquals(sha256(replacement), updated.sha256)
            store.open(created.id).use { assertArrayEquals(replacement, it.readBytes()) }
        } finally {
            store.delete(created.id)
        }
    }

    @Test
    fun image_document_gets_a_bounded_cached_thumbnail() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = LocalSecureFileStore(context)
        val pngBytes = syntheticPng(width = 800, height = 600)
        val created = store.preserveOriginal(ByteArrayInputStream(pngBytes), "image/png", "photo.png")
        try {
            val thumbnail = store.thumbnailFor(context, created.id, "image/png")
            assertTrue("a thumbnail file must be produced for an image document", thumbnail != null && thumbnail.exists())
            val decoded = android.graphics.BitmapFactory.decodeFile(thumbnail!!.path)
            assertTrue("thumbnail must be decodable", decoded != null)
            assertTrue(
                "thumbnail must be bounded to roughly 120px, not the original 800x600",
                (decoded!!.width <= 120 && decoded.height <= 120),
            )

            // Second call must reuse the cached file rather than regenerating it.
            val cachedAgain = store.thumbnailFor(context, created.id, "image/png")
            assertEquals(thumbnail.path, cachedAgain?.path)
        } finally {
            store.delete(created.id)
        }
    }

    @Test
    fun pdf_document_has_no_thumbnail() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = LocalSecureFileStore(context)
        val created = store.preserveOriginal(ByteArrayInputStream("not really a pdf".toByteArray()), "application/pdf", "doc.pdf")
        try {
            assertEquals(null, store.thumbnailFor(context, created.id, "application/pdf"))
        } finally {
            store.delete(created.id)
        }
    }

    @Test
    fun deleting_a_document_also_clears_its_cached_thumbnail() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = LocalSecureFileStore(context)
        val created = store.preserveOriginal(ByteArrayInputStream(syntheticPng(200, 200)), "image/png", "photo.png")
        val thumbnail = store.thumbnailFor(context, created.id, "image/png")
        assertTrue(thumbnail != null && thumbnail.exists())
        store.delete(created.id)
        assertTrue("thumbnail must not survive document deletion", thumbnail?.exists() != true)
    }

    private fun syntheticPng(width: Int, height: Int): ByteArray {
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.BLUE)
        val output = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output)
        bitmap.recycle()
        return output.toByteArray()
    }

    private fun sha256(bytes: ByteArray): String = java.security.MessageDigest.getInstance("SHA-256")
        .digest(bytes).joinToString("") { "%02x".format(it) }
}
