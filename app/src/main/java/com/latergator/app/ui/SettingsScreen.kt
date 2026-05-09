package com.latergator.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.latergator.app.data.DarkModeOption

private enum class EditingTimeSlot {
    DARK_START, DARK_END, QUIET_START, QUIET_END
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    var editingSlot by remember { mutableStateOf<EditingTimeSlot?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Text("←", fontSize = 22.sp) }
            Text("Einstellungen", style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Erscheinungsbild ──────────────────────────────────────────
        SettingCard(title = "Erscheinungsbild") {
            Text(
                text = "Dunkler Modus",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            val darkOptions = listOf(
                DarkModeOption.SYSTEM to "System",
                DarkModeOption.LIGHT to "Hell",
                DarkModeOption.DARK to "Dunkel",
                DarkModeOption.SCHEDULED to "Zeitplan"
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                darkOptions.forEachIndexed { index, (option, label) ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = darkOptions.size),
                        onClick = { viewModel.setDarkMode(option) },
                        selected = settings.darkMode == option,
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            if (settings.darkMode == DarkModeOption.SCHEDULED) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Dunkel-Zeitraum",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TimeRangeRow(
                    startMinutes = settings.scheduledDarkStartMinutes,
                    endMinutes = settings.scheduledDarkEndMinutes,
                    onStartClick = { editingSlot = EditingTimeSlot.DARK_START },
                    onEndClick = { editingSlot = EditingTimeSlot.DARK_END }
                )
            }
        }

        // ── Ruhemodus ────────────────────────────────────────────────
        SettingCard(title = "Ruhemodus") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Keine Benachrichtigungen", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Erinnerungen werden auf nach dem Ruhemodus verschoben",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.quietHoursEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.setQuietHours(
                            enabled,
                            settings.quietHoursStartMinutes,
                            settings.quietHoursEndMinutes
                        )
                    }
                )
            }

            if (settings.quietHoursEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Ruhemodus-Zeitraum",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TimeRangeRow(
                    startMinutes = settings.quietHoursStartMinutes,
                    endMinutes = settings.quietHoursEndMinutes,
                    onStartClick = { editingSlot = EditingTimeSlot.QUIET_START },
                    onEndClick = { editingSlot = EditingTimeSlot.QUIET_END }
                )
            }
        }
    }

    editingSlot?.let { slot ->
        val (currentMinutes, title) = when (slot) {
            EditingTimeSlot.DARK_START -> settings.scheduledDarkStartMinutes to "Dunkel ab"
            EditingTimeSlot.DARK_END -> settings.scheduledDarkEndMinutes to "Hell ab"
            EditingTimeSlot.QUIET_START -> settings.quietHoursStartMinutes to "Ruhemodus ab"
            EditingTimeSlot.QUIET_END -> settings.quietHoursEndMinutes to "Ruhemodus bis"
        }
        TimeOnlyPickerDialog(
            initialMinutes = currentMinutes,
            title = title,
            onDismiss = { editingSlot = null },
            onConfirm = { newMinutes ->
                when (slot) {
                    EditingTimeSlot.DARK_START -> viewModel.setScheduledDark(newMinutes, settings.scheduledDarkEndMinutes)
                    EditingTimeSlot.DARK_END -> viewModel.setScheduledDark(settings.scheduledDarkStartMinutes, newMinutes)
                    EditingTimeSlot.QUIET_START -> viewModel.setQuietHours(true, newMinutes, settings.quietHoursEndMinutes)
                    EditingTimeSlot.QUIET_END -> viewModel.setQuietHours(true, settings.quietHoursStartMinutes, newMinutes)
                }
            }
        )
    }
}

@Composable
private fun SettingCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun TimeRangeRow(
    startMinutes: Int,
    endMinutes: Int,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Von", style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onStartClick) {
            Text(
                formatMinutes(startMinutes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text("bis", style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onEndClick) {
            Text(
                formatMinutes(endMinutes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
