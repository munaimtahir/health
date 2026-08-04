package pk.vexel.healthpassport

import android.content.Context
import android.content.Intent
import android.Manifest
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Color

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pk.vexel.healthpassport.core.database.HealthDatabase
import pk.vexel.healthpassport.core.database.HealthEventEntity
import pk.vexel.healthpassport.core.database.MedicationEntity
import pk.vexel.healthpassport.core.database.DocumentEntity
import pk.vexel.healthpassport.core.database.ReminderEntity
import pk.vexel.healthpassport.core.database.ProfileEntity
import pk.vexel.healthpassport.core.datastore.PreferencesStore
import pk.vexel.healthpassport.core.files.SecureFileStore
import pk.vexel.healthpassport.core.notifications.ReminderScheduler
import pk.vexel.healthpassport.core.designsystem.InformationCard
import pk.vexel.healthpassport.core.designsystem.VexelHealthPassportTheme
import pk.vexel.healthpassport.core.model.SymptomDraft
import pk.vexel.healthpassport.core.model.MedicationDraft
import pk.vexel.healthpassport.core.model.validationErrors
import pk.vexel.healthpassport.core.model.TrendEvent
import pk.vexel.healthpassport.core.model.summarizeSymptoms
import pk.vexel.healthpassport.core.security.PinVerifier
import pk.vexel.healthpassport.core.security.PinMaterialCipher

private data class Destination(val label: String, val icon: ImageVector)
private val destinations = listOf(
    Destination("Home", Icons.Outlined.Home), Destination("Timeline", Icons.Outlined.Event),
    Destination("Records", Icons.Outlined.Folder), Destination("Plan", Icons.Outlined.Schedule),
    Destination("Profile", Icons.Outlined.Person),
)

