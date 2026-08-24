package com.vexel.passport.feature.dashboard

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.vexel.passport.core.database.HealthEventEntity
import com.vexel.passport.core.database.MedicationEntity
import com.vexel.passport.core.database.ProfileEntity
import com.vexel.passport.core.database.ConditionEntity
import com.vexel.passport.core.database.AllergyEntity
import com.vexel.passport.core.database.MeasurementEntity
import com.vexel.passport.core.database.ProcedureEntity
import com.vexel.passport.core.database.HospitalisationEntity
import com.vexel.passport.core.database.VaccinationEntity
import com.vexel.passport.core.database.DeviceEntity
import com.vexel.passport.core.database.FamilyHistoryEntity
import com.vexel.passport.core.designsystem.InformationCard
import com.vexel.passport.core.designsystem.SectionHeader
import com.vexel.passport.core.designsystem.StatusPill
import com.vexel.passport.core.model.MedicationDraft
import com.vexel.passport.core.model.SymptomDraft
import com.vexel.passport.core.model.ConditionDraft
import com.vexel.passport.core.model.AllergyDraft
import com.vexel.passport.core.model.ProcedureDraft
import com.vexel.passport.core.model.HospitalisationDraft
import com.vexel.passport.core.model.VaccinationDraft
import com.vexel.passport.core.model.DeviceDraft
import com.vexel.passport.core.model.FamilyHistoryDraft
import com.vexel.passport.core.ui.ConditionDialog
import com.vexel.passport.core.ui.AllergyDialog
import com.vexel.passport.core.ui.ProcedureDialog
import com.vexel.passport.core.ui.HospitalisationDialog
import com.vexel.passport.core.ui.VaccinationDialog
import com.vexel.passport.core.ui.DeviceDialog
import com.vexel.passport.core.ui.FamilyHistoryDialog
import com.vexel.passport.core.ui.MedicationDialog
import com.vexel.passport.core.ui.MedicationChangeDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val medications by viewModel.medications.collectAsState()
    val events by viewModel.events.collectAsState()
    val conditions by viewModel.conditions.collectAsState()
    val allergies by viewModel.allergies.collectAsState()
    val measurements by viewModel.measurements.collectAsState()
    val procedures by viewModel.procedures.collectAsState()
    val hospitalisations by viewModel.hospitalisations.collectAsState()
    val vaccinations by viewModel.vaccinations.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val familyHistory by viewModel.familyHistory.collectAsState()

    HomeScreen(
        profile = profile,
        medications = medications,
        events = events,
        conditions = conditions,
        allergies = allergies,
        measurements = measurements,
        procedures = procedures,
        hospitalisations = hospitalisations,
        vaccinations = vaccinations,
        devices = devices,
        familyHistory = familyHistory,
        modifier = modifier,
        onUpdateCondition = viewModel::updateCondition,
        onDeleteCondition = viewModel::deleteCondition,
        onUpdateAllergy = viewModel::updateAllergy,
        onDeleteAllergy = viewModel::deleteAllergy,
        onUpdateMedication = viewModel::updateMedication,
        onDeleteMedication = viewModel::deleteMedication,
        onUpdateProcedure = viewModel::updateProcedure,
        onDeleteProcedure = viewModel::deleteProcedure,
        onUpdateHospitalisation = viewModel::updateHospitalisation,
        onDeleteHospitalisation = viewModel::deleteHospitalisation,
        onUpdateVaccination = viewModel::updateVaccination,
        onDeleteVaccination = viewModel::deleteVaccination,
        onUpdateDevice = viewModel::updateDevice,
        onDeleteDevice = viewModel::deleteDevice,
        onUpdateFamilyHistory = viewModel::updateFamilyHistory,
        onDeleteFamilyHistory = viewModel::deleteFamilyHistory,
        onDeleteMeasurement = viewModel::deleteMeasurement,
        onRecordMedicationChange = viewModel::recordMedicationChange
    )
}

