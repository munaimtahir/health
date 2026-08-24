package com.vexel.passport

import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.height
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.vexel.passport.core.datastore.PreferencesStore
import com.vexel.passport.core.datastore.UserPreferences
import com.vexel.passport.core.security.PinVerifier
import com.vexel.passport.core.security.PinMaterialCipher
import com.vexel.passport.core.notifications.ReminderScheduler
import com.vexel.passport.core.designsystem.VexelHealthPassportTheme
import com.vexel.passport.feature.onboarding.OnboardingScreen
import com.vexel.passport.feature.dashboard.HomeScreen
import com.vexel.passport.feature.timeline.TimelineScreen
import com.vexel.passport.feature.reminders.RemindersScreen
import com.vexel.passport.feature.records.DocumentsScreen
import com.vexel.passport.feature.profile.ProfileScreen
import com.vexel.passport.core.database.HealthDatabase
import com.vexel.passport.core.database.HealthEventEntity
import com.vexel.passport.core.database.MedicationEntity
import com.vexel.passport.core.database.MedicationChangeEntity
import com.vexel.passport.core.database.DocumentEntity
import com.vexel.passport.core.database.ConditionEntity
import com.vexel.passport.core.database.AllergyEntity
import com.vexel.passport.core.database.MeasurementEntity
import com.vexel.passport.core.database.ProcedureEntity
import com.vexel.passport.core.database.HospitalisationEntity
import com.vexel.passport.core.database.VaccinationEntity
import com.vexel.passport.core.database.DeviceEntity
import com.vexel.passport.core.database.FamilyHistoryEntity
import com.vexel.passport.core.files.SecureFileStore
import com.vexel.passport.core.model.ConditionDraft
import com.vexel.passport.core.model.AllergyDraft
import com.vexel.passport.core.model.ProcedureDraft
import com.vexel.passport.core.model.HospitalisationDraft
import com.vexel.passport.core.model.VaccinationDraft
import com.vexel.passport.core.model.DeviceDraft
import com.vexel.passport.core.model.FamilyHistoryDraft
import com.vexel.passport.core.model.MedicationDraft
import android.provider.OpenableColumns
import android.net.Uri
import java.util.UUID
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.filled.Add
import com.vexel.passport.core.ui.ConditionDialog
import com.vexel.passport.core.ui.AllergyDialog
import com.vexel.passport.core.ui.ProcedureDialog
import com.vexel.passport.core.ui.HospitalisationDialog
import com.vexel.passport.core.ui.VaccinationDialog
import com.vexel.passport.core.ui.DeviceDialog
import com.vexel.passport.core.ui.FamilyHistoryDialog
import com.vexel.passport.core.ui.MeasurementDialog
import com.vexel.passport.core.ui.MedicationDialog

private data class Destination(val route: String, val label: String, val icon: ImageVector)
private val destinations = listOf(
    Destination(Routes.HOME, "Health", Icons.Outlined.Home),
    Destination(Routes.RECORDS, "Timeline", Icons.Outlined.Event),
    Destination(Routes.ADD, "+", Icons.Default.Add),
    Destination(Routes.VAULT, "Vault", Icons.Outlined.Folder),
    Destination(Routes.PROFILE, "Profile", Icons.Outlined.Person),
)
internal val primaryDestinationLabels: List<String> = destinations.map { it.label }

/** Centralized, stable string routes for the primary bottom-navigation destinations. */
internal object Routes {
    const val HOME = "home"
    const val RECORDS = "records"
    const val ADD = "add"
    const val PLAN = "plan"
    const val VAULT = "vault"
    const val PROFILE = "profile"
}