@HiltViewModel
class PassportViewModel @Inject constructor(
    private val database: HealthDatabase,
    private val preferences: PreferencesStore,
    private val pinMaterialCipher: PinMaterialCipher,
    private val secureFileStore: SecureFileStore,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    private val pinVerifier = PinVerifier()
    val events = database.healthEventDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val profile = database.profileDao().observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val medications = database.medicationDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val documents = database.documentDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val reminders = database.reminderDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val settings = preferences.preferences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), pk.vexel.healthpassport.core.datastore.UserPreferences())

    fun completeOnboarding() = viewModelScope.launch { preferences.setOnboardingComplete(true) }
    fun setDarkTheme(value: Boolean) = viewModelScope.launch { preferences.setDarkTheme(value) }
    fun saveProfile(p: ProfileEntity) = viewModelScope.launch { database.profileDao().upsert(p) }
    fun addEvent(kind: String, title: String, details: String, severity: Int? = null) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        database.healthEventDao().insert(HealthEventEntity(UUID.randomUUID().toString(), title, details, kind, now, now, now, "ACTIVE", severity))
    }
    fun archive(event: HealthEventEntity) = viewModelScope.launch { database.healthEventDao().archive(event.id, System.currentTimeMillis()) }
    fun delete(event: HealthEventEntity) = viewModelScope.launch { database.healthEventDao().delete(event.id) }
    fun addMedication(draft: MedicationDraft) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        database.medicationDao().insert(MedicationEntity(UUID.randomUUID().toString(), draft.name.trim(), strength = draft.strength.trim(), dose = draft.dose.trim(), route = draft.route.trim(), frequency = draft.frequency.trim(), notes = draft.notes.trim(), createdAtEpochMillis = now, updatedAtEpochMillis = now))
        database.healthEventDao().insert(HealthEventEntity(UUID.randomUUID().toString(), draft.name.trim(), listOf(draft.strength, draft.dose, draft.frequency).filter { it.isNotBlank() }.joinToString(" · "), "MEDICATION", now, now, now, "ACTIVE"))
    }
    fun savePin(pin: String, confirmation: String): Boolean {
        if (pin != confirmation || pin.length !in 4..12 || pin.any { !it.isDigit() }) return false
        val record = pinVerifier.create(pin.toCharArray())
        viewModelScope.launch { preferences.setPinMaterial(pinMaterialCipher.encrypt(record)) }
        return true
    }
    fun verifyPin(pin: String, prefs: pk.vexel.healthpassport.core.datastore.UserPreferences): Boolean {
        if (!prefs.lockEnabled) return true
        return runCatching { pinVerifier.matches(pin.toCharArray(), pinMaterialCipher.decrypt(prefs.pinMaterial)) }.getOrDefault(false)
    }
    fun disablePin() = viewModelScope.launch { preferences.clearPinMaterial() }
    fun deleteAllData() = viewModelScope.launch {
        database.healthEventDao().deleteAll()
        database.medicationDao().deleteAll()
        database.profileDao().deleteAll()
        database.documentDao().deleteAll()
        reminders.value.forEach { reminderScheduler.cancel(it.id) }
        database.reminderDao().deleteAll()
        secureFileStore.deleteAll()
        preferences.clearAll()
    }
    fun addReminder(title: String, type: String, notes: String, dueAtEpochMillis: Long, recurrence: String) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val reminder = ReminderEntity(UUID.randomUUID().toString(), title.trim(), type.trim().ifBlank { "CUSTOM" }, notes.trim(), dueAtEpochMillis, recurrence, createdAtEpochMillis = now, updatedAtEpochMillis = now)
        database.reminderDao().insert(reminder)
        reminderScheduler.schedule(reminder.id, reminder.dueAtEpochMillis, reminder.recurrence)
    }
    fun completeReminder(reminder: ReminderEntity) = viewModelScope.launch { reminderScheduler.cancel(reminder.id); database.reminderDao().setStatus(reminder.id, "COMPLETED", System.currentTimeMillis()) }
    fun deleteReminder(reminder: ReminderEntity) = viewModelScope.launch { reminderScheduler.cancel(reminder.id); database.reminderDao().delete(reminder.id) }
    fun importDocument(context: Context, uri: Uri, title: String, category: String, documentDate: String, notes: String) = viewModelScope.launch {
        val mimeType = context.contentResolver.getType(uri) ?: return@launch
        val displayName = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else "document"
        } ?: "document"
        val preserved = context.contentResolver.openInputStream(uri)?.use { input -> secureFileStore.preserveOriginal(input, mimeType, displayName) } ?: return@launch
        val now = System.currentTimeMillis()
        database.documentDao().insert(DocumentEntity(preserved.id, title.trim().ifBlank { displayName }, category.trim().ifBlank { "OTHER" }, documentDate.trim(), notes.trim(), displayName, preserved.mimeType, preserved.byteCount, preserved.sha256, now))
    }
    fun deleteDocument(document: DocumentEntity) = viewModelScope.launch {
        secureFileStore.delete(document.id)
        database.documentDao().delete(document.id)
    }
    fun openDocument(context: Context, document: DocumentEntity) = viewModelScope.launch {
        val file = secureFileStore.copyToShareCache(context, document.id, document.originalFileName)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, document.mimeType); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "Open document").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    fun exportJson(): String {
        val root = JSONObject().put("formatVersion", 1).put("generatedAtEpochMillis", System.currentTimeMillis())
        profile.value?.let { p -> root.put("profile", JSONObject().put("name", p.name).put("dateOfBirth", p.dateOfBirth).put("bloodGroup", p.bloodGroup).put("allergies", p.allergies).put("conditions", p.conditions).put("emergencyContact", p.emergencyContact)) }
        root.put("events", JSONArray(events.value.map { e -> JSONObject().put("id", e.id).put("title", e.title).put("details", e.details).put("kind", e.kind).put("effectiveAtEpochMillis", e.effectiveAtEpochMillis).put("createdAtEpochMillis", e.createdAtEpochMillis).put("status", e.status).put("severity", e.severity) }))
        root.put("medications", JSONArray(medications.value.map { m -> JSONObject().put("id", m.id).put("name", m.name).put("genericName", m.genericName).put("strength", m.strength).put("dose", m.dose).put("unit", m.unit).put("route", m.route).put("frequency", m.frequency).put("startDate", m.startDate).put("stopDate", m.stopDate).put("status", m.status).put("indication", m.indication).put("physician", m.physician).put("notes", m.notes) }))
        root.put("documents", JSONArray(documents.value.map { d -> JSONObject().put("id", d.id).put("title", d.title).put("category", d.category).put("documentDate", d.documentDate).put("notes", d.notes).put("originalFileName", d.originalFileName).put("mimeType", d.mimeType).put("byteCount", d.byteCount).put("sha256", d.sha256) }))
        root.put("reminders", JSONArray(reminders.value.map { r -> JSONObject().put("id", r.id).put("title", r.title).put("type", r.type).put("notes", r.notes).put("dueAtEpochMillis", r.dueAtEpochMillis).put("recurrence", r.recurrence).put("status", r.status) }))
        return root.toString(2)
    }
    fun createBackup(context: Context, uri: Uri) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            ZipOutputStream(output).use { zip ->
                zip.putNextEntry(ZipEntry("data.json")); zip.write(exportJson().toByteArray(Charsets.UTF_8)); zip.closeEntry()
                documents.value.forEach { document ->
                    zip.putNextEntry(ZipEntry("documents/${document.id}")); secureFileStore.open(document.id).use { it.copyTo(zip) }; zip.closeEntry()
                }
            }
        }
    }
    fun restoreBackup(context: Context, uri: Uri) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val entries = linkedMapOf<String, ByteArray>()
        context.contentResolver.openInputStream(uri)?.use { input -> ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                require(!entry.isDirectory && (entry.name == "data.json" || entry.name.startsWith("documents/"))) { "Unsupported backup entry" }
                entries[entry.name] = zip.readBytes()
                entry = zip.nextEntry
            }
        } } ?: error("Unable to read backup")
        val data = JSONObject(String(entries["data.json"] ?: error("Missing backup data"), Charsets.UTF_8))
        require(data.optInt("formatVersion", -1) == 1) { "Unsupported backup version" }
        val restoredDocuments = mutableListOf<DocumentEntity>()
        val documentData = data.optJSONArray("documents") ?: JSONArray()
        for (index in 0 until documentData.length()) {
            val source = documentData.getJSONObject(index); val bytes = entries["documents/${source.getString("id")}"] ?: error("Missing document binary")
            val digest = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
            require(digest.equals(source.getString("sha256"), ignoreCase = true)) { "Document integrity check failed" }
            val preserved = secureFileStore.preserveOriginal(ByteArrayInputStream(bytes), source.getString("mimeType"), source.optString("originalFileName", "document"))
            restoredDocuments += DocumentEntity(preserved.id, source.optString("title"), source.optString("category", "OTHER"), source.optString("documentDate"), source.optString("notes"), source.optString("originalFileName", "document"), preserved.mimeType, preserved.byteCount, preserved.sha256, System.currentTimeMillis())
        }
        database.healthEventDao().deleteAll(); database.medicationDao().deleteAll(); database.profileDao().deleteAll(); database.documentDao().deleteAll(); database.reminderDao().deleteAll()
        data.optJSONObject("profile")?.let { p -> database.profileDao().upsert(ProfileEntity(name = p.optString("name"), dateOfBirth = p.optString("dateOfBirth"), bloodGroup = p.optString("bloodGroup"), allergies = p.optString("allergies"), conditions = p.optString("conditions"), emergencyContact = p.optString("emergencyContact"), updatedAtEpochMillis = System.currentTimeMillis())) }
        (data.optJSONArray("events") ?: JSONArray()).let { array -> for (i in 0 until array.length()) { val e = array.getJSONObject(i); database.healthEventDao().insert(HealthEventEntity(e.getString("id"), e.optString("title"), e.optString("details"), e.optString("kind", "OTHER"), if (e.isNull("effectiveAtEpochMillis")) null else e.optLong("effectiveAtEpochMillis"), e.optLong("createdAtEpochMillis"), status = e.optString("status", "ACTIVE"), severity = if (e.isNull("severity")) null else e.optInt("severity"))) } }
        (data.optJSONArray("medications") ?: JSONArray()).let { array -> for (i in 0 until array.length()) { val m = array.getJSONObject(i); database.medicationDao().insert(MedicationEntity(m.getString("id"), m.optString("name"), m.optString("genericName"), m.optString("strength"), m.optString("dose"), m.optString("unit"), m.optString("route"), m.optString("frequency"), m.optString("startDate"), m.optString("stopDate"), m.optString("status", "CURRENT"), m.optString("indication"), m.optString("physician"), m.optString("notes"), System.currentTimeMillis(), System.currentTimeMillis())) } }
        restoredDocuments.forEach { database.documentDao().insert(it) }
        (data.optJSONArray("reminders") ?: JSONArray()).let { array -> for (i in 0 until array.length()) { val r = array.getJSONObject(i); val reminder = ReminderEntity(r.getString("id"), r.optString("title"), r.optString("type", "CUSTOM"), r.optString("notes"), r.optLong("dueAtEpochMillis"), r.optString("recurrence", "ONCE"), r.optString("status", "SCHEDULED"), createdAtEpochMillis = System.currentTimeMillis(), updatedAtEpochMillis = System.currentTimeMillis()); database.reminderDao().insert(reminder); if (reminder.status == "SCHEDULED") reminderScheduler.schedule(reminder.id, reminder.dueAtEpochMillis, reminder.recurrence) } } }
    fun createPdfReport(context: Context, uri: Uri) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val lines = mutableListOf("Vexel Health Passport", "Appointment report · generated ${DateFormat.getDateTimeInstance().format(Date())}", "User-recorded data; not a diagnosis or medical advice.", "")
        profile.value?.let { lines += listOf("PROFILE", "Name: ${it.name}", "Allergies: ${it.allergies}", "Conditions: ${it.conditions}", "") }
        lines += "HEALTH EVENTS"; events.value.forEach { lines += "${it.kind} · ${it.title} · ${it.details}" }
        lines += "MEDICATIONS"; medications.value.forEach { lines += "${it.name} ${it.strength} · ${it.dose} · ${it.frequency}" }
        lines += "DOCUMENTS"; documents.value.forEach { lines += "${it.title} · ${it.category} · ${it.originalFileName}" }
        lines += "REMINDERS"; reminders.value.forEach { lines += "${it.title} · ${DateFormat.getDateTimeInstance().format(Date(it.dueAtEpochMillis))} · ${it.status}" }
        val pdf = PdfDocument(); val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; textSize = 11f }; var pageNo = 0; var page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, ++pageNo).create()); var y = 48f
        lines.forEach { raw -> raw.chunked(88).ifEmpty { listOf("") }.forEach { text -> if (y > 800f) { pdf.finishPage(page); page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, ++pageNo).create()); y = 48f }; paint.textSize = if (pageNo == 1 && y < 60f) 20f else 11f; page.canvas.drawText(text, 48f, y, paint); y += 18f } }
        pdf.finishPage(page); context.contentResolver.openOutputStream(uri)?.use { pdf.writeTo(it) }; pdf.close()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VexelHealthPassportApp(viewModel: PassportViewModel = hiltViewModel()) {
    val prefs by viewModel.settings.collectAsState()
    val profile by viewModel.profile.collectAsState()
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    VexelHealthPassportTheme(darkTheme = prefs.darkTheme) {
        if (!prefs.onboardingComplete) {
            OnboardingScreen(onComplete = viewModel::completeOnboarding)
        } else {
            LockGate(prefs, viewModel) {
                Scaffold(
                topBar = { TopAppBar(title = { Text("Vexel Health Passport") }) },
                bottomBar = { NavigationBar { destinations.forEachIndexed { index, destination ->
                    NavigationBarItem(index == selectedIndex, { selectedIndex = index }, icon = { Icon(destination.icon, destination.label) }, label = { Text(destination.label) })
                } } },
                ) { padding ->
                when (selectedIndex) {
                    0 -> HomeScreen(viewModel, profile, viewModel.medications.collectAsState().value, viewModel.events.collectAsState().value, Modifier.padding(padding))
                    1 -> TimelineScreen(viewModel, Modifier.padding(padding))
                    2 -> DocumentsScreen(viewModel, viewModel.documents.collectAsState().value, Modifier.padding(padding))
                    3 -> RemindersScreen(viewModel, viewModel.reminders.collectAsState().value, Modifier.padding(padding))
                    else -> ProfileScreen(viewModel, profile, Modifier.padding(padding))
                }
                }
            }
        }
    }
}

