package com.latergator.app.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeOnlyPickerDialog(
    initialMinutes: Int,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (totalMinutes: Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialMinutes / 60,
        initialMinute = initialMinutes % 60,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(state.hour * 60 + state.minute)
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}

fun formatMinutes(totalMinutes: Int): String =
    "%02d:%02d".format(totalMinutes / 60, totalMinutes % 60)
