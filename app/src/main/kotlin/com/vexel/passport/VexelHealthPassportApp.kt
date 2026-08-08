package com.vexel.passport

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.room.withTransaction
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.vexel.passport.core.ui.DateTimeField
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.vexel.passport.core.database.HealthDatabase
import com.vexel.passport.core.database.HealthEventEntity
import com.vexel.passport.core.database.MedicationEntity
import com.vexel.passport.core.database.MedicationChangeEntity
import com.vexel.passport.core.database.DocumentEntity
import com.vexel.passport.core.database.ReminderEntity
import com.vexel.passport.core.database.ProfileEntity
import com.vexel.passport.core.datastore.PreferencesStore
import com.vexel.passport.core.files.SecureFileStore
import com.vexel.passport.core.notifications.ReminderScheduler
import com.vexel.passport.core.designsystem.ActionRow
import com.vexel.passport.core.designsystem.InformationCard
import com.vexel.passport.core.designsystem.EmptyState
import com.vexel.passport.core.designsystem.SectionHeader
import com.vexel.passport.core.designsystem.StatusPill
import com.vexel.passport.core.designsystem.VexelHealthPassportTheme
import com.vexel.passport.core.model.SymptomDraft
import com.vexel.passport.core.model.MedicationDraft
import com.vexel.passport.core.model.validationErrors
import com.vexel.passport.core.model.TrendEvent
import com.vexel.passport.core.model.summarizeSymptoms
import com.vexel.passport.core.model.EpisodeEvent
import com.vexel.passport.core.model.summarizeSymptomEpisodes
import com.vexel.passport.core.model.remapRestoredSymptomReferences
import com.vexel.passport.core.model.ExportFormat
import com.vexel.passport.core.model.exportShareDescriptor
import com.vexel.passport.core.model.hasSelectedReportSection
import com.vexel.passport.core.model.isWithinDateScope
import com.vexel.passport.core.model.validateDateScope
import com.vexel.passport.core.security.PinVerifier
import com.vexel.passport.core.security.PinMaterialCipher
import com.vexel.passport.core.security.BackupCrypto

private data class Destination(val label: String, val icon: ImageVector)
private val destinations = listOf(
    Destination("Home", Icons.Outlined.Home), Destination("Records", Icons.Outlined.Event),
    Destination("Plan", Icons.Outlined.Schedule), Destination("Vault", Icons.Outlined.Folder),
    Destination("Profile", Icons.Outlined.Person),
)
internal val primaryDestinationLabels: List<String> = destinations.map { it.label }