@Composable
private fun LockGate(prefs: pk.vexel.healthpassport.core.datastore.UserPreferences, vm: PassportViewModel, content: @Composable () -> Unit) {
    var unlocked by rememberSaveable(prefs.lockEnabled) { mutableStateOf(!prefs.lockEnabled) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, prefs.lockEnabled) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP && prefs.lockEnabled) unlocked = false }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    if (unlocked || !prefs.lockEnabled) content() else PinUnlockDialog(prefs, vm) { unlocked = true }
}

@Composable
private fun PinUnlockDialog(prefs: pk.vexel.healthpassport.core.datastore.UserPreferences, vm: PassportViewModel, onUnlocked: () -> Unit) {
    var pin by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }
    val activity = LocalContext.current as? FragmentActivity
    val canUseBiometric = activity != null && BiometricManager.from(activity).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
    val biometricPrompt = remember(activity) {
        activity?.let { host ->
            BiometricPrompt(host, ContextCompat.getMainExecutor(host), object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onUnlocked()
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { error = false }
            })
        }
    }
    val promptInfo = remember { BiometricPrompt.PromptInfo.Builder().setTitle("Unlock Vexel Health Passport").setSubtitle("Authenticate to view your private health information").setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL).build() }
    AlertDialog(onDismissRequest = {}, title = { Text("Unlock Vexel Health Passport") }, text = { OutlinedTextField(pin, { pin = it.filter(Char::isDigit).take(12); error = false }, label = { Text("PIN") }, isError = error, supportingText = { if (error) Text("Incorrect PIN") }) }, confirmButton = { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { if (canUseBiometric) TextButton({ biometricPrompt?.authenticate(promptInfo) }) { Text("Use device authentication") }; Button({ if (vm.verifyPin(pin, prefs)) onUnlocked() else error = true }) { Text("Unlock") } } })
}

