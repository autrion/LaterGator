package com.latergator.app

import android.app.Application
import com.latergator.app.data.AppSettingsRepository
import com.latergator.app.notification.NotificationHelper

class LaterGatorApp : Application() {
    val settingsRepository by lazy { AppSettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
