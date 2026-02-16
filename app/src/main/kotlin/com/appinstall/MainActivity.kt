package com.appinstall

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.appinstall.model.InstallState
import com.appinstall.ui.InstallScreen
import com.appinstall.ui.NoApkContent
import com.appinstall.ui.UninstallScreen
import com.appinstall.ui.theme.AppInstallTheme
import com.appinstall.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        handleIntent(intent)
        
        setContent {
            AppInstallTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val apkInfo by viewModel.apkInfo.collectAsState()
                    val installState by viewModel.installState.collectAsState()
                    val isLoading by viewModel.isLoading.collectAsState()
                    val errorMessage by viewModel.errorMessage.collectAsState()
                    val hasApk by viewModel.hasApk.collectAsState()
                    val uninstallPackageName by viewModel.uninstallPackageName.collectAsState()
                    val uninstallAppName by viewModel.uninstallAppName.collectAsState()
                    
                    LaunchedEffect(errorMessage) {
                        errorMessage?.let {
                            finish()
                        }
                    }
                    
                    when {
                        uninstallPackageName != null -> {
                            UninstallScreen(
                                packageName = uninstallPackageName!!,
                                appName = uninstallAppName,
                                onConfirmClick = {
                                    viewModel.uninstallPackage()
                                    finish()
                                },
                                onCancelClick = {
                                    viewModel.reset()
                                    finish()
                                }
                            )
                        }
                        !hasApk -> {
                            NoApkContent()
                        }
                        else -> {
                            InstallScreen(
                                apkInfo = apkInfo,
                                installState = installState,
                                onInstallClick = { viewModel.installApk() },
                                onCancelClick = { 
                                    viewModel.reset()
                                    finish()
                                },
                                onOpenClick = {
                                    viewModel.openApp()
                                    finish()
                                },
                                onDoneClick = { finish() }
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        intent?.let { viewModel.handleIntent(it) }
    }
}