@Composable private fun OnboardingScreen(onComplete: () -> Unit) {
    var acknowledged by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Vexel Health Passport", style = MaterialTheme.typography.headlineMedium)
        Text("Your health history, organized.", style = MaterialTheme.typography.titleMedium)
        Text("A private, offline-first place to organize your personal health information.")
        Text("This app is not a diagnostic tool and does not replace advice from a qualified healthcare professional.")
        TextButton(onClick = { acknowledged = !acknowledged }) { Text(if (acknowledged) "✓ I understand" else "I understand") }
        Button(onClick = onComplete, enabled = acknowledged, modifier = Modifier.fillMaxWidth()) { Text("Continue") }
    }
}

@Composable private fun HomeScreen(vm: PassportViewModel, profile: ProfileEntity?, medications: List<MedicationEntity>, events: List<HealthEventEntity>, modifier: Modifier) {
    var showSymptom by rememberSaveable { mutableStateOf(false) }
    var showMedication by rememberSaveable { mutableStateOf(false) }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (profile?.name.isNullOrBlank()) "Welcome" else "Welcome, ${profile?.name}", style = MaterialTheme.typography.headlineSmall)
        Text("Keep your records together and ready for your next appointment.")
        InformationCard("Privacy", "Stored on this device. No account required.")
        val trends = summarizeSymptoms(events.map { TrendEvent(it.title, it.kind, it.severity) })
        if (trends.totalEntries > 0) {
            Text("Recorded symptom summary", style = MaterialTheme.typography.titleMedium)
            Text("${trends.totalEntries} symptom entr${if (trends.totalEntries == 1) "y" else "ies"}; most frequent: ${trends.mostFrequentSymptom ?: "none"}")
            trends.averageRecordedSeverity?.let { Text("Average recorded severity: ${"%.1f".format(it)}/10") }
            Text("This is a summary of your entries, not a diagnosis.", style = MaterialTheme.typography.bodySmall)
        }
        if (medications.isNotEmpty()) {
            Text("Current medications", style = MaterialTheme.typography.titleMedium)
            medications.filter { it.status == "CURRENT" }.take(3).forEach { medication ->
                Text("• ${medication.name}${medication.strength.takeIf { it.isNotBlank() }?.let { " — $it" } ?: ""}")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button({ showSymptom = true }, Modifier.weight(1f)) { Text("Log symptom") }
            Button({ showMedication = true }, Modifier.weight(1f)) { Text("Add medication") }
        }
        if (showSymptom) CaptureDialog("SYMPTOM", "Log a symptom", { showSymptom = false }, vm::addEvent)
        if (showMedication) MedicationDialog({ showMedication = false }, vm::addMedication)
    }
}

