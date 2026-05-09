package com.latergator.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE status = 'PENDING' ORDER BY snoozeTargetTime ASC")
    fun getPendingReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE status = 'PENDING' AND snoozeTargetTime > :now")
    suspend fun getPendingRemindersAfter(now: Long): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Query("UPDATE reminders SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: ReminderStatus)

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Int): Reminder?
}
