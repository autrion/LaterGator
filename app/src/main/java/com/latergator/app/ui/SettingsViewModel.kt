package com.latergator.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.latergator.app.data.AppSettings
import com.latergator.app.data.AppSettingsRepository
import com.latergator.app.data.DarkModeOption
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val repo: AppSettingsRepository
) : AndroidViewModel(application) {

    val settings: StateFlow<AppSettings> = repo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setDarkMode(option: DarkModeOption) {
        viewModelScope.launch { repo.updateDarkMode(option) }
    }

    fun setScheduledDark(startMinutes: Int, endMinutes: Int) {
        viewModelScope.launch { repo.updateScheduledDark(startMinutes, endMinutes) }
    }

    fun setQuietHours(enabled: Boolean, startMinutes: Int, endMinutes: Int) {
        viewModelScope.launch { repo.updateQuietHours(enabled, startMinutes, endMinutes) }
    }
}
