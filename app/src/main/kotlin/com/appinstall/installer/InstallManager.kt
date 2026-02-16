package com.appinstall.installer

import android.content.Context
import android.net.Uri
import android.util.Log
import com.appinstall.model.ApkInfo
import com.appinstall.model.InstallState
import com.appinstall.notification.InstallNotificationManager
import com.appinstall.parser.ApkParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstallManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apkParser: ApkParser,
    private val notificationManager: InstallNotificationManager
) {
    private val sessionInstaller = SessionInstaller(context)
    
    private val _installState = MutableStateFlow<InstallState>(InstallState.PENDING)
    val installState: StateFlow<InstallState> = _installState.asStateFlow()
    
    private val _currentApkInfo = MutableStateFlow<ApkInfo?>(null)
    val currentApkInfo: StateFlow<ApkInfo?> = _currentApkInfo.asStateFlow()
    
    suspend fun parseApkFromUri(uri: Uri): ApkInfo? {
        return try {
            val cachedApkPath = copyApkToCache(uri)
            if (cachedApkPath == null) {
                Log.e(TAG, "Failed to copy APK to cache")
                return null
            }
            
            val apkInfo = apkParser.parseApk(cachedApkPath)
            _currentApkInfo.value = apkInfo
            apkInfo
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing APK from URI", e)
            null
        }
    }
    
    suspend fun installApk(apkInfo: ApkInfo): InstallState {
        _installState.value = InstallState.INSTALLING
        notificationManager.showInstallingNotification(apkInfo.packageName, apkInfo.appName)
        
        val apkFile = File(apkInfo.path)
        val result = sessionInstaller.install(apkFile)
        
        _installState.value = result
        
        when (result) {
            InstallState.INSTALLED -> {
                notificationManager.showInstallSuccessNotification(apkInfo.packageName)
            }
            InstallState.FAILED -> {
                notificationManager.showInstallFailedNotification(apkInfo.packageName, null)
            }
            else -> {}
        }
        
        return result
    }
    
    suspend fun uninstallPackage(packageName: String): Boolean {
        notificationManager.showUninstallingNotification(packageName, null)
        
        val result = sessionInstaller.uninstall(packageName)
        
        if (result) {
            notificationManager.showUninstallSuccessNotification(packageName)
        } else {
            notificationManager.showUninstallFailedNotification(packageName, null)
        }
        
        return result
    }
    
    fun reset() {
        _installState.value = InstallState.PENDING
        _currentApkInfo.value = null
    }
    
    private fun copyApkToCache(uri: Uri): String? {
        return try {
            val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.apk")
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            tempFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error copying APK to cache", e)
            null
        }
    }
    
    fun cleanup() {
        sessionInstaller.cleanup()
    }
    
    companion object {
        private const val TAG = "InstallManager"
    }
}
