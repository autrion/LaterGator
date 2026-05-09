package com.latergator.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.latergator.app.data.Reminder
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditReminderDialog(
    reminder: Reminder,
    onSave: (description: String, newTime: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("EEE, dd.MM.yyyy · HH:mm 'Uhr'", Locale.GERMAN) }

    var editedDescription by remember { mutableStateOf(reminder.description) }
    var selectedTime by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pickedDateMs by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Erinnerung bearbeiten", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = editedDescription,
                    onValueChange = { editedDescription = it },
                    label = { Text("Beschreibung") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                val displayTime = selectedTime ?: reminder.snoozeTargetTime
                Text(
                    text = "Fällig: ${formatter.format(Date(displayTime))}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (selectedTime != null) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTime != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                Text(
                    text = "Neue Zeit wählen:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = { selectedTime = System.currentTimeMillis() + ReminderViewModel.TWO_HOURS_MS },
                        label = { Text("+2h") }
                    )
                    SuggestionChip(
                        onClick = { selectedTime = ReminderViewModel.todayEvening() },
                        label = { Text("+Heute Abend") }
                    )
                    SuggestionChip(
                        onClick = { selectedTime = ReminderViewModel.tomorrowMorning() },
                        label = { Text("+Morgen früh") }
                    )
                    SuggestionChip(
                        onClick = { selectedTime = ReminderViewModel.nextWeekMonday() },
                        label = { Text("+Nächste Woche") }
                    )
                    SuggestionChip(
                        onClick = { showDatePicker = true },
                        label = { Text("Datum & Uhrzeit …") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = editedDescription.isNotBlank(),
                onClick = { onSave(editedDescription, selectedTime ?: reminder.snoozeTargetTime) }
            ) { Text("Speichern") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = reminder.snoozeTargetTime
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickedDateMs = datePickerState.selectedDateMillis
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Weiter →") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Abbrechen") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val initCal = Calendar.getInstance().apply { timeInMillis = reminder.snoozeTargetTime }
        val timePickerState = rememberTimePickerState(
            initialHour = initCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = initCal.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Uhrzeit wählen") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val dateMs = pickedDateMs ?: System.currentTimeMillis()
                    // DatePicker liefert UTC-Mitternacht → in lokales Datum+Uhrzeit umrechnen
                    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = dateMs
                    }
                    val localCal = Calendar.getInstance().apply {
                        set(
                            utcCal.get(Calendar.YEAR),
                            utcCal.get(Calendar.MONTH),
                            utcCal.get(Calendar.DAY_OF_MONTH),
                            timePickerState.hour,
                            timePickerState.minute,
                            0
                        )
                        set(Calendar.MILLISECOND, 0)
                    }
                    selectedTime = localCal.timeInMillis
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Abbrechen") }
            }
        )
    }
}
