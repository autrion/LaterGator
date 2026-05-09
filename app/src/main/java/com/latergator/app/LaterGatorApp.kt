package com.latergator.app

import android.app.Application
import com.latergator.app.notification.NotificationHelper

class LaterGatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