@HiltViewModel
class PassportViewModel @Inject constructor(
    @param:dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context,
    private val database: HealthDatabase,
    private val secureFileStore: SecureFileStore,
    private val preferences: PreferencesStore,
    private val pinMaterialCipher: PinMaterialCipher,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    init {
        viewModelScope.launch { reminderScheduler.reconcile() }
    }
    private val pinVerifier = PinVerifier()
    val settings = preferences.preferences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())
    
    private val _operationError = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val operationError: kotlinx.coroutines.flow.StateFlow<String?> = _operationError
    
    fun dismissOperationError() { _operationError.value = null }
    fun completeOnboarding() = viewModelScope.launch { preferences.setOnboardingComplete(true) }

    fun verifyPin(pin: String, prefs: UserPreferences): Boolean {
        if (!prefs.lockEnabled) return true
        return runCatching { pinVerifier.matches(pin.toCharArray(), pinMaterialCipher.decrypt(prefs.pinMaterial)) }.getOrDefault(false)
    }

    fun saveCondition(draft: ConditionDraft) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        database.conditionDao().insert(
            ConditionEntity(
                id = id,
                name = draft.name.trim(),
                status = draft.status,
                diagnosisDate = draft.diagnosisDate.trim(),
                resolvedDate = draft.resolvedDate.trim(),
                notes = draft.notes.trim(),
                treatingDoctor = draft.treatingDoctor.trim(),
                tags = draft.tags.trim(),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )
        database.healthEventDao().insert(
            HealthEventEntity(
                id = UUID.randomUUID().toString(),
                title = draft.name.trim(),
                details = "Condition added (${draft.status})",
                kind = "CONDITION",
                effectiveAtEpochMillis = now,
                createdAtEpochMillis = now
            )
        )
    }

    fun saveAllergy(draft: AllergyDraft) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        database.allergyDao().insert(
            AllergyEntity(
                id = id,
                allergen = draft.allergen.trim(),
                category = draft.category,
                reaction = draft.reaction.trim(),
                severity = draft.severity,
                notes = draft.notes.trim(),
                status = draft.status,
                allergyDate = draft.allergyDate.trim(),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )
        database.healthEventDao().insert(
            HealthEventEntity(
                id = UUID.randomUUID().toString(),
                title = draft.allergen.trim(),
                details = "Allergy added: ${draft.reaction}",
                kind = "ALLERGY",
                effectiveAtEpochMillis = now,
                createdAtEpochMillis = now
            )
        )
    }

    fun saveMedication(draft: MedicationDraft) = viewModelScope.launch {
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
                prescriptionId = draft.prescriptionId,
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
                createdAtEpochMillis = now
            )
        )
    }

    fun saveProcedure(draft: ProcedureDraft) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        database.procedureDao().insert(
            ProcedureEntity(
                id = id,
                name = draft.name.trim(),
                date = draft.date.trim(),
                hospital = draft.hospital.trim(),
                doctor = draft.doctor.trim(),
                indication = draft.indication.trim(),
                notes = draft.notes.trim(),
                linkedDocumentId = draft.linkedDocumentId,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )
        database.healthEventDao().insert(
            HealthEventEntity(
                id = UUID.randomUUID().toString(),
                title = draft.name.trim(),
                details = "${draft.doctor} · ${draft.hospital}".trim(' ', '·'),
                kind = "PROCEDURE",
                effectiveAtEpochMillis = now,
                createdAtEpochMillis = now
            )
        )
    }

    fun saveHospitalisation(draft: HospitalisationDraft) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        database.hospitalisationDao().insert(
            HospitalisationEntity(
                id = id,
                admissionDate = draft.admissionDate.trim(),
                dischargeDate = draft.dischargeDate.trim(),
                hospital = draft.hospital.trim(),
                reason = draft.reason.trim(),
                diagnosis = draft.diagnosis.trim(),
                notes = draft.notes.trim(),
                linkedDocumentId = draft.linkedDocumentId,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )
        database.healthEventDao().insert(
            HealthEventEntity(
                id = UUID.randomUUID().toString(),
                title = "Hospitalised: ${draft.reason}",
                details = draft.hospital.trim(),
                kind = "HOSPITALISATION",
                effectiveAtEpochMillis = now,
                createdAtEpochMillis = now
            )
        )
    }

    fun saveVaccination(draft: VaccinationDraft) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        database.vaccinationDao().insert(
            VaccinationEntity(
                id = id,
                vaccineName = draft.vaccineName.trim(),
                dose = draft.dose.trim(),
                date = draft.date.trim(),
                provider = draft.provider.trim(),
                lotNumber = draft.lotNumber.trim(),
                nextDueDate = draft.nextDueDate.trim(),
                linkedDocumentId = draft.linkedDocumentId,
                notes = draft.notes.trim(),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )
        database.healthEventDao().insert(
            HealthEventEntity(
                id = UUID.randomUUID().toString(),
                title = "Vaccinated: ${draft.vaccineName}",
                details = "${draft.dose} · ${draft.provider}".trim(' ', '·'),
                kind = "VACCINATION",
                effectiveAtEpochMillis = now,
                createdAtEpochMillis = now
            )
        )
    }

    fun saveDevice(draft: DeviceDraft) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        database.deviceDao().insert(
            DeviceEntity(
                id = id,
                type = draft.type,
                name = draft.name.trim(),
                manufacturer = draft.manufacturer.trim(),
                model = draft.model.trim(),
                serialNumber = draft.serialNumber.trim(),
                implantationDate = draft.implantationDate.trim(),
                hospital = draft.hospital.trim(),
                notes = draft.notes.trim(),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )
        database.healthEventDao().insert(
            HealthEventEntity(
                id = UUID.randomUUID().toString(),
                title = "Implanted: ${draft.name}",
                details = "${draft.type} · ${draft.manufacturer}".trim(' ', '·'),
                kind = "DEVICE",
                effectiveAtEpochMillis = now,
                createdAtEpochMillis = now
            )
        )
    }

    fun saveFamilyHistory(draft: FamilyHistoryDraft) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        database.familyHistoryDao().insert(
            FamilyHistoryEntity(
                id = UUID.randomUUID().toString(),
                relationship = draft.relationship.trim(),
                condition = draft.condition.trim(),
                notes = draft.notes.trim(),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )
    }

    fun saveMeasurement(type: String, value1: Double, value2: Double?, unit: String, context: String) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        database.measurementDao().insert(
            MeasurementEntity(
                id = UUID.randomUUID().toString(),
                type = type,
                primaryValue = value1,
                secondaryValue = value2,
                unit = unit,
                context = context,
                recordedAtEpochMillis = now
            )
        )
        val display = if (value2 == null) "$value1 $unit" else "$value1/$value2 $unit"
        database.healthEventDao().insert(
            HealthEventEntity(
                id = UUID.randomUUID().toString(),
                title = type.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                details = display,
                kind = "MEASUREMENT",
                effectiveAtEpochMillis = now,
                createdAtEpochMillis = now
            )
        )
    }

    fun saveDocument(uri: Uri, title: String, category: String, documentDate: String, notes: String) = viewModelScope.launch {
        try {
            val mimeType = appContext.contentResolver.getType(uri) ?: "application/octet-stream"
            val displayName = appContext.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else "document"
            } ?: "document"
            val preserved = appContext.contentResolver.openInputStream(uri)?.use { input ->
                secureFileStore.preserveOriginal(input, mimeType, displayName)
            } ?: return@launch
            val now = System.currentTimeMillis()
            database.documentDao().insert(
                DocumentEntity(
                    id = preserved.id,
                    title = title.trim().ifBlank { displayName },
                    category = category.trim().ifBlank { "OTHER" },
                    documentDate = documentDate.trim(),
                    notes = notes.trim(),
                    originalFileName = displayName,
                    mimeType = preserved.mimeType,
                    byteCount = preserved.byteCount,
                    sha256 = preserved.sha256,
                    createdAtEpochMillis = now
                )
            )
            database.healthEventDao().insert(
                HealthEventEntity(
                    id = UUID.randomUUID().toString(),
                    title = title.trim().ifBlank { displayName },
                    details = "Document added: $category",
                    kind = "DOCUMENT",
                    effectiveAtEpochMillis = now,
                    createdAtEpochMillis = now
                )
            )
        } catch (cancellation: kotlinx.coroutines.CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            _operationError.value = "Document capture failed. Please try again."
        }
    }

    fun saveHealthMedia(uri: Uri, title: String, description: String, bodyLocation: String, linkedSymptom: String, linkedCondition: String) = viewModelScope.launch {
        try {
            val mimeType = appContext.contentResolver.getType(uri) ?: "image/jpeg"
            val displayName = appContext.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else "media"
            } ?: "media"
            val preserved = appContext.contentResolver.openInputStream(uri)?.use { input ->
                secureFileStore.preserveOriginal(input, mimeType, displayName)
            } ?: return@launch
            val now = System.currentTimeMillis()
            database.documentDao().insert(
                DocumentEntity(
                    id = preserved.id,
                    title = title.trim().ifBlank { displayName },
                    category = "HEALTH_MEDIA",
                    documentDate = "",
                    notes = description.trim(),
                    originalFileName = displayName,
                    mimeType = preserved.mimeType,
                    byteCount = preserved.byteCount,
                    sha256 = preserved.sha256,
                    createdAtEpochMillis = now,
                    bodyLocation = bodyLocation.trim(),
                    linkedSymptom = linkedSymptom.trim(),
                    linkedCondition = linkedCondition.trim()
                )
            )
            database.healthEventDao().insert(
                HealthEventEntity(
                    id = UUID.randomUUID().toString(),
                    title = title.trim().ifBlank { displayName },
                    details = listOf(description, bodyLocation, linkedSymptom, linkedCondition).filter { it.isNotBlank() }.joinToString(" · "),
                    kind = "HEALTH_MEDIA",
                    effectiveAtEpochMillis = now,
                    createdAtEpochMillis = now,
                    imageAttachmentId = preserved.id,
                    bodyLocation = bodyLocation.trim()
                )
            )
        } catch (cancellation: kotlinx.coroutines.CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            _operationError.value = "Media capture failed. Please try again."
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VexelHealthPassportApp(viewModel: PassportViewModel = hiltViewModel()) {
    val prefs by viewModel.settings.collectAsState()
    val currentWindow = (androidx.compose.ui.platform.LocalView.current.context as? android.app.Activity)?.window
    LaunchedEffect(currentWindow, prefs.hideRecentAppsPreview) {
        if (prefs.hideRecentAppsPreview) {
            currentWindow?.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            currentWindow?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
    VexelHealthPassportTheme(darkTheme = prefs.darkTheme) {
        if (!prefs.onboardingComplete) {
            OnboardingScreen(onComplete = viewModel::completeOnboarding)
        } else {
            LockGate(prefs, viewModel) {
                var showUniversalAdd by rememberSaveable { mutableStateOf(false) }
                var showConditionDialog by rememberSaveable { mutableStateOf(false) }
                var showAllergyDialog by rememberSaveable { mutableStateOf(false) }
                var showMedicationDialog by rememberSaveable { mutableStateOf(false) }
                var showProcedureDialog by rememberSaveable { mutableStateOf(false) }
                var showHospitalisationDialog by rememberSaveable { mutableStateOf(false) }
                var showVaccinationDialog by rememberSaveable { mutableStateOf(false) }
                var showDeviceDialog by rememberSaveable { mutableStateOf(false) }
                var showFamilyHistoryDialog by rememberSaveable { mutableStateOf(false) }
                var showMeasurementType by rememberSaveable { mutableStateOf<String?>(null) }
                var showDocumentCategory by rememberSaveable { mutableStateOf<String?>(null) }
                var showHealthMediaDialog by rememberSaveable { mutableStateOf(false) }

                val fontScale = LocalDensity.current.fontScale
                val navigationLabelStyle = if (fontScale >= 1.8f) {
                    MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, lineHeight = 10.sp)
                } else {
                    MaterialTheme.typography.labelMedium
                }
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route ?: Routes.HOME
                val currentLabel = if (currentRoute == Routes.ADD) "Add Capture" else destinations.firstOrNull { it.route == currentRoute }?.label ?: destinations.first().label
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    topBar = { TopAppBar(title = { Text(currentLabel) }) },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = { NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                    ) { destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = destination.route == currentRoute,
                            onClick = {
                                if (destination.route == Routes.ADD) {
                                    showUniversalAdd = true
                                } else {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label, maxLines = 1, style = navigationLabelStyle) },
                        )
                    } } },
                ) { padding ->
                    NavHost(navController = navController, startDestination = Routes.HOME) {
                        composable(Routes.HOME) {
                            HomeScreen(modifier = Modifier.padding(padding))
                        }
                        composable(Routes.RECORDS) {
                            TimelineScreen(modifier = Modifier.padding(padding))
                        }
                        composable(Routes.PLAN) {
                            RemindersScreen(modifier = Modifier.padding(padding))
                        }
                        composable(Routes.VAULT) {
                            DocumentsScreen(modifier = Modifier.padding(padding))
                        }
                        composable(Routes.PROFILE) {
                            ProfileScreen(modifier = Modifier.padding(padding))
                        }
                    }
                }

                if (showUniversalAdd) {
                    androidx.compose.material3.ModalBottomSheet(
                        onDismissRequest = { showUniversalAdd = false },
                        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("ADD HEALTH INFORMATION", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            
                            // Medical History
                            Text("Medical History", style = MaterialTheme.typography.labelLarge)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showConditionDialog = true }, label = { Text("Condition") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showMedicationDialog = true }, label = { Text("Medicine") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showAllergyDialog = true }, label = { Text("Allergy") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showProcedureDialog = true }, label = { Text("Procedure/Surgery") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showHospitalisationDialog = true }, label = { Text("Hospitalisation") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showVaccinationDialog = true }, label = { Text("Vaccination") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showDeviceDialog = true }, label = { Text("Device/Implant") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showFamilyHistoryDialog = true }, label = { Text("Family History") })
                            }

                            // Track Measurements
                            Text("Track / Log", style = MaterialTheme.typography.labelLarge)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showMeasurementType = "BLOOD_PRESSURE" }, label = { Text("Blood Pressure") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showMeasurementType = "BLOOD_GLUCOSE" }, label = { Text("Blood Glucose") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showMeasurementType = "TEMPERATURE" }, label = { Text("Temperature") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showMeasurementType = "WEIGHT" }, label = { Text("Weight") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showMeasurementType = "PULSE" }, label = { Text("Pulse / Heart Rate") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showMeasurementType = "SPO2" }, label = { Text("SpO₂") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showMeasurementType = "RESPIRATORY_RATE" }, label = { Text("Respiratory Rate") })
                            }

                            // Documents
                            Text("Documents / Records", style = MaterialTheme.typography.labelLarge)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showDocumentCategory = "LABORATORY_REPORT" }, label = { Text("Lab Report") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showDocumentCategory = "RADIOLOGY_REPORT" }, label = { Text("Radiology Report") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showDocumentCategory = "PRESCRIPTION" }, label = { Text("Prescription") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showDocumentCategory = "MEDICAL_CERTIFICATE" }, label = { Text("Medical Certificate") })
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showDocumentCategory = "OTHER" }, label = { Text("Other Document") })
                            }

                            // Health Media
                            Text("Health Media / Photos", style = MaterialTheme.typography.labelLarge)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(selected = false, onClick = { showUniversalAdd = false; showHealthMediaDialog = true }, label = { Text("Photograph / Video") })
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                if (showConditionDialog) {
                    ConditionDialog(
                        onDismiss = { showConditionDialog = false },
                        onSave = viewModel::saveCondition
                    )
                }
                if (showAllergyDialog) {
                    AllergyDialog(
                        onDismiss = { showAllergyDialog = false },
                        onSave = viewModel::saveAllergy
                    )
                }
                if (showMedicationDialog) {
                    MedicationDialog(
                        onDismiss = { showMedicationDialog = false },
                        onSave = viewModel::saveMedication
                    )
                }
                if (showProcedureDialog) {
                    ProcedureDialog(
                        onDismiss = { showProcedureDialog = false },
                        onSave = viewModel::saveProcedure
                    )
                }
                if (showHospitalisationDialog) {
                    HospitalisationDialog(
                        onDismiss = { showHospitalisationDialog = false },
                        onSave = viewModel::saveHospitalisation
                    )
                }
                if (showVaccinationDialog) {
                    VaccinationDialog(
                        onDismiss = { showVaccinationDialog = false },
                        onSave = viewModel::saveVaccination
                    )
                }
                if (showDeviceDialog) {
                    DeviceDialog(
                        onDismiss = { showDeviceDialog = false },
                        onSave = viewModel::saveDevice
                    )
                }
                if (showFamilyHistoryDialog) {
                    FamilyHistoryDialog(
                        onDismiss = { showFamilyHistoryDialog = false },
                        onSave = viewModel::saveFamilyHistory
                    )
                }
                showMeasurementType?.let { mType ->
                    MeasurementDialog(
                        type = mType,
                        onDismiss = { showMeasurementType = null },
                        onSave = { type, v1, v2, unit, context ->
                            viewModel.saveMeasurement(type, v1, v2, unit, context)
                        }
                    )
                }
                showDocumentCategory?.let { category ->
                    DocumentCaptureDialog(
                        initialCategory = category,
                        onDismiss = { showDocumentCategory = null },
                        onSave = { uri, title, cat, date, notes ->
                            viewModel.saveDocument(uri, title, cat, date, notes)
                        }
                    )
                }
                if (showHealthMediaDialog) {
                    HealthMediaCaptureDialog(
                        onDismiss = { showHealthMediaDialog = false },
                        onSave = { uri, title, desc, body, symptom, condition ->
                            viewModel.saveHealthMedia(uri, title, desc, body, symptom, condition)
                        }
                    )
                }

                val operationError by viewModel.operationError.collectAsState()
                operationError?.let { message ->
                    AlertDialog(
                        onDismissRequest = viewModel::dismissOperationError,
                        title = { Text("Something went wrong") },
                        text = { Text(message) },
                        confirmButton = { TextButton(viewModel::dismissOperationError) { Text("OK") } }
                    )
                }
            }
        }
    }
}

