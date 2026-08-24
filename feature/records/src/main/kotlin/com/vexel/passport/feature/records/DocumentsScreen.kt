package com.vexel.passport.feature.records

import android.net.Uri
import com.vexel.passport.core.ui.FullScreenDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vexel.passport.core.database.DocumentEntity
import com.vexel.passport.core.designsystem.EmptyState
import com.vexel.passport.core.designsystem.StatusPill

private enum class DocumentSort(val label: String, val value: String) {
    DATE("Date", "DATE"),
    CATEGORY("Category", "CATEGORY"),
    TYPE("Type", "TYPE")
}

private val CATEGORIES = listOf(
    "ALL" to "All Files",
    "LABORATORY_REPORT" to "Lab Reports",
    "RADIOLOGY_IMAGE" to "Radiology & Imaging",
    "PRESCRIPTION" to "Prescriptions",
    "MEDICAL_CERTIFICATE" to "Certificates",
    "HEALTH_MEDIA" to "Health Media",
    "OTHER" to "Other Docs"
)

@Composable
fun DocumentsScreen(
    modifier: Modifier = Modifier,
    viewModel: RecordsViewModel = hiltViewModel()
) {
    val documents by viewModel.documents.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val operationError by viewModel.operationError.collectAsState()

    DocumentsScreen(
        vm = viewModel,
        documents = documents,
        sortBy = sortBy,
        modifier = modifier,
        onSortByChange = viewModel::setSortBy
    )

    operationError?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissOperationError,
            title = { Text("Something went wrong") },
            text = { Text(message) },
            confirmButton = { TextButton(viewModel::dismissOperationError) { Text("OK") } }
        )
    }
}

