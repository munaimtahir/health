package com.vexel.passport.feature.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vexel.passport.core.database.ProfileEntity
import com.vexel.passport.core.designsystem.ActionRow
import com.vexel.passport.core.designsystem.SectionHeader
import com.vexel.passport.core.model.ExportFormat
import com.vexel.passport.core.model.exportShareDescriptor
import com.vexel.passport.core.model.hasSelectedReportSection
import com.vexel.passport.core.model.validateDateScope
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val profile by viewModel.profile.collectAsState()
    val operationError by viewModel.operationError.collectAsState()

    ProfileScreen(
        vm = viewModel,
        profile = profile,
        modifier = modifier
    )

    operationError?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissOperationError,
            title = { Text("Something went wrong") },
            text = { Text(message) },
            confirmButton = { TextButton(viewModel::dismissOperationError) { Text("OK") } }
        )
    }
}

private class PdfPrintDocumentAdapter(
    private val context: Context,
    private val sourceUri: Uri,
    private val jobLabel: String,
) : android.print.PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: android.print.PrintAttributes?,
        newAttributes: android.print.PrintAttributes,
        cancellationSignal: android.os.CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?,
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }
        callback.onLayoutFinished(
            android.print.PrintDocumentInfo.Builder(jobLabel)
                .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .build(),
            oldAttributes != newAttributes,
        )
    }

    override fun onWrite(
        pages: Array<out android.print.PageRange>?,
        destination: android.os.ParcelFileDescriptor,
        cancellationSignal: android.os.CancellationSignal?,
        callback: WriteResultCallback,
    ) {
        try {
            if (cancellationSignal?.isCanceled == true) {
                callback.onWriteCancelled()
                return
            }
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                android.os.ParcelFileDescriptor.AutoCloseOutputStream(destination).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } >= 0) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback.onWriteCancelled()
                            return
                        }
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
            callback.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
        } catch (failure: java.lang.Exception) {
            callback.onWriteFailed(null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(
    vm: ProfileViewModel,
    profile: ProfileEntity?,
    modifier: Modifier
) {
    var name by rememberSaveable(profile?.name) { mutableStateOf(profile?.name.orEmpty()) }
    var allergies by rememberSaveable(profile?.allergies) { mutableStateOf(profile?.allergies.orEmpty()) }
    var conditions by rememberSaveable(profile?.conditions) { mutableStateOf(profile?.conditions.orEmpty()) }
    var dateOfBirth by rememberSaveable(profile?.dateOfBirth) { mutableStateOf(profile?.dateOfBirth.orEmpty()) }
    var bloodGroup by rememberSaveable(profile?.bloodGroup) { mutableStateOf(profile?.bloodGroup.orEmpty()) }
    var emergencyContact by rememberSaveable(profile?.emergencyContact) { mutableStateOf(profile?.emergencyContact.orEmpty()) }
    val profileDateParser = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false } }
    val dateOfBirthValid = dateOfBirth.isBlank() || runCatching { profileDateParser.parse(dateOfBirth) }.getOrNull() != null
    val prefs by vm.settings.collectAsState()
    var showPinSetup by rememberSaveable { mutableStateOf(false) }
    var showHelp by rememberSaveable { mutableStateOf(false) }
    var showPrivacyInfo by rememberSaveable { mutableStateOf(false) }
    var showDeleteAll by rememberSaveable { mutableStateOf(false) }
    var showReportOptions by rememberSaveable { mutableStateOf(false) }
    var showBackupPassword by rememberSaveable { mutableStateOf(false) }
    var backupAction by rememberSaveable { mutableStateOf("CREATE") }
    var backupPassword by rememberSaveable { mutableStateOf("") }
    var includeProfile by rememberSaveable { mutableStateOf(true) }
    var includeEvents by rememberSaveable { mutableStateOf(true) }
    var includeMedications by rememberSaveable { mutableStateOf(true) }
    var includeDocuments by rememberSaveable { mutableStateOf(true) }
    var includeReminders by rememberSaveable { mutableStateOf(true) }
    var reportFrom by rememberSaveable { mutableStateOf("") }
    var reportTo by rememberSaveable { mutableStateOf("") }
    var generatedReportUri by rememberSaveable { mutableStateOf<Uri?>(null) }
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
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri -> if (uri != null) vm.createBackup(uri, backupPassword) }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) vm.restoreBackup(uri, backupPassword) }
    val reportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(exportShareDescriptor(ExportFormat.PDF).mimeType)) { uri ->
        if (uri != null) {
            generatedReportUri = uri
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }
            val from = runCatching { parser.parse(reportFrom)?.time }.getOrNull()
            val to = runCatching { parser.parse(reportTo)?.time?.plus(86_399_999L) }.getOrNull()
            vm.createPdfReport(uri, includeProfile, includeEvents, includeMedications, includeDocuments, includeReminders, from, to)
        }
    }

    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("profileScroll"), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)) {
        item { Text("Personal profile", style = MaterialTheme.typography.headlineSmall); Text("Keep your personal details and app controls in one place.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        
        // 1. Personal details
        item {
            CollapsibleSection("Personal details", initialExpanded = true) {
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") })
                OutlinedTextField(dateOfBirth, { dateOfBirth = it }, Modifier.fillMaxWidth(), label = { Text("Date of birth") }, placeholder = { Text("yyyy-MM-dd") }, isError = !dateOfBirthValid, supportingText = { if (!dateOfBirthValid) Text("Use yyyy-MM-dd or leave blank") })
                OutlinedTextField(bloodGroup, { bloodGroup = it }, Modifier.fillMaxWidth(), label = { Text("Blood group") })
                OutlinedTextField(allergies, { allergies = it }, Modifier.fillMaxWidth(), label = { Text("Allergies") })
                OutlinedTextField(conditions, { conditions = it }, Modifier.fillMaxWidth(), label = { Text("Conditions") })
                OutlinedTextField(emergencyContact, { emergencyContact = it }, Modifier.fillMaxWidth(), label = { Text("Emergency contact") })
                Button({ vm.saveProfile(ProfileEntity(name = name, dateOfBirth = dateOfBirth, bloodGroup = bloodGroup, allergies = allergies, conditions = conditions, emergencyContact = emergencyContact, updatedAtEpochMillis = System.currentTimeMillis())) }, enabled = dateOfBirthValid, modifier = Modifier.fillMaxWidth()) { Text("Save profile") }
            }
        }
        
        // 2. Reports and exports
        item {
            CollapsibleSection("Reports and exports") {
                Text("Optional date range for exports and reports (yyyy-MM-dd). Leave blank for all dates.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(reportFrom, { reportFrom = it }, Modifier.weight(1f), label = { Text("From") }, singleLine = true)
                    OutlinedTextField(reportTo, { reportTo = it }, Modifier.weight(1f), label = { Text("To") }, singleLine = true)
                }
                if (!dateScope.isValid && (reportFrom.isNotBlank() || reportTo.isNotBlank())) {
                    Text("Enter valid dates in yyyy-MM-dd order before exporting.", color = MaterialTheme.colorScheme.error)
                }
                ActionRow("Export my data (JSON)", enabled = dateScope.isValid, leadingIcon = Icons.Outlined.Download, onClick = { exportLauncher.launch("vexel-health-export.json") })
                ActionRow("Export readable summary", enabled = dateScope.isValid, leadingIcon = Icons.Outlined.Description, onClick = { readableExportLauncher.launch("vexel-health-export.txt") })
                ActionRow("Create PDF report", leadingIcon = Icons.Outlined.PictureAsPdf, onClick = { showReportOptions = true })
                generatedReportUri?.let { uri ->
                    ActionRow("Share last PDF report", leadingIcon = Icons.Outlined.Share, onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = exportShareDescriptor(ExportFormat.PDF).mimeType
                            putExtra(Intent.EXTRA_STREAM, uri)
                            clipData = android.content.ClipData.newUri(context.contentResolver, "Vexel shared content", uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share health report").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    })
                    ActionRow("Print last PDF report", leadingIcon = Icons.Outlined.PictureAsPdf, onClick = {
                        val printManager = context.getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
                        printManager.print("Vexel Health Passport report", PdfPrintDocumentAdapter(context, uri, "Vexel Health Passport report"), null)
                    })
                }
            }
        }
        
        // 3. Backup and restore
        item {
            CollapsibleSection("Backup and restore") {
                ActionRow("Create encrypted backup", leadingIcon = Icons.Outlined.Lock, onClick = { backupAction = "CREATE"; showBackupPassword = true })
                ActionRow("Restore backup", leadingIcon = Icons.Outlined.LockOpen, onClick = { backupAction = "RESTORE"; showBackupPassword = true })
            }
        }

        // 4. Privacy and app lock
        item {
            CollapsibleSection("Privacy and app lock") {
                ActionRow(if (prefs.lockEnabled) "Disable PIN lock" else "Set up PIN lock", leadingIcon = Icons.Outlined.Password, onClick = { if (prefs.lockEnabled) vm.disablePin() else showPinSetup = true })
                if (prefs.lockEnabled) {
                    Text("Lock after inactivity", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "When leaving", 5 to "5 minutes", 15 to "15 minutes", 30 to "30 minutes").forEach { (minutes, label) ->
                            FilterChip(selected = prefs.lockTimeoutMinutes == minutes, onClick = { vm.setLockTimeoutMinutes(minutes) }, label = { Text(label) })
                        }
                    }
                }
                ActionRow(
                    if (prefs.hideRecentAppsPreview) "Show app preview in recent apps" else "Hide app preview in recent apps and screenshots",
                    leadingIcon = if (prefs.hideRecentAppsPreview) Icons.Outlined.LockOpen else Icons.Outlined.Lock,
                    onClick = { vm.setHideRecentAppsPreview(!prefs.hideRecentAppsPreview) },
                )
            }
        }

        // 5. Appearance and accessibility
        item {
            CollapsibleSection("Appearance and accessibility") {
                ActionRow(if (prefs.darkTheme) "Use light theme" else "Use dark theme", leadingIcon = if (prefs.darkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode, onClick = { vm.setDarkTheme(!prefs.darkTheme) })
            }
        }

        // 6. Data deletion and legal information
        item {
            CollapsibleSection("Data deletion and legal info") {
                Text("Version ${context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ActionRow("Using this app", leadingIcon = Icons.Outlined.Description, onClick = { showHelp = true })
                ActionRow("Privacy and safety", leadingIcon = Icons.Outlined.Lock, onClick = { showPrivacyInfo = true })
                OutlinedButton({ showDeleteAll = true }, modifier = Modifier.fillMaxWidth()) { Text("Delete all app data", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
    if (showHelp) HelpDialog { showHelp = false }
    if (showPrivacyInfo) PrivacyInfoDialog { showPrivacyInfo = false }
    if (showReportOptions) ReportOptionsDialog({ showReportOptions = false }, { showReportOptions = false; reportLauncher.launch("vexel-health-report.pdf") }, { includeProfile = it }, { includeEvents = it }, { includeMedications = it }, { includeDocuments = it }, { includeReminders = it }, reportFrom, { reportFrom = it }, reportTo, { reportTo = it })
    if (showBackupPassword) BackupPasswordDialog(backupAction == "CREATE", backupPassword, { backupPassword = it }, { password -> backupPassword = password; showBackupPassword = false; if (backupAction == "CREATE") backupLauncher.launch("vexel-health-backup.vexel") else restoreLauncher.launch(arrayOf("application/octet-stream", "application/zip")) }, { showBackupPassword = false })
    if (showPinSetup) PinSetupDialog(vm) { showPinSetup = false }
    if (showDeleteAll) AlertDialog(onDismissRequest = { showDeleteAll = false }, title = { Text("Delete all data?") }, text = { Text("This permanently removes your profile, events, medications, private documents, and security settings from this device. This cannot be undone.") }, confirmButton = { Button({ vm.deleteAllData(); showDeleteAll = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete everything") } }, dismissButton = { TextButton({ showDeleteAll = false }) { Text("Cancel") } })
}

@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Using this app") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Vexel Health Passport is a private, offline record of information you enter yourself. It does not diagnose, interpret results, or recommend treatment -- always confirm health decisions with a qualified professional.", style = MaterialTheme.typography.bodyMedium)
                Text("Your data", style = MaterialTheme.typography.titleSmall)
                Text("Everything you enter stays on this device. There is no account and no cloud sync -- if this device is lost, factory reset, or the app is uninstalled without a backup, your data is gone.", style = MaterialTheme.typography.bodySmall)
                Text("Backups", style = MaterialTheme.typography.titleSmall)
                Text("Create an encrypted backup from Profile before uninstalling the app, switching devices, or as routine safekeeping. You choose the password; it is not stored anywhere, so write it down somewhere safe -- a lost backup password cannot be recovered.", style = MaterialTheme.typography.bodySmall)
                Text("App lock", style = MaterialTheme.typography.titleSmall)
                Text("An optional PIN (with biometric unlock where available) can lock the app after a period of inactivity. This protects against casual access to this device, not against a determined attacker with full device access.", style = MaterialTheme.typography.bodySmall)
                Text("Sharing documents", style = MaterialTheme.typography.titleSmall)
                Text("Opening or sharing a document from the vault creates a temporary, permission-scoped copy for the app you send it to. The original stays private in this app's storage.", style = MaterialTheme.typography.bodySmall)
                Text("Reminders", style = MaterialTheme.typography.titleSmall)
                Text("Reminders rely on the device's notification system and battery/power settings. Aggressive battery optimization on some devices can delay or suppress a reminder notification -- reminders are a convenience, not a guaranteed medical alert.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(onDismiss) { Text("Close") } },
    )
}

@Composable
private fun PrivacyInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Privacy and safety") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("This screen describes how the app actually behaves; it is not a substitute for the full legal privacy policy.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("No network access", style = MaterialTheme.typography.titleSmall)
                Text("This app does not request Internet permission and cannot send data anywhere. All information you enter stays in this app's private storage on this device.", style = MaterialTheme.typography.bodySmall)
                Text("No account, no analytics, no ads", style = MaterialTheme.typography.titleSmall)
                Text("There is no sign-in, no usage tracking, and no advertising in this app.", style = MaterialTheme.typography.bodySmall)
                Text("Encryption", style = MaterialTheme.typography.titleSmall)
                Text("An optional PIN is protected using the device's hardware Keystore. Backups you create are encrypted with a password you choose, using standard AES-GCM encryption.", style = MaterialTheme.typography.bodySmall)
                Text("Deleting your data", style = MaterialTheme.typography.titleSmall)
                Text("Profile > Delete all app data permanently removes your profile, records, medications, documents, reminders, and security settings from this device. This cannot be undone.", style = MaterialTheme.typography.bodySmall)
                Text("Medical disclaimer", style = MaterialTheme.typography.titleSmall)
                Text("Vexel Health Passport organizes information you enter yourself. It does not diagnose conditions, interpret lab or clinical results, or recommend treatment. Always consult a qualified healthcare professional for medical decisions.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(onDismiss) { Text("Close") } },
    )
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
    var profileChecked by rememberSaveable { mutableStateOf(true) }
    var eventsChecked by rememberSaveable { mutableStateOf(true) }
    var medicationsChecked by rememberSaveable { mutableStateOf(true) }
    var documentsChecked by rememberSaveable { mutableStateOf(true) }
    var remindersChecked by rememberSaveable { mutableStateOf(true) }
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
private fun PinSetupDialog(vm: ProfileViewModel, onDismiss: () -> Unit) {
    var pin by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Set up PIN lock") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Use 4–12 digits. The PIN is never stored as plaintext.")
        OutlinedTextField(pin, { pin = it.filter(Char::isDigit).take(12) }, label = { Text("PIN") }, isError = error.isNotBlank())
        OutlinedTextField(confirmation, { confirmation = it.filter(Char::isDigit).take(12) }, label = { Text("Confirm PIN") }, isError = error.isNotBlank(), supportingText = { if (error.isNotBlank()) Text(error) })
    } }, confirmButton = { Button({ if (pin != confirmation) error = "PINs do not match" else if (pin.length !in 4..12) error = "Use 4–12 digits" else if (vm.savePin(pin, confirmation)) onDismiss() }) { Text("Enable") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}

@Composable
private fun CollapsibleSection(
    title: String,
    initialExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(initialExpanded) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (expanded) "Collapse" else "Expand",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content()
                }
            }
        }
    }
}

