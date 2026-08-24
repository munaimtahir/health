package com.vexel.passport.feature.records

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vexel.passport.core.database.HealthDatabase
import com.vexel.passport.core.database.DocumentEntity
import com.vexel.passport.core.files.SecureFileStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RecordsViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val database: HealthDatabase,
    private val secureFileStore: SecureFileStore,
) : ViewModel() {

    private val _sortBy = MutableStateFlow("DATE")
    val sortBy: StateFlow<String> = _sortBy

    private val _limit = MutableStateFlow(50)
    val limit: StateFlow<Int> = _limit

    private val _operationError = MutableStateFlow<String?>(null)
    val operationError: StateFlow<String?> = _operationError

    private val _statusEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val statusEvents = _statusEvents.asSharedFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val documents = kotlinx.coroutines.flow.combine(_sortBy, _limit) { sort, l ->
        Pair(sort, l)
    }.flatMapLatest { (sort, l) ->
        database.documentDao().observeSortedPaginated(sort, l)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSortBy(sort: String) {
        _sortBy.value = sort
        _limit.value = 50
    }

    fun loadMore() {
        _limit.value = _limit.value + 50
    }

    fun dismissOperationError() {
        _operationError.value = null
    }

    fun importDocument(
        uri: Uri,
        title: String,
        category: String,
        documentDate: String,
        notes: String,
        testName: String = "",
        laboratoryName: String = "",
        radiologyModality: String = "",
        radiologyRegion: String = "",
        centreName: String = "",
        reportingDoctors: String = "",
        prescribingDoctor: String = "",
        doctorSpecialty: String = "",
        certificateType: String = "",
        validityStartDate: String = "",
        validityEndDate: String = "",
        bodyLocation: String = "",
        linkedSymptom: String = "",
        linkedCondition: String = "",
    ) = viewModelScope.launch {
        try {
            val mimeType = appContext.contentResolver.getType(uri)
            if (mimeType == null) {
                _operationError.value = "Could not read the selected file's type. Try choosing it again."
                return@launch
            }
            val displayName = appContext.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else "document"
            } ?: "document"
            val preserved = appContext.contentResolver.openInputStream(uri)?.use { input ->
                secureFileStore.preserveOriginal(input, mimeType, displayName)
            }
            if (preserved == null) {
                _operationError.value = "Could not open the selected file. It may have been moved or access was revoked."
                return@launch
            }
            val now = System.currentTimeMillis()
            database.documentDao().insert(
                DocumentEntity(
                    id = preserved.id,
                    title = title.trim().ifBlank { displayName },
                    category = category.trim().ifBlank { "OTHER" },
                    documentDate = documentDate.trim(),
                    notes = notes.trim(),
                    originalFileName = displayName,
                    mimeType = preserved.mimeType,
                    byteCount = preserved.byteCount,
                    sha256 = preserved.sha256,
                    createdAtEpochMillis = now,
                    testName = testName.trim(),
                    laboratoryName = laboratoryName.trim(),
                    radiologyModality = radiologyModality.trim(),
                    radiologyRegion = radiologyRegion.trim(),
                    centreName = centreName.trim(),
                    reportingDoctors = reportingDoctors.trim(),
                    prescribingDoctor = prescribingDoctor.trim(),
                    doctorSpecialty = doctorSpecialty.trim(),
                    certificateType = certificateType.trim(),
                    validityStartDate = validityStartDate.trim(),
                    validityEndDate = validityEndDate.trim(),
                    bodyLocation = bodyLocation.trim(),
                    linkedSymptom = linkedSymptom.trim(),
                    linkedCondition = linkedCondition.trim(),
                )
            )
            _statusEvents.tryEmit("Document imported")
        } catch (cancellation: kotlinx.coroutines.CancellationException) {
            throw cancellation
        } catch (failure: IllegalArgumentException) {
            _operationError.value = "That file type isn't supported, or it's larger than the 50 MB limit."
        } catch (failure: Exception) {
            _operationError.value = "Import failed. Try again."
        }
    }

    fun deleteDocument(document: DocumentEntity) = viewModelScope.launch {
        secureFileStore.delete(document.id)
        database.documentDao().delete(document.id)
    }

    suspend fun documentThumbnail(document: DocumentEntity) =
        secureFileStore.thumbnailFor(appContext, document.id, document.mimeType)

    fun updateDocument(
        document: DocumentEntity,
        title: String,
        category: String,
        documentDate: String,
        notes: String,
        testName: String = "",
        laboratoryName: String = "",
        radiologyModality: String = "",
        radiologyRegion: String = "",
        centreName: String = "",
        reportingDoctors: String = "",
        prescribingDoctor: String = "",
        doctorSpecialty: String = "",
        certificateType: String = "",
        validityStartDate: String = "",
        validityEndDate: String = "",
        bodyLocation: String = "",
        linkedSymptom: String = "",
        linkedCondition: String = "",
    ) = viewModelScope.launch {
        database.documentDao().update(
            document.copy(
                title = title.trim().ifBlank { document.title },
                category = category.trim().ifBlank { document.category },
                documentDate = documentDate.trim(),
                notes = notes.trim(),
                testName = testName.trim(),
                laboratoryName = laboratoryName.trim(),
                radiologyModality = radiologyModality.trim(),
                radiologyRegion = radiologyRegion.trim(),
                centreName = centreName.trim(),
                reportingDoctors = reportingDoctors.trim(),
                prescribingDoctor = prescribingDoctor.trim(),
                doctorSpecialty = doctorSpecialty.trim(),
                certificateType = certificateType.trim(),
                validityStartDate = validityStartDate.trim(),
                validityEndDate = validityEndDate.trim(),
                bodyLocation = bodyLocation.trim(),
                linkedSymptom = linkedSymptom.trim(),
                linkedCondition = linkedCondition.trim(),
            )
        )
    }

    fun replaceDocument(document: DocumentEntity, uri: Uri) = viewModelScope.launch {
        try {
            val mimeType = appContext.contentResolver.getType(uri)
            if (mimeType == null) {
                _operationError.value = "Could not read the selected file's type. Try choosing it again."
                return@launch
            }
            val displayName = appContext.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else document.originalFileName
            } ?: document.originalFileName
            val replaced = appContext.contentResolver.openInputStream(uri)?.use { input ->
                secureFileStore.replaceOriginal(document.id, input, mimeType, displayName)
            }
            if (replaced == null) {
                _operationError.value = "Could not open the selected file. It may have been moved or access was revoked."
                return@launch
            }
            database.documentDao().update(
                document.copy(
                    originalFileName = displayName,
                    mimeType = replaced.mimeType,
                    byteCount = replaced.byteCount,
                    sha256 = replaced.sha256
                )
            )
            _statusEvents.tryEmit("Document replaced")
        } catch (cancellation: kotlinx.coroutines.CancellationException) {
            throw cancellation
        } catch (failure: IllegalArgumentException) {
            _operationError.value = "That file type isn't supported, or it's larger than the 50 MB limit."
        } catch (failure: Exception) {
            _operationError.value = "Replace failed. Try again."
        }
    }

    fun openDocument(document: DocumentEntity) = viewModelScope.launch {
        val file = secureFileStore.copyToShareCache(appContext, document.id, document.originalFileName)
        val uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.files", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, document.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Open document").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        appContext.startActivity(chooser)
    }

    fun shareDocument(document: DocumentEntity) = viewModelScope.launch {
        val file = secureFileStore.copyToShareCache(appContext, document.id, document.originalFileName)
        val uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.files", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = document.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = android.content.ClipData.newUri(appContext.contentResolver, "Vexel shared content", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share document").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        appContext.startActivity(chooser)
    }
}
