package pk.vexel.healthpassport

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pk.vexel.healthpassport.core.database.HealthDatabase
import pk.vexel.healthpassport.core.database.HealthEventEntity
import pk.vexel.healthpassport.core.database.ProfileEntity
import pk.vexel.healthpassport.core.datastore.PreferencesStore
import pk.vexel.healthpassport.core.designsystem.InformationCard
import pk.vexel.healthpassport.core.designsystem.VexelHealthPassportTheme
import pk.vexel.healthpassport.core.model.SymptomDraft
import pk.vexel.healthpassport.core.model.validationErrors

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
) : ViewModel() {
    val events = database.healthEventDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val profile = database.profileDao().observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
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
            Scaffold(
                topBar = { TopAppBar(title = { Text("Vexel Health Passport") }) },
                bottomBar = { NavigationBar { destinations.forEachIndexed { index, destination ->
                    NavigationBarItem(index == selectedIndex, { selectedIndex = index }, icon = { Icon(destination.icon, destination.label) }, label = { Text(destination.label) })
                } } },
            ) { padding ->
                when (selectedIndex) {
                    0 -> HomeScreen(viewModel, profile, Modifier.padding(padding))
                    1 -> TimelineScreen(viewModel, Modifier.padding(padding))
                    2 -> CaptureScreen(viewModel, "RECORD", "Add a medical record", Modifier.padding(padding))
                    3 -> CaptureScreen(viewModel, "REMINDER", "Add a follow-up reminder", Modifier.padding(padding))
                    else -> ProfileScreen(viewModel, profile, Modifier.padding(padding))
                }
            }
        }
    }
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

@Composable private fun HomeScreen(vm: PassportViewModel, profile: ProfileEntity?, modifier: Modifier) {
    var showSymptom by rememberSaveable { mutableStateOf(false) }
    var showMedication by rememberSaveable { mutableStateOf(false) }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (profile?.name.isNullOrBlank()) "Welcome" else "Welcome, ${profile?.name}", style = MaterialTheme.typography.headlineSmall)
        Text("Keep your records together and ready for your next appointment.")
        InformationCard("Privacy", "Stored on this device. No account required.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button({ showSymptom = true }, Modifier.weight(1f)) { Text("Log symptom") }
            Button({ showMedication = true }, Modifier.weight(1f)) { Text("Add medication") }
        }
        if (showSymptom) CaptureDialog("SYMPTOM", "Log a symptom", { showSymptom = false }, vm::addEvent)
        if (showMedication) CaptureDialog("MEDICATION", "Add medication", { showMedication = false }, vm::addEvent)
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

@Composable private fun ProfileScreen(vm: PassportViewModel, profile: ProfileEntity?, modifier: Modifier) {
    var name by remember(profile?.name) { mutableStateOf(profile?.name.orEmpty()) }; var allergies by remember(profile?.allergies) { mutableStateOf(profile?.allergies.orEmpty()) }; var conditions by remember(profile?.conditions) { mutableStateOf(profile?.conditions.orEmpty()) }; val prefs by vm.settings.collectAsState()
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("Personal profile", style = MaterialTheme.typography.headlineSmall); OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") }); OutlinedTextField(allergies, { allergies = it }, Modifier.fillMaxWidth(), label = { Text("Allergies") }); OutlinedTextField(conditions, { conditions = it }, Modifier.fillMaxWidth(), label = { Text("Diagnoses or conditions") }); Button({ vm.saveProfile(ProfileEntity(name = name, allergies = allergies, conditions = conditions, updatedAtEpochMillis = System.currentTimeMillis())) }) { Text("Save profile") }; TextButton({ vm.setDarkTheme(!prefs.darkTheme) }) { Text(if (prefs.darkTheme) "Use light theme" else "Use dark theme") } }
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
