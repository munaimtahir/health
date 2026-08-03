package pk.vexel.healthpassport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pk.vexel.healthpassport.core.designsystem.InformationCard
import pk.vexel.healthpassport.core.designsystem.VexelHealthPassportTheme

private data class Destination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val destinations = listOf(
    Destination("Home", Icons.Outlined.Home),
    Destination("Timeline", Icons.Outlined.Event),
    Destination("Records", Icons.Outlined.Folder),
    Destination("Plan", Icons.Outlined.Schedule),
    Destination("Profile", Icons.Outlined.Person),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VexelHealthPassportApp() {
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    VexelHealthPassportTheme {
        Scaffold(
            topBar = { SmallTopAppBar(title = { Text("Vexel Health Passport") }) },
            bottomBar = {
                NavigationBar {
                    destinations.forEachIndexed { index, destination ->
                        NavigationBarItem(
                            selected = index == selectedIndex,
                            onClick = { selectedIndex = index },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            },
        ) { padding ->
            if (selectedIndex == 0) {
                HomeContent(Modifier.padding(padding))
            } else {
                PlaceholderContent(destinations[selectedIndex].label, Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun HomeContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Your health history, organized.")
        Text("A private, offline-first place for your health information.")
        InformationCard("Next follow-up", "No follow-ups recorded")
        InformationCard("Active symptoms", "No symptoms recorded")
        InformationCard("Recent report", "No reports recorded")
        InformationCard("Current medications", "No medications recorded")
        Text("Quick actions")
        listOf("Log symptom", "Add report", "Add medication", "Add consultation", "Add reminder")
            .forEach { action -> InformationCard(action, "Coming in a future sprint") }
    }
}

@Composable
private fun PlaceholderContent(label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(label)
        Text("This area is reserved for a future sprint.")
    }
}

