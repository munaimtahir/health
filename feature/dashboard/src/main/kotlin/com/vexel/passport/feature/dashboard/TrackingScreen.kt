package com.vexel.passport.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vexel.passport.core.database.MeasurementEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val METRICS = listOf(
    "BLOOD_PRESSURE" to "Blood Pressure",
    "BLOOD_GLUCOSE" to "Blood Glucose",
    "TEMPERATURE" to "Temperature",
    "WEIGHT" to "Weight",
    "PULSE" to "Pulse Rate",
    "SPO2" to "Oxygen Saturation",
    "RESPIRATORY_RATE" to "Respiration Rate"
)

private val PERIODS = listOf(
    7 to "7 Days",
    30 to "30 Days",
    90 to "90 Days",
    365 to "1 Year",
    0 to "All Time"
)

@Composable
fun TrackingScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val measurements by viewModel.measurements.collectAsState()
    TrackingScreen(
        measurements = measurements,
        modifier = modifier,
        onDeleteMeasurement = viewModel::deleteMeasurement
    )
}

@Composable
fun TrackingScreen(
    measurements: List<MeasurementEntity>,
    modifier: Modifier,
    onDeleteMeasurement: (String) -> Unit
) {
    var selectedMetric by rememberSaveable { mutableStateOf("BLOOD_PRESSURE") }
    var selectedPeriodDays by rememberSaveable { mutableStateOf(30) }

    val filteredList = remember(measurements, selectedMetric, selectedPeriodDays) {
        val now = System.currentTimeMillis()
        val limitMillis = if (selectedPeriodDays > 0) now - (selectedPeriodDays * 24 * 60 * 60 * 1000L) else 0L
        measurements
            .filter { it.type == selectedMetric && it.recordedAtEpochMillis >= limitMillis }
            .sortedByDescending { it.recordedAtEpochMillis }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Text("Health Metrics & Tracking", style = MaterialTheme.typography.headlineSmall)
            Text("Track your vitals over time in a secure offline log.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Horizontal Metric Selector
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                METRICS.forEach { (metricId, metricLabel) ->
                    FilterChip(
                        selected = selectedMetric == metricId,
                        onClick = { selectedMetric = metricId },
                        label = { Text(metricLabel) }
                    )
                }
            }
        }

        // Horizontal Period Selector
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PERIODS.forEach { (days, label) ->
                    FilterChip(
                        selected = selectedPeriodDays == days,
                        onClick = { selectedPeriodDays = days },
                        label = { Text(label) }
                    )
                }
            }
        }

        // Interactive Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${METRICS.find { it.first == selectedMetric }?.second} Chart",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (filteredList.size >= 2) {
                        MetricChart(readings = filteredList.sortedBy { it.recordedAtEpochMillis })
                    } else {
                        Box(
                            Modifier.fillMaxWidth().height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("At least 2 points are required to render trend charts.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // History Log List
        item {
            Text("History Logs", style = MaterialTheme.typography.titleMedium)
        }

        if (filteredList.isEmpty()) {
            item {
                Text("No logged data for the selected filter criteria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(filteredList, key = { it.id }) { reading ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val valueStr = if (reading.secondaryValue != null) {
                                "${reading.primaryValue}/${reading.secondaryValue} ${reading.unit}"
                            } else {
                                "${reading.primaryValue} ${reading.unit}"
                            }
                            Text(valueStr, style = MaterialTheme.typography.titleMedium)
                            Text(
                                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(reading.recordedAtEpochMillis)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (reading.context.isNotBlank()) {
                                Text(reading.context, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        TextButton(onClick = { onDeleteMeasurement(reading.id) }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricChart(readings: List<MeasurementEntity>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)

    Canvas(
        modifier = Modifier.fillMaxWidth().height(180.dp)
    ) {
        val width = size.width
        val height = size.height

        val paddingLeft = 60f
        val paddingRight = 20f
        val paddingTop = 20f
        val paddingBottom = 40f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        val primaryValues = readings.map { it.primaryValue }
        val minVal = primaryValues.minOrNull() ?: 0.0
        val maxVal = primaryValues.maxOrNull() ?: 100.0
        val valRange = (maxVal - minVal).coerceAtLeast(1.0)

        val times = readings.map { it.recordedAtEpochMillis }
        val minTime = times.minOrNull() ?: 0L
        val maxTime = times.maxOrNull() ?: 1L
        val timeRange = (maxTime - minTime).coerceAtLeast(1L)

        // Draw horizontal grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val ratio = i.toFloat() / gridLines
            val y = paddingTop + ratio * chartHeight
            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw path connecting data points
        val points = readings.map { reading ->
            val xRatio = if (timeRange > 1) ((reading.recordedAtEpochMillis - minTime).toDouble() / timeRange).toFloat() else 0.5f
            val yRatio = ((reading.primaryValue - minVal) / valRange).toFloat()

            val x = paddingLeft + xRatio * chartWidth
            val y = paddingTop + (1f - yRatio) * chartHeight
            Offset(x, y)
        }

        val fillPath = Path()
        val linePath = Path()

        if (points.isNotEmpty()) {
            linePath.moveTo(points[0].x, points[0].y)
            fillPath.moveTo(points[0].x, points[0].y)

            for (i in 1 until points.size) {
                linePath.lineTo(points[i].x, points[i].y)
                fillPath.lineTo(points[i].x, points[i].y)
            }

            fillPath.lineTo(points.last().x, paddingTop + chartHeight)
            fillPath.lineTo(points.first().x, paddingTop + chartHeight)
            fillPath.close()

            // Draw area gradient
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.4f), Color.Transparent),
                    startY = paddingTop,
                    endY = paddingTop + chartHeight
                )
            )

            // Draw primary line
            drawPath(
                path = linePath,
                color = primaryColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )

            // Draw points
            points.forEach { point ->
                drawCircle(
                    color = primaryColor,
                    radius = 5.dp.toPx(),
                    center = point
                )
            }
        }
    }
}