@Composable private fun TimelineScreen(vm: PassportViewModel, modifier: Modifier) {
    val events by vm.events.collectAsState()
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<HealthEventEntity?>(null) }
    val visibleEvents = events.filter { event -> query.isBlank() || event.title.contains(query, ignoreCase = true) || event.details.contains(query, ignoreCase = true) }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Health timeline", style = MaterialTheme.typography.headlineSmall); TextButton({ showAdd = true }) { Text("Add") } }
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Search timeline") }, singleLine = true)
        if (events.isEmpty()) Text("Your health events will appear here.")
        else if (visibleEvents.isEmpty()) Text("No events match your search.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(visibleEvents, key = { it.id }) { event ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(event.title, style = MaterialTheme.typography.titleMedium); Text(event.kind.lowercase().replaceFirstChar { it.uppercase() }); if (event.details.isNotBlank()) Text(event.details); event.severity?.let { Text("Recorded severity: $it/10") }; Text(DateFormat.getDateInstance().format(Date(event.effectiveAtEpochMillis ?: event.createdAtEpochMillis))); Row { TextButton({ vm.archive(event) }) { Text("Archive") }; TextButton({ pendingDelete = event }) { Text("Delete") } } } }
        } }
    }
    if (showAdd) CaptureDialog("OTHER", "Add health event", { showAdd = false }, vm::addEvent)
    pendingDelete?.let { event ->
        AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("Delete event?") }, text = { Text("This removes the selected user-entered event from the timeline.") }, confirmButton = { Button({ vm.delete(event); pendingDelete = null }) { Text("Delete") } }, dismissButton = { TextButton({ pendingDelete = null }) { Text("Cancel") } })
    }
}

