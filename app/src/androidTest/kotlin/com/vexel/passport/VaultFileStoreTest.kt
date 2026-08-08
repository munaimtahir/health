package com.vexel.passport

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
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

    private fun sha256(bytes: ByteArray): String = java.security.MessageDigest.getInstance("SHA-256")
        .digest(bytes).joinToString("") { "%02x".format(it) }
}
