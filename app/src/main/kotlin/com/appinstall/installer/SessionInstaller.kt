package com.appinstall.installer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.appinstall.model.InstallState
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

class SessionInstaller(private val context: Context) {
    
    private val packageInstaller = context.packageManager.packageInstaller
    
    private var sessionCallback: PackageInstaller.SessionCallback? = null
    
    suspend fun install(apkFile: File): InstallState = suspendCancellableCoroutine { cont ->
        if (!apkFile.exists()) {
            Log.e(TAG, "APK file does not exist: ${apkFile.absolutePath}")
            cont.resume(InstallState.FAILED)
            return@suspendCancellableCoroutine
        }
        
        val params = createSessionParams()
        val sessionId = createSession(params)
        
        if (sessionId == -1) {
            Log.e(TAG, "Failed to create install session")
            cont.resume(InstallState.FAILED)
            return@suspendCancellableCoroutine
        }
        
        val callback = object : PackageInstaller.SessionCallback() {
            override fun onCreated(sessionId: Int) {}
            override fun onBadgingChanged(sessionId: Int) {}
            override fun onActiveChanged(sessionId: Int, active: Boolean) {}
            override fun onProgressChanged(sessionId: Int, progress: Float) {}
            
            override fun onFinished(sessionId: Int, success: Boolean) {
                if (sessionId == this@SessionInstaller.sessionId) {
                    val result = if (success) InstallState.INSTALLED else InstallState.FAILED
                    cont.resume(result)
                }
            }
        }
        
        sessionCallback = callback
        packageInstaller.registerSessionCallback(callback, Handler(Looper.getMainLooper()))
        
        try {
            writeApkToSession(sessionId, apkFile)
            commitSession(sessionId)
            
            this.sessionId = sessionId
        } catch (e: Exception) {
            Log.e(TAG, "Error during installation", e)
            abandonSession(sessionId)
            cont.resume(InstallState.FAILED)
        }
        
        cont.invokeOnCancellation {
            abandonSession(sessionId)
        }
    }
    
    suspend fun uninstall(packageName: String): Boolean = suspendCancellableCoroutine { cont ->
        try {
            val intent = Intent(context, InstallResultReceiver::class.java).apply {
                putExtra(InstallResultReceiver.EXTRA_ACTION, InstallResultReceiver.ACTION_UNINSTALL)
            }
            
            val flags = getPendingIntentFlags()
            val pendingIntent = PendingIntent.getBroadcast(context, -1, intent, flags)
            
            packageInstaller.uninstall(packageName, pendingIntent.intentSender)
            cont.resume(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error uninstalling package: $packageName", e)
            cont.resume(false)
        }
    }
    
    private fun createSessionParams(): PackageInstaller.SessionParams {
        return PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                setRequestUpdateOwnership(true)
            }
        }
    }
    
    private fun createSession(params: PackageInstaller.SessionParams): Int {
        return try {
            packageInstaller.createSession(params)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create session", e)
            -1
        }
    }
    
    private fun writeApkToSession(sessionId: Int, apkFile: File) {
        val session = packageInstaller.openSession(sessionId)
        session.use { activeSession ->
            val sizeBytes = apkFile.length()
            apkFile.inputStream().use { inputStream ->
                activeSession.openWrite("base.apk", 0, sizeBytes).use { outputStream ->
                    inputStream.copyTo(outputStream)
                    activeSession.fsync(outputStream)
                }
            }
        }
    }
    
    private fun commitSession(sessionId: Int) {
        val session = packageInstaller.openSession(sessionId)
        val intent = Intent(context, InstallResultReceiver::class.java).apply {
            putExtra(InstallResultReceiver.EXTRA_ACTION, InstallResultReceiver.ACTION_INSTALL)
        }
        
        val flags = getPendingIntentFlags()
        val pendingIntent = PendingIntent.getBroadcast(context, sessionId, intent, flags)
        
        session.commit(pendingIntent.intentSender)
        session.close()
    }
    
    private fun abandonSession(sessionId: Int) {
        try {
            packageInstaller.abandonSession(sessionId)
        } catch (e: SecurityException) {
            Log.e(TAG, "Error abandoning session", e)
        }
    }
    
    private fun getPendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }
    
    fun cleanup() {
        sessionCallback?.let {
            packageInstaller.unregisterSessionCallback(it)
            sessionCallback = null
        }
        
        try {
            packageInstaller.mySessions.forEach { session ->
                packageInstaller.abandonSession(session.sessionId)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Error cleaning up sessions", e)
        }
    }
    
    private var sessionId: Int = -1
    
    companion object {
        private const val TAG = "SessionInstaller"
    }
}
