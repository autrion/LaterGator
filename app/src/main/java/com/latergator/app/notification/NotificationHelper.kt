package com.latergator.app.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.latergator.app.R

object NotificationHelper {
    const val CHANNEL_ID = "latergator_reminders"
    const val EXTRA_REMINDER_ID = "reminder_id"
    const val EXTRA_DESCRIPTION = "description"
    const val ACTION_COMPLETE = "com.latergator.app.ACTION_COMPLETE"
    const val ACTION_SNOOZE_10 = "com.latergator.app.ACTION_SNOOZE_10"
    const val ACTION_SNOOZE_2H = "com.latergator.app.ACTION_SNOOZE_2H"
    const val ACTION_DISMISS_RESNOOZE = "com.latergator.app.ACTION_DISMISS_RESNOOZE"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "LaterGator Erinnerungen",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Wiedervorlage-Erinnerungen"
                enableVibration(true)
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(context: Context, reminderId: Long, triggerAtMillis: Long) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Fallback wenn exakte Alarme nicht erlaubt — trotzdem planen
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancelReminder(context: Context, reminderId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            context.getSystemService(AlarmManager::class.java).cancel(it)
        }
    }

    fun showReminderNotification(context: Context, reminderId: Long, description: String) {
        fun actionIntent(action: String, requestCode: Int): PendingIntent {
            val i = Intent(context, ReminderReceiver::class.java).apply {
                this.action = action
                putExtra(EXTRA_REMINDER_ID, reminderId)
                putExtra(EXTRA_DESCRIPTION, description)
            }
            return PendingIntent.getBroadcast(
                context, requestCode, i,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val base = (reminderId * 10).toInt()
        val completePi = actionIntent(ACTION_COMPLETE, base + 1)
        val snooze10Pi = actionIntent(ACTION_SNOOZE_10, base + 2)
        val snooze2hPi = actionIntent(ACTION_SNOOZE_2H, base + 3)
        // Wenn Benachrichtigung weggewischt wird → automatisch nach 2h nochmal erinnern
        val dismissPi = actionIntent(ACTION_DISMISS_RESNOOZE, base + 4)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🐊 LaterGator")
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDeleteIntent(dismissPi)
            .addAction(0, "✓ Erledigt", completePi)
            .addAction(0, "+10 Min", snooze10Pi)
            .addAction(0, "+2 Std", snooze2hPi)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(reminderId.toInt(), notification)
    }
}
