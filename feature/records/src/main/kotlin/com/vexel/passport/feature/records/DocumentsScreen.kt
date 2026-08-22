package com.vexel.passport.feature.records

import android.net.Uri
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
    val replaceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val document = pendingReplace
        if (uri != null && document != null) vm.replaceDocument(document, uri)
        pendingReplace = null
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
                TextButton({ showImport = true }) { Text("Import") }
            }
        }
        item { Text("PDF, JPG, JPEG, and PNG files are copied into app-private storage.") }
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sort by:", modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically), color = MaterialTheme.colorScheme.onSurfaceVariant)
                DocumentSort.entries.forEach { option ->
                    FilterChip(selected = sortBy == option.value, onClick = { onSortByChange(option.value) }, label = { Text(option.label) })
                }
            }
        }
        if (documents.isEmpty()) item { EmptyState("Your vault is empty", "Import a PDF or image to keep a private copy on this device.", "Import a document", onAction = { showImport = true }) }
        else items(documents, key = { it.id }) { document ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DocumentThumbnail(vm, document)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(document.title, style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusPill(document.category)
                            Text("${document.mimeType.substringAfterLast('/')}, ${document.byteCount / 1024} KB", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (document.documentDate.isNotBlank()) Text("Document date: ${document.documentDate}")
                        if (document.notes.isNotBlank()) Text(document.notes)
                        Row {
                            TextButton({ vm.openDocument(document) }) { Text("Open") }
                            TextButton({ vm.shareDocument(document) }) { Text("Share") }
                            TextButton({ pendingEdit = document }) { Text("Edit") }
                            TextButton({ pendingReplace = document; replaceLauncher.launch(arrayOf("application/pdf", "image/jpeg", "image/png")) }) { Text("Replace") }
                            TextButton({ pendingDelete = document }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
    if (showImport) DocumentImportDialog(vm) { showImport = false }
    pendingDelete?.let { document ->
        AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("Delete document?") }, text = { Text("The private file and its metadata will be removed from this device.") }, confirmButton = { Button({ vm.deleteDocument(document); pendingDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") } }, dismissButton = { TextButton({ pendingDelete = null }) { Text("Cancel") } })
    }
    pendingEdit?.let { document -> DocumentEditDialog(vm, document) { pendingEdit = null } }
}

@Composable
private fun DocumentEditDialog(vm: RecordsViewModel, document: DocumentEntity, onDismiss: () -> Unit) {
    var title by rememberSaveable(document.id) { mutableStateOf(document.title) }
    var category by rememberSaveable(document.id) { mutableStateOf(document.category) }
    var date by rememberSaveable(document.id) { mutableStateOf(document.documentDate) }
    var notes by rememberSaveable(document.id) { mutableStateOf(document.notes) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Edit document details") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(title, { title = it }, label = { Text("Title") })
        OutlinedTextField(category, { category = it }, label = { Text("Category") })
        OutlinedTextField(date, { date = it }, label = { Text("Document date") })
        OutlinedTextField(notes, { notes = it }, label = { Text("Notes") })
    } }, confirmButton = { Button({ vm.updateDocument(document, title, category, date, notes); onDismiss() }) { Text("Save") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}

@Composable
private fun DocumentImportDialog(vm: RecordsViewModel, onDismiss: () -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("OTHER") }
    var documentDate by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importDocument(uri, title, category, documentDate, notes)
        if (uri != null) onDismiss()
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Import private document") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Choose a PDF or image. The original is preserved privately; unsupported types are rejected.")
        OutlinedTextField(title, { title = it }, label = { Text("Title") })
        OutlinedTextField(category, { category = it }, label = { Text("Category") })
        OutlinedTextField(documentDate, { documentDate = it }, label = { Text("Document date (optional)") })
        OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") })
    } }, confirmButton = { Button({ launcher.launch(arrayOf("application/pdf", "image/jpeg", "image/png")) }) { Text("Choose file") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}