@Composable private fun CaptureScreen(vm: PassportViewModel, kind: String, heading: String, modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(heading, style = MaterialTheme.typography.headlineSmall); Text("Add information you entered or confirmed yourself."); Button({ vm.addEvent(kind, heading, "User-entered reminder or record") }) { Text("Add") } }
}

@Composable
private fun DocumentsScreen(vm: PassportViewModel, documents: List<DocumentEntity>, modifier: Modifier) {
    val context = LocalContext.current
    var showImport by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<DocumentEntity?>(null) }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Private document vault", style = MaterialTheme.typography.headlineSmall)
            TextButton({ showImport = true }) { Text("Import") }
        }
        Text("PDF, JPG, JPEG, and PNG files are copied into app-private storage.")
        if (documents.isEmpty()) Text("No documents imported yet.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(documents, key = { it.id }) { document ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(document.title, style = MaterialTheme.typography.titleMedium)
                    Text("${document.category} · ${document.mimeType} · ${document.byteCount / 1024} KB")
                    if (document.documentDate.isNotBlank()) Text("Document date: ${document.documentDate}")
                    if (document.notes.isNotBlank()) Text(document.notes)
                    Row { TextButton({ vm.openDocument(context, document) }) { Text("Open") }; TextButton({ pendingDelete = document }) { Text("Delete") } }
                } }
            }
        }
    }
    if (showImport) DocumentImportDialog(vm, context) { showImport = false }
    pendingDelete?.let { document ->
        AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("Delete document?") }, text = { Text("The private file and its metadata will be removed from this device.") }, confirmButton = { Button({ vm.deleteDocument(document); pendingDelete = null }) { Text("Delete") } }, dismissButton = { TextButton({ pendingDelete = null }) { Text("Cancel") } })
    }
}

@Composable
private fun RemindersScreen(vm: PassportViewModel, reminders: List<ReminderEntity>, modifier: Modifier) {
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<ReminderEntity?>(null) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Reminders", style = MaterialTheme.typography.headlineSmall); TextButton({ showAdd = true }) { Text("Add") } }
        Text("Reminders are user-created and do not determine medical intervals.")
        if (reminders.isEmpty()) Text("No reminders yet.") else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(reminders, key = { it.id }) { reminder ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                Text("${reminder.type} · ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(reminder.dueAtEpochMillis))} · ${reminder.status.lowercase()}")
                if (reminder.notes.isNotBlank()) Text(reminder.notes)
                Row { if (reminder.status == "SCHEDULED") TextButton({ vm.completeReminder(reminder) }) { Text("Complete") }; TextButton({ pendingDelete = reminder }) { Text("Delete") } }
            } }
        } }
    }
    if (showAdd) ReminderDialog(vm, { showAdd = false }) { if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }
    pendingDelete?.let { reminder -> AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("Delete reminder?") }, text = { Text("The scheduled notification will be cancelled.") }, confirmButton = { Button({ vm.deleteReminder(reminder); pendingDelete = null }) { Text("Delete") } }, dismissButton = { TextButton({ pendingDelete = null }) { Text("Cancel") } }) }
}

