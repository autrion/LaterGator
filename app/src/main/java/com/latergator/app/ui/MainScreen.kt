package com.latergator.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.latergator.app.data.Reminder
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ReminderViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var descriptionText by remember { mutableStateOf("") }
    var showGator by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }
    val pendingReminders by viewModel.pendingReminders.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Custom time picker state
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var showCustomTimePicker by remember { mutableStateOf(false) }
    var customPickedDateMs by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(showGator) {
        if (showGator) { delay(2_200); showGator = false }
    }

    fun saveWith(targetTime: Long) {
        viewModel.saveReminder(descriptionText, targetTime)
        descriptionText = ""
        showGator = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateToHistory) { Text("📋", fontSize = 20.sp) }
            Text(
                text = "🐊 LaterGator",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = onNavigateToSettings) { Text("⚙️", fontSize = 22.sp) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Eingabe-Card ──────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    placeholder = {
                        Text(
                            "Was musst du später tun?",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (descriptionText.isNotBlank())
                            saveWith(System.currentTimeMillis() + ReminderViewModel.TWO_HOURS_MS)
                    })
                )

                Spacer(modifier = Modifier.height(14.dp))

                val inputEnabled = descriptionText.isNotBlank()
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SnoozeButton("+2h", inputEnabled, isPrimary = true) {
                        saveWith(System.currentTimeMillis() + ReminderViewModel.TWO_HOURS_MS)
                    }
                    SnoozeButton("+Heute Abend", inputEnabled) { saveWith(ReminderViewModel.todayEvening()) }
                    SnoozeButton("+Morgen früh", inputEnabled) { saveWith(ReminderViewModel.tomorrowMorning()) }
                    SnoozeButton("+Nächste Woche", inputEnabled) { saveWith(ReminderViewModel.nextWeekMonday()) }
                    SnoozeButton("+Eigene Zeit…", inputEnabled) { showCustomDatePicker = true }
                }
            }
        }

        // ── Gator-Feedback ───────────────────────────────────────────
        AnimatedVisibility(
            visible = showGator,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🐊", fontSize = 56.sp)
                Text(
                    "Hab's! Ich erinner dich!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // ── Offene Erinnerungen ──────────────────────────────────────
        if (!showGator && pendingReminders.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ausstehend (${pendingReminders.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pendingReminders, key = { it.id }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onEdit = { editingReminder = it },
                        onComplete = { viewModel.completeReminder(it) },
                        onSnooze2h = { viewModel.snoozeReminder(it, ReminderViewModel.TWO_HOURS_MS) }
                    )
                }
            }
        }
    }

    editingReminder?.let { reminder ->
        EditReminderDialog(
            reminder = reminder,
            onSave = { desc, time ->
                viewModel.updateReminder(reminder, desc, time)
                editingReminder = null
            },
            onDismiss = { editingReminder = null }
        )
    }

    // ── Custom Zeit-Picker ────────────────────────────────────────────
    if (showCustomDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCustomDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    customPickedDateMs = datePickerState.selectedDateMillis
                    showCustomDatePicker = false
                    showCustomTimePicker = true
                }) { Text("Weiter →") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDatePicker = false }) { Text("Abbrechen") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCustomTimePicker) {
        val now = Calendar.getInstance()
        val timePickerState = rememberTimePickerState(
            initialHour = now.get(Calendar.HOUR_OF_DAY),
            initialMinute = now.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showCustomTimePicker = false },
            title = { Text("Uhrzeit wählen") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val dateMs = customPickedDateMs ?: System.currentTimeMillis()
                    val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
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
                    showCustomTimePicker = false
                    if (descriptionText.isNotBlank()) saveWith(localCal.timeInMillis)
                }) { Text("Speichern") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTimePicker = false }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
private fun SnoozeButton(
    label: String,
    enabled: Boolean,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        colors = if (isPrimary)
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        else
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
            ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    onEdit: (Reminder) -> Unit,
    onComplete: (Reminder) -> Unit,
    onSnooze2h: (Reminder) -> Unit
) {
    val formatter = remember { SimpleDateFormat("EEE, dd.MM. · HH:mm 'Uhr'", Locale.GERMAN) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(reminder.description, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        formatter.format(Date(reminder.snoozeTargetTime)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (reminder.placeType != null) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            tonalElevation = 0.dp
                        ) {
                            Text(
                                reminder.placeType,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            IconButton(onClick = { onEdit(reminder) }) { Text("✏️", fontSize = 20.sp) }
            IconButton(onClick = { onSnooze2h(reminder) }) { Text("⏰", fontSize = 20.sp) }
            IconButton(onClick = { onComplete(reminder) }) { Text("✅", fontSize = 20.sp) }
        }
    }
}
