package com.latergator.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ReminderStatus { PENDING, COMPLETED, IGNORED }

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val snoozeTargetTime: Long,
    val createdTime: Long = System.currentTimeMillis(),
    val status: ReminderStatus = ReminderStatus.PENDING
)
