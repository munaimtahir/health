package com.vexel.passport.feature.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** First-launch acknowledgement of the app's offline, non-diagnostic scope; gates entry into the main shell. */
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var acknowledged by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Step 1 of 1", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text("Welcome to Vexel", style = MaterialTheme.typography.headlineLarge)
        Text("Your health history, organized.", style = MaterialTheme.typography.titleLarge)
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("A calm, offline-first place to organize your personal health information.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Your entries are user-recorded. This app does not diagnose or replace advice from a qualified healthcare professional.", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = acknowledged, onCheckedChange = { acknowledged = it })
            Text("I understand and want to continue", Modifier.clickable { acknowledged = !acknowledged })
        }
        Button(onClick = onComplete, enabled = acknowledged, modifier = Modifier.fillMaxWidth()) { Text("Continue") }
    }
}