@Composable
private fun DocumentThumbnail(vm: RecordsViewModel, document: DocumentEntity) {
    var thumbnailBitmap by remember(document.id) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(document.id, document.mimeType) {
        thumbnailBitmap = null
        val file = vm.documentThumbnail(document)
        if (file != null) {
            val decoded = android.graphics.BitmapFactory.decodeFile(file.path)
            thumbnailBitmap = decoded?.asImageBitmap()
        }
    }
    val bitmap = thumbnailBitmap
    if (bitmap != null) {
        Image(bitmap, contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)))
    } else {
        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentsScreen(
    vm: RecordsViewModel,
    documents: List<DocumentEntity>,
    sortBy: String,
    modifier: Modifier,
    onSortByChange: (String) -> Unit
) {
    var showImport by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<DocumentEntity?>(null) }
    var pendingEdit by remember { mutableStateOf<DocumentEntity?>(null) }
    var pendingReplace by remember { mutableStateOf<DocumentEntity?>(null) }
    var filterCategory by rememberSaveable { mutableStateOf("ALL") }

    val replaceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val document = pendingReplace
        if (uri != null && document != null) vm.replaceDocument(document, uri)
        pendingReplace = null
    }

    val filteredDocuments = remember(documents, filterCategory) {
        if (filterCategory == "ALL") documents
        else documents.filter { it.category == filterCategory }
    }

    val listState = rememberLazyListState()
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false
            lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5
        }
    }
    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }.collect { isNearEnd ->
            if (isNearEnd) {
                vm.loadMore()
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Private document vault", style = MaterialTheme.typography.headlineSmall)
                TextButton({ showImport = true }) { Text("Import File") }
            }
        }
        item { Text("All medical documentation, media scans, and prescriptions are stored locally.") }
        
        // Category Filter Chips
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CATEGORIES.forEach { (catId, catLabel) ->
                    FilterChip(
                        selected = filterCategory == catId,
                        onClick = { filterCategory = catId },
                        label = { Text(catLabel) }
                    )
                }
            }
        }

        // Sorting Chips
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sort by:", modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically), color = MaterialTheme.colorScheme.onSurfaceVariant)
                DocumentSort.entries.forEach { option ->
                    FilterChip(selected = sortBy == option.value, onClick = { onSortByChange(option.value) }, label = { Text(option.label) })
                }
            }
        }

        if (filteredDocuments.isEmpty()) {
            item {
                EmptyState(
                    title = "No documents found",
                    message = "Import files and medical scans to organize them securely on your device.",
                    actionLabel = "Import a document",
                    onAction = { showImport = true }
                )
            }
        } else {
            items(filteredDocuments, key = { it.id }) { document ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DocumentThumbnail(vm, document)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(document.title, style = MaterialTheme.typography.titleMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val readableCat = CATEGORIES.find { it.first == document.category }?.second ?: document.category
                                StatusPill(readableCat)
                                Text("${document.mimeType.substringAfterLast('/')}, ${document.byteCount / 1024} KB", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (document.documentDate.isNotBlank()) {
                                Text("Document date: ${document.documentDate}", style = MaterialTheme.typography.bodySmall)
                            }

                            // Subtype Specific Metadata Display
                            when (document.category) {
                                "LABORATORY_REPORT" -> {
                                    if (document.testName.isNotBlank() || document.laboratoryName.isNotBlank()) {
                                        Text("Test: ${document.testName} · Lab: ${document.laboratoryName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                "RADIOLOGY_IMAGE" -> {
                                    if (document.radiologyModality.isNotBlank() || document.radiologyRegion.isNotBlank()) {
                                        Text("Modality: ${document.radiologyModality} (${document.radiologyRegion})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    if (document.centreName.isNotBlank() || document.reportingDoctors.isNotBlank()) {
                                        Text("Facility: ${document.centreName} · Doctor: ${document.reportingDoctors}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                "PRESCRIPTION" -> {
                                    if (document.prescribingDoctor.isNotBlank() || document.doctorSpecialty.isNotBlank()) {
                                        Text("Prescribed by: Dr. ${document.prescribingDoctor} (${document.doctorSpecialty})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                "MEDICAL_CERTIFICATE" -> {
                                    if (document.certificateType.isNotBlank() || document.reportingDoctors.isNotBlank()) {
                                        Text("Type: ${document.certificateType} · Certifier: Dr. ${document.reportingDoctors}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    if (document.validityStartDate.isNotBlank() || document.validityEndDate.isNotBlank()) {
                                        Text("Validity: ${document.validityStartDate} to ${document.validityEndDate}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                "HEALTH_MEDIA" -> {
                                    if (document.bodyLocation.isNotBlank() || document.linkedSymptom.isNotBlank()) {
                                        Text("Location: ${document.bodyLocation} · Symptom: ${document.linkedSymptom}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            if (document.notes.isNotBlank()) {
                                Text(document.notes, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row {
                                    TextButton({ vm.openDocument(document) }) { Text("Open") }
                                    TextButton({ vm.shareDocument(document) }) { Text("Share") }
                                }
                                Row {
                                    TextButton({ pendingEdit = document }) { Text("Edit") }
                                    TextButton({ pendingReplace = document; replaceLauncher.launch(arrayOf("application/pdf", "image/jpeg", "image/png")) }) { Text("Replace") }
                                    TextButton({ pendingDelete = document }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showImport) DocumentImportDialog(vm) { showImport = false }
    pendingDelete?.let { document ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete document?") },
            text = { Text("The private file and its metadata will be removed from this device.") },
            confirmButton = {
                Button(
                    onClick = { vm.deleteDocument(document); pendingDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton({ pendingDelete = null }) { Text("Cancel") } }
        )
    }
    pendingEdit?.let { document -> DocumentEditDialog(vm, document) { pendingEdit = null } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentEditDialog(vm: RecordsViewModel, document: DocumentEntity, onDismiss: () -> Unit) {
    var title by rememberSaveable(document.id) { mutableStateOf(document.title) }
    var category by rememberSaveable(document.id) { mutableStateOf(document.category) }
    var date by rememberSaveable(document.id) { mutableStateOf(document.documentDate) }
    var notes by rememberSaveable(document.id) { mutableStateOf(document.notes) }

    // Subtype metadata states
    var testName by rememberSaveable(document.id) { mutableStateOf(document.testName) }
    var laboratoryName by rememberSaveable(document.id) { mutableStateOf(document.laboratoryName) }
    var radiologyModality by rememberSaveable(document.id) { mutableStateOf(document.radiologyModality) }
    var radiologyRegion by rememberSaveable(document.id) { mutableStateOf(document.radiologyRegion) }
    var centreName by rememberSaveable(document.id) { mutableStateOf(document.centreName) }
    var reportingDoctors by rememberSaveable(document.id) { mutableStateOf(document.reportingDoctors) }
    var prescribingDoctor by rememberSaveable(document.id) { mutableStateOf(document.prescribingDoctor) }
    var doctorSpecialty by rememberSaveable(document.id) { mutableStateOf(document.doctorSpecialty) }
    var certificateType by rememberSaveable(document.id) { mutableStateOf(document.certificateType) }
    var validityStartDate by rememberSaveable(document.id) { mutableStateOf(document.validityStartDate) }
    var validityEndDate by rememberSaveable(document.id) { mutableStateOf(document.validityEndDate) }
    var bodyLocation by rememberSaveable(document.id) { mutableStateOf(document.bodyLocation) }
    var linkedSymptom by rememberSaveable(document.id) { mutableStateOf(document.linkedSymptom) }
    var linkedCondition by rememberSaveable(document.id) { mutableStateOf(document.linkedCondition) }

    FullScreenDialog(
        title = "Edit document details",
        onDismiss = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    vm.updateDocument(
                        document = document,
                        title = title,
                        category = category,
                        documentDate = date,
                        notes = notes,
                        testName = testName,
                        laboratoryName = laboratoryName,
                        radiologyModality = radiologyModality,
                        radiologyRegion = radiologyRegion,
                        centreName = centreName,
                        reportingDoctors = if (category == "MEDICAL_CERTIFICATE") reportingDoctors else reportingDoctors,
                        prescribingDoctor = prescribingDoctor,
                        doctorSpecialty = doctorSpecialty,
                        certificateType = certificateType,
                        validityStartDate = validityStartDate,
                        validityEndDate = validityEndDate,
                        bodyLocation = bodyLocation,
                        linkedSymptom = linkedSymptom,
                        linkedCondition = linkedCondition
                    )
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") }
            )

            Text("Document Category", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CATEGORIES.filter { it.first != "ALL" }.forEach { (catId, catLabel) ->
                    FilterChip(
                        selected = category == catId,
                        onClick = { category = catId },
                        label = { Text(catLabel) }
                    )
                }
            }

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Document Date (yyyy-MM-dd)") }
            )

            // Dynamic Forms
            when (category) {
                "LABORATORY_REPORT" -> {
                    OutlinedTextField(testName, { testName = it }, Modifier.fillMaxWidth(), label = { Text("Test Name") })
                    OutlinedTextField(laboratoryName, { laboratoryName = it }, Modifier.fillMaxWidth(), label = { Text("Laboratory Name") })
                }
                "RADIOLOGY_IMAGE" -> {
                    OutlinedTextField(radiologyModality, { radiologyModality = it }, Modifier.fillMaxWidth(), label = { Text("Modality (e.g., X-ray, MRI, CT)") })
                    OutlinedTextField(radiologyRegion, { radiologyRegion = it }, Modifier.fillMaxWidth(), label = { Text("Body Region") })
                    OutlinedTextField(centreName, { centreName = it }, Modifier.fillMaxWidth(), label = { Text("Facility/Centre Name") })
                    OutlinedTextField(reportingDoctors, { reportingDoctors = it }, Modifier.fillMaxWidth(), label = { Text("Reporting Doctor(s)") })
                }
                "PRESCRIPTION" -> {
                    OutlinedTextField(prescribingDoctor, { prescribingDoctor = it }, Modifier.fillMaxWidth(), label = { Text("Prescribing Doctor") })
                    OutlinedTextField(doctorSpecialty, { doctorSpecialty = it }, Modifier.fillMaxWidth(), label = { Text("Doctor Specialty") })
                }
                "MEDICAL_CERTIFICATE" -> {
                    OutlinedTextField(certificateType, { certificateType = it }, Modifier.fillMaxWidth(), label = { Text("Certificate Type (e.g., Sick Leave)") })
                    OutlinedTextField(reportingDoctors, { reportingDoctors = it }, Modifier.fillMaxWidth(), label = { Text("Certifying Doctor") })
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(validityStartDate, { validityStartDate = it }, Modifier.weight(1f), label = { Text("Start Date") })
                        OutlinedTextField(validityEndDate, { validityEndDate = it }, Modifier.weight(1f), label = { Text("End Date") })
                    }
                }
                "HEALTH_MEDIA" -> {
                    OutlinedTextField(bodyLocation, { bodyLocation = it }, Modifier.fillMaxWidth(), label = { Text("Body Location") })
                    OutlinedTextField(linkedSymptom, { linkedSymptom = it }, Modifier.fillMaxWidth(), label = { Text("Symptom/Condition Context") })
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes (optional)") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentImportDialog(vm: RecordsViewModel, onDismiss: () -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("OTHER") }
    var documentDate by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    // Subtype metadata states
    var testName by rememberSaveable { mutableStateOf("") }
    var laboratoryName by rememberSaveable { mutableStateOf("") }
    var radiologyModality by rememberSaveable { mutableStateOf("") }
    var radiologyRegion by rememberSaveable { mutableStateOf("") }
    var centreName by rememberSaveable { mutableStateOf("") }
    var reportingDoctors by rememberSaveable { mutableStateOf("") }
    var prescribingDoctor by rememberSaveable { mutableStateOf("") }
    var doctorSpecialty by rememberSaveable { mutableStateOf("") }
    var certificateType by rememberSaveable { mutableStateOf("") }
    var validityStartDate by rememberSaveable { mutableStateOf("") }
    var validityEndDate by rememberSaveable { mutableStateOf("") }
    var bodyLocation by rememberSaveable { mutableStateOf("") }
    var linkedSymptom by rememberSaveable { mutableStateOf("") }
    var linkedCondition by rememberSaveable { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            vm.importDocument(
                uri = uri,
                title = title,
                category = category,
                documentDate = documentDate,
                notes = notes,
                testName = testName,
                laboratoryName = laboratoryName,
                radiologyModality = radiologyModality,
                radiologyRegion = radiologyRegion,
                centreName = centreName,
                reportingDoctors = reportingDoctors,
                prescribingDoctor = prescribingDoctor,
                doctorSpecialty = doctorSpecialty,
                certificateType = certificateType,
                validityStartDate = validityStartDate,
                validityEndDate = validityEndDate,
                bodyLocation = bodyLocation,
                linkedSymptom = linkedSymptom,
                linkedCondition = linkedCondition
            )
            onDismiss()
        }
    }

    FullScreenDialog(
        title = "Import private document",
        onDismiss = onDismiss,
        confirmButton = {
            TextButton(onClick = { launcher.launch(arrayOf("application/pdf", "image/jpeg", "image/png")) }) {
                Text("Choose file")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Choose a PDF or image. The original file is preserved privately inside the app's secure sandbox.")

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") }
            )

            Text("Document Category", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CATEGORIES.filter { it.first != "ALL" }.forEach { (catId, catLabel) ->
                    FilterChip(
                        selected = category == catId,
                        onClick = { category = catId },
                        label = { Text(catLabel) }
                    )
                }
            }

            OutlinedTextField(
                value = documentDate,
                onValueChange = { documentDate = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Document Date (yyyy-MM-dd)") }
            )

            // Dynamic Forms based on Category
            when (category) {
                "LABORATORY_REPORT" -> {
                    OutlinedTextField(testName, { testName = it }, Modifier.fillMaxWidth(), label = { Text("Test Name (e.g., Blood Panel)") })
                    OutlinedTextField(laboratoryName, { laboratoryName = it }, Modifier.fillMaxWidth(), label = { Text("Laboratory Name") })
                }
                "RADIOLOGY_IMAGE" -> {
                    OutlinedTextField(radiologyModality, { radiologyModality = it }, Modifier.fillMaxWidth(), label = { Text("Modality (e.g., MRI, CT, X-ray)") })
                    OutlinedTextField(radiologyRegion, { radiologyRegion = it }, Modifier.fillMaxWidth(), label = { Text("Body Region") })
                    OutlinedTextField(centreName, { centreName = it }, Modifier.fillMaxWidth(), label = { Text("Facility/Centre Name") })
                    OutlinedTextField(reportingDoctors, { reportingDoctors = it }, Modifier.fillMaxWidth(), label = { Text("Reporting Doctor(s)") })
                }
                "PRESCRIPTION" -> {
                    OutlinedTextField(prescribingDoctor, { prescribingDoctor = it }, Modifier.fillMaxWidth(), label = { Text("Prescribing Doctor") })
                    OutlinedTextField(doctorSpecialty, { doctorSpecialty = it }, Modifier.fillMaxWidth(), label = { Text("Doctor Specialty") })
                }
                "MEDICAL_CERTIFICATE" -> {
                    OutlinedTextField(certificateType, { certificateType = it }, Modifier.fillMaxWidth(), label = { Text("Certificate Type (e.g., sick leave, travel clearance)") })
                    OutlinedTextField(reportingDoctors, { reportingDoctors = it }, Modifier.fillMaxWidth(), label = { Text("Certifying Doctor") })
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(validityStartDate, { validityStartDate = it }, Modifier.weight(1f), label = { Text("Start Date") })
                        OutlinedTextField(validityEndDate, { validityEndDate = it }, Modifier.weight(1f), label = { Text("End Date") })
                    }
                }
                "HEALTH_MEDIA" -> {
                    OutlinedTextField(bodyLocation, { bodyLocation = it }, Modifier.fillMaxWidth(), label = { Text("Body Location") })
                    OutlinedTextField(linkedSymptom, { linkedSymptom = it }, Modifier.fillMaxWidth(), label = { Text("Symptom/Condition Context") })
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes (optional)") }
            )
        }
    }
}
