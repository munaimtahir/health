package pk.vexel.healthpassport.core.files

import java.io.InputStream

interface SecureFileStore {
    suspend fun preserveOriginal(input: InputStream, mimeType: String): String
}

