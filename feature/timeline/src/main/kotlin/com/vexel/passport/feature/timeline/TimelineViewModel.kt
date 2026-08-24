package com.vexel.passport.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vexel.passport.core.database.HealthDatabase
import com.vexel.passport.core.database.HealthEventEntity
import com.vexel.passport.core.database.ConditionEntity
import com.vexel.passport.core.database.AllergyEntity
import com.vexel.passport.core.database.MedicationEntity
import com.vexel.passport.core.database.ProcedureEntity
import com.vexel.passport.core.database.HospitalisationEntity
import com.vexel.passport.core.database.VaccinationEntity
import com.vexel.passport.core.database.DeviceEntity
import com.vexel.passport.core.database.FamilyHistoryEntity
import com.vexel.passport.core.database.DocumentEntity
import com.vexel.passport.core.files.SecureFileStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val database: HealthDatabase,
    private val secureFileStore: SecureFileStore,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _selectedKind = MutableStateFlow<String?>(null)
    val selectedKind: StateFlow<String?> = _selectedKind

    private val _limit = MutableStateFlow(50)
    val limit: StateFlow<Int> = _limit

    private val _archivedEvents = MutableSharedFlow<HealthEventEntity>(extraBufferCapacity = 1)
    val archivedEvents = _archivedEvents.asSharedFlow()

    val allEvents = database.healthEventDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val events = kotlinx.coroutines.flow.combine(
        _query,
        _selectedKind,
        _limit,
        database.healthEventDao().observeAll(),
        database.conditionDao().observeAll(),
        database.allergyDao().observeAll(),
        database.medicationDao().observeAll(),
        database.procedureDao().observeAll(),
        database.hospitalisationDao().observeAll(),
        database.vaccinationDao().observeAll(),
        database.deviceDao().observeAll(),
        database.familyHistoryDao().observeAll(),
        database.documentDao().observeAll()
    ) { args ->
        val q = (args[0] as String).trim().lowercase()
        val kind = args[1] as? String
        val limit = args[2] as Int

        @Suppress("UNCHECKED_CAST")
        val rawEvents = args[3] as List<HealthEventEntity>
        @Suppress("UNCHECKED_CAST")
        val conditions = args[4] as List<ConditionEntity>
        @Suppress("UNCHECKED_CAST")
        val allergies = args[5] as List<AllergyEntity>
        @Suppress("UNCHECKED_CAST")
        val medications = args[6] as List<MedicationEntity>
        @Suppress("UNCHECKED_CAST")
        val procedures = args[7] as List<ProcedureEntity>
        @Suppress("UNCHECKED_CAST")
        val hospitalisations = args[8] as List<HospitalisationEntity>
        @Suppress("UNCHECKED_CAST")
        val vaccinations = args[9] as List<VaccinationEntity>
        @Suppress("UNCHECKED_CAST")
        val devices = args[10] as List<DeviceEntity>
        @Suppress("UNCHECKED_CAST")
        val familyHistory = args[11] as List<FamilyHistoryEntity>
        @Suppress("UNCHECKED_CAST")
        val documents = args[12] as List<DocumentEntity>

        if (q.isEmpty()) {
            val filtered = if (kind.isNullOrEmpty()) rawEvents else rawEvents.filter { it.kind == kind }
            filtered.take(limit)
        } else {
            val matches = mutableListOf<HealthEventEntity>()

            // 1. Raw events
            rawEvents.filter { it.title.lowercase().contains(q) || it.details.lowercase().contains(q) || it.bodyLocation.lowercase().contains(q) }.forEach { matches += it }

            // 2. Conditions
            conditions.filter { it.name.lowercase().contains(q) || it.notes.lowercase().contains(q) || it.treatingDoctor.lowercase().contains(q) }.forEach {
                matches += HealthEventEntity(
                    id = it.id,
                    title = it.name,
                    details = "Condition · Status: ${it.status} · ${it.notes}",
                    kind = "CONDITION",
                    effectiveAtEpochMillis = it.updatedAtEpochMillis,
                    createdAtEpochMillis = it.createdAtEpochMillis
                )
            }

            // 3. Allergies
            allergies.filter { it.allergen.lowercase().contains(q) || it.reaction.lowercase().contains(q) || it.notes.lowercase().contains(q) }.forEach {
                matches += HealthEventEntity(
                    id = it.id,
                    title = it.allergen,
                    details = "Allergy · Reaction: ${it.reaction} · Severity: ${it.severity} · ${it.notes}",
                    kind = "ALLERGY",
                    effectiveAtEpochMillis = it.updatedAtEpochMillis,
                    createdAtEpochMillis = it.createdAtEpochMillis
                )
            }

            // 4. Medications
            medications.filter { it.name.lowercase().contains(q) || it.genericName.lowercase().contains(q) || it.notes.lowercase().contains(q) || it.physician.lowercase().contains(q) }.forEach {
                matches += HealthEventEntity(
                    id = it.id,
                    title = it.name,
                    details = "Medication · Generic: ${it.genericName} · Status: ${it.status} · Note: ${it.notes}",
                    kind = "MEDICATION",
                    effectiveAtEpochMillis = it.updatedAtEpochMillis,
                    createdAtEpochMillis = it.createdAtEpochMillis
                )
            }

            // 5. Procedures
            procedures.filter { it.name.lowercase().contains(q) || it.hospital.lowercase().contains(q) || it.doctor.lowercase().contains(q) || it.notes.lowercase().contains(q) }.forEach {
                matches += HealthEventEntity(
                    id = it.id,
                    title = it.name,
                    details = "Procedure · Hospital: ${it.hospital} · Doctor: ${it.doctor} · ${it.notes}",
                    kind = "PROCEDURE",
                    effectiveAtEpochMillis = it.updatedAtEpochMillis,
                    createdAtEpochMillis = it.createdAtEpochMillis
                )
            }

            // 6. Hospitalisations
            hospitalisations.filter { it.reason.lowercase().contains(q) || it.diagnosis.lowercase().contains(q) || it.hospital.lowercase().contains(q) || it.notes.lowercase().contains(q) }.forEach {
                matches += HealthEventEntity(
                    id = it.id,
                    title = it.reason,
                    details = "Hospitalisation · Hospital: ${it.hospital} · Diagnosis: ${it.diagnosis} · ${it.notes}",
                    kind = "HOSPITALISATION",
                    effectiveAtEpochMillis = it.updatedAtEpochMillis,
                    createdAtEpochMillis = it.createdAtEpochMillis
                )
            }

            // 7. Vaccinations
            vaccinations.filter { it.vaccineName.lowercase().contains(q) || it.provider.lowercase().contains(q) || it.notes.lowercase().contains(q) }.forEach {
                matches += HealthEventEntity(
                    id = it.id,
                    title = it.vaccineName,
                    details = "Vaccination · Dose: ${it.dose} · Provider: ${it.provider} · Lot: ${it.lotNumber} · ${it.notes}",
                    kind = "VACCINATION",
                    effectiveAtEpochMillis = it.updatedAtEpochMillis,
                    createdAtEpochMillis = it.createdAtEpochMillis
                )
            }

            // 8. Devices
            devices.filter { it.name.lowercase().contains(q) || it.manufacturer.lowercase().contains(q) || it.model.lowercase().contains(q) || it.notes.lowercase().contains(q) }.forEach {
                matches += HealthEventEntity(
                    id = it.id,
                    title = it.name,
                    details = "Device/Implant · Type: ${it.type} · Manufacturer: ${it.manufacturer} · Model: ${it.model} · ${it.notes}",
                    kind = "DEVICE",
                    effectiveAtEpochMillis = it.updatedAtEpochMillis,
                    createdAtEpochMillis = it.createdAtEpochMillis
                )
            }

            // 9. Family History
            familyHistory.filter { it.relationship.lowercase().contains(q) || it.condition.lowercase().contains(q) || it.notes.lowercase().contains(q) }.forEach {
                matches += HealthEventEntity(
                    id = it.id,
                    title = "${it.relationship}: ${it.condition}",
                    details = "Family History · ${it.notes}",
                    kind = "FAMILY_HISTORY",
                    effectiveAtEpochMillis = it.updatedAtEpochMillis,
                    createdAtEpochMillis = it.createdAtEpochMillis
                )
            }

            // 10. Documents
            documents.filter {
                it.title.lowercase().contains(q) || it.notes.lowercase().contains(q) ||
                it.testName.lowercase().contains(q) || it.laboratoryName.lowercase().contains(q) ||
                it.radiologyModality.lowercase().contains(q) || it.radiologyRegion.lowercase().contains(q) ||
                it.centreName.lowercase().contains(q) || it.reportingDoctors.lowercase().contains(q) ||
                it.prescribingDoctor.lowercase().contains(q) || it.doctorSpecialty.lowercase().contains(q) ||
                it.certificateType.lowercase().contains(q) || it.bodyLocation.lowercase().contains(q)
            }.forEach {
                matches += HealthEventEntity(
                    id = it.id,
                    title = it.title,
                    details = "Document (${it.category}) · ${it.notes}",
                    kind = "DOCUMENT",
                    effectiveAtEpochMillis = it.createdAtEpochMillis,
                    createdAtEpochMillis = it.createdAtEpochMillis
                )
            }

            val filtered = if (kind.isNullOrEmpty()) matches else matches.filter { it.kind == kind }
            filtered.distinctBy { it.id }.sortedByDescending { it.effectiveAtEpochMillis ?: it.createdAtEpochMillis }.take(limit)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) {
        _query.value = q
        _limit.value = 50
    }

    fun setSelectedKind(kind: String?) {
        _selectedKind.value = kind
        _limit.value = 50
    }

    fun loadMore() {
        _limit.value = _limit.value + 50
    }

    fun addEvent(kind: String, title: String, details: String, severity: Int?) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        database.healthEventDao().insert(
            HealthEventEntity(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                details = details.trim(),
                kind = kind,
                effectiveAtEpochMillis = now,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
                status = "ACTIVE",
                severity = severity
            )
        )
    }

    fun archive(event: HealthEventEntity) = viewModelScope.launch {
        database.healthEventDao().archive(event.id, System.currentTimeMillis())
        _archivedEvents.tryEmit(event)
    }

    fun unarchive(event: HealthEventEntity) = viewModelScope.launch {
        database.healthEventDao().unarchive(event.id, System.currentTimeMillis())
    }

    fun delete(event: HealthEventEntity) = viewModelScope.launch {
        event.imageAttachmentId?.let {
            secureFileStore.delete(it)
            database.documentDao().delete(it)
        }
        database.healthEventDao().delete(event.id)
    }
}
