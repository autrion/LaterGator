package com.latergator.app.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.latergator.app.data.ReminderDatabase
import com.latergator.app.data.ReminderRepository
import com.latergator.app.data.ReminderStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                // Alarm ausgelöst → Benachrichtigung anzeigen
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
            val original = repo.getById(originalId.toInt())
            val newTime = System.currentTimeMillis() + delayMs
            val newReminder = original?.copy(id = 0, snoozeTargetTime = newTime, status = ReminderStatus.PENDING)
                ?: com.latergator.app.data.Reminder(
                    description = description,
                    snoozeTargetTime = newTime
                )
            val newId = repo.insert(newReminder)
            NotificationHelper.scheduleReminder(context, newId, newTime)
            repo.updateStatus(originalId.toInt(), ReminderStatus.IGNORED)
        }
    }
}
