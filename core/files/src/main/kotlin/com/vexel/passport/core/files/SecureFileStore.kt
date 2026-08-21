package com.vexel.passport.core.files

import android.content.Context
import java.io.File
import java.io.InputStream

data class PreservedDocument(
    val id: String,
    val fileName: String,
    val mimeType: String,
    val byteCount: Long,
    val sha256: String,
)

interface SecureFileStore {
    suspend fun preserveOriginal(input: InputStream, mimeType: String, displayName: String = "document"): PreservedDocument
    suspend fun replaceOriginal(id: String, input: InputStream, mimeType: String, displayName: String = "document"): PreservedDocument
    fun open(id: String): java.io.InputStream
    fun delete(id: String): Boolean
    suspend fun deleteAll()
    suspend fun copyToShareCache(context: Context, id: String, fileName: String): File

    /**
     * Returns a bounded (~120px) private, disposable JPEG thumbnail for a JPEG/PNG document,
     * generating and caching it on first use. Returns null for non-image documents (e.g. PDF,
     * which should show a generic icon instead) or if decoding fails.
     */
    suspend fun thumbnailFor(context: Context, id: String, mimeType: String): File?
}
