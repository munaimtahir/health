package com.vexel.passport.core.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vexel.passport.core.designsystem.SectionHeader
import com.vexel.passport.core.model.SymptomDraft
import com.vexel.passport.core.model.validationErrors

@Composable
fun CaptureDialog(
    kind: String,
    heading: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int?) -> Unit,
    onSaveSymptom: ((SymptomDraft, Uri?) -> Unit)? = null
) {
    var title by rememberSaveable { mutableStateOf("") }
    var details by rememberSaveable { mutableStateOf("") }
    var severityText by rememberSaveable { mutableStateOf("") }
    var startAtText by rememberSaveable { mutableStateOf("") }
    var endAtText by rememberSaveable { mutableStateOf("") }
    var durationText by rememberSaveable { mutableStateOf("") }
    var ongoing by rememberSaveable { mutableStateOf(false) }
    var bodyLocation by rememberSaveable { mutableStateOf("") }
    var associatedSymptoms by rememberSaveable { mutableStateOf("") }
    var possibleTrigger by rememberSaveable { mutableStateOf("") }
    var relatedMedication by rememberSaveable { mutableStateOf("") }
    var episodeId by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var hasAttemptedSave by rememberSaveable { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { imageUri = it }
    val severity = severityText.toIntOrNull()
    val draft = SymptomDraft(title, if (kind == "SYMPTOM") severity else null, details, startAtText, endAtText, durationText.toIntOrNull(), ongoing, bodyLocation, associatedSymptoms, possibleTrigger, relatedMedication, episodeId)
    val errors = draft.validationErrors()

    FullScreenDialog(
        title = heading,
        onDismiss = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                hasAttemptedSave = true
                if (errors.isEmpty()) {
                    if (kind == "SYMPTOM" && onSaveSymptom != null) {
                        onSaveSymptom(draft, imageUri)
                    } else {
                        onSave(kind, title.trim(), details.trim(), if (kind == "SYMPTOM") severity else null)
                    }
                    onDismiss()
                }
            }) {
                Text("Save")
            }
        }
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (kind == "SYMPTOM") "Symptom" else "Title") },
            isError = hasAttemptedSave && errors.containsKey("name"),
            supportingText = { if (hasAttemptedSave && errors.containsKey("name")) errors["name"]?.let { Text(it) } }
        )
        if (kind == "SYMPTOM") {
            OutlinedTextField(
                value = severityText,
                onValueChange = { severityText = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Severity (0–10, optional)") },
                isError = errors.containsKey("severity"),
                supportingText = { errors["severity"]?.let { Text(it) } }
            )
        }
        OutlinedTextField(
            value = details,
            onValueChange = { details = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") },
            isError = errors.containsKey("notes"),
            supportingText = { errors["notes"]?.let { Text(it) } }
        )
        if (kind == "SYMPTOM") {
            SectionHeader("Timing and details")
            DateTimeField(
                label = "Start (optional)",
                value = startAtText,
                onValueChange = { startAtText = it },
                isError = errors.containsKey("startAt"),
                supportingText = errors["startAt"]?.let { message -> { Text(message) } }
            )
            DateTimeField(
                label = "End (optional)",
                value = endAtText,
                onValueChange = { endAtText = it },
                enabled = !ongoing,
                isError = errors.containsKey("endAt"),
                supportingText = errors["endAt"]?.let { message -> { Text(message) } }
            )
            OutlinedTextField(
                value = durationText,
                onValueChange = { durationText = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Duration in minutes (optional)") },
                isError = errors.containsKey("duration"),
                supportingText = { errors["duration"]?.let { Text(it) } }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = ongoing, onCheckedChange = { ongoing = it })
                Text("Ongoing")
            }
            OutlinedTextField(
                value = bodyLocation,
                onValueChange = { bodyLocation = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Body location (optional)") }
            )
            OutlinedTextField(
                value = associatedSymptoms,
                onValueChange = { associatedSymptoms = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Associated symptoms (optional)") }
            )
            OutlinedTextField(
                value = possibleTrigger,
                onValueChange = { possibleTrigger = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Possible trigger, as you observed (optional)") }
            )
            OutlinedTextField(
                value = relatedMedication,
                onValueChange = { relatedMedication = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Related medication (optional)") }
            )
            OutlinedTextField(
                value = episodeId,
                onValueChange = { episodeId = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Episode or flare ID (optional)") }
            )
            OutlinedButton(
                onClick = { imagePicker.launch(arrayOf("image/jpeg", "image/png")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (imageUri == null) "Attach symptom image (optional)" else "Image attached")
            }
        }
    }
}