@Composable
private fun ReminderDialog(vm: PassportViewModel, onDismiss: () -> Unit, onScheduled: () -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("CUSTOM") }
    var dueText by rememberSaveable { mutableStateOf(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis() + 3_600_000))) }
    var notes by rememberSaveable { mutableStateOf("") }
    var recurrence by rememberSaveable { mutableStateOf("ONCE") }
    val dueAt = runCatching { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { isLenient = false }.parse(dueText)?.time }.getOrNull()
    val error = when { title.isBlank() -> "A title is required"; dueAt == null -> "Use yyyy-MM-dd HH:mm"; dueAt <= System.currentTimeMillis() -> "Choose a future time"; else -> "" }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add reminder") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(title, { title = it }, label = { Text("Title") }, isError = error.isNotBlank())
        OutlinedTextField(type, { type = it }, label = { Text("Type") })
        OutlinedTextField(dueText, { dueText = it }, label = { Text("Date and time") }, supportingText = { Text("yyyy-MM-dd HH:mm") }, isError = dueAt == null)
        OutlinedTextField(recurrence, { recurrence = it.uppercase(Locale.getDefault()).take(12) }, label = { Text("Recurrence: ONCE or DAILY") })
        OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") })
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
    } }, confirmButton = { Button(enabled = error.isBlank(), onClick = { vm.addReminder(title, type, notes, dueAt!!, if (recurrence == "DAILY") "DAILY" else "ONCE"); onScheduled(); onDismiss() }) { Text("Schedule") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}

@Composable
private fun DocumentImportDialog(vm: PassportViewModel, context: Context, onDismiss: () -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("OTHER") }
    var documentDate by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importDocument(context, uri, title, category, documentDate, notes)
        if (uri != null) onDismiss()
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Import private document") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Choose a PDF or image. The original is preserved privately; unsupported types are rejected.")
        OutlinedTextField(title, { title = it }, label = { Text("Title") })
        OutlinedTextField(category, { category = it }, label = { Text("Category") })
        OutlinedTextField(documentDate, { documentDate = it }, label = { Text("Document date (optional)") })
        OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") })
    } }, confirmButton = { Button({ launcher.launch(arrayOf("application/pdf", "image/jpeg", "image/png")) }) { Text("Choose file") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}

@Composable private fun ProfileScreen(vm: PassportViewModel, profile: ProfileEntity?, modifier: Modifier) {
    var name by remember(profile?.name) { mutableStateOf(profile?.name.orEmpty()) }; var allergies by remember(profile?.allergies) { mutableStateOf(profile?.allergies.orEmpty()) }; var conditions by remember(profile?.conditions) { mutableStateOf(profile?.conditions.orEmpty()) }; val prefs by vm.settings.collectAsState(); var showPinSetup by rememberSaveable { mutableStateOf(false) }
    var showDeleteAll by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) context.contentResolver.openOutputStream(uri)?.use { it.write(vm.exportJson().toByteArray(Charsets.UTF_8)) }
    }
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri -> if (uri != null) vm.createBackup(context, uri) }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) vm.restoreBackup(context, uri) }
    val reportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri -> if (uri != null) vm.createPdfReport(context, uri) }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("Personal profile", style = MaterialTheme.typography.headlineSmall); OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") }); OutlinedTextField(allergies, { allergies = it }, Modifier.fillMaxWidth(), label = { Text("Allergies") }); OutlinedTextField(conditions, { conditions = it }, Modifier.fillMaxWidth(), label = { Text("Diagnoses or conditions") }); Button({ vm.saveProfile(ProfileEntity(name = name, allergies = allergies, conditions = conditions, updatedAtEpochMillis = System.currentTimeMillis())) }) { Text("Save profile") }; TextButton({ exportLauncher.launch("vexel-health-export.json") }) { Text("Export my data (JSON)") }; TextButton({ backupLauncher.launch("vexel-health-backup.vexel") }) { Text("Create local backup") }; TextButton({ restoreLauncher.launch(arrayOf("application/zip", "application/octet-stream")) }) { Text("Restore backup") }; TextButton({ reportLauncher.launch("vexel-health-report.pdf") }) { Text("Create PDF report") }; TextButton({ vm.setDarkTheme(!prefs.darkTheme) }) { Text(if (prefs.darkTheme) "Use light theme" else "Use dark theme") }; TextButton({ if (prefs.lockEnabled) vm.disablePin() else showPinSetup = true }) { Text(if (prefs.lockEnabled) "Disable PIN lock" else "Set up PIN lock") }; TextButton({ showDeleteAll = true }) { Text("Delete all local data", color = MaterialTheme.colorScheme.error) } }
    if (showPinSetup) PinSetupDialog(vm) { showPinSetup = false }
    if (showDeleteAll) AlertDialog(onDismissRequest = { showDeleteAll = false }, title = { Text("Delete all data?") }, text = { Text("This permanently removes your profile, events, medications, private documents, and security settings from this device. This cannot be undone.") }, confirmButton = { Button({ vm.deleteAllData(); showDeleteAll = false }) { Text("Delete everything") } }, dismissButton = { TextButton({ showDeleteAll = false }) { Text("Cancel") } })
}

