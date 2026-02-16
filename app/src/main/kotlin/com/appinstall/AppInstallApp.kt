package com.appinstall

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppInstallApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val installChannel = NotificationChannel(
                CHANNEL_INSTALL,
                getString(R.string.notification_channel_install),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_install_desc)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(installChannel)
        }
    }
    
    companion object {
        const val CHANNEL_INSTALL = "install_channel"
        const val NOTIFICATION_ID_INSTALL = 1001
    }
}
