package com.latergator.app.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Reminder::class], version = 2, exportSchema = false)
@TypeConverters(ReminderConverters::class)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile private var instance: ReminderDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN placeType TEXT")
            }
        }

        fun getInstance(context: Context): ReminderDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "latergator.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}

class ReminderConverters {
    @TypeConverter
    fun fromStatus(value: ReminderStatus): String = value.name

    @TypeConverter
    fun toStatus(value: String): ReminderStatus = ReminderStatus.valueOf(value)
}