@Composable
private fun LockGate(prefs: UserPreferences, vm: PassportViewModel, content: @Composable () -> Unit) {
    var unlocked by rememberSaveable(prefs.lockEnabled) { mutableStateOf(!prefs.lockEnabled) }
    var unlockedAt by rememberSaveable(prefs.lockEnabled) { mutableStateOf<Long?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, prefs.lockEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && prefs.lockEnabled) {
                unlocked = false
                unlockedAt = null
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(unlocked, prefs.lockEnabled, prefs.lockTimeoutMinutes) {
        while (unlocked && prefs.lockEnabled && prefs.lockTimeoutMinutes > 0) {
            delay(1_000)
            val started = unlockedAt ?: continue
            if (System.currentTimeMillis() - started >= prefs.lockTimeoutMinutes * 60_000L) {
                unlocked = false
                unlockedAt = null
            }
        }
    }
    if (unlocked || !prefs.lockEnabled) content() else PinUnlockDialog(prefs, vm) {
        unlocked = true
        unlockedAt = System.currentTimeMillis()
    }
}

@Composable
private fun PinUnlockDialog(prefs: UserPreferences, vm: PassportViewModel, onUnlocked: () -> Unit) {
    var pin by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }
    val activity = LocalContext.current as? FragmentActivity
    val canUseBiometric = activity != null && BiometricManager.from(activity).canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    ) == BiometricManager.BIOMETRIC_SUCCESS
    val biometricPrompt = remember(activity) {
        activity?.let { host ->
            BiometricPrompt(host, ContextCompat.getMainExecutor(host), object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onUnlocked()
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { error = false }
            })
        }
    }
    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Vexel Health Passport")
            .setSubtitle("Authenticate to view your private health information")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
    }
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Unlock Vexel Health Passport") },
        text = {
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter(Char::isDigit).take(12); error = false },
                label = { Text("PIN") },
                isError = error,
                supportingText = { if (error) Text("Incorrect PIN") }
            )
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (canUseBiometric) {
                    TextButton({ biometricPrompt?.authenticate(promptInfo) }) {
                        Text("Use device authentication")
                    }
                }
                Button({
                    if (vm.verifyPin(pin, prefs)) onUnlocked() else error = true
                }) {
                    Text("Unlock")
                }
            }
        }
    )
}

