package com.vexel.passport.feature.timeline

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vexel.passport.core.database.HealthEventEntity
import com.vexel.passport.core.designsystem.EmptyState
import com.vexel.passport.core.designsystem.StatusPill
import com.vexel.passport.core.model.EpisodeEvent
import com.vexel.passport.core.model.summarizeSymptomEpisodes
import com.vexel.passport.core.ui.CaptureDialog
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun TimelineScreen(
    modifier: Modifier = Modifier,
    viewModel: TimelineViewModel = hiltViewModel(),
) {
    val events by viewModel.events.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val query by viewModel.query.collectAsState()
    val selectedKind by viewModel.selectedKind.collectAsState()

    TimelineScreen(
        events = events,
        allEvents = allEvents,
        archivedEvents = viewModel.archivedEvents,
        query = query,
        onQueryChange = viewModel::setQuery,
        selectedKind = selectedKind,
        onSelectedKindChange = viewModel::setSelectedKind,
        onLoadMore = viewModel::loadMore,
        modifier = modifier,
        onAddEvent = viewModel::addEvent,
        onArchive = viewModel::archive,
        onUnarchive = viewModel::unarchive,
        onDelete = viewModel::delete,
    )
}

/** Timeline tab: search/filter over all user-entered records, add flow, archive-with-undo, permanent delete. */
@Composable
fun TimelineScreen(
    events: List<HealthEventEntity>,
    allEvents: List<HealthEventEntity>,
    archivedEvents: SharedFlow<HealthEventEntity>,
    query: String,
    onQueryChange: (String) -> Unit,
    selectedKind: String?,
    onSelectedKindChange: (String?) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier,
    onAddEvent: (kind: String, title: String, details: String, severity: Int?) -> Unit,
    onArchive: (HealthEventEntity) -> Unit,
    onUnarchive: (HealthEventEntity) -> Unit,
    onDelete: (HealthEventEntity) -> Unit,
) {
    var showKindPicker by rememberSaveable { mutableStateOf(false) }
    var addKind by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<HealthEventEntity?>(null) }
    val availableKinds = remember(allEvents) { allEvents.map { it.kind }.distinct().sorted() }
    val episodeSummaries = summarizeSymptomEpisodes(allEvents.filter { it.kind == "SYMPTOM" }.map { EpisodeEvent(it.title, it.episodeId, it.effectiveAtEpochMillis ?: it.createdAtEpochMillis, it.ongoing) })
    val archiveSnackbarState = remember { SnackbarHostState() }
    LaunchedEffect(archivedEvents) {
        archivedEvents.collect { event ->
            val result = archiveSnackbarState.showSnackbar("Archived", actionLabel = "Undo", withDismissAction = true)
            if (result == SnackbarResult.ActionPerformed) onUnarchive(event)
        }
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
                onLoadMore()
            }
        }
    }

    Box(modifier.fillMaxSize()) {
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Health timeline", style = MaterialTheme.typography.headlineSmall); TextButton({ showKindPicker = true }) { Text("Add") } } }
        item { OutlinedTextField(query, onQueryChange, Modifier.fillMaxWidth(), label = { Text("Search timeline") }, singleLine = true) }
        if (availableKinds.size > 1) item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selectedKind == null, onClick = { onSelectedKindChange(null) }, label = { Text("All types") })
                availableKinds.forEach { kind ->
                    FilterChip(selected = selectedKind == kind, onClick = { onSelectedKindChange(if (selectedKind == kind) null else kind) }, label = { Text(kind.lowercase().replaceFirstChar { it.uppercase() }) })
                }
            }
        }
        if (episodeSummaries.isNotEmpty()) item { Text("Recorded episodes: ${episodeSummaries.size} · ${episodeSummaries.sumOf { it.entryCount }} linked entries", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (events.isEmpty() && allEvents.isEmpty()) item { EmptyState("No records yet", "Symptoms, medications and other user-entered events will appear here.", "Log a record", onAction = { showKindPicker = true }) }
        else if (events.isEmpty()) item { EmptyState("No matches", "Try a different search term, or clear the search and type filters.") }
        else items(events, key = { it.id }) { event ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(16.dp)) { Text(event.title, style = MaterialTheme.typography.titleMedium); Text(event.kind.lowercase().replaceFirstChar { it.uppercase() }); if (event.details.isNotBlank()) Text(event.details); event.severity?.let { Text("Recorded severity: $it/10") }; if (event.durationMinutes != null) Text("Duration: ${event.durationMinutes} minutes"); if (event.ongoing) Text("Ongoing"); if (event.bodyLocation.isNotBlank()) Text("Location: ${event.bodyLocation}"); if (event.associatedSymptoms.isNotBlank()) Text("Associated symptoms: ${event.associatedSymptoms}"); if (event.possibleTrigger.isNotBlank()) Text("Observed possible trigger: ${event.possibleTrigger}"); if (event.relatedMedication.isNotBlank()) Text("Related medication: ${event.relatedMedication}"); event.episodeId?.let { Text("Episode or flare ID: $it") }; Text(DateFormat.getDateInstance().format(Date(event.effectiveAtEpochMillis ?: event.createdAtEpochMillis))); Row { TextButton({ onArchive(event) }) { Text("Archive") }; TextButton({ pendingDelete = event }) { Text("Delete") } } } }
        }
    }
    SnackbarHost(archiveSnackbarState, modifier = Modifier.align(Alignment.BottomCenter))
    }
    if (showKindPicker) {
        AlertDialog(
            onDismissRequest = { showKindPicker = false },
            title = { Text("What would you like to add?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        "CONSULTATION" to "Consultation",
                        "PROCEDURE" to "Procedure",
                        "OTHER" to "Other record",
                    ).forEach { (kind, label) ->
                        TextButton(onClick = { addKind = kind; showKindPicker = false }, modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth()) { Text(label) }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton({ showKindPicker = false }) { Text("Cancel") } },
        )
    }
    addKind?.let { kind ->
        val heading = when (kind) {
            "CONSULTATION" -> "Log a consultation"
            "PROCEDURE" -> "Log a procedure"
            else -> "Add health event"
        }
        CaptureDialog(kind, heading, { addKind = null }, onAddEvent)
    }
    pendingDelete?.let { event ->
        AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("Delete event?") }, text = { Text("This removes the selected user-entered event from the timeline.") }, confirmButton = { Button({ onDelete(event); pendingDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") } }, dismissButton = { TextButton({ pendingDelete = null }) { Text("Cancel") } })
    }
}
