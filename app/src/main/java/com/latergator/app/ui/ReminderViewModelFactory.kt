package com.latergator.app.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.latergator.app.data.AppSettingsRepository
import com.latergator.app.data.ReminderRepository

class ReminderViewModelFactory(
    private val application: Application,
    private val repository: ReminderRepository,
    private val settingsRepository: AppSettingsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ReminderViewModel::class.java))
        return ReminderViewModel(application, repository, settingsRepository) as T
    }
}
