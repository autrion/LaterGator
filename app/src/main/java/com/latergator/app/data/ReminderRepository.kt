package com.latergator.app.data

import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val dao: ReminderDao) {
    fun getPendingReminders(): Flow<List<Reminder>> = dao.getPendingReminders()
    suspend fun getPendingRemindersAfter(now: Long): List<Reminder> = dao.getPendingRemindersAfter(now)
    suspend fun insert(reminder: Reminder): Long = dao.insert(reminder)
    suspend fun update(reminder: Reminder) = dao.update(reminder)
    suspend fun updateStatus(id: Int, status: ReminderStatus) = dao.updateStatus(id, status)
    suspend fun getById(id: Int): Reminder? = dao.getById(id)
    fun getHistoryReminders(): Flow<List<Reminder>> = dao.getHistoryReminders()
}
