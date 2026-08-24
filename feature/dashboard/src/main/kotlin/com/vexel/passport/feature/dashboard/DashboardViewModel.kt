package com.vexel.passport.feature.dashboard

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vexel.passport.core.database.HealthDatabase
import com.vexel.passport.core.database.HealthEventEntity
import com.vexel.passport.core.database.MedicationEntity
import com.vexel.passport.core.database.MedicationChangeEntity
import com.vexel.passport.core.database.DocumentEntity
import com.vexel.passport.core.database.ProfileEntity
import com.vexel.passport.core.database.ConditionEntity
import com.vexel.passport.core.database.AllergyEntity
import com.vexel.passport.core.database.MeasurementEntity
import com.vexel.passport.core.database.ProcedureEntity
import com.vexel.passport.core.database.HospitalisationEntity
import com.vexel.passport.core.database.VaccinationEntity
import com.vexel.passport.core.database.DeviceEntity
import com.vexel.passport.core.database.FamilyHistoryEntity
import com.vexel.passport.core.files.SecureFileStore
import com.vexel.passport.core.model.SymptomDraft
import com.vexel.passport.core.model.MedicationDraft
import com.vexel.passport.core.model.parseSymptomDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val database: HealthDatabase,
    private val secureFileStore: SecureFileStore,
) : ViewModel() {

    val profile = database.profileDao().observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val medications = database.medicationDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val events = database.healthEventDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val conditions = database.conditionDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val allergies = database.allergyDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val measurements = database.measurementDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val procedures = database.procedureDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val hospitalisations = database.hospitalisationDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val vaccinations = database.vaccinationDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val devices = database.deviceDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val familyHistory = database.familyHistoryDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addCondition(name: String) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        database.conditionDao().insert(ConditionEntity(UUID.randomUUID().toString(), name.trim(), createdAtEpochMillis = now, updatedAtEpochMillis = now))
        database.healthEventDao().insert(HealthEventEntity(UUID.randomUUID().toString(), name.trim(), kind = "CONDITION", effectiveAtEpochMillis = now, createdAtEpochMillis = now))
    }

    fun addAllergy(allergen: String, reaction: String) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        database.allergyDao().insert(AllergyEntity(UUID.randomUUID().toString(), allergen.trim(), reaction = reaction.trim(), createdAtEpochMillis = now, updatedAtEpochMillis = now))
        database.healthEventDao().insert(HealthEventEntity(UUID.randomUUID().toString(), allergen.trim(), reaction.trim(), "ALLERGY", now, now))
    }

    fun addMeasurement(type: String, value: Double, secondary: Double?, unit: String, context: String = "") = viewModelScope.launch {
        val now = System.currentTimeMillis()
        database.measurementDao().insert(MeasurementEntity(UUID.randomUUID().toString(), type, value, secondary, unit, context, now))
        val display = if (secondary == null) "$value $unit" else "$value/$secondary $unit"
        database.healthEventDao().insert(HealthEventEntity(UUID.randomUUID().toString(), type.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }, display, "MEASUREMENT", now, now))
    }

    fun addEvent(kind: String, title: String, details: String, severity: Int? = null) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        database.healthEventDao().insert(
            HealthEventEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                details = details,
                kind = kind,
                effectiveAtEpochMillis = now,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
                status = "ACTIVE",
                severity = severity
            )
        )
    }

    fun addSymptom(draft: SymptomDraft, imageUri: Uri? = null) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        fun epochMillisOf(text: String): Long? = parseSymptomDateTime(text)
            ?.atZone(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli()

        val start = epochMillisOf(draft.startAtText) ?: now
        val end = epochMillisOf(draft.endAtText)
        val imageId = imageUri?.let { uri ->
            val mime = appContext.contentResolver.getType(uri) ?: return@let null
            if (mime !in setOf("image/jpeg", "image/png")) null else appContext.contentResolver.openInputStream(uri)?.use { input ->
                val preserved = secureFileStore.preserveOriginal(input, mime, "symptom-image")
                database.documentDao().insert(
                    DocumentEntity(
                        id = preserved.id,
                        title = "Symptom image",
                        category = "SYMPTOM_IMAGE",
                        documentDate = "",
                        notes = "Attached to symptom",
                        originalFileName = "symptom-image",
                        mimeType = preserved.mimeType,
                        byteCount = preserved.byteCount,
                        sha256 = preserved.sha256,
                        createdAtEpochMillis = now
                    )
                )
                preserved.id
            }
        }
        database.healthEventDao().insert(
            HealthEventEntity(
                id = UUID.randomUUID().toString(),
                title = draft.name.trim(),
                details = draft.notes.trim(),
                kind = "SYMPTOM",
                effectiveAtEpochMillis = start,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
                status = "ACTIVE",
                severity = draft.severity,
                archived = false,
                durationMinutes = draft.durationMinutes,
                startAtEpochMillis = start,
                endAtEpochMillis = end,
                ongoing = draft.ongoing,
                bodyLocation = draft.bodyLocation.trim(),
                associatedSymptoms = draft.associatedSymptoms.trim(),
                possibleTrigger = draft.possibleTrigger.trim(),
                relatedMedication = draft.relatedMedication.trim(),
                imageAttachmentId = imageId,
                episodeId = draft.episodeId.trim().ifBlank { null }
            )
        )
    }

    fun addMedication(draft: MedicationDraft) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val medicationId = UUID.randomUUID().toString()
        database.medicationDao().insert(
            MedicationEntity(
                id = medicationId,
                name = draft.name.trim(),
                genericName = draft.genericName.trim(),
                strength = draft.strength.trim(),
                dose = draft.dose.trim(),
                unit = draft.unit.trim(),
                route = draft.route.trim(),
                frequency = draft.frequency.trim(),
                startDate = draft.startDate.trim(),
                stopDate = draft.stopDate.trim(),
                status = draft.status,
                indication = draft.indication.trim(),
                physician = draft.physician.trim(),
                notes = draft.notes.trim(),
                formulation = draft.formulation.trim(),
                prescriptionId = draft.prescriptionId?.trim()?.ifBlank { null },
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )
        database.medicationChangeDao().insert(
            MedicationChangeEntity(
                id = UUID.randomUUID().toString(),
                medicationId = medicationId,
                changedAtEpochMillis = now,
                changeType = "STARTED",
                strength = draft.strength.trim(),
                dose = draft.dose.trim(),
                unit = draft.unit.trim(),
                frequency = draft.frequency.trim(),
                status = draft.status,
                notes = draft.notes.trim()
            )
        )
        database.healthEventDao().insert(
            HealthEventEntity(
                id = UUID.randomUUID().toString(),
                title = draft.name.trim(),
                details = listOf(draft.strength, draft.dose, draft.unit, draft.frequency, draft.status.lowercase()).filter { it.isNotBlank() }.joinToString(" · "),
                kind = "MEDICATION",
                effectiveAtEpochMillis = now,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
                status = "ACTIVE"
            )
        )
    }

    fun recordMedicationChange(medication: MedicationEntity, strength: String, dose: String, unit: String, frequency: String, status: String, notes: String) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        database.medicationDao().update(
            medication.copy(
                strength = strength.trim(),
                dose = dose.trim(),
                unit = unit.trim(),
                frequency = frequency.trim(),
                status = status,
                notes = notes.trim(),
                updatedAtEpochMillis = now,
                stopDate = if (status == "STOPPED") SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now)) else medication.stopDate
            )
        )
        database.medicationChangeDao().insert(
            MedicationChangeEntity(
                id = UUID.randomUUID().toString(),
                medicationId = medication.id,
                changedAtEpochMillis = now,
                changeType = if (status == "STOPPED") "STOPPED" else if (medication.status == "STOPPED" && status == "CURRENT") "RESTARTED" else "DOSE_CHANGED",
                strength = strength.trim(),
                dose = dose.trim(),
                unit = unit.trim(),
                frequency = frequency.trim(),
                status = status,
                notes = notes.trim()
            )
        )
    }

    fun updateCondition(entity: ConditionEntity) = viewModelScope.launch { database.conditionDao().update(entity) }
    fun deleteCondition(id: String) = viewModelScope.launch { database.conditionDao().delete(id) }

    fun updateAllergy(entity: AllergyEntity) = viewModelScope.launch { database.allergyDao().update(entity) }
    fun deleteAllergy(id: String) = viewModelScope.launch { database.allergyDao().delete(id) }

    fun updateMedication(entity: MedicationEntity) = viewModelScope.launch { database.medicationDao().update(entity) }
    fun deleteMedication(id: String) = viewModelScope.launch { database.medicationDao().delete(id) }

    fun updateProcedure(entity: ProcedureEntity) = viewModelScope.launch { database.procedureDao().update(entity) }
    fun deleteProcedure(id: String) = viewModelScope.launch { database.procedureDao().delete(id) }

    fun updateHospitalisation(entity: HospitalisationEntity) = viewModelScope.launch { database.hospitalisationDao().update(entity) }
    fun deleteHospitalisation(id: String) = viewModelScope.launch { database.hospitalisationDao().delete(id) }

    fun updateVaccination(entity: VaccinationEntity) = viewModelScope.launch { database.vaccinationDao().update(entity) }
    fun deleteVaccination(id: String) = viewModelScope.launch { database.vaccinationDao().delete(id) }

    fun updateDevice(entity: DeviceEntity) = viewModelScope.launch { database.deviceDao().update(entity) }
    fun deleteDevice(id: String) = viewModelScope.launch { database.deviceDao().delete(id) }

    fun updateFamilyHistory(entity: FamilyHistoryEntity) = viewModelScope.launch { database.familyHistoryDao().update(entity) }
    fun deleteFamilyHistory(id: String) = viewModelScope.launch { database.familyHistoryDao().delete(id) }

    fun deleteMeasurement(id: String) = viewModelScope.launch { database.measurementDao().delete(id) }
}