@Composable
fun HomeScreen(
    profile: ProfileEntity?,
    medications: List<MedicationEntity>,
    events: List<HealthEventEntity>,
    conditions: List<ConditionEntity>,
    allergies: List<AllergyEntity>,
    measurements: List<MeasurementEntity>,
    procedures: List<ProcedureEntity>,
    hospitalisations: List<HospitalisationEntity>,
    vaccinations: List<VaccinationEntity>,
    devices: List<DeviceEntity>,
    familyHistory: List<FamilyHistoryEntity>,
    modifier: Modifier,
    onUpdateCondition: (ConditionEntity) -> Unit,
    onDeleteCondition: (String) -> Unit,
    onUpdateAllergy: (AllergyEntity) -> Unit,
    onDeleteAllergy: (String) -> Unit,
    onUpdateMedication: (MedicationEntity) -> Unit,
    onDeleteMedication: (String) -> Unit,
    onUpdateProcedure: (ProcedureEntity) -> Unit,
    onDeleteProcedure: (String) -> Unit,
    onUpdateHospitalisation: (HospitalisationEntity) -> Unit,
    onDeleteHospitalisation: (String) -> Unit,
    onUpdateVaccination: (VaccinationEntity) -> Unit,
    onDeleteVaccination: (String) -> Unit,
    onUpdateDevice: (DeviceEntity) -> Unit,
    onDeleteDevice: (String) -> Unit,
    onUpdateFamilyHistory: (FamilyHistoryEntity) -> Unit,
    onDeleteFamilyHistory: (String) -> Unit,
    onDeleteMeasurement: (String) -> Unit,
    onRecordMedicationChange: (medication: MedicationEntity, strength: String, dose: String, unit: String, frequency: String, status: String, notes: String) -> Unit
) {
    var editingCondition by remember { mutableStateOf<ConditionEntity?>(null) }
    var editingAllergy by remember { mutableStateOf<AllergyEntity?>(null) }
    var editingMedication by remember { mutableStateOf<MedicationEntity?>(null) }
    var editingProcedure by remember { mutableStateOf<ProcedureEntity?>(null) }
    var editingHospitalisation by remember { mutableStateOf<HospitalisationEntity?>(null) }
    var editingVaccination by remember { mutableStateOf<VaccinationEntity?>(null) }
    var editingDevice by remember { mutableStateOf<DeviceEntity?>(null) }
    var editingFamilyHistory by remember { mutableStateOf<FamilyHistoryEntity?>(null) }

    var changeMedication by remember { mutableStateOf<MedicationEntity?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Text(if (profile?.name.isNullOrBlank()) "My Health" else "My Health · ${profile?.name}", style = MaterialTheme.typography.headlineSmall)
            Text("Your private, longitudinal health profile.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // 1. Key Emergency Details
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("EMERGENCY INFO", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                    profile?.let {
                        if (it.bloodGroup.isNotBlank()) Text("Blood Group: ${it.bloodGroup}", color = MaterialTheme.colorScheme.onErrorContainer)
                        if (it.emergencyContact.isNotBlank()) Text("Emergency Contact: ${it.emergencyContact}", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                    val activeAllergies = allergies.filter { it.status == "ACTIVE" }
                    if (activeAllergies.isNotEmpty()) {
                        Text("Active Allergies: " + activeAllergies.joinToString { it.allergen }, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }

        // 2. Conditions Section
        if (conditions.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Medical Conditions")
                        conditions.forEach { condition ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(condition.name, style = MaterialTheme.typography.titleMedium)
                                        StatusPill(condition.status)
                                    }
                                    if (condition.diagnosisDate.isNotBlank()) Text("Diagnosed: ${condition.diagnosisDate}", style = MaterialTheme.typography.bodySmall)
                                    if (condition.treatingDoctor.isNotBlank()) Text("Doctor: ${condition.treatingDoctor}", style = MaterialTheme.typography.bodySmall)
                                }
                                Row {
                                    TextButton(onClick = { editingCondition = condition }) { Text("Edit") }
                                    TextButton(onClick = { onDeleteCondition(condition.id) }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Medications Section
        if (medications.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Medications")
                        medications.forEach { med ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("${med.name} ${med.strength}", style = MaterialTheme.typography.titleMedium)
                                        StatusPill(med.status)
                                    }
                                    Text("${med.dose} · ${med.frequency}", style = MaterialTheme.typography.bodyMedium)
                                    if (med.formulation.isNotBlank()) Text("Form: ${med.formulation}", style = MaterialTheme.typography.bodySmall)
                                    if (med.startDate.isNotBlank()) Text("Started: ${med.startDate}", style = MaterialTheme.typography.bodySmall)
                                }
                                Column {
                                    TextButton(onClick = { changeMedication = med }) { Text("Log Change") }
                                    Row {
                                        TextButton(onClick = { editingMedication = med }) { Text("Edit") }
                                        TextButton(onClick = { onDeleteMedication(med.id) }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Procedures & Surgeries Section
        if (procedures.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Procedures & Surgeries")
                        procedures.forEach { proc ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(proc.name, style = MaterialTheme.typography.titleMedium)
                                    Text("Date: ${proc.date} · Hospital: ${proc.hospital}", style = MaterialTheme.typography.bodyMedium)
                                    if (proc.doctor.isNotBlank()) Text("Doctor: ${proc.doctor}", style = MaterialTheme.typography.bodySmall)
                                }
                                Row {
                                    TextButton(onClick = { editingProcedure = proc }) { Text("Edit") }
                                    TextButton(onClick = { onDeleteProcedure(proc.id) }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Hospitalisations Section
        if (hospitalisations.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Hospitalisations")
                        hospitalisations.forEach { hosp ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Reason: ${hosp.reason}", style = MaterialTheme.typography.titleMedium)
                                    Text("Admission: ${hosp.admissionDate} · Discharge: ${hosp.dischargeDate}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Hospital: ${hosp.hospital}", style = MaterialTheme.typography.bodySmall)
                                }
                                Row {
                                    TextButton(onClick = { editingHospitalisation = hosp }) { Text("Edit") }
                                    TextButton(onClick = { onDeleteHospitalisation(hosp.id) }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Vaccinations Section
        if (vaccinations.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Vaccinations")
                        vaccinations.forEach { vacc ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(vacc.vaccineName, style = MaterialTheme.typography.titleMedium)
                                    Text("Date: ${vacc.date} · Dose: ${vacc.dose}", style = MaterialTheme.typography.bodyMedium)
                                    if (vacc.nextDueDate.isNotBlank()) Text("Next due: ${vacc.nextDueDate}", style = MaterialTheme.typography.bodySmall)
                                }
                                Row {
                                    TextButton(onClick = { editingVaccination = vacc }) { Text("Edit") }
                                    TextButton(onClick = { onDeleteVaccination(vacc.id) }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Devices & Implants Section
        if (devices.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Devices & Implants")
                        devices.forEach { dev ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(dev.name, style = MaterialTheme.typography.titleMedium)
                                    Text("Type: ${dev.type} · Implantation: ${dev.implantationDate}", style = MaterialTheme.typography.bodyMedium)
                                    if (dev.manufacturer.isNotBlank()) Text("Manufacturer: ${dev.manufacturer} · Model: ${dev.model}", style = MaterialTheme.typography.bodySmall)
                                }
                                Row {
                                    TextButton(onClick = { editingDevice = dev }) { Text("Edit") }
                                    TextButton(onClick = { onDeleteDevice(dev.id) }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. Family History Section
        if (familyHistory.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Family History")
                        familyHistory.forEach { history ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${history.relationship}: ${history.condition}", style = MaterialTheme.typography.titleMedium)
                                    if (history.notes.isNotBlank()) Text(history.notes, style = MaterialTheme.typography.bodyMedium)
                                }
                                Row {
                                    TextButton(onClick = { editingFamilyHistory = history }) { Text("Edit") }
                                    TextButton(onClick = { onDeleteFamilyHistory(history.id) }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 9. Recent Tracking & Measurements Section
        if (measurements.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Recent Tracking & Measurements")
                        measurements.take(10).forEach { reading ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(reading.type.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${reading.primaryValue}${reading.secondaryValue?.let { "/$it" } ?: ""} ${reading.unit} · ${reading.context}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                TextButton(onClick = { onDeleteMeasurement(reading.id) }) {
                                    Text("Delete", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { InformationCard("Privacy & Local Storage", "Offline-first. Your medical files and details never leave this device.") }
    }

    // Dialog Rendering for Editing
    editingCondition?.let { entity ->
        ConditionDialog(
            initialCondition = ConditionDraft(entity.name, entity.status, entity.diagnosisDate, entity.resolvedDate, entity.notes, entity.treatingDoctor, entity.tags),
            onDismiss = { editingCondition = null },
            onSave = { draft ->
                onUpdateCondition(entity.copy(
                    name = draft.name,
                    status = draft.status,
                    diagnosisDate = draft.diagnosisDate,
                    resolvedDate = draft.resolvedDate,
                    notes = draft.notes,
                    treatingDoctor = draft.treatingDoctor,
                    tags = draft.tags,
                    updatedAtEpochMillis = System.currentTimeMillis()
                ))
                editingCondition = null
            }
        )
    }

    editingAllergy?.let { entity ->
        AllergyDialog(
            initialAllergy = AllergyDraft(entity.allergen, entity.category, entity.reaction, entity.severity, entity.notes, entity.status, entity.allergyDate),
            onDismiss = { editingAllergy = null },
            onSave = { draft ->
                onUpdateAllergy(entity.copy(
                    allergen = draft.allergen,
                    category = draft.category,
                    reaction = draft.reaction,
                    severity = draft.severity,
                    notes = draft.notes,
                    status = draft.status,
                    allergyDate = draft.allergyDate,
                    updatedAtEpochMillis = System.currentTimeMillis()
                ))
                editingAllergy = null
            }
        )
    }

    editingMedication?.let { med ->
        MedicationDialog(
            onDismiss = { editingMedication = null },
            onSave = { draft ->
                onUpdateMedication(med.copy(
                    name = draft.name,
                    genericName = draft.genericName,
                    strength = draft.strength,
                    dose = draft.dose,
                    unit = draft.unit,
                    route = draft.route,
                    frequency = draft.frequency,
                    startDate = draft.startDate,
                    stopDate = draft.stopDate,
                    status = draft.status,
                    indication = draft.indication,
                    physician = draft.physician,
                    notes = draft.notes,
                    formulation = draft.formulation,
                    prescriptionId = draft.prescriptionId,
                    updatedAtEpochMillis = System.currentTimeMillis()
                ))
                editingMedication = null
            }
        )
    }

    editingProcedure?.let { entity ->
        ProcedureDialog(
            initialProcedure = ProcedureDraft(entity.name, entity.date, entity.hospital, entity.doctor, entity.indication, entity.notes, entity.linkedDocumentId),
            onDismiss = { editingProcedure = null },
            onSave = { draft ->
                onUpdateProcedure(entity.copy(
                    name = draft.name,
                    date = draft.date,
                    hospital = draft.hospital,
                    doctor = draft.doctor,
                    indication = draft.indication,
                    notes = draft.notes,
                    linkedDocumentId = draft.linkedDocumentId,
                    updatedAtEpochMillis = System.currentTimeMillis()
                ))
                editingProcedure = null
            }
        )
    }

    editingHospitalisation?.let { entity ->
        HospitalisationDialog(
            initialHospitalisation = HospitalisationDraft(entity.admissionDate, entity.dischargeDate, entity.hospital, entity.reason, entity.diagnosis, entity.notes, entity.linkedDocumentId),
            onDismiss = { editingHospitalisation = null },
            onSave = { draft ->
                onUpdateHospitalisation(entity.copy(
                    admissionDate = draft.admissionDate,
                    dischargeDate = draft.dischargeDate,
                    hospital = draft.hospital,
                    reason = draft.reason,
                    diagnosis = draft.diagnosis,
                    notes = draft.notes,
                    linkedDocumentId = draft.linkedDocumentId,
                    updatedAtEpochMillis = System.currentTimeMillis()
                ))
                editingHospitalisation = null
            }
        )
    }

    editingVaccination?.let { entity ->
        VaccinationDialog(
            initialVaccination = VaccinationDraft(entity.vaccineName, entity.dose, entity.date, entity.provider, entity.lotNumber, entity.nextDueDate, entity.linkedDocumentId, entity.notes),
            onDismiss = { editingVaccination = null },
            onSave = { draft ->
                onUpdateVaccination(entity.copy(
                    vaccineName = draft.vaccineName,
                    dose = draft.dose,
                    date = draft.date,
                    provider = draft.provider,
                    lotNumber = draft.lotNumber,
                    nextDueDate = draft.nextDueDate,
                    linkedDocumentId = draft.linkedDocumentId,
                    notes = draft.notes,
                    updatedAtEpochMillis = System.currentTimeMillis()
                ))
                editingVaccination = null
            }
        )
    }

    editingDevice?.let { entity ->
        DeviceDialog(
            initialDevice = DeviceDraft(entity.type, entity.name, entity.manufacturer, entity.model, entity.serialNumber, entity.implantationDate, entity.hospital, entity.notes),
            onDismiss = { editingDevice = null },
            onSave = { draft ->
                onUpdateDevice(entity.copy(
                    type = draft.type,
                    name = draft.name,
                    manufacturer = draft.manufacturer,
                    model = draft.model,
                    serialNumber = draft.serialNumber,
                    implantationDate = draft.implantationDate,
                    hospital = draft.hospital,
                    notes = draft.notes,
                    updatedAtEpochMillis = System.currentTimeMillis()
                ))
                editingDevice = null
            }
        )
    }

    editingFamilyHistory?.let { entity ->
        FamilyHistoryDialog(
            initialHistory = FamilyHistoryDraft(entity.relationship, entity.condition, entity.notes),
            onDismiss = { editingFamilyHistory = null },
            onSave = { draft ->
                onUpdateFamilyHistory(entity.copy(
                    relationship = draft.relationship,
                    condition = draft.condition,
                    notes = draft.notes,
                    updatedAtEpochMillis = System.currentTimeMillis()
                ))
                editingFamilyHistory = null
            }
        )
    }

    changeMedication?.let { medication ->
        MedicationChangeDialog(
            medicationId = medication.id,
            initialStrength = medication.strength,
            initialDose = medication.dose,
            initialUnit = medication.unit,
            initialFrequency = medication.frequency,
            initialStatus = medication.status,
            onDismiss = { changeMedication = null },
            onSave = { strength, dose, unit, frequency, status, notes ->
                onRecordMedicationChange(medication, strength, dose, unit, frequency, status, notes)
                changeMedication = null
            }
        )
    }
}
