package com.vexel.passport.core.database

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
    val testName: String = "",
    val laboratoryName: String = "",
    val radiologyModality: String = "",
    val radiologyRegion: String = "",
    val centreName: String = "",
    val reportingDoctors: String = "",
    val prescribingDoctor: String = "",
    val doctorSpecialty: String = "",
    val certificateType: String = "",
    val validityStartDate: String = "",
    val validityEndDate: String = "",
    val bodyLocation: String = "",
    val linkedSymptom: String = "",
    val linkedCondition: String = "",
)
