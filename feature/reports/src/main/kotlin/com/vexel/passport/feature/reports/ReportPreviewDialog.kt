package com.vexel.passport.feature.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReportPreviewDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Health Report Guide") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("This app generates offline PDF reports based on data you enter manually. PDF reports include generation timestamp, date filtering scope, and standard disclaimers.", style = MaterialTheme.typography.bodyMedium)
                Text("Medically Neutral Disclaimer", style = MaterialTheme.typography.titleSmall)
                Text("Vexel Health Passport organizes user-entered records only. Content does not constitute professional medical advice, diagnosis, treatment, or clinical recommendations.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
