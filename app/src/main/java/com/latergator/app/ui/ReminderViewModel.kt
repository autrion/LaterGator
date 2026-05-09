package com.latergator.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.latergator.app.data.*
import com.latergator.app.notification.NotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderViewModel(
    application: Application,
    private val repository: ReminderRepository,
    private val settingsRepository: AppSettingsRepository
) : AndroidViewModel(application) {

    val pendingReminders: StateFlow<List<Reminder>> = repository.getPendingReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun saveReminder(description: String, snoozeTargetTime: Long) {
        if (description.isBlank()) return
        viewModelScope.launch {
            val adjusted = applyQuietHours(snoozeTargetTime, settings.value)
            val id = repository.insert(Reminder(description = description.trim(), snoozeTargetTime = adjusted))
            NotificationHelper.scheduleReminder(getApplication(), id, adjusted)
        }
    }

    fun updateReminder(reminder: Reminder, newDescription: String, newTime: Long) {
        viewModelScope.launch {
            val adjusted = applyQuietHours(newTime, settings.value)
            repository.update(reminder.copy(description = newDescription.trim(), snoozeTargetTime = adjusted))
            NotificationHelper.cancelReminder(getApplication(), reminder.id.toLong())
            NotificationHelper.scheduleReminder(getApplication(), reminder.id.toLong(), adjusted)
        }
    }

    fun completeReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateStatus(reminder.id, ReminderStatus.COMPLETED)
            NotificationHelper.cancelReminder(getApplication(), reminder.id.toLong())
        }
    }

    fun snoozeReminder(reminder: Reminder, addMillis: Long) {
        viewModelScope.launch {
            val adjusted = applyQuietHours(System.currentTimeMillis() + addMillis, settings.value)
            repository.update(reminder.copy(snoozeTargetTime = adjusted))
            NotificationHelper.cancelReminder(getApplication(), reminder.id.toLong())
            NotificationHelper.scheduleReminder(getApplication(), reminder.id.toLong(), adjusted)
        }
    }

    companion object {
        const val TWO_HOURS_MS = 2 * 60 * 60 * 1000L

        fun applyQuietHours(triggerMs: Long, settings: AppSettings): Long {
            if (!settings.quietHoursEnabled) return triggerMs
            val cal = Calendar.getInstance().apply { timeInMillis = triggerMs }
            val minuteOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            val start = settings.quietHoursStartMinutes
            val end = settings.quietHoursEndMinutes
            val inQuiet = if (start > end) minuteOfDay >= start || minuteOfDay < end
                          else minuteOfDay in start until end
            if (!inQuiet) return triggerMs
            val reschedCal = Calendar.getInstance().apply {
                timeInMillis = triggerMs
                set(Calendar.HOUR_OF_DAY, end / 60)
                set(Calendar.MINUTE, end % 60)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // Nächster Tagesanbruch falls Endzeit bereits verstrichen
                if (timeInMillis <= triggerMs) add(Calendar.DAY_OF_YEAR, 1)
            }
            return reschedCal.timeInMillis
        }

        fun todayEvening(): Long = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 19); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        fun tomorrowMorning(): Long = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 8); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        fun nextWeekMonday(): Long = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, 1)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
