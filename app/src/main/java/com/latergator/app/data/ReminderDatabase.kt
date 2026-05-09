package com.latergator.app.data

import android.content.Context
import androidx.room.*

@Database(entities = [Reminder::class], version = 1, exportSchema = false)
@TypeConverters(ReminderConverters::class)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile private var instance: ReminderDatabase? = null

        fun getInstance(context: Context): ReminderDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "latergator.db"
                ).build().also { instance = it }
            }
    }
}

class ReminderConverters {
    @TypeConverter
    fun fromStatus(value: ReminderStatus): String = value.name

    @TypeConverter
    fun toStatus(value: String): ReminderStatus = ReminderStatus.valueOf(value)
}
