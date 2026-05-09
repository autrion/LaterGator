package com.latergator.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.latergator.app.MainActivity
import com.latergator.app.R
import com.latergator.app.data.Reminder
import com.latergator.app.data.ReminderDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LaterGatorWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminders = ReminderDatabase.getInstance(context).reminderDao()
                    .getPendingRemindersAfter(0)
                for (id in appWidgetIds) {
                    applyViews(context, appWidgetManager, id, reminders)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private val formatter = SimpleDateFormat("EEE HH:mm", Locale.GERMAN)

        fun requestUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, LaterGatorWidget::class.java))
            if (ids.isEmpty()) return
            val intent = Intent(context, LaterGatorWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }

        fun applyViews(context: Context, manager: AppWidgetManager, widgetId: Int, reminders: List<Reminder>) {
            val count = reminders.size
            val views = RemoteViews(context.packageName, R.layout.widget_latergator)

            views.setTextViewText(R.id.widget_count, count.toString())
            views.setTextViewText(
                R.id.widget_label,
                if (count == 1) "offen" else "offen"
            )
            views.setTextViewText(
                R.id.widget_next,
                when {
                    count == 0 -> "Alles erledigt! ✓"
                    else -> {
                        val next = reminders.minByOrNull { it.snoozeTargetTime }!!
                        "${formatter.format(Date(next.snoozeTargetTime))} · ${next.description}"
                    }
                }
            )

            val launchIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            manager.updateAppWidget(widgetId, views)
        }
    }
}
