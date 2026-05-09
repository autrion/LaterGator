package com.latergator.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.latergator.app.data.Reminder
import com.latergator.app.data.ReminderRepository
import com.latergator.app.data.ReminderStatus
import com.latergator.app.notification.NotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderViewModel(
    application: Application,
    private val repository: ReminderRepository
) : AndroidViewModel(application) {

    val pendingReminders: StateFlow<List<Reminder>> = repository.getPendingReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveReminder(description: String, snoozeTargetTime: Long) {
        if (description.isBlank()) return
        viewModelScope.launch {
            val reminder = Reminder(
                description = description.trim(),
                snoozeTargetTime = snoozeTargetTime
            )
            val id = repository.insert(reminder)
            NotificationHelper.scheduleReminder(getApplication(), id, snoozeTargetTime)
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
            val newTime = System.currentTimeMillis() + addMillis
            repository.update(reminder.copy(snoozeTargetTime = newTime))
            NotificationHelper.cancelReminder(getApplication(), reminder.id.toLong())
            NotificationHelper.scheduleReminder(getApplication(), reminder.id.toLong(), newTime)
        }
    }

    companion object {
        const val TWO_HOURS_MS = 2 * 60 * 60 * 1000L

        fun todayEvening(): Long = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        fun tomorrowMorning(): Long = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        fun nextWeekMonday(): Long = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, 1)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
