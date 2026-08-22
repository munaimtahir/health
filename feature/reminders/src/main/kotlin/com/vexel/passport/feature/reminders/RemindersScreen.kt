package com.vexel.passport.feature.reminders

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vexel.passport.core.database.ReminderEntity
import com.vexel.passport.core.designsystem.EmptyState
import com.vexel.passport.core.designsystem.StatusPill
import com.vexel.passport.core.ui.DateTimeField
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RemindersScreen(
    modifier: Modifier = Modifier,
    viewModel: RemindersViewModel = hiltViewModel(),
) {
    val reminders by viewModel.reminders.collectAsState()
    RemindersScreen(
        reminders = reminders,
        modifier = modifier,
        onAddReminder = viewModel::addReminder,
        onUpdateReminder = viewModel::updateReminder,
        onSnooze = viewModel::snoozeReminder,
        onComplete = viewModel::completeReminder,
        onDelete = viewModel::deleteReminder,
    )
}

/** Plan tab: reminder list (upcoming/history), create/edit/snooze/complete/delete. */
@Composable
fun RemindersScreen(
    reminders: List<ReminderEntity>,
    modifier: Modifier,
    onAddReminder: (title: String, type: String, notes: String, dueAtEpochMillis: Long, recurrence: String) -> Unit,
    onUpdateReminder: (ReminderEntity, title: String, type: String, notes: String, dueAtEpochMillis: Long, recurrence: String) -> Unit,
    onSnooze: (ReminderEntity) -> Unit,
    onComplete: (ReminderEntity) -> Unit,
    onDelete: (ReminderEntity) -> Unit,
) {
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<ReminderEntity?>(null) }
    var pendingEdit by remember { mutableStateOf<ReminderEntity?>(null) }
    var selectedView by rememberSaveable { mutableStateOf("UPCOMING") }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val visibleReminders = if (selectedView == "HISTORY") reminders.filter { it.status != "SCHEDULED" } else reminders.filter { it.status == "SCHEDULED" }
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Reminders", style = MaterialTheme.typography.headlineSmall); TextButton({ showAdd = true }) { Text("Add") } } }
        item { Text("Reminders are user-created and do not determine medical intervals.") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selectedView == "UPCOMING", onClick = { selectedView = "UPCOMING" }, label = { Text("Upcoming") })
                FilterChip(selected = selectedView == "HISTORY", onClick = { selectedView = "HISTORY" }, label = { Text("History") })
            }
        }
        if (reminders.isEmpty()) item { EmptyState("No reminders yet", "Create a follow-up, review or custom reminder when you want one.", "Create reminder", onAction = { showAdd = true }) }
        else if (visibleReminders.isEmpty()) item { EmptyState(if (selectedView == "HISTORY") "No reminder history" else "No upcoming reminders", if (selectedView == "HISTORY") "Completed or missed reminders will appear here." else "Your scheduled reminders will appear here.") }
        else items(visibleReminders, key = { it.id }) { reminder ->
            val isOverdue = reminder.status == "SCHEDULED" && reminder.dueAtEpochMillis < System.currentTimeMillis()
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                    StatusPill(if (isOverdue) "Overdue" else reminder.status.lowercase().replaceFirstChar { it.uppercase() })
                }
                Text(
                    "${reminder.type} · ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(reminder.dueAtEpochMillis))}",
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (reminder.notes.isNotBlank()) Text(reminder.notes)
                Row { if (reminder.status == "SCHEDULED") { TextButton({ onSnooze(reminder) }) { Text("Snooze 1h") }; TextButton({ onComplete(reminder) }) { Text("Complete") } }; TextButton({ pendingEdit = reminder }) { Text("Edit") }; TextButton({ pendingDelete = reminder }) { Text("Delete") } }
            } }
        }
    }
    if (showAdd) ReminderDialog(onAddReminder, { showAdd = false }) { if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }
    pendingEdit?.let { reminder -> ReminderEditDialog(reminder, onUpdateReminder) { pendingEdit = null } }
    pendingDelete?.let { reminder -> AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("Delete reminder?") }, text = { Text("The scheduled notification will be cancelled.") }, confirmButton = { Button({ onDelete(reminder); pendingDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") } }, dismissButton = { TextButton({ pendingDelete = null }) { Text("Cancel") } }) }
}

@Composable
private fun ReminderEditDialog(reminder: ReminderEntity, onSave: (ReminderEntity, String, String, String, Long, String) -> Unit, onDismiss: () -> Unit) {
    var title by rememberSaveable(reminder.id) { mutableStateOf(reminder.title) }
    var type by rememberSaveable(reminder.id) { mutableStateOf(reminder.type) }
    var dueText by rememberSaveable(reminder.id) { mutableStateOf(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(reminder.dueAtEpochMillis))) }
    var notes by rememberSaveable(reminder.id) { mutableStateOf(reminder.notes) }
    var recurrence by rememberSaveable(reminder.id) { mutableStateOf(reminder.recurrence) }
    val dueAt = runCatching { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { isLenient = false }.parse(dueText)?.time }.getOrNull()
    val error = when { title.isBlank() -> "A title is required"; dueAt == null -> "Use yyyy-MM-dd HH:mm"; dueAt <= System.currentTimeMillis() -> "Choose a future time"; else -> "" }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Edit reminder") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(title, { title = it }, label = { Text("Title") }, isError = error.isNotBlank())
        OutlinedTextField(type, { type = it }, label = { Text("Type") })
        DateTimeField("Date and time", dueText, { dueText = it }, isError = dueAt == null)
        Text("Repeats", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ONCE" to "Once", "DAILY" to "Daily", "WEEKLY" to "Weekly", "MONTHLY" to "Monthly").forEach { (value, label) ->
                FilterChip(selected = recurrence == value, onClick = { recurrence = value }, label = { Text(label) })
            }
        }
        OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") })
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
    } }, confirmButton = { Button(enabled = error.isBlank(), onClick = { onSave(reminder, title, type, notes, dueAt!!, recurrence); onDismiss() }) { Text("Save") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}

@Composable
private fun ReminderDialog(onSave: (String, String, String, Long, String) -> Unit, onDismiss: () -> Unit, onScheduled: () -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("CUSTOM") }
    var dueText by rememberSaveable { mutableStateOf(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis() + 3_600_000))) }
    var notes by rememberSaveable { mutableStateOf("") }
    var recurrence by rememberSaveable { mutableStateOf("ONCE") }
    val dueAt = runCatching { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { isLenient = false }.parse(dueText)?.time }.getOrNull()
    val error = when { title.isBlank() -> "A title is required"; dueAt == null -> "Use yyyy-MM-dd HH:mm"; dueAt <= System.currentTimeMillis() -> "Choose a future time"; else -> "" }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add reminder") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(title, { title = it }, label = { Text("Title") }, isError = error.isNotBlank())
        OutlinedTextField(type, { type = it }, label = { Text("Type") })
        DateTimeField("Date and time", dueText, { dueText = it }, isError = dueAt == null)
        Text("Repeats", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ONCE" to "Once", "DAILY" to "Daily", "WEEKLY" to "Weekly", "MONTHLY" to "Monthly").forEach { (value, label) ->
                FilterChip(selected = recurrence == value, onClick = { recurrence = value }, label = { Text(label) })
            }
        }
        OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") })
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
    } }, confirmButton = { Button(enabled = error.isBlank(), onClick = { onSave(title, type, notes, dueAt!!, recurrence); onScheduled(); onDismiss() }) { Text("Schedule") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}