@Composable
private fun PinSetupDialog(vm: PassportViewModel, onDismiss: () -> Unit) {
    var pin by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Set up PIN lock") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Use 4–12 digits. The PIN is never stored as plaintext.")
        OutlinedTextField(pin, { pin = it.filter(Char::isDigit).take(12) }, label = { Text("PIN") }, isError = error.isNotBlank())
        OutlinedTextField(confirmation, { confirmation = it.filter(Char::isDigit).take(12) }, label = { Text("Confirm PIN") }, isError = error.isNotBlank(), supportingText = { if (error.isNotBlank()) Text(error) })
    } }, confirmButton = { Button({ if (pin != confirmation) error = "PINs do not match" else if (pin.length !in 4..12) error = "Use 4–12 digits" else if (vm.savePin(pin, confirmation)) onDismiss() }) { Text("Enable") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}

@Composable private fun CaptureDialog(kind: String, heading: String, onDismiss: () -> Unit, onSave: (String, String, String, Int?) -> Unit) {
    var title by remember { mutableStateOf("") }; var details by remember { mutableStateOf("") }; var severityText by remember { mutableStateOf("") }
    val severity = severityText.toIntOrNull()
    val errors = SymptomDraft(title, if (kind == "SYMPTOM") severity else null, details).validationErrors()
    AlertDialog(onDismissRequest = onDismiss, title = { Text(heading) }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(title, { title = it }, label = { Text(if (kind == "SYMPTOM") "Symptom" else "Title") }, isError = errors.containsKey("name"))
        if (kind == "SYMPTOM") OutlinedTextField(severityText, { severityText = it.filter(Char::isDigit) }, label = { Text("Severity (0–10, optional)") }, isError = errors.containsKey("severity"), supportingText = { errors["severity"]?.let { Text(it) } })
        OutlinedTextField(details, { details = it }, label = { Text("Notes (optional)") }, isError = errors.containsKey("notes"), supportingText = { errors["notes"]?.let { Text(it) } })
    } }, confirmButton = { Button({ if (errors.isEmpty()) { onSave(kind, title.trim(), details.trim(), if (kind == "SYMPTOM") severity else null); onDismiss() } }) { Text("Save") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}

@Composable
private fun MedicationDialog(onDismiss: () -> Unit, onSave: (MedicationDraft) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var strength by rememberSaveable { mutableStateOf("") }
    var dose by rememberSaveable { mutableStateOf("") }
    var route by rememberSaveable { mutableStateOf("") }
    var frequency by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    val draft = MedicationDraft(name, strength, dose, route, frequency, notes)
    val errors = draft.validationErrors()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add medication") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Medication name") }, isError = errors.containsKey("name"))
            OutlinedTextField(strength, { strength = it }, label = { Text("Strength") })
            OutlinedTextField(dose, { dose = it }, label = { Text("Dose") })
            OutlinedTextField(route, { route = it }, label = { Text("Route (optional)") })
            OutlinedTextField(frequency, { frequency = it }, label = { Text("Frequency (optional)") })
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") }, isError = errors.containsKey("notes"))
            errors.values.firstOrNull()?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        } },
        confirmButton = { Button(enabled = errors.isEmpty(), onClick = { onSave(draft); onDismiss() }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
