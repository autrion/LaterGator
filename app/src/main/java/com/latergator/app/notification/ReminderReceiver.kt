package com.latergator.app.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.latergator.app.LaterGatorApp
import com.latergator.app.data.Reminder
import com.latergator.app.data.ReminderDatabase
import com.latergator.app.data.ReminderRepository
import com.latergator.app.data.ReminderStatus
import com.latergator.app.ui.ReminderViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        val repo = ReminderRepository(ReminderDatabase.getInstance(context).reminderDao())

        when (intent.action) {
            NotificationHelper.ACTION_COMPLETE -> {
                cancelNotification(context, reminderId)
                CoroutineScope(Dispatchers.IO).launch {
                    repo.updateStatus(reminderId.toInt(), ReminderStatus.COMPLETED)
                }
            }

            NotificationHelper.ACTION_SNOOZE_10 -> {
                cancelNotification(context, reminderId)
                resnooze(context, repo, reminderId, 10 * 60 * 1000L, intent)
            }

            NotificationHelper.ACTION_SNOOZE_2H,
            NotificationHelper.ACTION_DISMISS_RESNOOZE -> {
                cancelNotification(context, reminderId)
                resnooze(context, repo, reminderId, 2 * 60 * 60 * 1000L, intent)
            }

            else -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val reminder = repo.getById(reminderId.toInt()) ?: return@launch
                    if (reminder.status == ReminderStatus.PENDING) {
                        NotificationHelper.showReminderNotification(
                            context, reminderId, reminder.description
                        )
                    }
                }
            }
        }
    }

    private fun cancelNotification(context: Context, reminderId: Long) {
        context.getSystemService(NotificationManager::class.java).cancel(reminderId.toInt())
    }

    private fun resnooze(
        context: Context,
        repo: ReminderRepository,
        originalId: Long,
        delayMs: Long,
        intent: Intent
    ) {
        val description = intent.getStringExtra(NotificationHelper.EXTRA_DESCRIPTION) ?: ""
        CoroutineScope(Dispatchers.IO).launch {
            val settings = runBlocking {
                (context.applicationContext as LaterGatorApp).settingsRepository.settingsFlow.first()
            }
            val rawTime = System.currentTimeMillis() + delayMs
            val adjustedTime = ReminderViewModel.applyQuietHours(rawTime, settings)
            val original = repo.getById(originalId.toInt())
            val newReminder = original?.copy(id = 0, snoozeTargetTime = adjustedTime, status = ReminderStatus.PENDING)
                ?: Reminder(description = description, snoozeTargetTime = adjustedTime)
            val newId = repo.insert(newReminder)
            NotificationHelper.scheduleReminder(context, newId, adjustedTime)
            repo.updateStatus(originalId.toInt(), ReminderStatus.IGNORED)
        }
    }
}
