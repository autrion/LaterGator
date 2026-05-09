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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.latergator.app.data.Reminder
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(viewModel: ReminderViewModel, modifier: Modifier = Modifier) {
    var descriptionText by remember { mutableStateOf("") }
    var showGator by remember { mutableStateOf(false) }
    val pendingReminders by viewModel.pendingReminders.collectAsState()

    LaunchedEffect(showGator) {
        if (showGator) {
            delay(2_200)
            showGator = false
        }
    }

    fun saveWith(targetTime: Long) {
        viewModel.saveReminder(descriptionText, targetTime)
        descriptionText = ""
        showGator = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 8.dp)
    ) {
        Text(
            text = "🐊 LaterGator",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

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
                        if (descriptionText.isNotBlank()) {
                            saveWith(System.currentTimeMillis() + ReminderViewModel.TWO_HOURS_MS)
                        }
                    })
                )

                Spacer(modifier = Modifier.height(14.dp))

                val inputEnabled = descriptionText.isNotBlank()

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SnoozeButton(
                        label = "+2h",
                        enabled = inputEnabled,
                        isPrimary = true,
                        onClick = { saveWith(System.currentTimeMillis() + ReminderViewModel.TWO_HOURS_MS) }
                    )
                    SnoozeButton(
                        label = "+Heute Abend",
                        enabled = inputEnabled,
                        onClick = { saveWith(ReminderViewModel.todayEvening()) }
                    )
                    SnoozeButton(
                        label = "+Morgen früh",
                        enabled = inputEnabled,
                        onClick = { saveWith(ReminderViewModel.tomorrowMorning()) }
                    )
                    SnoozeButton(
                        label = "+Nächste Woche",
                        enabled = inputEnabled,
                        onClick = { saveWith(ReminderViewModel.nextWeekMonday()) }
                    )
                }
            }
        }

        // Gator-Feedback nach dem Speichern
        AnimatedVisibility(
            visible = showGator,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🐊", fontSize = 56.sp)
                Text(
                    text = "Hab's! Ich erinner dich!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

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
                        onComplete = { viewModel.completeReminder(it) },
                        onSnooze2h = { viewModel.snoozeReminder(it, ReminderViewModel.TWO_HOURS_MS) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SnoozeButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    isPrimary: Boolean = false
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
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatter.format(Date(reminder.snoozeTargetTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = { onSnooze2h(reminder) }) {
                    Text("⏰", fontSize = 22.sp)
                }
                IconButton(onClick = { onComplete(reminder) }) {
                    Text("✅", fontSize = 22.sp)
                }
            }
        }
    }
}