private fun sha256Hex(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
    .digest(bytes).joinToString("") { "%02x".format(it) }

private fun shareContentUri(context: Context, uri: Uri, mimeType: String, chooserTitle: String) {
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

@HiltViewModel
class PassportViewModel @Inject constructor(
    private val database: HealthDatabase,
    private val preferences: PreferencesStore,
    private val pinMaterialCipher: PinMaterialCipher,
    private val secureFileStore: SecureFileStore,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    init {
        viewModelScope.launch { reminderScheduler.reconcile() }
    }
    private val pinVerifier = PinVerifier()
    val events = database.healthEventDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val profile = database.profileDao().observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val medications = database.medicationDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val medicationChanges = database.medicationChangeDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val documents = database.documentDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val reminders = database.reminderDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val settings = preferences.preferences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.vexel.passport.core.datastore.UserPreferences())

    fun completeOnboarding() = viewModelScope.launch { preferences.setOnboardingComplete(true) }
    fun setDarkTheme(value: Boolean) = viewModelScope.launch { preferences.setDarkTheme(value) }
    fun saveProfile(p: ProfileEntity) = viewModelScope.launch { database.profileDao().upsert(p) }
    fun addEvent(kind: String, title: String, details: String, severity: Int? = null) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        database.healthEventDao().insert(HealthEventEntity(UUID.randomUUID().toString(), title, details, kind, now, now, now, "ACTIVE", severity))
    }
    fun addSymptom(context: Context, draft: SymptomDraft, imageUri: Uri? = null) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { isLenient = false }
        val start = runCatching { parser.parse(draft.startAtText)?.time }.getOrNull() ?: now
        val end = runCatching { parser.parse(draft.endAtText)?.time }.getOrNull()
        val imageId = imageUri?.let { uri ->
            val mime = context.contentResolver.getType(uri) ?: return@let null
            if (mime !in setOf("image/jpeg", "image/png")) null else context.contentResolver.openInputStream(uri)?.use { input ->
                val preserved = secureFileStore.preserveOriginal(input, mime, "symptom-image")
                database.documentDao().insert(DocumentEntity(preserved.id, "Symptom image", "SYMPTOM_IMAGE", "", "Attached to symptom", "symptom-image", preserved.mimeType, preserved.byteCount, preserved.sha256, now))
                preserved.id
            }
        }
        database.healthEventDao().insert(HealthEventEntity(UUID.randomUUID().toString(), draft.name.trim(), draft.notes.trim(), "SYMPTOM", start, now, now, "ACTIVE", draft.severity, false, draft.durationMinutes, start, end, draft.ongoing, draft.bodyLocation.trim(), draft.associatedSymptoms.trim(), draft.possibleTrigger.trim(), draft.relatedMedication.trim(), imageId, draft.episodeId.trim().ifBlank { null }))
    }
    fun archive(event: HealthEventEntity) = viewModelScope.launch { database.healthEventDao().archive(event.id, System.currentTimeMillis()) }
    fun delete(event: HealthEventEntity) = viewModelScope.launch { event.imageAttachmentId?.let { secureFileStore.delete(it); database.documentDao().delete(it) }; database.healthEventDao().delete(event.id) }
    fun addMedication(draft: MedicationDraft) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val medicationId = UUID.randomUUID().toString()
        database.medicationDao().insert(MedicationEntity(medicationId, draft.name.trim(), genericName = draft.genericName.trim(), strength = draft.strength.trim(), dose = draft.dose.trim(), unit = draft.unit.trim(), route = draft.route.trim(), frequency = draft.frequency.trim(), startDate = draft.startDate.trim(), stopDate = draft.stopDate.trim(), status = draft.status, indication = draft.indication.trim(), physician = draft.physician.trim(), notes = draft.notes.trim(), createdAtEpochMillis = now, updatedAtEpochMillis = now))
        database.medicationChangeDao().insert(MedicationChangeEntity(UUID.randomUUID().toString(), medicationId, now, "STARTED", draft.strength.trim(), draft.dose.trim(), draft.unit.trim(), draft.frequency.trim(), draft.status, draft.notes.trim()))
        database.healthEventDao().insert(HealthEventEntity(UUID.randomUUID().toString(), draft.name.trim(), listOf(draft.strength, draft.dose, draft.unit, draft.frequency, draft.status.lowercase()).filter { it.isNotBlank() }.joinToString(" · "), "MEDICATION", now, now, now, "ACTIVE"))
    }
    fun recordMedicationChange(medication: MedicationEntity, strength: String, dose: String, unit: String, frequency: String, status: String, notes: String) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        database.medicationDao().update(medication.copy(strength = strength.trim(), dose = dose.trim(), unit = unit.trim(), frequency = frequency.trim(), status = status, notes = notes.trim(), updatedAtEpochMillis = now, stopDate = if (status == "STOPPED") SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now)) else medication.stopDate))
        database.medicationChangeDao().insert(MedicationChangeEntity(UUID.randomUUID().toString(), medication.id, now, if (status == "STOPPED") "STOPPED" else if (medication.status == "STOPPED" && status == "CURRENT") "RESTARTED" else "DOSE_CHANGED", strength.trim(), dose.trim(), unit.trim(), frequency.trim(), status, notes.trim()))
    }
    fun savePin(pin: String, confirmation: String): Boolean {
        if (pin != confirmation || pin.length !in 4..12 || pin.any { !it.isDigit() }) return false
        val record = pinVerifier.create(pin.toCharArray())
        viewModelScope.launch { preferences.setPinMaterial(pinMaterialCipher.encrypt(record)) }
        return true
    }
    fun verifyPin(pin: String, prefs: com.vexel.passport.core.datastore.UserPreferences): Boolean {
        if (!prefs.lockEnabled) return true
        return runCatching { pinVerifier.matches(pin.toCharArray(), pinMaterialCipher.decrypt(prefs.pinMaterial)) }.getOrDefault(false)
    }
    fun disablePin() = viewModelScope.launch { preferences.clearPinMaterial() }
    fun setLockTimeoutMinutes(minutes: Int) = viewModelScope.launch { preferences.setLockTimeoutMinutes(minutes) }
    fun deleteAllData() = viewModelScope.launch {
        database.healthEventDao().deleteAll()
        database.medicationDao().deleteAll()
        database.medicationChangeDao().deleteAll()
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
    fun updateReminder(reminder: ReminderEntity, title: String, type: String, notes: String, dueAtEpochMillis: Long, recurrence: String) = viewModelScope.launch {
        reminderScheduler.cancel(reminder.id)
        val updated = reminder.copy(title = title.trim(), type = type.trim().ifBlank { "CUSTOM" }, notes = notes.trim(), dueAtEpochMillis = dueAtEpochMillis, recurrence = recurrence, status = "SCHEDULED", snoozeUntilEpochMillis = null, updatedAtEpochMillis = System.currentTimeMillis())
        database.reminderDao().update(updated)
        reminderScheduler.schedule(updated.id, updated.dueAtEpochMillis, updated.recurrence)
    }
    fun snoozeReminder(reminder: ReminderEntity) = viewModelScope.launch {
        val dueAt = System.currentTimeMillis() + 60 * 60 * 1000L
        database.reminderDao().reschedule(reminder.id, dueAt, System.currentTimeMillis())
        reminderScheduler.schedule(reminder.id, dueAt, reminder.recurrence)
    }
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
    fun updateDocument(document: DocumentEntity, title: String, category: String, documentDate: String, notes: String) = viewModelScope.launch {
        database.documentDao().update(document.copy(title = title.trim().ifBlank { document.title }, category = category.trim().ifBlank { document.category }, documentDate = documentDate.trim(), notes = notes.trim()))
    }
    fun replaceDocument(context: Context, document: DocumentEntity, uri: Uri) = viewModelScope.launch {
        val mimeType = context.contentResolver.getType(uri) ?: return@launch
        val displayName = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else document.originalFileName
        } ?: document.originalFileName
        val replaced = context.contentResolver.openInputStream(uri)?.use { input -> secureFileStore.replaceOriginal(document.id, input, mimeType, displayName) } ?: return@launch
        database.documentDao().update(document.copy(originalFileName = displayName, mimeType = replaced.mimeType, byteCount = replaced.byteCount, sha256 = replaced.sha256))
    }
    fun openDocument(context: Context, document: DocumentEntity) = viewModelScope.launch {
        val file = secureFileStore.copyToShareCache(context, document.id, document.originalFileName)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, document.mimeType); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "Open document").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    fun shareDocument(context: Context, document: DocumentEntity) = viewModelScope.launch {
        val file = secureFileStore.copyToShareCache(context, document.id, document.originalFileName)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        shareContentUri(context, uri, document.mimeType, "Share document")
    }
    fun exportJson(fromEpochMillis: Long? = null, toEpochMillis: Long? = null): String {
        fun inRange(epoch: Long?): Boolean = isWithinDateScope(epoch, fromEpochMillis, toEpochMillis)
        val root = JSONObject().put("formatVersion", 1).put("generatedAtEpochMillis", System.currentTimeMillis())
        profile.value?.let { p -> root.put("profile", JSONObject().put("name", p.name).put("dateOfBirth", p.dateOfBirth).put("bloodGroup", p.bloodGroup).put("allergies", p.allergies).put("conditions", p.conditions).put("emergencyContact", p.emergencyContact)) }
        root.put("events", JSONArray(events.value.filter { inRange(it.effectiveAtEpochMillis ?: it.createdAtEpochMillis) }.map { e -> JSONObject().put("id", e.id).put("title", e.title).put("details", e.details).put("kind", e.kind).put("effectiveAtEpochMillis", e.effectiveAtEpochMillis).put("createdAtEpochMillis", e.createdAtEpochMillis).put("status", e.status).put("severity", e.severity).put("durationMinutes", e.durationMinutes).put("startAtEpochMillis", e.startAtEpochMillis).put("endAtEpochMillis", e.endAtEpochMillis).put("ongoing", e.ongoing).put("bodyLocation", e.bodyLocation).put("associatedSymptoms", e.associatedSymptoms).put("possibleTrigger", e.possibleTrigger).put("relatedMedication", e.relatedMedication).put("imageAttachmentId", e.imageAttachmentId).put("episodeId", e.episodeId) }))
        root.put("medications", JSONArray(medications.value.filter { inRange(runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it.startDate)?.time }.getOrNull() ?: it.createdAtEpochMillis) }.map { m -> JSONObject().put("id", m.id).put("name", m.name).put("genericName", m.genericName).put("strength", m.strength).put("dose", m.dose).put("unit", m.unit).put("route", m.route).put("frequency", m.frequency).put("startDate", m.startDate).put("stopDate", m.stopDate).put("status", m.status).put("indication", m.indication).put("physician", m.physician).put("notes", m.notes) }))
        root.put("medicationChanges", JSONArray(medicationChanges.value.filter { inRange(it.changedAtEpochMillis) }.map { c -> JSONObject().put("id", c.id).put("medicationId", c.medicationId).put("changedAtEpochMillis", c.changedAtEpochMillis).put("changeType", c.changeType).put("strength", c.strength).put("dose", c.dose).put("unit", c.unit).put("frequency", c.frequency).put("status", c.status).put("notes", c.notes) }))
        root.put("documents", JSONArray(documents.value.filter { inRange(runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it.documentDate)?.time }.getOrNull() ?: it.createdAtEpochMillis) }.map { d -> JSONObject().put("id", d.id).put("title", d.title).put("category", d.category).put("documentDate", d.documentDate).put("notes", d.notes).put("originalFileName", d.originalFileName).put("mimeType", d.mimeType).put("byteCount", d.byteCount).put("sha256", d.sha256) }))
        root.put("reminders", JSONArray(reminders.value.filter { inRange(it.dueAtEpochMillis) }.map { r -> JSONObject().put("id", r.id).put("title", r.title).put("type", r.type).put("notes", r.notes).put("dueAtEpochMillis", r.dueAtEpochMillis).put("recurrence", r.recurrence).put("status", r.status) }))
        return root.toString(2)
    }
    fun exportHumanReadable(fromEpochMillis: Long? = null, toEpochMillis: Long? = null): String = buildString {
        fun inRange(epoch: Long?): Boolean = isWithinDateScope(epoch, fromEpochMillis, toEpochMillis)
        appendLine("Vexel Health Passport")
        appendLine("User-recorded information · generated ${DateFormat.getDateTimeInstance().format(Date())}")
        appendLine("This export is not a diagnosis or medical advice.")
        appendLine()
        profile.value?.let {
            appendLine("PROFILE")
            appendLine("Name: ${it.name}")
            appendLine("Allergies: ${it.allergies}")
            appendLine("Conditions: ${it.conditions}")
            appendLine()
        }
        appendLine("HEALTH EVENTS")
        events.value.filter { inRange(it.effectiveAtEpochMillis ?: it.createdAtEpochMillis) }.forEach { appendLine("${it.kind} · ${it.title} · ${it.details}") }
        appendLine()
        appendLine("MEDICATIONS")
        medications.value.filter { inRange(runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it.startDate)?.time }.getOrNull() ?: it.createdAtEpochMillis) }.forEach { appendLine("${it.name} · ${it.genericName} · ${it.strength} · ${it.dose} ${it.unit} · ${it.frequency} · ${it.status} · ${it.startDate}–${it.stopDate}") }
        appendLine("MEDICATION CHANGES")
        medicationChanges.value.filter { inRange(it.changedAtEpochMillis) }.forEach { appendLine("${it.changeType} · ${DateFormat.getDateTimeInstance().format(Date(it.changedAtEpochMillis))} · ${it.strength} · ${it.dose} ${it.unit} · ${it.frequency} · ${it.status} · ${it.notes}") }
        appendLine()
        appendLine("DOCUMENTS")
        documents.value.filter { inRange(runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it.documentDate)?.time }.getOrNull() ?: it.createdAtEpochMillis) }.forEach { appendLine("${it.title} · ${it.category} · ${it.originalFileName} · SHA-256 ${it.sha256}") }
        appendLine()
        appendLine("REMINDERS")
        reminders.value.filter { inRange(it.dueAtEpochMillis) }.forEach { appendLine("${it.title} · ${it.status} · ${DateFormat.getDateTimeInstance().format(Date(it.dueAtEpochMillis))}") }
    }
    fun createBackup(context: Context, uri: Uri, password: String) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val zipBytes = ByteArrayOutputStream().use { output ->
            ZipOutputStream(output).use { zip ->
                val data = exportJson().toByteArray(Charsets.UTF_8)
                zip.putNextEntry(ZipEntry("data.json")); zip.write(data); zip.closeEntry()
                val manifest = JSONObject()
                    .put("formatVersion", 1)
                    .put("createdAtEpochMillis", System.currentTimeMillis())
                    .put("dataSha256", sha256Hex(data))
                    .put("documentCount", documents.value.size)
                    .toString()
                    .toByteArray(Charsets.UTF_8)
                zip.putNextEntry(ZipEntry("manifest.json")); zip.write(manifest); zip.closeEntry()
                documents.value.forEach { document ->
                    zip.putNextEntry(ZipEntry("documents/${document.id}")); secureFileStore.open(document.id).use { it.copyTo(zip) }; zip.closeEntry()
                }
            }
            output.toByteArray()
        }
        context.contentResolver.openOutputStream(uri)?.use { it.write(BackupCrypto.encrypt(zipBytes, password.toCharArray())) }
    }
    fun restoreBackup(context: Context, uri: Uri, password: String) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val sourceBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: error("Unable to read backup")
        val backupBytes = if (BackupCrypto.isEncrypted(sourceBytes)) BackupCrypto.decrypt(sourceBytes, password.toCharArray()) else sourceBytes
        val entries = linkedMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(backupBytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                require(!entry.isDirectory && (entry.name == "data.json" || entry.name == "manifest.json" || entry.name.startsWith("documents/"))) { "Unsupported backup entry" }
                entries[entry.name] = zip.readBytes()
                entry = zip.nextEntry
            }
        }
        val dataBytes = entries["data.json"] ?: error("Missing backup data")
        entries["manifest.json"]?.let { manifestBytes ->
            val manifest = JSONObject(String(manifestBytes, Charsets.UTF_8))
            require(manifest.optInt("formatVersion", -1) == 1) { "Unsupported backup version" }
            require(manifest.optString("dataSha256").equals(sha256Hex(dataBytes), ignoreCase = true)) { "Backup integrity check failed" }
        }
        val data = JSONObject(String(dataBytes, Charsets.UTF_8))
        require(data.optInt("formatVersion", -1) == 1) { "Unsupported backup version" }
        val restoredDocuments = mutableListOf<DocumentEntity>()
        val restoredDocumentIds = mutableMapOf<String, String>()
        val documentData = data.optJSONArray("documents") ?: JSONArray()
        for (index in 0 until documentData.length()) {
            val source = documentData.getJSONObject(index); val bytes = entries["documents/${source.getString("id")}"] ?: error("Missing document binary")
            val digest = sha256Hex(bytes)
            require(digest.equals(source.getString("sha256"), ignoreCase = true)) { "Document integrity check failed" }
            val preserved = secureFileStore.preserveOriginal(ByteArrayInputStream(bytes), source.getString("mimeType"), source.optString("originalFileName", "document"))
            restoredDocumentIds[source.getString("id")] = preserved.id
            restoredDocuments += DocumentEntity(preserved.id, source.optString("title"), source.optString("category", "OTHER"), source.optString("documentDate"), source.optString("notes"), source.optString("originalFileName", "document"), preserved.mimeType, preserved.byteCount, preserved.sha256, System.currentTimeMillis())
        }
        val scheduledReminders = mutableListOf<ReminderEntity>()
        try {
            database.withTransaction {
                database.healthEventDao().deleteAll(); database.medicationDao().deleteAll(); database.medicationChangeDao().deleteAll(); database.profileDao().deleteAll(); database.documentDao().deleteAll(); database.reminderDao().deleteAll()
                data.optJSONObject("profile")?.let { p -> database.profileDao().upsert(ProfileEntity(name = p.optString("name"), dateOfBirth = p.optString("dateOfBirth"), bloodGroup = p.optString("bloodGroup"), allergies = p.optString("allergies"), conditions = p.optString("conditions"), emergencyContact = p.optString("emergencyContact"), updatedAtEpochMillis = System.currentTimeMillis())) }
                (data.optJSONArray("events") ?: JSONArray()).let { array -> for (i in 0 until array.length()) { val e = array.getJSONObject(i); val references = remapRestoredSymptomReferences(e.optString("imageAttachmentId"), e.optString("episodeId"), restoredDocumentIds); database.healthEventDao().insert(HealthEventEntity(e.getString("id"), e.optString("title"), e.optString("details"), e.optString("kind", "OTHER"), if (e.isNull("effectiveAtEpochMillis")) null else e.optLong("effectiveAtEpochMillis"), e.optLong("createdAtEpochMillis"), status = e.optString("status", "ACTIVE"), severity = if (e.isNull("severity")) null else e.optInt("severity"), durationMinutes = if (e.isNull("durationMinutes")) null else e.optInt("durationMinutes"), startAtEpochMillis = if (e.isNull("startAtEpochMillis")) null else e.optLong("startAtEpochMillis"), endAtEpochMillis = if (e.isNull("endAtEpochMillis")) null else e.optLong("endAtEpochMillis"), ongoing = e.optBoolean("ongoing"), bodyLocation = e.optString("bodyLocation"), associatedSymptoms = e.optString("associatedSymptoms"), possibleTrigger = e.optString("possibleTrigger"), relatedMedication = e.optString("relatedMedication"), imageAttachmentId = references.imageAttachmentId, episodeId = references.episodeId)) } }
                (data.optJSONArray("medications") ?: JSONArray()).let { array -> for (i in 0 until array.length()) { val m = array.getJSONObject(i); database.medicationDao().insert(MedicationEntity(m.getString("id"), m.optString("name"), m.optString("genericName"), m.optString("strength"), m.optString("dose"), m.optString("unit"), m.optString("route"), m.optString("frequency"), m.optString("startDate"), m.optString("stopDate"), m.optString("status", "CURRENT"), m.optString("indication"), m.optString("physician"), m.optString("notes"), System.currentTimeMillis(), System.currentTimeMillis())) } }
                (data.optJSONArray("medicationChanges") ?: JSONArray()).let { array -> for (i in 0 until array.length()) { val c = array.getJSONObject(i); database.medicationChangeDao().insert(MedicationChangeEntity(c.getString("id"), c.getString("medicationId"), c.optLong("changedAtEpochMillis"), c.optString("changeType", "DOSE_CHANGED"), c.optString("strength"), c.optString("dose"), c.optString("unit"), c.optString("frequency"), c.optString("status", "CURRENT"), c.optString("notes"))) } }
                restoredDocuments.forEach { database.documentDao().insert(it) }
                val reminderData = data.optJSONArray("reminders") ?: JSONArray()
                for (i in 0 until reminderData.length()) {
                    val r = reminderData.getJSONObject(i)
                    val reminder = ReminderEntity(
                        id = r.getString("id"),
                        title = r.optString("title"),
                        type = r.optString("type", "CUSTOM"),
                        notes = r.optString("notes"),
                        dueAtEpochMillis = r.optLong("dueAtEpochMillis"),
                        recurrence = r.optString("recurrence", "ONCE"),
                        status = r.optString("status", "SCHEDULED"),
                        createdAtEpochMillis = System.currentTimeMillis(),
                        updatedAtEpochMillis = System.currentTimeMillis(),
                    )
                    database.reminderDao().insert(reminder)
                    if (reminder.status == "SCHEDULED") scheduledReminders += reminder
                }
            }
            reminders.value.forEach { reminderScheduler.cancel(it.id) }
            scheduledReminders.forEach { reminderScheduler.schedule(it.id, it.dueAtEpochMillis, it.recurrence) }
        } catch (error: Throwable) {
            restoredDocuments.forEach { secureFileStore.delete(it.id) }
            throw error
        }
    }
    fun createPdfReport(context: Context, uri: Uri, includeProfile: Boolean = true, includeEvents: Boolean = true, includeMedications: Boolean = true, includeDocuments: Boolean = true, includeReminders: Boolean = true, fromEpochMillis: Long? = null, toEpochMillis: Long? = null) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        fun inRange(epoch: Long?): Boolean = epoch == null || ((fromEpochMillis == null || epoch >= fromEpochMillis) && (toEpochMillis == null || epoch <= toEpochMillis))
        val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        fun dateTextInRange(value: String, fallback: Long): Boolean = value.isBlank() || inRange(runCatching { dateParser.parse(value)?.time }.getOrNull() ?: fallback)
        val rangeLabel = if (fromEpochMillis == null && toEpochMillis == null) "All dates" else "${fromEpochMillis?.let { DateFormat.getDateInstance().format(Date(it)) } ?: "Start"} – ${toEpochMillis?.let { DateFormat.getDateInstance().format(Date(it)) } ?: "End"}"
        val lines = mutableListOf("Vexel Health Passport", "Appointment report · generated ${DateFormat.getDateTimeInstance().format(Date())}", "Selected range: $rangeLabel", "User-recorded data; not a diagnosis or medical advice.", "")
        if (includeProfile) profile.value?.let { lines += listOf("PROFILE", "Name: ${it.name}", "Allergies: ${it.allergies}", "Conditions: ${it.conditions}", "") }
        if (includeEvents) { lines += "HEALTH EVENTS"; events.value.filter { inRange(it.effectiveAtEpochMillis ?: it.createdAtEpochMillis) }.forEach { lines += "${it.kind} · ${it.title} · ${it.details}" } }
        if (includeMedications) { lines += "MEDICATIONS"; medications.value.filter { dateTextInRange(it.startDate, it.createdAtEpochMillis) }.forEach { lines += "${it.name} ${it.strength} · ${it.dose} · ${it.frequency}" } }
        if (includeDocuments) { lines += "DOCUMENTS"; documents.value.filter { dateTextInRange(it.documentDate, it.createdAtEpochMillis) }.forEach { lines += "${it.title} · ${it.category} · ${it.originalFileName}" } }
        if (includeReminders) { lines += "REMINDERS"; reminders.value.filter { inRange(it.dueAtEpochMillis) }.forEach { lines += "${it.title} · ${DateFormat.getDateTimeInstance().format(Date(it.dueAtEpochMillis))} · ${it.status}" } }
        val pdf = PdfDocument(); val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; textSize = 11f }; var pageNo = 0; var page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, ++pageNo).create()); var y = 48f
        fun finishPage() { paint.textSize = 9f; page.canvas.drawText("Vexel Health Passport · Page $pageNo", 48f, 824f, paint); pdf.finishPage(page) }
        lines.forEach { raw -> raw.chunked(88).ifEmpty { listOf("") }.forEach { text -> if (y > 790f) { finishPage(); page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, ++pageNo).create()); y = 48f }; paint.textSize = if (pageNo == 1 && y < 70f) 20f else 11f; page.canvas.drawText(text, 48f, y, paint); y += 18f } }
        finishPage(); context.contentResolver.openOutputStream(uri)?.use { pdf.writeTo(it) }; pdf.close()
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
                topBar = { TopAppBar(title = { Text(destinations[selectedIndex].label) }) },
                bottomBar = { NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) { destinations.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = index == selectedIndex,
                        onClick = { selectedIndex = index },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                    )
                } } },
                ) { padding ->
                when (selectedIndex) {
                    0 -> HomeScreen(viewModel, profile, viewModel.medications.collectAsState().value, viewModel.events.collectAsState().value, Modifier.padding(padding))
                    1 -> TimelineScreen(viewModel, Modifier.padding(padding))
                    2 -> RemindersScreen(viewModel, viewModel.reminders.collectAsState().value, Modifier.padding(padding))
                    3 -> DocumentsScreen(viewModel, viewModel.documents.collectAsState().value, Modifier.padding(padding))
                    else -> ProfileScreen(viewModel, profile, Modifier.padding(padding))
                }
                }
            }
        }
    }
}

