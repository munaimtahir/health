package pk.vexel.healthpassport.core.files

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
    fun open(id: String): java.io.InputStream
    fun delete(id: String): Boolean
    suspend fun deleteAll()
}
