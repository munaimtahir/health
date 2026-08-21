package com.vexel.passport.core.files

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalSecureFileStore(context: Context) : SecureFileStore {
    private val root = File(context.filesDir, "documents").apply { mkdirs() }
    private val shareCacheDir = File(context.cacheDir, "shared")
    private val thumbnailCacheDir = File(context.cacheDir, "vault_thumbnails")

    override suspend fun preserveOriginal(input: InputStream, mimeType: String, displayName: String): PreservedDocument =
        withContext(Dispatchers.IO) {
            require(mimeType in SUPPORTED_MIME_TYPES) { "Unsupported document type" }
            val id = UUID.randomUUID().toString()
            val destination = File(root, id)
            val digest = MessageDigest.getInstance("SHA-256")
            var count = 0L
            try {
                BufferedInputStream(input).use { source ->
                    destination.outputStream().use { target ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var read = source.read(buffer)
                        while (read >= 0) {
                            count += read
                            require(count <= MAX_DOCUMENT_BYTES) { "Document is too large" }
                            digest.update(buffer, 0, read)
                            target.write(buffer, 0, read)
                            read = source.read(buffer)
                        }
                    }
                }
                PreservedDocument(id, id, mimeType, count, digest.digest().toHex())
            } catch (error: Throwable) {
                destination.delete()
                throw error
            }
        }

    override suspend fun replaceOriginal(id: String, input: InputStream, mimeType: String, displayName: String): PreservedDocument =
        withContext(Dispatchers.IO) {
            require(ID_PATTERN.matches(id)) { "Invalid document identifier" }
            require(mimeType in SUPPORTED_MIME_TYPES) { "Unsupported document type" }
            val destination = File(root, id)
            val temporary = File(root, ".${id}.replacement")
            val digest = MessageDigest.getInstance("SHA-256")
            var count = 0L
            try {
                BufferedInputStream(input).use { source ->
                    temporary.outputStream().use { target ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var read = source.read(buffer)
                        while (read >= 0) {
                            count += read
                            require(count <= MAX_DOCUMENT_BYTES) { "Document is too large" }
                            digest.update(buffer, 0, read)
                            target.write(buffer, 0, read)
                            read = source.read(buffer)
                        }
                    }
                }
                require(temporary.renameTo(destination)) { "Unable to replace document" }
                thumbnailFile(id).delete()
                PreservedDocument(id, id, mimeType, count, digest.digest().toHex())
            } catch (error: Throwable) {
                temporary.delete()
                throw error
            }
        }

    override fun open(id: String): InputStream {
        require(ID_PATTERN.matches(id)) { "Invalid document identifier" }
        return File(root, id).inputStream()
    }

    override fun delete(id: String): Boolean {
        require(ID_PATTERN.matches(id)) { "Invalid document identifier" }
        thumbnailFile(id).delete()
        return File(root, id).delete()
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            root.listFiles()?.forEach { file -> if (file.isFile) file.delete() }
            // Also purge temporary open/share copies created by copyToShareCache(), and any
            // cached vault thumbnails, so a full data deletion doesn't leave document bytes
            // (even downsampled ones) behind in app-private cache.
            shareCacheDir.listFiles()?.forEach { file -> if (file.isFile) file.delete() }
            thumbnailCacheDir.listFiles()?.forEach { file -> if (file.isFile) file.delete() }
        }
    }

    override suspend fun copyToShareCache(context: Context, id: String, fileName: String): File = withContext(Dispatchers.IO) {
        require(ID_PATTERN.matches(id)) { "Invalid document identifier" }
        val safeName = fileName.substringAfterLast('/').substringAfterLast('\\').replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "document" }
        val destination = shareCacheDir.apply { mkdirs() }.resolve("${UUID.randomUUID()}_$safeName")
        open(id).use { source -> destination.outputStream().use { target -> source.copyTo(target) } }
        destination
    }

    override suspend fun thumbnailFor(context: Context, id: String, mimeType: String): File? = withContext(Dispatchers.IO) {
        require(ID_PATTERN.matches(id)) { "Invalid document identifier" }
        if (mimeType !in setOf("image/jpeg", "image/png")) return@withContext null
        val cached = thumbnailFile(id)
        if (cached.exists() && cached.length() > 0) return@withContext cached
        val source = File(root, id)
        if (!source.exists()) return@withContext null
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(source.path, bounds)
            var sampleSize = 1
            while ((bounds.outWidth / sampleSize) > THUMBNAIL_MAX_DIMENSION_PX * 2 || (bounds.outHeight / sampleSize) > THUMBNAIL_MAX_DIMENSION_PX * 2) {
                sampleSize *= 2
            }
            val decoded = BitmapFactory.decodeFile(source.path, BitmapFactory.Options().apply { inSampleSize = sampleSize })
                ?: return@runCatching null
            val scale = THUMBNAIL_MAX_DIMENSION_PX.toFloat() / maxOf(decoded.width, decoded.height)
            val bounded = if (scale < 1f) {
                Bitmap.createScaledBitmap(decoded, (decoded.width * scale).toInt().coerceAtLeast(1), (decoded.height * scale).toInt().coerceAtLeast(1), true)
            } else {
                decoded
            }
            thumbnailCacheDir.mkdirs()
            FileOutputStream(cached).use { output -> bounded.compress(Bitmap.CompressFormat.JPEG, 80, output) }
            if (bounded !== decoded) decoded.recycle()
            bounded.recycle()
            cached
        }.getOrNull()
    }

    private fun thumbnailFile(id: String): File = File(thumbnailCacheDir, "$id.jpg")

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private companion object {
        const val MAX_DOCUMENT_BYTES = 50L * 1024 * 1024
        const val THUMBNAIL_MAX_DIMENSION_PX = 120
        val ID_PATTERN = Regex("[0-9a-fA-F-]{36}")
        val SUPPORTED_MIME_TYPES = setOf("application/pdf", "image/jpeg", "image/png")
    }
}