@Composable
private fun LockGate(prefs: com.vexel.passport.core.datastore.UserPreferences, vm: PassportViewModel, content: @Composable () -> Unit) {
    var unlocked by rememberSaveable(prefs.lockEnabled) { mutableStateOf(!prefs.lockEnabled) }
    var unlockedAt by rememberSaveable(prefs.lockEnabled) { mutableStateOf<Long?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, prefs.lockEnabled) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP && prefs.lockEnabled) { unlocked = false; unlockedAt = null } }
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
    if (unlocked || !prefs.lockEnabled) content() else PinUnlockDialog(prefs, vm) { unlocked = true; unlockedAt = System.currentTimeMillis() }
}

@Composable
private fun PinUnlockDialog(prefs: com.vexel.passport.core.datastore.UserPreferences, vm: PassportViewModel, onUnlocked: () -> Unit) {
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
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Step 1 of 1", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text("Welcome to Vexel", style = MaterialTheme.typography.headlineLarge)
        Text("Your health history, organized.", style = MaterialTheme.typography.titleLarge)
        Card(Modifier.fillMaxWidth(), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("A calm, offline-first place to organize your personal health information.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Your entries are user-recorded. This app does not diagnose or replace advice from a qualified healthcare professional.", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = acknowledged, onCheckedChange = { acknowledged = it })
            Text("I understand and want to continue", Modifier.clickable { acknowledged = !acknowledged })
        }
        Button(onClick = onComplete, enabled = acknowledged, modifier = Modifier.fillMaxWidth()) { Text("Continue") }
    }
}

