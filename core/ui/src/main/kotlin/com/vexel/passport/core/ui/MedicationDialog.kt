package com.vexel.passport.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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

    FullScreenDialog(
        title = "Add medication",
        onDismiss = onDismiss,
        confirmButton = {
            TextButton(enabled = errors.isEmpty(), onClick = { onSave(draft); onDismiss() }) {
                Text("Save")
            }
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Medication name") },
            isError = errors.containsKey("name"),
            supportingText = { errors["name"]?.let { Text(it) } }
        )
        OutlinedTextField(
            value = genericName,
            onValueChange = { genericName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Generic or brand name (optional)") }
        )
        OutlinedTextField(
            value = strength,
            onValueChange = { strength = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Strength") }
        )
        OutlinedTextField(
            value = dose,
            onValueChange = { dose = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Dose") }
        )
        OutlinedTextField(
            value = unit,
            onValueChange = { unit = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Unit (optional)") }
        )
        OutlinedTextField(
            value = route,
            onValueChange = { route = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Route (optional)") }
        )
        OutlinedTextField(
            value = frequency,
            onValueChange = { frequency = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Frequency (optional)") }
        )
        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Start date, as yyyy-MM-dd (optional)") },
            isError = errors.containsKey("startDate"),
            supportingText = { errors["startDate"]?.let { Text(it) } }
        )
        OutlinedTextField(
            value = stopDate,
            onValueChange = { stopDate = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Stop date, as yyyy-MM-dd (optional)") },
            isError = errors.containsKey("stopDate"),
            supportingText = { errors["stopDate"]?.let { Text(it) } }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = status == "CURRENT", onClick = { status = "CURRENT" }, label = { Text("Current") })
            FilterChip(selected = status == "STOPPED", onClick = { status = "STOPPED" }, label = { Text("Stopped") })
        }
        OutlinedTextField(
            value = indication,
            onValueChange = { indication = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Indication (optional)") }
        )
        OutlinedTextField(
            value = physician,
            onValueChange = { physician = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Physician (optional)") }
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") },
            isError = errors.containsKey("notes"),
            supportingText = { errors["notes"]?.let { Text(it) } }
        )
        errors.values.firstOrNull()?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

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

    FullScreenDialog(
        title = "Record medication change",
        onDismiss = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(strength, dose, unit, frequency, status, notes); onDismiss() }) {
                Text("Save change")
            }
        }
    ) {
        Text("This records a new treatment period; it does not recommend a dose or treatment.")
        OutlinedTextField(
            value = strength,
            onValueChange = { strength = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Strength") }
        )
        OutlinedTextField(
            value = dose,
            onValueChange = { dose = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Dose") }
        )
        OutlinedTextField(
            value = unit,
            onValueChange = { unit = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Unit") }
        )
        OutlinedTextField(
            value = frequency,
            onValueChange = { frequency = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Frequency") }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = status == "CURRENT", onClick = { status = "CURRENT" }, label = { Text("Current/restarted") })
            FilterChip(selected = status == "STOPPED", onClick = { status = "STOPPED" }, label = { Text("Stopped") })
        }
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Change notes (optional)") }
        )
    }
}
