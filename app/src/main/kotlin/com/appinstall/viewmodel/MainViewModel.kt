package com.appinstall.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appinstall.installer.InstallManager
import com.appinstall.model.ApkInfo
import com.appinstall.model.InstallState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val installManager: InstallManager
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _hasApk = MutableStateFlow(false)
    val hasApk: StateFlow<Boolean> = _hasApk.asStateFlow()
    
    private val _uninstallPackageName = MutableStateFlow<String?>(null)
    val uninstallPackageName: StateFlow<String?> = _uninstallPackageName.asStateFlow()
    
    private val _uninstallAppName = MutableStateFlow<String?>(null)
    val uninstallAppName: StateFlow<String?> = _uninstallAppName.asStateFlow()
    
    val apkInfo: StateFlow<ApkInfo?> = installManager.currentApkInfo
    val installState: StateFlow<InstallState> = installManager.installState
    
    fun handleIntent(intent: Intent) {
        @Suppress("DEPRECATION")
        when (intent.action) {
            Intent.ACTION_VIEW -> handleViewIntent(intent)
            Intent.ACTION_SEND -> handleSendIntent(intent)
            Intent.ACTION_SEND_MULTIPLE -> handleSendMultipleIntent(intent)
            Intent.ACTION_INSTALL_PACKAGE -> handleInstallIntent(intent)
            Intent.ACTION_UNINSTALL_PACKAGE, Intent.ACTION_DELETE -> handleUninstallIntent(intent)
            else -> Log.d(TAG, "Unknown intent action: ${intent.action}")
        }
    }
    
    private fun handleViewIntent(intent: Intent) {
        val uri = intent.data
        if (uri != null) {
            parseAndLoadApk(uri)
        } else {
            _errorMessage.value = "No APK file provided"
        }
    }
    
    private fun handleSendIntent(intent: Intent) {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
        }
        
        if (uri != null) {
            parseAndLoadApk(uri)
        } else {
            _errorMessage.value = "No APK file provided"
        }
    }
    
    private fun handleSendMultipleIntent(intent: Intent) {
        val uris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
        }
        
        if (!uris.isNullOrEmpty()) {
            parseAndLoadApk(uris.first())
        } else {
            _errorMessage.value = "No APK files provided"
        }
    }
    
    private fun handleInstallIntent(intent: Intent) {
        val uri = intent.data
        if (uri != null) {
            parseAndLoadApk(uri)
        } else {
            _errorMessage.value = "No APK file provided"
        }
    }
    
    private fun handleUninstallIntent(intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart
        if (packageName != null) {
            _uninstallPackageName.value = packageName
            _uninstallAppName.value = getAppName(packageName)
        } else {
            _errorMessage.value = "No package specified"
        }
    }
    
    private fun parseAndLoadApk(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = installManager.parseApkFromUri(uri)
            
            if (result == null) {
                _errorMessage.value = "Failed to parse APK file"
            } else {
                _hasApk.value = true
            }
            
            _isLoading.value = false
        }
    }
    
    fun installApk() {
        viewModelScope.launch {
            val info = apkInfo.value
            if (info != null) {
                installManager.installApk(info)
            }
        }
    }
    
    fun uninstallPackage() {
        viewModelScope.launch {
            val packageName = _uninstallPackageName.value
            if (packageName != null) {
                installManager.uninstallPackage(packageName)
            }
        }
    }
    
    fun openApp() {
        val packageName = apkInfo.value?.packageName ?: return
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
    
    fun reset() {
        installManager.reset()
        _uninstallPackageName.value = null
        _uninstallAppName.value = null
        _errorMessage.value = null
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    private fun getAppName(packageName: String): String? {
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getApplicationInfo(packageName, 0)
            }
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        installManager.cleanup()
    }
    
    companion object {
        private const val TAG = "MainViewModel"
    }
}
