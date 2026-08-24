package com.vexel.passport.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.vexel.passport.core.model.ConditionDraft
import com.vexel.passport.core.model.AllergyDraft
import com.vexel.passport.core.model.ProcedureDraft
import com.vexel.passport.core.model.HospitalisationDraft
import com.vexel.passport.core.model.VaccinationDraft
import com.vexel.passport.core.model.DeviceDraft
import com.vexel.passport.core.model.FamilyHistoryDraft
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter
    .ofPattern("uuuu-MM-dd")
    .withResolverStyle(ResolverStyle.STRICT)

private fun isValidDateOrBlank(text: String): Boolean {
    if (text.isBlank()) return true
    return try {
        LocalDate.parse(text.trim(), DATE_FORMATTER)
        true
    } catch (_: Exception) {
        false
    }
}

@Composable
fun ConditionDialog(
    initialCondition: ConditionDraft? = null,
    onDismiss: () -> Unit,
    onSave: (ConditionDraft) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialCondition?.name.orEmpty()) }
    var status by rememberSaveable { mutableStateOf(initialCondition?.status ?: "ACTIVE") }
    var diagnosisDate by rememberSaveable { mutableStateOf(initialCondition?.diagnosisDate.orEmpty()) }
    var resolvedDate by rememberSaveable { mutableStateOf(initialCondition?.resolvedDate.orEmpty()) }
    var treatingDoctor by rememberSaveable { mutableStateOf(initialCondition?.treatingDoctor.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(initialCondition?.notes.orEmpty()) }
    var tags by rememberSaveable { mutableStateOf(initialCondition?.tags.orEmpty()) }

    val nameValid = name.isNotBlank() && name.length <= 160
    val datesValid = isValidDateOrBlank(diagnosisDate) && isValidDateOrBlank(resolvedDate)
    val isFormValid = nameValid && datesValid

    FullScreenDialog(
        title = if (initialCondition == null) "Add Condition" else "Edit Condition",
        onDismiss = onDismiss,
        confirmButton = {
            TextButton(enabled = isFormValid, onClick = {
                val draft = ConditionDraft(
                    name = name.trim(),
                    status = status,
                    diagnosisDate = diagnosisDate.trim(),
                    resolvedDate = resolvedDate.trim(),
                    notes = notes.trim(),
                    treatingDoctor = treatingDoctor.trim(),
                    tags = tags.trim()
                )
                onSave(draft)
                onDismiss()
            }) {
                Text("Save")
            }
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Condition Name (Required)") },
            isError = name.isNotBlank() && name.length > 160,
            supportingText = { if (name.length > 160) Text("Must be 160 characters or fewer") }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = status == "ACTIVE", onClick = { status = "ACTIVE" }, label = { Text("Active") })
            FilterChip(selected = status == "RESOLVED", onClick = { status = "RESOLVED" }, label = { Text("Resolved") })
            FilterChip(selected = status == "HISTORICAL", onClick = { status = "HISTORICAL" }, label = { Text("Historical") })
        }
        OutlinedTextField(
            value = diagnosisDate,
            onValueChange = { diagnosisDate = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Diagnosis Date (yyyy-MM-dd)") },
            placeholder = { Text("yyyy-MM-dd") },
            isError = !isValidDateOrBlank(diagnosisDate),
            supportingText = { if (!isValidDateOrBlank(diagnosisDate)) Text("Use yyyy-MM-dd format") }
        )
        OutlinedTextField(
            value = resolvedDate,
            onValueChange = { resolvedDate = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Resolved Date (yyyy-MM-dd)") },
            placeholder = { Text("yyyy-MM-dd") },
            isError = !isValidDateOrBlank(resolvedDate),
            supportingText = { if (!isValidDateOrBlank(resolvedDate)) Text("Use yyyy-MM-dd format") }
        )
        OutlinedTextField(
            value = treatingDoctor,
            onValueChange = { treatingDoctor = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Treating Doctor (optional)") }
        )
        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Tags (comma separated) (optional)") }
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") }
        )
    }
}

@Composable
fun AllergyDialog(
    initialAllergy: AllergyDraft? = null,
    onDismiss: () -> Unit,
    onSave: (AllergyDraft) -> Unit
) {
    var allergen by rememberSaveable { mutableStateOf(initialAllergy?.allergen.orEmpty()) }
    var category by rememberSaveable { mutableStateOf(initialAllergy?.category ?: "OTHER") }
    var reaction by rememberSaveable { mutableStateOf(initialAllergy?.reaction.orEmpty()) }
    var severity by rememberSaveable { mutableStateOf(initialAllergy?.severity ?: "MILD") }
    var allergyDate by rememberSaveable { mutableStateOf(initialAllergy?.allergyDate.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(initialAllergy?.notes.orEmpty()) }
    var status by rememberSaveable { mutableStateOf(initialAllergy?.status ?: "ACTIVE") }

    val allergenValid = allergen.isNotBlank() && allergen.length <= 160
    val dateValid = isValidDateOrBlank(allergyDate)
    val isFormValid = allergenValid && dateValid

    FullScreenDialog(
        title = if (initialAllergy == null) "Add Allergy" else "Edit Allergy",
        onDismiss = onDismiss,
        confirmButton = {
            TextButton(enabled = isFormValid, onClick = {
                val draft = AllergyDraft(
                    allergen = allergen.trim(),
                    category = category,
                    reaction = reaction.trim(),
                    severity = severity,
                    notes = notes.trim(),
                    status = status,
                    allergyDate = allergyDate.trim()
                )
                onSave(draft)
                onDismiss()
            }) {
                Text("Save")
            }
        }
    ) {
        OutlinedTextField(
            value = allergen,
            onValueChange = { allergen = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Allergen (Required)") },
            isError = allergen.isNotBlank() && allergen.length > 160,
            supportingText = { if (allergen.length > 160) Text("Must be 160 characters or fewer") }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = status == "ACTIVE", onClick = { status = "ACTIVE" }, label = { Text("Active") })
            FilterChip(selected = status == "RESOLVED", onClick = { status = "RESOLVED" }, label = { Text("Resolved") })
        }
        Text("Category", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("FOOD", "DRUG", "ENVIRONMENTAL", "OTHER").forEach { cat ->
                FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat.lowercase().replaceFirstChar { it.uppercase() }) })
            }
        }
        Text("Severity", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("MILD", "MODERATE", "SEVERE").forEach { sev ->
                FilterChip(selected = severity == sev, onClick = { severity = sev }, label = { Text(sev.lowercase().replaceFirstChar { it.uppercase() }) })
            }
        }
        OutlinedTextField(
            value = reaction,
            onValueChange = { reaction = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Reaction (e.g. rash, breathing difficulty)") }
        )
        OutlinedTextField(
            value = allergyDate,
            onValueChange = { allergyDate = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date of onset / discovery (yyyy-MM-dd) (optional)") },
            placeholder = { Text("yyyy-MM-dd") },
            isError = !dateValid,
            supportingText = { if (!dateValid) Text("Use yyyy-MM-dd format") }
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") }
        )
    }
}

@Composable
fun ProcedureDialog(
    initialProcedure: ProcedureDraft? = null,
    onDismiss: () -> Unit,
    onSave: (ProcedureDraft) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialProcedure?.name.orEmpty()) }
    var date by rememberSaveable { mutableStateOf(initialProcedure?.date.orEmpty()) }
    var hospital by rememberSaveable { mutableStateOf(initialProcedure?.hospital.orEmpty()) }
    var doctor by rememberSaveable { mutableStateOf(initialProcedure?.doctor.orEmpty()) }
    var indication by rememberSaveable { mutableStateOf(initialProcedure?.indication.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(initialProcedure?.notes.orEmpty()) }
    var linkedDocumentId by rememberSaveable { mutableStateOf(initialProcedure?.linkedDocumentId) }

    val nameValid = name.isNotBlank() && name.length <= 160
    val dateValid = isValidDateOrBlank(date)
    val isFormValid = nameValid && dateValid

    FullScreenDialog(
        title = if (initialProcedure == null) "Add Procedure/Surgery" else "Edit Procedure/Surgery",
        onDismiss = onDismiss,
        confirmButton = {
            TextButton(enabled = isFormValid, onClick = {
                val draft = ProcedureDraft(
                    name = name.trim(),
                    date = date.trim(),
                    hospital = hospital.trim(),
                    doctor = doctor.trim(),
                    indication = indication.trim(),
                    notes = notes.trim(),
                    linkedDocumentId = linkedDocumentId
                )
                onSave(draft)
                onDismiss()
            }) {
                Text("Save")
            }
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Procedure/Surgery Name (Required)") }
        )
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date of Procedure (yyyy-MM-dd)") },
            placeholder = { Text("yyyy-MM-dd") },
            isError = !dateValid,
            supportingText = { if (!dateValid) Text("Use yyyy-MM-dd format") }
        )
        OutlinedTextField(
            value = hospital,
            onValueChange = { hospital = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Hospital / Clinic (optional)") }
        )
        OutlinedTextField(
            value = doctor,
            onValueChange = { doctor = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Doctor / Surgeon (optional)") }
        )
        OutlinedTextField(
            value = indication,
            onValueChange = { indication = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Indication (optional)") }
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes / Outcome (optional)") }
        )
    }
}

@Composable
fun HospitalisationDialog(
    initialHospitalisation: HospitalisationDraft? = null,
    onDismiss: () -> Unit,
    onSave: (HospitalisationDraft) -> Unit
) {
    var admissionDate by rememberSaveable { mutableStateOf(initialHospitalisation?.admissionDate.orEmpty()) }
    var dischargeDate by rememberSaveable { mutableStateOf(initialHospitalisation?.dischargeDate.orEmpty()) }
    var hospital by rememberSaveable { mutableStateOf(initialHospitalisation?.hospital.orEmpty()) }
    var reason by rememberSaveable { mutableStateOf(initialHospitalisation?.reason.orEmpty()) }
    var diagnosis by rememberSaveable { mutableStateOf(initialHospitalisation?.diagnosis.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(initialHospitalisation?.notes.orEmpty()) }
    var linkedDocumentId by rememberSaveable { mutableStateOf(initialHospitalisation?.linkedDocumentId) }

    val reasonValid = reason.isNotBlank() && reason.length <= 200
    val datesValid = isValidDateOrBlank(admissionDate) && isValidDateOrBlank(dischargeDate)
    val isFormValid = reasonValid && datesValid

    FullScreenDialog(
        title = if (initialHospitalisation == null) "Add Hospitalisation" else "Edit Hospitalisation",
        onDismiss = onDismiss,
        confirmButton = {
            TextButton(enabled = isFormValid, onClick = {
                val draft = HospitalisationDraft(
                    admissionDate = admissionDate.trim(),
                    dischargeDate = dischargeDate.trim(),
                    hospital = hospital.trim(),
                    reason = reason.trim(),
                    diagnosis = diagnosis.trim(),
                    notes = notes.trim(),
                    linkedDocumentId = linkedDocumentId
                )
                onSave(draft)
                onDismiss()
            }) {
                Text("Save")
            }
        }
    ) {
        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Reason for Admission (Required)") }
        )
        OutlinedTextField(
            value = admissionDate,
            onValueChange = { admissionDate = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Admission Date (yyyy-MM-dd)") },
            placeholder = { Text("yyyy-MM-dd") },
            isError = !isValidDateOrBlank(admissionDate),
            supportingText = { if (!isValidDateOrBlank(admissionDate)) Text("Use yyyy-MM-dd format") }
        )
        OutlinedTextField(
            value = dischargeDate,
            onValueChange = { dischargeDate = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Discharge Date (yyyy-MM-dd)") },
            placeholder = { Text("yyyy-MM-dd") },
            isError = !isValidDateOrBlank(dischargeDate),
            supportingText = { if (!isValidDateOrBlank(dischargeDate)) Text("Use yyyy-MM-dd format") }
        )
        OutlinedTextField(
            value = hospital,
            onValueChange = { hospital = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Hospital Name (optional)") }
        )
        OutlinedTextField(
            value = diagnosis,
            onValueChange = { diagnosis = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Discharge Diagnosis (optional)") }
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") }
        )
    }
}

@Composable
fun VaccinationDialog(
    initialVaccination: VaccinationDraft? = null,
    onDismiss: () -> Unit,
    onSave: (VaccinationDraft) -> Unit
) {
    var vaccineName by rememberSaveable { mutableStateOf(initialVaccination?.vaccineName.orEmpty()) }
    var dose by rememberSaveable { mutableStateOf(initialVaccination?.dose.orEmpty()) }
    var date by rememberSaveable { mutableStateOf(initialVaccination?.date.orEmpty()) }
    var provider by rememberSaveable { mutableStateOf(initialVaccination?.provider.orEmpty()) }
    var lotNumber by rememberSaveable { mutableStateOf(initialVaccination?.lotNumber.orEmpty()) }
    var nextDueDate by rememberSaveable { mutableStateOf(initialVaccination?.nextDueDate.orEmpty()) }
    var linkedDocumentId by rememberSaveable { mutableStateOf(initialVaccination?.linkedDocumentId) }
    var notes by rememberSaveable { mutableStateOf(initialVaccination?.notes.orEmpty()) }

    val nameValid = vaccineName.isNotBlank() && vaccineName.length <= 160
    val datesValid = isValidDateOrBlank(date) && isValidDateOrBlank(nextDueDate)
    val isFormValid = nameValid && datesValid

    FullScreenDialog(
        title = if (initialVaccination == null) "Add Vaccination" else "Edit Vaccination",
        onDismiss = onDismiss,
        confirmButton = {
            TextButton(enabled = isFormValid, onClick = {
                val draft = VaccinationDraft(
                    vaccineName = vaccineName.trim(),
                    dose = dose.trim(),
                    date = date.trim(),
                    provider = provider.trim(),
                    lotNumber = lotNumber.trim(),
                    nextDueDate = nextDueDate.trim(),
                    linkedDocumentId = linkedDocumentId,
                    notes = notes.trim()
                )
                onSave(draft)
                onDismiss()
            }) {
                Text("Save")
            }
        }
    ) {
        OutlinedTextField(
            value = vaccineName,
            onValueChange = { vaccineName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Vaccine Name (Required)") }
        )
        OutlinedTextField(
            value = dose,
            onValueChange = { dose = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Dose / Number (e.g. 1st Dose, Booster) (optional)") }
        )
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date Administered (yyyy-MM-dd)") },
            placeholder = { Text("yyyy-MM-dd") },
            isError = !isValidDateOrBlank(date),
            supportingText = { if (!isValidDateOrBlank(date)) Text("Use yyyy-MM-dd format") }
        )
        OutlinedTextField(
            value = provider,
            onValueChange = { provider = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Provider / Clinic (optional)") }
        )
        OutlinedTextField(
            value = lotNumber,
            onValueChange = { lotNumber = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Batch / Lot Number (optional)") }
        )
        OutlinedTextField(
            value = nextDueDate,
            onValueChange = { nextDueDate = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Next Due Date (yyyy-MM-dd) (optional)") },
            placeholder = { Text("yyyy-MM-dd") },
            isError = !isValidDateOrBlank(nextDueDate),
            supportingText = { if (!isValidDateOrBlank(nextDueDate)) Text("Use yyyy-MM-dd format") }
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") }
        )
    }
}

@Composable
fun DeviceDialog(
    initialDevice: DeviceDraft? = null,
    onDismiss: () -> Unit,
    onSave: (DeviceDraft) -> Unit
) {
    var type by rememberSaveable { mutableStateOf(initialDevice?.type ?: "OTHER") }
    var name by rememberSaveable { mutableStateOf(initialDevice?.name.orEmpty()) }
    var manufacturer by rememberSaveable { mutableStateOf(initialDevice?.manufacturer.orEmpty()) }
    var model by rememberSaveable { mutableStateOf(initialDevice?.model.orEmpty()) }
    var serialNumber by rememberSaveable { mutableStateOf(initialDevice?.serialNumber.orEmpty()) }
    var implantationDate by rememberSaveable { mutableStateOf(initialDevice?.implantationDate.orEmpty()) }
    var hospital by rememberSaveable { mutableStateOf(initialDevice?.hospital.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(initialDevice?.notes.orEmpty()) }

    val nameValid = name.isNotBlank() && name.length <= 160
    val dateValid = isValidDateOrBlank(implantationDate)
    val isFormValid = nameValid && dateValid

    FullScreenDialog(
        title = if (initialDevice == null) "Add Implant/Device" else "Edit Implant/Device",
        onDismiss = onDismiss,
        confirmButton = {
            TextButton(enabled = isFormValid, onClick = {
                val draft = DeviceDraft(
                    type = type,
                    name = name.trim(),
                    manufacturer = manufacturer.trim(),
                    model = model.trim(),
                    serialNumber = serialNumber.trim(),
                    implantationDate = implantationDate.trim(),
                    hospital = hospital.trim(),
                    notes = notes.trim()
                )
                onSave(draft)
                onDismiss()
            }) {
                Text("Save")
            }
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Device Name (e.g. Pacemaker, Insulin Pump) (Required)") }
        )
        Text("Device Type", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("PACEMAKER", "STENT", "PROSTHESIS", "PUMP", "OTHER").forEach { deviceType ->
                FilterChip(selected = type == deviceType, onClick = { type = deviceType }, label = { Text(deviceType.lowercase().replaceFirstChar { it.uppercase() }) })
            }
        }
        OutlinedTextField(
            value = implantationDate,
            onValueChange = { implantationDate = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Implantation Date (yyyy-MM-dd)") },
            placeholder = { Text("yyyy-MM-dd") },
            isError = !dateValid,
            supportingText = { if (!dateValid) Text("Use yyyy-MM-dd format") }
        )
        OutlinedTextField(
            value = manufacturer,
            onValueChange = { manufacturer = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Manufacturer (optional)") }
        )
        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Model (optional)") }
        )
        OutlinedTextField(
            value = serialNumber,
            onValueChange = { serialNumber = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Serial Number (optional)") }
        )
        OutlinedTextField(
            value = hospital,
            onValueChange = { hospital = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Hospital / Clinic (optional)") }
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") }
        )
    }
}

@Composable
fun FamilyHistoryDialog(
    initialHistory: FamilyHistoryDraft? = null,
    onDismiss: () -> Unit,
    onSave: (FamilyHistoryDraft) -> Unit
) {
    var relationship by rememberSaveable { mutableStateOf(initialHistory?.relationship.orEmpty()) }
    var condition by rememberSaveable { mutableStateOf(initialHistory?.condition.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(initialHistory?.notes.orEmpty()) }

    val isValid = relationship.isNotBlank() && condition.isNotBlank()

    FullScreenDialog(
        title = if (initialHistory == null) "Add Family History" else "Edit Family History",
        onDismiss = onDismiss,
        confirmButton = {
            TextButton(enabled = isValid, onClick = {
                val draft = FamilyHistoryDraft(
                    relationship = relationship.trim(),
                    condition = condition.trim(),
                    notes = notes.trim()
                )
                onSave(draft)
                onDismiss()
            }) {
                Text("Save")
            }
        }
    ) {
        OutlinedTextField(
            value = relationship,
            onValueChange = { relationship = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Relationship (e.g. Father, Mother) (Required)") }
        )
        OutlinedTextField(
            value = condition,
            onValueChange = { condition = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Medical Condition (Required)") }
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") }
        )
    }
}

@Composable
fun MeasurementDialog(
    type: String, // e.g. "BLOOD_PRESSURE", "BLOOD_GLUCOSE", "TEMPERATURE", "WEIGHT", "PULSE", "SPO2", "RESPIRATORY_RATE"
    onDismiss: () -> Unit,
    onSave: (String, Double, Double?, String, String) -> Unit // type, value1, value2, unit, context
) {
    var primaryValueText by rememberSaveable { mutableStateOf("") }
    var secondaryValueText by rememberSaveable { mutableStateOf("") }
    var unit by rememberSaveable { mutableStateOf(
        when (type) {
            "BLOOD_PRESSURE" -> "mmHg"
            "BLOOD_GLUCOSE" -> "mg/dL"
            "TEMPERATURE" -> "°C"
            "WEIGHT" -> "kg"
            "PULSE" -> "bpm"
            "SPO2" -> "%"
            "RESPIRATORY_RATE" -> "breaths/min"
            else -> ""
        }
    ) }
    var context by rememberSaveable { mutableStateOf("") }

    val primaryVal = primaryValueText.toDoubleOrNull()
    val secondaryVal = secondaryValueText.toDoubleOrNull()

    val isBloodPressure = type == "BLOOD_PRESSURE"
    val isFormValid = when {
        isBloodPressure -> primaryVal != null && secondaryVal != null && primaryVal > 0 && secondaryVal > 0
        else -> primaryVal != null && primaryVal > 0
    }

    FullScreenDialog(
        title = "Record " + type.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
        onDismiss = onDismiss,
        confirmButton = {
            TextButton(enabled = isFormValid, onClick = {
                onSave(type, primaryVal!!, secondaryVal, unit, context)
                onDismiss()
            }) {
                Text("Save")
            }
        }
    ) {
        if (isBloodPressure) {
            OutlinedTextField(
                value = primaryValueText,
                onValueChange = { primaryValueText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Systolic Value (mmHg)") }
            )
            OutlinedTextField(
                value = secondaryValueText,
                onValueChange = { secondaryValueText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Diastolic Value (mmHg)") }
            )
        } else {
            OutlinedTextField(
                value = primaryValueText,
                onValueChange = { primaryValueText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Value") }
            )
        }

        if (type == "BLOOD_GLUCOSE") {
            Text("Glucose Unit", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = unit == "mg/dL", onClick = { unit = "mg/dL" }, label = { Text("mg/dL") })
                FilterChip(selected = unit == "mmol/L", onClick = { unit = "mmol/L" }, label = { Text("mmol/L") })
            }
            Text("Context", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("FASTING", "BEFORE_MEAL", "AFTER_MEAL", "RANDOM").forEach { ctx ->
                    FilterChip(selected = context == ctx, onClick = { context = ctx }, label = { Text(ctx.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }) })
                }
            }
        }

        if (type == "TEMPERATURE") {
            Text("Temperature Unit", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = unit == "°C", onClick = { unit = "°C" }, label = { Text("°C") })
                FilterChip(selected = unit == "°F", onClick = { unit = "°F" }, label = { Text("°F") })
            }
        }

        if (type == "WEIGHT") {
            Text("Weight Unit", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = unit == "kg", onClick = { unit = "kg" }, label = { Text("kg") })
                FilterChip(selected = unit == "lb", onClick = { unit = "lb" }, label = { Text("lb") })
            }
        }
    }
}