@Composable
fun DocumentCaptureDialog(
    initialCategory: String,
    onDismiss: () -> Unit,
    onSave: (Uri, String, String, String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(initialCategory) }
    var documentDate by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            onSave(uri, title, category, documentDate, notes)
            onDismiss()
        }
    }
    com.vexel.passport.core.ui.FullScreenDialog(
        title = "Capture " + category.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
        onDismiss = onDismiss,
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = { launcher.launch(arrayOf("application/pdf", "image/jpeg", "image/png")) }
            ) {
                Text("Choose File")
            }
        }
    ) {
        Text("Provide a title and choose the PDF or image file from your device. The file is copied privately to secure storage.")
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Document Title (Required)") }
        )
        OutlinedTextField(
            value = documentDate,
            onValueChange = { documentDate = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Document Date (yyyy-MM-dd) (optional)") },
            placeholder = { Text("yyyy-MM-dd") }
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes / Summary (optional)") }
        )
    }
}

@Composable
fun HealthMediaCaptureDialog(
    onDismiss: () -> Unit,
    onSave: (Uri, String, String, String, String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var bodyLocation by rememberSaveable { mutableStateOf("") }
    var linkedSymptom by rememberSaveable { mutableStateOf("") }
    var linkedCondition by rememberSaveable { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            onSave(uri, title, description, bodyLocation, linkedSymptom, linkedCondition)
            onDismiss()
        }
    }

    com.vexel.passport.core.ui.FullScreenDialog(
        title = "Record Photograph/Video",
        onDismiss = onDismiss,
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = { launcher.launch(arrayOf("image/jpeg", "image/png", "video/mp4")) }
            ) {
                Text("Select Media File")
            }
        }
    ) {
        Text("Record a clinical photograph/video (e.g. of a skin rash, wound, or joint movement) and describe it.")
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Title / Observation Name (Required)") }
        )
        OutlinedTextField(
            value = bodyLocation,
            onValueChange = { bodyLocation = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Body Location (optional)") }
        )
        OutlinedTextField(
            value = linkedSymptom,
            onValueChange = { linkedSymptom = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Linked Symptom (optional)") }
        )
        OutlinedTextField(
            value = linkedCondition,
            onValueChange = { linkedCondition = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Linked Condition (optional)") }
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Description / Clinical Notes (optional)") }
        )
    }
}

