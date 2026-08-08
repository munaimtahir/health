package com.vexel.passport.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private const val DateTimePattern = "yyyy-MM-dd HH:mm"

/**
 * Read-only date/time entry backed by Material3 [DatePicker] + [TimePicker], formatting the
 * result as "yyyy-MM-dd HH:mm" to match the string format callers already parse/store.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
) {
    var showPicker by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat(DateTimePattern, Locale.getDefault()).apply { isLenient = false } }
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            isError = isError,
            supportingText = supportingText,
            trailingIcon = {
                Icon(Icons.Filled.DateRange, contentDescription = "Choose date and time")
            },
            modifier = Modifier.fillMaxWidth(),
        )
        // OutlinedTextField consumes its own touch input for cursor/focus handling even when
        // readOnly, so an invisible overlay is needed to reliably catch the tap and open the
        // picker instead of just focusing the field.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = enabled,
                ) { showPicker = true },
        )
    }
    if (showPicker) {
        val initialMillis = remember(value) { runCatching { formatter.parse(value)?.time }.getOrNull() ?: System.currentTimeMillis() }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        val initialCalendar = remember(initialMillis) { Calendar.getInstance().apply { timeInMillis = initialMillis } }
        val timePickerState = rememberTimePickerState(
            initialHour = initialCalendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = initialCalendar.get(Calendar.MINUTE),
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(label) },
            text = {
                Column {
                    DatePicker(state = datePickerState, showModeToggle = false)
                    Spacer(Modifier.height(8.dp))
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        // DatePicker returns UTC midnight for the selected date; pull Y/M/D from
                        // that in UTC, then combine with the (local) TimePicker hour/minute.
                        val utcDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = selectedDateMillis }
                        val result = Calendar.getInstance().apply {
                            set(Calendar.YEAR, utcDate.get(Calendar.YEAR))
                            set(Calendar.MONTH, utcDate.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, utcDate.get(Calendar.DAY_OF_MONTH))
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onValueChange(formatter.format(result.time))
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                Row {
                    if (value.isNotBlank()) TextButton(onClick = { onValueChange(""); showPicker = false }) { Text("Clear") }
                    TextButton(onClick = { showPicker = false }) { Text("Cancel") }
                }
            },
        )
    }
}
