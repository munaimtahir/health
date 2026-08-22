package com.vexel.passport.feature.medications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vexel.passport.core.database.MedicationEntity
import com.vexel.passport.core.designsystem.StatusPill

@Composable
fun MedicationStatusCard(medication: MedicationEntity, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(medication.name, style = MaterialTheme.typography.titleMedium)
                StatusPill(medication.status.lowercase().replaceFirstChar { it.uppercase() })
            }
            if (medication.genericName.isNotBlank()) {
                Text("Generic: ${medication.genericName}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                "Regimen: ${medication.strength} · ${medication.dose} ${medication.unit} · ${medication.frequency}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (medication.notes.isNotBlank()) {
                Text(medication.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
