package com.appinstall.installer

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import com.appinstall.AppInstallApp
import com.appinstall.R
import com.appinstall.model.InstallState
import com.appinstall.notification.InstallNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InstallResultReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var notificationManager: InstallNotificationManager
    
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
            ?: intent.data?.schemeSpecificPart
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        val action = intent.getStringExtra(EXTRA_ACTION) ?: ACTION_INSTALL
        
        Log.d(TAG, "Received install result: status=$status, package=$packageName, action=$action")
        
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                handlePendingUserAction(context, intent)
            }
            PackageInstaller.STATUS_SUCCESS -> {
                handleSuccess(context, packageName, action)
            }
            PackageInstaller.STATUS_FAILURE,
            PackageInstaller.STATUS_FAILURE_ABORTED,
            PackageInstaller.STATUS_FAILURE_BLOCKED,
            PackageInstaller.STATUS_FAILURE_CONFLICT,
            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE,
            PackageInstaller.STATUS_FAILURE_INVALID,
            PackageInstaller.STATUS_FAILURE_STORAGE -> {
                handleFailure(context, packageName, message, action)
            }
        }
    }
    
    private fun handlePendingUserAction(context: Context, intent: Intent) {
        val promptIntent: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_INTENT)
        }
        
        promptIntent?.let {
            it.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            it.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.packageName)
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
    
    private fun handleSuccess(context: Context, packageName: String?, action: String) {
        val isUninstall = action == ACTION_UNINSTALL
        
        if (isUninstall) {
            notificationManager.showUninstallSuccessNotification(packageName)
        } else {
            notificationManager.showInstallSuccessNotification(packageName)
        }
    }
    
    private fun handleFailure(
        context: Context,
        packageName: String?,
        message: String?,
        action: String
    ) {
        val isUninstall = action == ACTION_UNINSTALL
        
        if (isUninstall) {
            notificationManager.showUninstallFailedNotification(packageName, message)
        } else {
            notificationManager.showInstallFailedNotification(packageName, message)
        }
    }
    
    companion object {
        const val TAG = "InstallResultReceiver"
        const val EXTRA_ACTION = "extra_action"
        const val ACTION_INSTALL = "action_install"
        const val ACTION_UNINSTALL = "action_uninstall"
    }
}