@Composable private fun HomeScreen(vm: PassportViewModel, profile: ProfileEntity?, medications: List<MedicationEntity>, events: List<HealthEventEntity>, modifier: Modifier) {
    var showSymptom by rememberSaveable { mutableStateOf(false) }
    var showMedication by rememberSaveable { mutableStateOf(false) }
    var changeMedication by remember { mutableStateOf<MedicationEntity?>(null) }
    val context = LocalContext.current
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 24.dp)) {
        item {
            Text(if (profile?.name.isNullOrBlank()) "Welcome" else "Welcome, ${profile?.name}", style = MaterialTheme.typography.headlineSmall)
            Text("Your health history, organized.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
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
    if (showSymptom) CaptureDialog("SYMPTOM", "Log a symptom", { showSymptom = false }, vm::addEvent) { draft, imageUri -> vm.addSymptom(context, draft, imageUri) }
    if (showMedication) MedicationDialog({ showMedication = false }, vm::addMedication)
    changeMedication?.let { medication -> MedicationChangeDialog(medication, { changeMedication = null }) { strength, dose, unit, frequency, status, notes -> vm.recordMedicationChange(medication, strength, dose, unit, frequency, status, notes); changeMedication = null } }
}

@Composable private fun TimelineScreen(vm: PassportViewModel, modifier: Modifier) {
    val events by vm.events.collectAsState()
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<HealthEventEntity?>(null) }
    val visibleEvents = events.filter { event -> query.isBlank() || event.title.contains(query, ignoreCase = true) || event.details.contains(query, ignoreCase = true) }
    val episodeSummaries = summarizeSymptomEpisodes(events.filter { it.kind == "SYMPTOM" }.map { EpisodeEvent(it.title, it.episodeId, it.effectiveAtEpochMillis ?: it.createdAtEpochMillis, it.ongoing) })
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 24.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Health timeline", style = MaterialTheme.typography.headlineSmall); TextButton({ showAdd = true }) { Text("Add") } } }
        item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Search timeline") }, singleLine = true) }
        if (episodeSummaries.isNotEmpty()) item { Text("Recorded episodes: ${episodeSummaries.size} · ${episodeSummaries.sumOf { it.entryCount }} linked entries", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (events.isEmpty()) item { EmptyState("No records yet", "Symptoms, medications and other user-entered events will appear here.", "Log a record", onAction = { showAdd = true }) }
        else if (visibleEvents.isEmpty()) item { EmptyState("No matches", "Try a different search term or clear the search field.") }
        else items(visibleEvents, key = { it.id }) { event ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(16.dp)) { Text(event.title, style = MaterialTheme.typography.titleMedium); Text(event.kind.lowercase().replaceFirstChar { it.uppercase() }); if (event.details.isNotBlank()) Text(event.details); event.severity?.let { Text("Recorded severity: $it/10") }; if (event.durationMinutes != null) Text("Duration: ${event.durationMinutes} minutes"); if (event.ongoing) Text("Ongoing"); if (event.bodyLocation.isNotBlank()) Text("Location: ${event.bodyLocation}"); if (event.associatedSymptoms.isNotBlank()) Text("Associated symptoms: ${event.associatedSymptoms}"); if (event.possibleTrigger.isNotBlank()) Text("Observed possible trigger: ${event.possibleTrigger}"); if (event.relatedMedication.isNotBlank()) Text("Related medication: ${event.relatedMedication}"); event.episodeId?.let { Text("Episode or flare ID: $it") }; Text(DateFormat.getDateInstance().format(Date(event.effectiveAtEpochMillis ?: event.createdAtEpochMillis))); Row { TextButton({ vm.archive(event) }) { Text("Archive") }; TextButton({ pendingDelete = event }) { Text("Delete") } } } }
        }
    }
    if (showAdd) CaptureDialog("OTHER", "Add health event", { showAdd = false }, vm::addEvent)
    pendingDelete?.let { event ->
        AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("Delete event?") }, text = { Text("This removes the selected user-entered event from the timeline.") }, confirmButton = { Button({ vm.delete(event); pendingDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") } }, dismissButton = { TextButton({ pendingDelete = null }) { Text("Cancel") } })
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
    var pendingEdit by remember { mutableStateOf<DocumentEntity?>(null) }
    var pendingReplace by remember { mutableStateOf<DocumentEntity?>(null) }
    val replaceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val document = pendingReplace
        if (uri != null && document != null) vm.replaceDocument(context, document, uri)
        pendingReplace = null
    }
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 24.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Private document vault", style = MaterialTheme.typography.headlineSmall)
                TextButton({ showImport = true }) { Text("Import") }
            }
        }
        item { Text("PDF, JPG, JPEG, and PNG files are copied into app-private storage.") }
        if (documents.isEmpty()) item { EmptyState("Your vault is empty", "Import a PDF or image to keep a private copy on this device.", "Import a document", onAction = { showImport = true }) }
        else items(documents, key = { it.id }) { document ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(document.title, style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(document.category)
                    Text("${document.mimeType.substringAfterLast('/')}, ${document.byteCount / 1024} KB", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (document.documentDate.isNotBlank()) Text("Document date: ${document.documentDate}")
                if (document.notes.isNotBlank()) Text(document.notes)
                Row { TextButton({ vm.openDocument(context, document) }) { Text("Open") }; TextButton({ vm.shareDocument(context, document) }) { Text("Share") }; TextButton({ pendingEdit = document }) { Text("Edit") }; TextButton({ pendingReplace = document; replaceLauncher.launch(arrayOf("application/pdf", "image/jpeg", "image/png")) }) { Text("Replace") }; TextButton({ pendingDelete = document }) { Text("Delete") } }
            } }
        }
    }
    if (showImport) DocumentImportDialog(vm, context) { showImport = false }
    pendingDelete?.let { document ->
        AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("Delete document?") }, text = { Text("The private file and its metadata will be removed from this device.") }, confirmButton = { Button({ vm.deleteDocument(document); pendingDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") } }, dismissButton = { TextButton({ pendingDelete = null }) { Text("Cancel") } })
    }
    pendingEdit?.let { document -> DocumentEditDialog(vm, document) { pendingEdit = null } }
}

@Composable
private fun DocumentEditDialog(vm: PassportViewModel, document: DocumentEntity, onDismiss: () -> Unit) {
    var title by rememberSaveable(document.id) { mutableStateOf(document.title) }
    var category by rememberSaveable(document.id) { mutableStateOf(document.category) }
    var date by rememberSaveable(document.id) { mutableStateOf(document.documentDate) }
    var notes by rememberSaveable(document.id) { mutableStateOf(document.notes) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Edit document details") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(title, { title = it }, label = { Text("Title") })
        OutlinedTextField(category, { category = it }, label = { Text("Category") })
        OutlinedTextField(date, { date = it }, label = { Text("Document date") })
        OutlinedTextField(notes, { notes = it }, label = { Text("Notes") })
    } }, confirmButton = { Button({ vm.updateDocument(document, title, category, date, notes); onDismiss() }) { Text("Save") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}

@Composable
private fun RemindersScreen(vm: PassportViewModel, reminders: List<ReminderEntity>, modifier: Modifier) {
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<ReminderEntity?>(null) }
    var pendingEdit by remember { mutableStateOf<ReminderEntity?>(null) }
    var selectedView by rememberSaveable { mutableStateOf("UPCOMING") }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val visibleReminders = if (selectedView == "HISTORY") reminders.filter { it.status != "SCHEDULED" } else reminders.filter { it.status == "SCHEDULED" }
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 24.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Reminders", style = MaterialTheme.typography.headlineSmall); TextButton({ showAdd = true }) { Text("Add") } } }
        item { Text("Reminders are user-created and do not determine medical intervals.") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selectedView == "UPCOMING", onClick = { selectedView = "UPCOMING" }, label = { Text("Upcoming") })
                FilterChip(selected = selectedView == "HISTORY", onClick = { selectedView = "HISTORY" }, label = { Text("History") })
            }
        }
        if (reminders.isEmpty()) item { EmptyState("No reminders yet", "Create a follow-up, review or custom reminder when you want one.", "Create reminder", onAction = { showAdd = true }) }
        else if (visibleReminders.isEmpty()) item { EmptyState(if (selectedView == "HISTORY") "No reminder history" else "No upcoming reminders", if (selectedView == "HISTORY") "Completed or missed reminders will appear here." else "Your scheduled reminders will appear here.") }
        else items(visibleReminders, key = { it.id }) { reminder ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                    StatusPill(reminder.status.lowercase().replaceFirstChar { it.uppercase() })
                }
                Text("${reminder.type} · ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(reminder.dueAtEpochMillis))}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (reminder.notes.isNotBlank()) Text(reminder.notes)
                Row { if (reminder.status == "SCHEDULED") { TextButton({ vm.snoozeReminder(reminder) }) { Text("Snooze 1h") }; TextButton({ vm.completeReminder(reminder) }) { Text("Complete") } }; TextButton({ pendingEdit = reminder }) { Text("Edit") }; TextButton({ pendingDelete = reminder }) { Text("Delete") } }
            } }
        }
    }
    if (showAdd) ReminderDialog(vm, { showAdd = false }) { if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }
    pendingEdit?.let { reminder -> ReminderEditDialog(vm, reminder) { pendingEdit = null } }
    pendingDelete?.let { reminder -> AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("Delete reminder?") }, text = { Text("The scheduled notification will be cancelled.") }, confirmButton = { Button({ vm.deleteReminder(reminder); pendingDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") } }, dismissButton = { TextButton({ pendingDelete = null }) { Text("Cancel") } }) }
}

@Composable
private fun ReminderEditDialog(vm: PassportViewModel, reminder: ReminderEntity, onDismiss: () -> Unit) {
    var title by rememberSaveable(reminder.id) { mutableStateOf(reminder.title) }
    var type by rememberSaveable(reminder.id) { mutableStateOf(reminder.type) }
    var dueText by rememberSaveable(reminder.id) { mutableStateOf(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(reminder.dueAtEpochMillis))) }
    var notes by rememberSaveable(reminder.id) { mutableStateOf(reminder.notes) }
    var recurrence by rememberSaveable(reminder.id) { mutableStateOf(reminder.recurrence) }
    val dueAt = runCatching { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { isLenient = false }.parse(dueText)?.time }.getOrNull()
    val error = when { title.isBlank() -> "A title is required"; dueAt == null -> "Use yyyy-MM-dd HH:mm"; dueAt <= System.currentTimeMillis() -> "Choose a future time"; else -> "" }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Edit reminder") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(title, { title = it }, label = { Text("Title") }, isError = error.isNotBlank())
        OutlinedTextField(type, { type = it }, label = { Text("Type") })
        DateTimeField("Date and time", dueText, { dueText = it }, isError = dueAt == null)
        OutlinedTextField(recurrence, { recurrence = it.uppercase(Locale.getDefault()).take(12) }, label = { Text("Recurrence: ONCE or DAILY") })
        OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") })
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
    } }, confirmButton = { Button(enabled = error.isBlank(), onClick = { vm.updateReminder(reminder, title, type, notes, dueAt!!, if (recurrence == "DAILY") "DAILY" else "ONCE"); onDismiss() }) { Text("Save") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
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
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add reminder") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(title, { title = it }, label = { Text("Title") }, isError = error.isNotBlank())
        OutlinedTextField(type, { type = it }, label = { Text("Type") })
        DateTimeField("Date and time", dueText, { dueText = it }, isError = dueAt == null)
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
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Import private document") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Choose a PDF or image. The original is preserved privately; unsupported types are rejected.")
        OutlinedTextField(title, { title = it }, label = { Text("Title") })
        OutlinedTextField(category, { category = it }, label = { Text("Category") })
        OutlinedTextField(documentDate, { documentDate = it }, label = { Text("Document date (optional)") })
        OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") })
    } }, confirmButton = { Button({ launcher.launch(arrayOf("application/pdf", "image/jpeg", "image/png")) }) { Text("Choose file") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}

@Composable private fun ProfileScreen(vm: PassportViewModel, profile: ProfileEntity?, modifier: Modifier) {
    var name by remember(profile?.name) { mutableStateOf(profile?.name.orEmpty()) }; var allergies by remember(profile?.allergies) { mutableStateOf(profile?.allergies.orEmpty()) }; var conditions by remember(profile?.conditions) { mutableStateOf(profile?.conditions.orEmpty()) }
    var dateOfBirth by remember(profile?.dateOfBirth) { mutableStateOf(profile?.dateOfBirth.orEmpty()) }; var bloodGroup by remember(profile?.bloodGroup) { mutableStateOf(profile?.bloodGroup.orEmpty()) }; var emergencyContact by remember(profile?.emergencyContact) { mutableStateOf(profile?.emergencyContact.orEmpty()) }
    val profileDateParser = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false } }
    val dateOfBirthValid = dateOfBirth.isBlank() || runCatching { profileDateParser.parse(dateOfBirth) }.getOrNull() != null
    val prefs by vm.settings.collectAsState(); var showPinSetup by rememberSaveable { mutableStateOf(false) }
    var showDeleteAll by rememberSaveable { mutableStateOf(false) }
    var showReportOptions by rememberSaveable { mutableStateOf(false) }
    var showBackupPassword by rememberSaveable { mutableStateOf(false) }
    var backupAction by rememberSaveable { mutableStateOf("CREATE") }
    var backupPassword by rememberSaveable { mutableStateOf("") }
    var includeProfile by rememberSaveable { mutableStateOf(true) }; var includeEvents by rememberSaveable { mutableStateOf(true) }; var includeMedications by rememberSaveable { mutableStateOf(true) }; var includeDocuments by rememberSaveable { mutableStateOf(true) }; var includeReminders by rememberSaveable { mutableStateOf(true) }
    var reportFrom by rememberSaveable { mutableStateOf("") }; var reportTo by rememberSaveable { mutableStateOf("") }
    var generatedReportUri by remember { mutableStateOf<Uri?>(null) }
    val dateScope = validateDateScope(reportFrom, reportTo)
    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(exportShareDescriptor(ExportFormat.JSON).mimeType)) { uri ->
        if (uri != null) {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }
            val from = runCatching { parser.parse(reportFrom)?.time }.getOrNull()
            val to = runCatching { parser.parse(reportTo)?.time?.plus(86_399_999L) }.getOrNull()
            context.contentResolver.openOutputStream(uri)?.use { it.write(vm.exportJson(from, to).toByteArray(Charsets.UTF_8)) }
        }
    }
    val readableExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(exportShareDescriptor(ExportFormat.READABLE).mimeType)) { uri ->
        if (uri != null) {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }
            val from = runCatching { parser.parse(reportFrom)?.time }.getOrNull()
            val to = runCatching { parser.parse(reportTo)?.time?.plus(86_399_999L) }.getOrNull()
            context.contentResolver.openOutputStream(uri)?.use { it.write(vm.exportHumanReadable(from, to).toByteArray(Charsets.UTF_8)) }
        }
    }
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri -> if (uri != null) vm.createBackup(context, uri, backupPassword) }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) vm.restoreBackup(context, uri, backupPassword) }
    val reportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(exportShareDescriptor(ExportFormat.PDF).mimeType)) { uri ->
        if (uri != null) {
            generatedReportUri = uri
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }
            val from = runCatching { parser.parse(reportFrom)?.time }.getOrNull()
            val to = runCatching { parser.parse(reportTo)?.time?.plus(86_399_999L) }.getOrNull()
            vm.createPdfReport(context, uri, includeProfile, includeEvents, includeMedications, includeDocuments, includeReminders, from, to)
        }
    }
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("profileScroll"), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 24.dp)) {
        item { Text("Personal profile", style = MaterialTheme.typography.headlineSmall); Text("Keep your personal details and app controls in one place.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("Personal details")
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") })
                OutlinedTextField(dateOfBirth, { dateOfBirth = it }, Modifier.fillMaxWidth(), label = { Text("Date of birth") }, placeholder = { Text("yyyy-MM-dd") }, isError = !dateOfBirthValid, supportingText = { if (!dateOfBirthValid) Text("Use yyyy-MM-dd or leave blank") })
                OutlinedTextField(bloodGroup, { bloodGroup = it }, Modifier.fillMaxWidth(), label = { Text("Blood group") })
                OutlinedTextField(allergies, { allergies = it }, Modifier.fillMaxWidth(), label = { Text("Allergies") })
                OutlinedTextField(conditions, { conditions = it }, Modifier.fillMaxWidth(), label = { Text("Conditions") })
                OutlinedTextField(emergencyContact, { emergencyContact = it }, Modifier.fillMaxWidth(), label = { Text("Emergency contact") })
                Button({ vm.saveProfile(ProfileEntity(name = name, dateOfBirth = dateOfBirth, bloodGroup = bloodGroup, allergies = allergies, conditions = conditions, emergencyContact = emergencyContact, updatedAtEpochMillis = System.currentTimeMillis())) }, enabled = dateOfBirthValid, modifier = Modifier.fillMaxWidth()) { Text("Save profile") }
            } }
        }
        item {
            SectionHeader("Reports and data tools")
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(vertical = 4.dp)) {
                Text("Optional date range for exports and reports (yyyy-MM-dd). Leave blank for all dates.", Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(reportFrom, { reportFrom = it }, Modifier.weight(1f), label = { Text("From") }, singleLine = true)
                    OutlinedTextField(reportTo, { reportTo = it }, Modifier.weight(1f), label = { Text("To") }, singleLine = true)
                }
                if (!dateScope.isValid && (reportFrom.isNotBlank() || reportTo.isNotBlank())) {
                    Text("Enter valid dates in yyyy-MM-dd order before exporting.", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
                }
                ActionRow("Export my data (JSON)", enabled = dateScope.isValid, leadingIcon = Icons.Outlined.Download, onClick = { exportLauncher.launch("vexel-health-export.json") })
                ActionRow("Export readable summary", enabled = dateScope.isValid, leadingIcon = Icons.Outlined.Description, onClick = { readableExportLauncher.launch("vexel-health-export.txt") })
                ActionRow("Create encrypted backup", leadingIcon = Icons.Outlined.Lock, onClick = { backupAction = "CREATE"; showBackupPassword = true })
                ActionRow("Restore backup", leadingIcon = Icons.Outlined.LockOpen, onClick = { backupAction = "RESTORE"; showBackupPassword = true })
                ActionRow("Create PDF report", leadingIcon = Icons.Outlined.PictureAsPdf, onClick = { showReportOptions = true })
                generatedReportUri?.let { uri -> ActionRow("Share last PDF report", leadingIcon = Icons.Outlined.Share, onClick = { shareContentUri(context, uri, exportShareDescriptor(ExportFormat.PDF).mimeType, "Share health report") }) }
            } }
        }
        item {
            SectionHeader("Appearance and security")
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(vertical = 4.dp)) {
                ActionRow(if (prefs.darkTheme) "Use light theme" else "Use dark theme", leadingIcon = if (prefs.darkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode, onClick = { vm.setDarkTheme(!prefs.darkTheme) })
                ActionRow(if (prefs.lockEnabled) "Disable PIN lock" else "Set up PIN lock", leadingIcon = Icons.Outlined.Password, onClick = { if (prefs.lockEnabled) vm.disablePin() else showPinSetup = true })
                if (prefs.lockEnabled) {
                    Text("Lock after inactivity", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "When leaving", 5 to "5 minutes", 15 to "15 minutes", 30 to "30 minutes").forEach { (minutes, label) ->
                            FilterChip(selected = prefs.lockTimeoutMinutes == minutes, onClick = { vm.setLockTimeoutMinutes(minutes) }, label = { Text(label) })
                        }
                    }
                }
            } }
        }
        item {
            SectionHeader("Privacy and data")
            Text("Health information is stored locally on this device. Exported files and backups may contain sensitive information.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton({ showDeleteAll = true }, modifier = Modifier.fillMaxWidth()) { Text("Delete all app data", color = MaterialTheme.colorScheme.error) }
        }
        item {
            SectionHeader("Help and about")
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Vexel Health Passport", style = MaterialTheme.typography.titleMedium)
                    Text("Your health history, organized.")
                    Text("Vexel stores user-entered information locally and works offline. It does not diagnose conditions or replace professional care.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Version ${context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
    if (showReportOptions) ReportOptionsDialog({ showReportOptions = false }, { showReportOptions = false; reportLauncher.launch("vexel-health-report.pdf") }, { includeProfile = it }, { includeEvents = it }, { includeMedications = it }, { includeDocuments = it }, { includeReminders = it }, reportFrom, { reportFrom = it }, reportTo, { reportTo = it })
    if (showBackupPassword) BackupPasswordDialog(backupAction == "CREATE", backupPassword, { backupPassword = it }, { password -> backupPassword = password; showBackupPassword = false; if (backupAction == "CREATE") backupLauncher.launch("vexel-health-backup.vexel") else restoreLauncher.launch(arrayOf("application/octet-stream", "application/zip")) }, { showBackupPassword = false })
    if (showPinSetup) PinSetupDialog(vm) { showPinSetup = false }
    if (showDeleteAll) AlertDialog(onDismissRequest = { showDeleteAll = false }, title = { Text("Delete all data?") }, text = { Text("This permanently removes your profile, events, medications, private documents, and security settings from this device. This cannot be undone.") }, confirmButton = { Button({ vm.deleteAllData(); showDeleteAll = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete everything") } }, dismissButton = { TextButton({ showDeleteAll = false }) { Text("Cancel") } })
}

@Composable
private fun BackupPasswordDialog(
    creating: Boolean,
    password: String,
    onPasswordChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val valid = password.length >= 8
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (creating) "Protect backup" else "Unlock backup") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (creating) "Use at least 8 characters. This password is not stored; you will need it to restore this backup." else "Enter the password used when this backup was created.")
            OutlinedTextField(password, onPasswordChange, label = { Text("Backup password") }, isError = password.isNotEmpty() && !valid, supportingText = { if (password.isNotEmpty() && !valid) Text("Use at least 8 characters") })
        } },
        confirmButton = { Button(enabled = valid, onClick = { onConfirm(password) }) { Text(if (creating) "Choose destination" else "Choose backup") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ReportOptionsDialog(onDismiss: () -> Unit, onGenerate: () -> Unit, setProfile: (Boolean) -> Unit, setEvents: (Boolean) -> Unit, setMedications: (Boolean) -> Unit, setDocuments: (Boolean) -> Unit, setReminders: (Boolean) -> Unit, from: String, setFrom: (String) -> Unit, to: String, setTo: (String) -> Unit) {
    var profileChecked by remember { mutableStateOf(true) }; var eventsChecked by remember { mutableStateOf(true) }; var medicationsChecked by remember { mutableStateOf(true) }; var documentsChecked by remember { mutableStateOf(true) }; var remindersChecked by remember { mutableStateOf(true) }
    val dateScope = validateDateScope(from, to)
    val validFrom = dateScope.fromValid
    val validTo = dateScope.toValid
    val datesOrdered = dateScope.ordered
    val hasSection = hasSelectedReportSection(profileChecked, eventsChecked, medicationsChecked, documentsChecked, remindersChecked)
    AlertDialog(onDismissRequest = onDismiss, title = { Text("PDF report options") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Select sections and optional date range. Use yyyy-MM-dd.")
        listOf("Profile" to profileChecked, "Health events" to eventsChecked, "Medications" to medicationsChecked, "Documents" to documentsChecked, "Reminders" to remindersChecked).forEach { (label, checked) -> Row { Checkbox(checked, { value -> when (label) { "Profile" -> { profileChecked = value; setProfile(value) }; "Health events" -> { eventsChecked = value; setEvents(value) }; "Medications" -> { medicationsChecked = value; setMedications(value) }; "Documents" -> { documentsChecked = value; setDocuments(value) }; else -> { remindersChecked = value; setReminders(value) } } }); Text(label) } }
        OutlinedTextField(from, setFrom, label = { Text("From (optional)") }, isError = !validFrom, supportingText = { if (!validFrom) Text("Use yyyy-MM-dd") })
        OutlinedTextField(to, setTo, label = { Text("To (optional)") }, isError = !validTo || !datesOrdered, supportingText = { if (!validTo) Text("Use yyyy-MM-dd") else if (!datesOrdered) Text("To must be on or after From") })
    } }, confirmButton = { Button(enabled = hasSection && validFrom && validTo && datesOrdered, onClick = onGenerate) { Text("Save PDF") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}

@Composable
private fun PinSetupDialog(vm: PassportViewModel, onDismiss: () -> Unit) {
    var pin by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Set up PIN lock") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Use 4–12 digits. The PIN is never stored as plaintext.")
        OutlinedTextField(pin, { pin = it.filter(Char::isDigit).take(12) }, label = { Text("PIN") }, isError = error.isNotBlank())
        OutlinedTextField(confirmation, { confirmation = it.filter(Char::isDigit).take(12) }, label = { Text("Confirm PIN") }, isError = error.isNotBlank(), supportingText = { if (error.isNotBlank()) Text(error) })
    } }, confirmButton = { Button({ if (pin != confirmation) error = "PINs do not match" else if (pin.length !in 4..12) error = "Use 4–12 digits" else if (vm.savePin(pin, confirmation)) onDismiss() }) { Text("Enable") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}

@Composable private fun CaptureDialog(kind: String, heading: String, onDismiss: () -> Unit, onSave: (String, String, String, Int?) -> Unit, onSaveSymptom: ((SymptomDraft, Uri?) -> Unit)? = null) {
    var title by remember { mutableStateOf("") }; var details by remember { mutableStateOf("") }; var severityText by remember { mutableStateOf("") }
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
            DateTimeField("Start (optional)", startAtText, { startAtText = it }, isError = errors.containsKey("startAt"))
            DateTimeField("End (optional)", endAtText, { endAtText = it }, enabled = !ongoing, isError = errors.containsKey("endAt"))
            OutlinedTextField(durationText, { durationText = it.filter(Char::isDigit) }, label = { Text("Duration in minutes (optional)") }, isError = errors.containsKey("duration"))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Checkbox(ongoing, { ongoing = it }); Text("Ongoing") }
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

@Composable
private fun MedicationDialog(onDismiss: () -> Unit, onSave: (MedicationDraft) -> Unit) {
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add medication") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Medication name") }, isError = errors.containsKey("name"))
            OutlinedTextField(genericName, { genericName = it }, label = { Text("Generic or brand name (optional)") })
            OutlinedTextField(strength, { strength = it }, label = { Text("Strength") })
            OutlinedTextField(dose, { dose = it }, label = { Text("Dose") })
            OutlinedTextField(unit, { unit = it }, label = { Text("Unit (optional)") })
            OutlinedTextField(route, { route = it }, label = { Text("Route (optional)") })
            OutlinedTextField(frequency, { frequency = it }, label = { Text("Frequency (optional)") })
            OutlinedTextField(startDate, { startDate = it }, label = { Text("Start date (optional)") })
            OutlinedTextField(stopDate, { stopDate = it }, label = { Text("Stop date (optional)") })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = status == "CURRENT", onClick = { status = "CURRENT" }, label = { Text("Current") })
                FilterChip(selected = status == "STOPPED", onClick = { status = "STOPPED" }, label = { Text("Stopped") })
            }
            OutlinedTextField(indication, { indication = it }, label = { Text("Indication (optional)") })
            OutlinedTextField(physician, { physician = it }, label = { Text("Physician (optional)") })
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") }, isError = errors.containsKey("notes"))
            errors.values.firstOrNull()?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        } },
        confirmButton = { Button(enabled = errors.isEmpty(), onClick = { onSave(draft); onDismiss() }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun MedicationChangeDialog(medication: MedicationEntity, onDismiss: () -> Unit, onSave: (String, String, String, String, String, String) -> Unit) {
    var strength by rememberSaveable(medication.id) { mutableStateOf(medication.strength) }
    var dose by rememberSaveable(medication.id) { mutableStateOf(medication.dose) }
    var unit by rememberSaveable(medication.id) { mutableStateOf(medication.unit) }
    var frequency by rememberSaveable(medication.id) { mutableStateOf(medication.frequency) }
    var status by rememberSaveable(medication.id) { mutableStateOf(medication.status) }
    var notes by rememberSaveable(medication.id) { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Record medication change") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("This records a new treatment period; it does not recommend a dose or treatment.")
        OutlinedTextField(strength, { strength = it }, label = { Text("Strength") })
        OutlinedTextField(dose, { dose = it }, label = { Text("Dose") })
        OutlinedTextField(unit, { unit = it }, label = { Text("Unit") })
        OutlinedTextField(frequency, { frequency = it }, label = { Text("Frequency") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(status == "CURRENT", { status = "CURRENT" }, label = { Text("Current/restarted") }); FilterChip(status == "STOPPED", { status = "STOPPED" }, label = { Text("Stopped") }) }
        OutlinedTextField(notes, { notes = it }, label = { Text("Change notes (optional)") })
    } }, confirmButton = { Button({ onSave(strength, dose, unit, frequency, status, notes) }) { Text("Save change") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}
