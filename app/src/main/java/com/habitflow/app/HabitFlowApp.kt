package com.habitflow.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.habitflow.app.util.Constants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HabitFlowApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_REMINDERS,
                getString(com.habitflow.app.R.string.channel_reminders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(com.habitflow.app.R.string.channel_reminders_desc)
                enableVibration(true)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
