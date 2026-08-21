package com.vexel.passport.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vexel.passport.core.model.MedicationDraft
import com.vexel.passport.core.model.validationErrors

/** New-medication capture dialog. Shared between any screen that can add a medication record. */
@Composable
fun MedicationDialog(onDismiss: () -> Unit, onSave: (MedicationDraft) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var genericName by rememberSaveable { mutableStateOf("") }
    var strength by rememberSaveable { mutableStateOf("") }
    var dose by rememberSaveable { mutableStateOf("") }
    var unit by rememberSaveable { mutableStateOf("") }
    var route by rememberSaveable { mutableStateOf("") }
    var frequency by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf("") }
    var stopDate by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf("CURRENT") }
    var indication by rememberSaveable { mutableStateOf("") }
    var physician by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    val draft = MedicationDraft(name, genericName, strength, dose, unit, route, frequency, startDate, stopDate, status, indication, physician, notes)
    val errors = draft.validationErrors()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add medication") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Medication name") }, isError = errors.containsKey("name"))
            OutlinedTextField(genericName, { genericName = it }, label = { Text("Generic or brand name (optional)") })
            OutlinedTextField(strength, { strength = it }, label = { Text("Strength") })
            OutlinedTextField(dose, { dose = it }, label = { Text("Dose") })
            OutlinedTextField(unit, { unit = it }, label = { Text("Unit (optional)") })
            OutlinedTextField(route, { route = it }, label = { Text("Route (optional)") })
            OutlinedTextField(frequency, { frequency = it }, label = { Text("Frequency (optional)") })
            OutlinedTextField(startDate, { startDate = it }, label = { Text("Start date, as yyyy-MM-dd (optional)") }, isError = errors.containsKey("startDate"), supportingText = { errors["startDate"]?.let { Text(it) } })
            OutlinedTextField(stopDate, { stopDate = it }, label = { Text("Stop date, as yyyy-MM-dd (optional)") }, isError = errors.containsKey("stopDate"), supportingText = { errors["stopDate"]?.let { Text(it) } })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = status == "CURRENT", onClick = { status = "CURRENT" }, label = { Text("Current") })
                FilterChip(selected = status == "STOPPED", onClick = { status = "STOPPED" }, label = { Text("Stopped") })
            }
            OutlinedTextField(indication, { indication = it }, label = { Text("Indication (optional)") })
            OutlinedTextField(physician, { physician = it }, label = { Text("Physician (optional)") })
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") }, isError = errors.containsKey("notes"))
            errors.values.firstOrNull()?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        } },
        confirmButton = { Button(enabled = errors.isEmpty(), onClick = { onSave(draft); onDismiss() }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** Records a dose/status change for an existing medication, starting a new treatment period. */
@Composable
fun MedicationChangeDialog(
    medicationId: String,
    initialStrength: String,
    initialDose: String,
    initialUnit: String,
    initialFrequency: String,
    initialStatus: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit,
) {
    var strength by rememberSaveable(medicationId) { mutableStateOf(initialStrength) }
    var dose by rememberSaveable(medicationId) { mutableStateOf(initialDose) }
    var unit by rememberSaveable(medicationId) { mutableStateOf(initialUnit) }
    var frequency by rememberSaveable(medicationId) { mutableStateOf(initialFrequency) }
    var status by rememberSaveable(medicationId) { mutableStateOf(initialStatus) }
    var notes by rememberSaveable(medicationId) { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Record medication change") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("This records a new treatment period; it does not recommend a dose or treatment.")
        OutlinedTextField(strength, { strength = it }, label = { Text("Strength") })
        OutlinedTextField(dose, { dose = it }, label = { Text("Dose") })
        OutlinedTextField(unit, { unit = it }, label = { Text("Unit") })
        OutlinedTextField(frequency, { frequency = it }, label = { Text("Frequency") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(status == "CURRENT", { status = "CURRENT" }, label = { Text("Current/restarted") }); FilterChip(status == "STOPPED", { status = "STOPPED" }, label = { Text("Stopped") }) }
        OutlinedTextField(notes, { notes = it }, label = { Text("Change notes (optional)") })
    } }, confirmButton = { Button({ onSave(strength, dose, unit, frequency, status, notes) }) { Text("Save change") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}
