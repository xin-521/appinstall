package com.appinstall.model

import android.graphics.drawable.Drawable

data class ApkInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val minSdkVersion: Int,
    val targetSdkVersion: Int,
    val icon: Drawable?,
    val fileSize: Long,
    val permissions: List<String>,
    val isUpdate: Boolean,
    val installedVersionName: String?,
    val installedVersionCode: Long?,
    val signatureMatch: Boolean,
    val signatures: List<String>,
    val path: String
) {
    val sizeFormatted: String
        get() = formatFileSize(fileSize)
    
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
}

enum class InstallState {
    PENDING,
    INSTALLING,
    INSTALLED,
    FAILED,
    CANCELLED
}

enum class UninstallState {
    PENDING,
    UNINSTALLING,
    UNINSTALLED,
    FAILED
}
