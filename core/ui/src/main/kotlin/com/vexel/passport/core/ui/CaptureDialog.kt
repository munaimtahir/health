package com.vexel.passport.core.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import com.vexel.passport.core.designsystem.SectionHeader
import com.vexel.passport.core.model.SymptomDraft
import com.vexel.passport.core.model.validationErrors

/**
 * Generic capture dialog for a health-event kind. When `kind == "SYMPTOM"` and [onSaveSymptom]
 * is supplied, the full structured symptom form (timing, location, associated symptoms, image
 * attachment) is shown and [onSaveSymptom] is called; otherwise only title/notes are shown and
 * [onSave] is called. Shared across any screen that logs a health-event kind (symptom,
 * consultation, procedure, other), so it lives in core:ui rather than a single feature module.
 */
@Composable
fun CaptureDialog(kind: String, heading: String, onDismiss: () -> Unit, onSave: (String, String, String, Int?) -> Unit, onSaveSymptom: ((SymptomDraft, Uri?) -> Unit)? = null) {
    var title by rememberSaveable { mutableStateOf("") }; var details by rememberSaveable { mutableStateOf("") }; var severityText by rememberSaveable { mutableStateOf("") }
    var startAtText by rememberSaveable { mutableStateOf("") }; var endAtText by rememberSaveable { mutableStateOf("") }; var durationText by rememberSaveable { mutableStateOf("") }; var ongoing by rememberSaveable { mutableStateOf(false) }; var bodyLocation by rememberSaveable { mutableStateOf("") }; var associatedSymptoms by rememberSaveable { mutableStateOf("") }; var possibleTrigger by rememberSaveable { mutableStateOf("") }; var relatedMedication by rememberSaveable { mutableStateOf("") }; var episodeId by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var hasAttemptedSave by rememberSaveable { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { imageUri = it }
    val severity = severityText.toIntOrNull()
    val draft = SymptomDraft(title, if (kind == "SYMPTOM") severity else null, details, startAtText, endAtText, durationText.toIntOrNull(), ongoing, bodyLocation, associatedSymptoms, possibleTrigger, relatedMedication, episodeId)
    val errors = draft.validationErrors()
    AlertDialog(onDismissRequest = onDismiss, title = { Text(heading) }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(title, { title = it }, label = { Text(if (kind == "SYMPTOM") "Symptom" else "Title") }, isError = hasAttemptedSave && errors.containsKey("name"))
        if (kind == "SYMPTOM") OutlinedTextField(severityText, { severityText = it.filter(Char::isDigit) }, label = { Text("Severity (0–10, optional)") }, isError = errors.containsKey("severity"), supportingText = { errors["severity"]?.let { Text(it) } })
        OutlinedTextField(details, { details = it }, label = { Text("Notes (optional)") }, isError = errors.containsKey("notes"), supportingText = { errors["notes"]?.let { Text(it) } })
        if (kind == "SYMPTOM") {
            SectionHeader("Timing and details")
            DateTimeField("Start (optional)", startAtText, { startAtText = it }, isError = errors.containsKey("startAt"), supportingText = errors["startAt"]?.let { message -> { Text(message) } })
            DateTimeField("End (optional)", endAtText, { endAtText = it }, enabled = !ongoing, isError = errors.containsKey("endAt"), supportingText = errors["endAt"]?.let { message -> { Text(message) } })
            OutlinedTextField(durationText, { durationText = it.filter(Char::isDigit) }, label = { Text("Duration in minutes (optional)") }, isError = errors.containsKey("duration"))
            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(ongoing, { ongoing = it }); Text("Ongoing") }
            OutlinedTextField(bodyLocation, { bodyLocation = it }, label = { Text("Body location (optional)") })
            OutlinedTextField(associatedSymptoms, { associatedSymptoms = it }, label = { Text("Associated symptoms (optional)") })
            OutlinedTextField(possibleTrigger, { possibleTrigger = it }, label = { Text("Possible trigger, as you observed (optional)") })
            OutlinedTextField(relatedMedication, { relatedMedication = it }, label = { Text("Related medication (optional)") })
            OutlinedTextField(episodeId, { episodeId = it }, label = { Text("Episode or flare ID (optional)") })
            OutlinedButton({ imagePicker.launch(arrayOf("image/jpeg", "image/png")) }) { Text(if (imageUri == null) "Attach symptom image (optional)" else "Image attached") }
        }
    } }, confirmButton = { Button(onClick = {
        hasAttemptedSave = true
        if (errors.isEmpty()) {
            if (kind == "SYMPTOM" && onSaveSymptom != null) onSaveSymptom(draft, imageUri) else onSave(kind, title.trim(), details.trim(), if (kind == "SYMPTOM") severity else null)
            onDismiss()
        }
    }) { Text("Save") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}
