package pk.vexel.healthpassport.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String = "OTHER",
    val documentDate: String = "",
    val notes: String = "",
    val originalFileName: String,
    val mimeType: String,
    val byteCount: Long,
    val sha256: String,
    val createdAtEpochMillis: Long,
    val archived: Boolean = false,
)
