package com.latergator.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("latergator_settings")

enum class DarkModeOption { SYSTEM, LIGHT, DARK, SCHEDULED }

data class AppSettings(
    val darkMode: DarkModeOption = DarkModeOption.SYSTEM,
    val scheduledDarkStartMinutes: Int = 22 * 60,
    val scheduledDarkEndMinutes: Int = 7 * 60,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartMinutes: Int = 22 * 60,
    val quietHoursEndMinutes: Int = 7 * 60
)

class AppSettingsRepository(private val context: Context) {

    private object Keys {
        val DARK_MODE = stringPreferencesKey("dark_mode")
        val SCHED_DARK_START = intPreferencesKey("sched_dark_start")
        val SCHED_DARK_END = intPreferencesKey("sched_dark_end")
        val QUIET_ENABLED = booleanPreferencesKey("quiet_enabled")
        val QUIET_START = intPreferencesKey("quiet_start")
        val QUIET_END = intPreferencesKey("quiet_end")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            AppSettings(
                darkMode = DarkModeOption.valueOf(prefs[Keys.DARK_MODE] ?: "SYSTEM"),
                scheduledDarkStartMinutes = prefs[Keys.SCHED_DARK_START] ?: (22 * 60),
                scheduledDarkEndMinutes = prefs[Keys.SCHED_DARK_END] ?: (7 * 60),
                quietHoursEnabled = prefs[Keys.QUIET_ENABLED] ?: false,
                quietHoursStartMinutes = prefs[Keys.QUIET_START] ?: (22 * 60),
                quietHoursEndMinutes = prefs[Keys.QUIET_END] ?: (7 * 60)
            )
        }

    suspend fun updateDarkMode(option: DarkModeOption) {
        context.dataStore.edit { it[Keys.DARK_MODE] = option.name }
    }

    suspend fun updateScheduledDark(startMinutes: Int, endMinutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SCHED_DARK_START] = startMinutes
            prefs[Keys.SCHED_DARK_END] = endMinutes
        }
    }

    suspend fun updateQuietHours(enabled: Boolean, startMinutes: Int, endMinutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.QUIET_ENABLED] = enabled
            prefs[Keys.QUIET_START] = startMinutes
            prefs[Keys.QUIET_END] = endMinutes
        }
    }
}
