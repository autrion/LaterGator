package com.latergator.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.latergator.app.data.ReminderDatabase
import com.latergator.app.data.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val repo = ReminderRepository(ReminderDatabase.getInstance(context).reminderDao())
        CoroutineScope(Dispatchers.IO).launch {
            repo.getPendingRemindersAfter(System.currentTimeMillis()).forEach { reminder ->
                NotificationHelper.scheduleReminder(context, reminder.id.toLong(), reminder.snoozeTargetTime)
            }
        }
    }
}
