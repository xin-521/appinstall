package com.appinstall.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.app.NotificationCompat
import com.appinstall.AppInstallApp
import com.appinstall.MainActivity
import com.appinstall.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstallNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    private val packageManager = context.packageManager
    
    fun showInstallingNotification(packageName: String?, appName: String?) {
        val notification = createNotificationBuilder(
            title = appName ?: packageName ?: context.getString(R.string.install),
            message = context.getString(R.string.installing),
            ongoing = true,
            progress = 0
        ).build()
        
        packageName?.let {
            notificationManager.notify(it.hashCode(), notification)
        } ?: notificationManager.notify(AppInstallApp.NOTIFICATION_ID_INSTALL, notification)
    }
    
    fun showInstallSuccessNotification(packageName: String?) {
        val appName = getAppName(packageName)
        
        val intent = packageManager.getLaunchIntentForPackage(packageName ?: "")
        val pendingIntent = if (intent != null) {
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            null
        }
        
        val notification = createNotificationBuilder(
            title = appName ?: context.getString(R.string.install),
            message = context.getString(R.string.install_success),
            ongoing = false
        ).apply {
            pendingIntent?.let { setContentIntent(it) }
            setTimeoutAfter(SUCCESS_TIMEOUT)
        }.build()
        
        packageName?.let {
            notificationManager.notify(it.hashCode(), notification)
        } ?: notificationManager.notify(AppInstallApp.NOTIFICATION_ID_INSTALL, notification)
    }
    
    fun showInstallFailedNotification(packageName: String?, errorMessage: String?) {
        val appName = getAppName(packageName)
        
        val notification = createNotificationBuilder(
            title = appName ?: context.getString(R.string.install),
            message = errorMessage ?: context.getString(R.string.install_failed),
            ongoing = false
        ).apply {
            setTimeoutAfter(FAILURE_TIMEOUT)
        }.build()
        
        packageName?.let {
            notificationManager.notify(it.hashCode(), notification)
        } ?: notificationManager.notify(AppInstallApp.NOTIFICATION_ID_INSTALL, notification)
    }
    
    fun showUninstallingNotification(packageName: String?, appName: String?) {
        val notification = createNotificationBuilder(
            title = appName ?: packageName ?: context.getString(R.string.uninstall),
            message = context.getString(R.string.uninstalling),
            ongoing = true,
            progress = 0
        ).build()
        
        packageName?.let {
            notificationManager.notify(it.hashCode(), notification)
        }
    }
    
    fun showUninstallSuccessNotification(packageName: String?) {
        val appName = getAppName(packageName)
        
        val notification = createNotificationBuilder(
            title = appName ?: context.getString(R.string.uninstall),
            message = context.getString(R.string.uninstall_success),
            ongoing = false
        ).apply {
            setTimeoutAfter(SUCCESS_TIMEOUT)
        }.build()
        
        packageName?.let {
            notificationManager.notify(it.hashCode(), notification)
        }
    }
    
    fun showUninstallFailedNotification(packageName: String?, errorMessage: String?) {
        val appName = getAppName(packageName)
        
        val notification = createNotificationBuilder(
            title = appName ?: context.getString(R.string.uninstall),
            message = errorMessage ?: context.getString(R.string.uninstall_failed),
            ongoing = false
        ).apply {
            setTimeoutAfter(FAILURE_TIMEOUT)
        }.build()
        
        packageName?.let {
            notificationManager.notify(it.hashCode(), notification)
        }
    }
    
    fun cancelNotification(packageName: String?) {
        packageName?.let {
            notificationManager.cancel(it.hashCode())
        }
    }
    
    private fun createNotificationBuilder(
        title: String,
        message: String,
        ongoing: Boolean,
        progress: Int = -1
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, AppInstallApp.CHANNEL_INSTALL)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(ongoing)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .apply {
                if (progress >= 0) {
                    setProgress(100, progress, true)
                }
            }
    }
    
    private fun getAppName(packageName: String?): String? {
        if (packageName.isNullOrEmpty()) return null
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName.substringAfterLast('.')
        }
    }
    
    companion object {
        private const val SUCCESS_TIMEOUT = 5_000L
        private const val FAILURE_TIMEOUT = 10_000L
    }
}
