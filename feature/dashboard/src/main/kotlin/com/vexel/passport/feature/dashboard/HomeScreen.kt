package com.vexel.passport.feature.dashboard

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.vexel.passport.core.database.HealthEventEntity
import com.vexel.passport.core.database.MedicationEntity
import com.vexel.passport.core.database.ProfileEntity
import com.vexel.passport.core.designsystem.InformationCard
import com.vexel.passport.core.designsystem.SectionHeader
import com.vexel.passport.core.model.MedicationDraft
import com.vexel.passport.core.model.SymptomDraft
import com.vexel.passport.core.model.TrendEvent
import com.vexel.passport.core.model.summarizeSymptoms
import com.vexel.passport.core.ui.CaptureDialog
import com.vexel.passport.core.ui.MedicationChangeDialog
import com.vexel.passport.core.ui.MedicationDialog

import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val medications by viewModel.medications.collectAsState()
    val events by viewModel.events.collectAsState()
    HomeScreen(
        profile = profile,
        medications = medications,
        events = events,
        modifier = modifier,
        onAddEvent = viewModel::addEvent,
        onAddSymptom = viewModel::addSymptom,
        onAddMedication = viewModel::addMedication,
        onRecordMedicationChange = viewModel::recordMedicationChange
    )
}

/** Home tab: a quick "how are you feeling" entry point, a neutral trend summary, and current medications. */
@Composable
fun HomeScreen(
    profile: ProfileEntity?,
    medications: List<MedicationEntity>,
    events: List<HealthEventEntity>,
    modifier: Modifier,
    onAddEvent: (kind: String, title: String, details: String, severity: Int?) -> Unit,
    onAddSymptom: (SymptomDraft, Uri?) -> Unit,
    onAddMedication: (MedicationDraft) -> Unit,
    onRecordMedicationChange: (medication: MedicationEntity, strength: String, dose: String, unit: String, frequency: String, status: String, notes: String) -> Unit,
) {
    var showSymptom by rememberSaveable { mutableStateOf(false) }
    var showMedication by rememberSaveable { mutableStateOf(false) }
    var changeMedication by remember { mutableStateOf<MedicationEntity?>(null) }
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)) {
        item {
            Text(if (profile?.name.isNullOrBlank()) "Welcome" else "Welcome, ${profile?.name}", style = MaterialTheme.typography.headlineSmall)
            Text("Your health history, organized.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Record how you feel", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Keep a clear, user-entered record for yourself and your care conversations.", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Button(onClick = { showSymptom = true }) { Text("Log symptom") }
                }
            }
        }
        item { InformationCard("Privacy", "Stored on this device. No account required.") }
        val trends = summarizeSymptoms(events.map { TrendEvent(it.title, it.kind, it.severity) })
        if (trends.totalEntries > 0) {
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Recorded symptom summary")
                    Text("${trends.totalEntries} symptom entr${if (trends.totalEntries == 1) "y" else "ies"}")
                    trends.mostFrequentSymptom?.let { Text("Most frequent: $it") }
                    trends.averageRecordedSeverity?.let { Text("Average recorded severity: ${"%.1f".format(it)}/10") }
                    Text("A neutral summary of your entries, not a diagnosis.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } }
            }
        }
        if (medications.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Current medications")
                    medications.filter { it.status == "CURRENT" }.take(3).forEach { medication ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${medication.name}${medication.strength.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""}")
                            TextButton({ changeMedication = medication }) { Text("Record change") }
                        }
                    }
                } }
            }
        }
        item {
            OutlinedButton(onClick = { showMedication = true }, modifier = Modifier.fillMaxWidth()) { Text("Add medication record") }
        }
    }
    if (showSymptom) CaptureDialog("SYMPTOM", "Log a symptom", { showSymptom = false }, onAddEvent) { draft, imageUri -> onAddSymptom(draft, imageUri) }
    if (showMedication) MedicationDialog({ showMedication = false }, onAddMedication)
    changeMedication?.let { medication ->
        MedicationChangeDialog(medication.id, medication.strength, medication.dose, medication.unit, medication.frequency, medication.status, { changeMedication = null }) { strength, dose, unit, frequency, status, notes ->
            onRecordMedicationChange(medication, strength, dose, unit, frequency, status, notes); changeMedication = null
        }
    }
}
