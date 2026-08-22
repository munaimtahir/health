package com.vexel.passport.feature.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vexel.passport.core.database.HealthDatabase
import androidx.room.withTransaction
import com.vexel.passport.core.database.ProfileEntity
import com.vexel.passport.core.database.DocumentEntity
import com.vexel.passport.core.database.HealthEventEntity
import com.vexel.passport.core.database.MedicationEntity
import com.vexel.passport.core.database.MedicationChangeEntity
import com.vexel.passport.core.database.ReminderEntity
import com.vexel.passport.core.datastore.PreferencesStore
import com.vexel.passport.core.datastore.UserPreferences
import com.vexel.passport.core.files.SecureFileStore
import com.vexel.passport.core.model.isWithinDateScope
import com.vexel.passport.core.model.remapRestoredSymptomReferences
import com.vexel.passport.core.notifications.ReminderScheduler
import com.vexel.passport.core.security.PinVerifier
import com.vexel.passport.core.security.PinMaterialCipher
import com.vexel.passport.core.security.BackupCrypto
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val database: HealthDatabase,
    private val preferences: PreferencesStore,
    private val pinMaterialCipher: PinMaterialCipher,
    private val secureFileStore: SecureFileStore,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    private val pinVerifier = PinVerifier()

    val profile = database.profileDao().observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val settings = preferences.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    private val _operationError = MutableStateFlow<String?>(null)
    val operationError: StateFlow<String?> = _operationError

    private val _statusEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val statusEvents = _statusEvents.asSharedFlow()

    // We also need all events, medications, documents, reminders for exports and report generation
    val events = database.healthEventDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val medications = database.medicationDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val medicationChanges = database.medicationChangeDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val documents = database.documentDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val reminders = database.reminderDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveProfile(p: ProfileEntity) = viewModelScope.launch {
        database.profileDao().upsert(p)
    }

    fun setDarkTheme(value: Boolean) = viewModelScope.launch {
        preferences.setDarkTheme(value)
    }

    fun savePin(pin: String, confirmation: String): Boolean {
        if (pin != confirmation || pin.length !in 4..12 || pin.any { !it.isDigit() }) return false
        val record = pinVerifier.create(pin.toCharArray())
        viewModelScope.launch {
            try {
                preferences.setPinMaterial(pinMaterialCipher.encrypt(record))
                _statusEvents.tryEmit("PIN setup complete")
            } catch (cancellation: kotlinx.coroutines.CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                _operationError.value = "Could not enable the PIN lock on this device. Try again."
            }
        }
        return true
    }

    fun verifyPin(pin: String, prefs: UserPreferences): Boolean {
        if (!prefs.lockEnabled) return true
        return runCatching { pinVerifier.matches(pin.toCharArray(), pinMaterialCipher.decrypt(prefs.pinMaterial)) }.getOrDefault(false)
    }

    fun disablePin() = viewModelScope.launch {
        preferences.clearPinMaterial()
        _statusEvents.tryEmit("PIN disabled")
    }

    fun setLockTimeoutMinutes(minutes: Int) = viewModelScope.launch {
        preferences.setLockTimeoutMinutes(minutes)
    }

    fun setHideRecentAppsPreview(enabled: Boolean) = viewModelScope.launch {
        preferences.setHideRecentAppsPreview(enabled)
    }

    fun dismissOperationError() {
        _operationError.value = null
    }

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
        _statusEvents.tryEmit("All data deleted")
    }

    private fun sha256Hex(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes).joinToString("") { "%02x".format(it) }

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

    fun createBackup(uri: Uri, password: String) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val zipBytes = ByteArrayOutputStream().use { output ->
                ZipOutputStream(output).use { zip ->
                    val data = exportJson().toByteArray(Charsets.UTF_8)
                    zip.putNextEntry(ZipEntry("data.json"))
                    zip.write(data)
                    zip.closeEntry()
                    val manifest = JSONObject()
                        .put("formatVersion", 1)
                        .put("createdAtEpochMillis", System.currentTimeMillis())
                        .put("dataSha256", sha256Hex(data))
                        .put("documentCount", documents.value.size)
                        .toString()
                        .toByteArray(Charsets.UTF_8)
                    zip.putNextEntry(ZipEntry("manifest.json"))
                    zip.write(manifest)
                    zip.closeEntry()
                    documents.value.forEach { document ->
                        zip.putNextEntry(ZipEntry("documents/${document.id}"))
                        secureFileStore.open(document.id).use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
                output.toByteArray()
            }
            appContext.contentResolver.openOutputStream(uri)?.use { it.write(BackupCrypto.encrypt(zipBytes, password.toCharArray())) }
            _statusEvents.tryEmit("Backup created")
        } catch (cancellation: kotlinx.coroutines.CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            _operationError.value = "Backup could not be created. Check available storage and try again."
        }
    }

    fun restoreBackup(uri: Uri, password: String) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val tempDir = java.io.File(appContext.cacheDir, "backup_restore_${UUID.randomUUID()}")
        tempDir.mkdirs()
        val restoredDocumentIds = mutableMapOf<String, String>()
        val restoredDocuments = mutableListOf<DocumentEntity>()
        var dataJsonString: String? = null
        var manifestJsonString: String? = null
        try {
            val sourceBytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: error("Unable to read backup")
            val backupBytes = if (BackupCrypto.isEncrypted(sourceBytes)) BackupCrypto.decrypt(sourceBytes, password.toCharArray()) else sourceBytes
            ZipInputStream(ByteArrayInputStream(backupBytes)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    require(!entry.isDirectory && (entry.name == "data.json" || entry.name == "manifest.json" || entry.name.startsWith("documents/"))) { "Unsupported backup entry" }
                    if (entry.name == "data.json") {
                        dataJsonString = String(zip.readBytes(), Charsets.UTF_8)
                    } else if (entry.name == "manifest.json") {
                        manifestJsonString = String(zip.readBytes(), Charsets.UTF_8)
                    } else if (entry.name.startsWith("documents/")) {
                        val docId = entry.name.substringAfter("documents/")
                        val tempFile = java.io.File(tempDir, docId)
                        tempFile.outputStream().use { out -> zip.copyTo(out) }
                    }
                    entry = zip.nextEntry
                }
            }
            val data = JSONObject(dataJsonString ?: error("Missing backup data"))
            require(data.optInt("formatVersion", -1) == 1) { "Unsupported backup version" }
            manifestJsonString?.let {
                val manifest = JSONObject(manifestJsonString)
                require(manifest.optInt("formatVersion", -1) == 1) { "Unsupported backup version" }
                val dataBytes = dataJsonString!!.toByteArray(Charsets.UTF_8)
                require(manifest.optString("dataSha256").equals(sha256Hex(dataBytes), ignoreCase = true)) { "Backup integrity check failed" }
            }
            val documentData = data.optJSONArray("documents") ?: JSONArray()
            for (index in 0 until documentData.length()) {
                val source = documentData.getJSONObject(index)
                val sourceId = source.getString("id")
                val tempFile = java.io.File(tempDir, sourceId)
                require(tempFile.exists()) { "Missing document binary in backup" }
                val digest = tempFile.inputStream().use { input ->
                    val md = java.security.MessageDigest.getInstance("SHA-256")
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } >= 0) {
                        md.update(buffer, 0, read)
                    }
                    md.digest().joinToString("") { "%02x".format(it) }
                }
                require(digest.equals(source.getString("sha256"), ignoreCase = true)) { "Document integrity check failed" }
                val preserved = tempFile.inputStream().use { input ->
                    secureFileStore.preserveOriginal(input, source.getString("mimeType"), source.optString("originalFileName", "document"))
                }
                restoredDocumentIds[sourceId] = preserved.id
                restoredDocuments += DocumentEntity(preserved.id, source.optString("title"), source.optString("category", "OTHER"), source.optString("documentDate"), source.optString("notes"), source.optString("originalFileName", "document"), preserved.mimeType, preserved.byteCount, preserved.sha256, System.currentTimeMillis())
            }
            val scheduledReminders = mutableListOf<ReminderEntity>()
            try {
                database.withTransaction {
                    database.healthEventDao().deleteAll()
                    database.medicationDao().deleteAll()
                    database.medicationChangeDao().deleteAll()
                    database.profileDao().deleteAll()
                    database.documentDao().deleteAll()
                    database.reminderDao().deleteAll()
                    data.optJSONObject("profile")?.let { p -> database.profileDao().upsert(ProfileEntity(name = p.optString("name"), dateOfBirth = p.optString("dateOfBirth"), bloodGroup = p.optString("bloodGroup"), allergies = p.optString("allergies"), conditions = p.optString("conditions"), emergencyContact = p.optString("emergencyContact"), updatedAtEpochMillis = System.currentTimeMillis())) }
                    (data.optJSONArray("events") ?: JSONArray()).let { array ->
                        for (i in 0 until array.length()) {
                            val e = array.getJSONObject(i)
                            val references = remapRestoredSymptomReferences(e.optString("imageAttachmentId"), e.optString("episodeId"), restoredDocumentIds)
                            database.healthEventDao().insert(HealthEventEntity(e.getString("id"), e.optString("title"), e.optString("details"), e.optString("kind", "OTHER"), if (e.isNull("effectiveAtEpochMillis")) null else e.optLong("effectiveAtEpochMillis"), e.optLong("createdAtEpochMillis"), status = e.optString("status", "ACTIVE"), severity = if (e.isNull("severity")) null else e.optInt("severity"), durationMinutes = if (e.isNull("durationMinutes")) null else e.optInt("durationMinutes"), startAtEpochMillis = if (e.isNull("startAtEpochMillis")) null else e.optLong("startAtEpochMillis"), endAtEpochMillis = if (e.isNull("endAtEpochMillis")) null else e.optLong("endAtEpochMillis"), ongoing = e.optBoolean("ongoing"), bodyLocation = e.optString("bodyLocation"), associatedSymptoms = e.optString("associatedSymptoms"), possibleTrigger = e.optString("possibleTrigger"), relatedMedication = e.optString("relatedMedication"), imageAttachmentId = references.imageAttachmentId, episodeId = references.episodeId))
                        }
                    }
                    (data.optJSONArray("medications") ?: JSONArray()).let { array ->
                        for (i in 0 until array.length()) {
                            val m = array.getJSONObject(i)
                            database.medicationDao().insert(MedicationEntity(m.getString("id"), m.optString("name"), m.optString("genericName"), m.optString("strength"), m.optString("dose"), m.optString("unit"), m.optString("route"), m.optString("frequency"), m.optString("startDate"), m.optString("stopDate"), m.optString("status", "CURRENT"), m.optString("indication"), m.optString("physician"), m.optString("notes"), System.currentTimeMillis(), System.currentTimeMillis()))
                        }
                    }
                    (data.optJSONArray("medicationChanges") ?: JSONArray()).let { array ->
                        for (i in 0 until array.length()) {
                            val c = array.getJSONObject(i)
                            database.medicationChangeDao().insert(MedicationChangeEntity(c.getString("id"), c.getString("medicationId"), c.optLong("changedAtEpochMillis"), c.optString("changeType", "DOSE_CHANGED"), c.optString("strength"), c.optString("dose"), c.optString("unit"), c.optString("frequency"), c.optString("status", "CURRENT"), c.optString("notes")))
                        }
                    }
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
                _statusEvents.tryEmit("Restore completed")
            } catch (error: Throwable) {
                restoredDocuments.forEach { secureFileStore.delete(it.id) }
                throw error
            }
        } catch (cancellation: kotlinx.coroutines.CancellationException) {
            throw cancellation
        } catch (failure: javax.crypto.AEADBadTagException) {
            _operationError.value = "Incorrect password, or the backup file is corrupted."
        } catch (failure: Exception) {
            _operationError.value = "Restore failed. The backup file may be corrupted or in an unsupported format."
        } finally {
            tempDir.deleteRecursively()
        }
    }

    // We'll keep PDF creation in ViewModel for now, but in Phase 4 we will extract it to a core report service/use case.
    fun createPdfReport(
        uri: Uri,
        includeProfile: Boolean = true,
        includeEvents: Boolean = true,
        includeMedications: Boolean = true,
        includeDocuments: Boolean = true,
        includeReminders: Boolean = true,
        fromEpochMillis: Long? = null,
        toEpochMillis: Long? = null
    ) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        try {
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
            val pdf = android.graphics.pdf.PdfDocument()
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.BLACK; textSize = 11f }
            var pageNo = 0
            var page = pdf.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, ++pageNo).create())
            var y = 48f
            fun finishPage() {
                paint.textSize = 9f
                page.canvas.drawText("Vexel Health Passport · Page $pageNo", 48f, 824f, paint)
                pdf.finishPage(page)
            }
            lines.forEach { raw ->
                raw.chunked(88).ifEmpty { listOf("") }.forEach { text ->
                    if (y > 790f) {
                        finishPage()
                        page = pdf.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, ++pageNo).create())
                        y = 48f
                    }
                    paint.textSize = if (pageNo == 1 && y < 70f) 20f else 11f
                    page.canvas.drawText(text, 48f, y, paint)
                    y += 18f
                }
            }
            finishPage()
            appContext.contentResolver.openOutputStream(uri)?.use { pdf.writeTo(it) }
            pdf.close()
            _statusEvents.tryEmit("Report generated")
        } catch (cancellation: kotlinx.coroutines.CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            _operationError.value = "Report generation failed. Try again."
        }
    }
}
