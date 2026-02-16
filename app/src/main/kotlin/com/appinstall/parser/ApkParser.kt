package com.appinstall.parser

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import com.appinstall.model.ApkInfo
import java.io.File
import java.security.MessageDigest

class ApkParser(private val context: Context) {
    
    private val packageManager: PackageManager = context.packageManager
    
    fun parseApk(apkPath: String): ApkInfo? {
        return try {
            val apkFile = File(apkPath)
            if (!apkFile.exists()) {
                Log.e(TAG, "APK file not found: $apkPath")
                return null
            }
            
            val packageInfo = getPackageInfoFromApk(apkPath)
            if (packageInfo == null) {
                Log.e(TAG, "Failed to parse APK: $apkPath")
                return null
            }
            
            val appInfo = packageInfo.applicationInfo ?: return null
            val appName = getAppName(appInfo)
            val icon = getIcon(appInfo)
            val permissions = getPermissions(packageInfo)
            
            val installedInfo = getInstalledPackageInfo(packageInfo.packageName)
            val isUpdate = installedInfo != null
            val installedVersionName = installedInfo?.versionName
            val installedVersionCode = installedInfo?.let { getVersionCode(it) }
            
            val signatureMatch = if (isUpdate) {
                checkSignatureMatch(apkPath, packageInfo.packageName)
            } else {
                true
            }
            
            val signatures = getApkSignatures(apkPath).toList()
            val md5 = calculateMd5(apkFile)
            
            ApkInfo(
                packageName = packageInfo.packageName,
                appName = appName,
                versionName = packageInfo.versionName ?: "Unknown",
                versionCode = getVersionCode(packageInfo),
                minSdkVersion = getMinSdkVersion(appInfo),
                targetSdkVersion = appInfo.targetSdkVersion,
                icon = icon,
                fileSize = apkFile.length(),
                permissions = permissions,
                isUpdate = isUpdate,
                installedVersionName = installedVersionName,
                installedVersionCode = installedVersionCode,
                signatureMatch = signatureMatch,
                signatures = signatures,
                md5 = md5,
                path = apkPath
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing APK: $apkPath", e)
            null
        }
    }
    
    private fun getPackageInfoFromApk(apkPath: String): PackageInfo? {
        return try {
            val flags = PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_PROVIDERS or
                    PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_SIGNING_CERTIFICATES or
                    PackageManager.GET_META_DATA
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageArchiveInfo(apkPath, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageArchiveInfo(apkPath, flags)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting package info from APK", e)
            null
        }?.also { packageInfo ->
            packageInfo.applicationInfo?.let { appInfo ->
                appInfo.sourceDir = apkPath
                appInfo.publicSourceDir = apkPath
            }
        }
    }
    
    private fun getAppName(appInfo: ApplicationInfo): String {
        return try {
            appInfo.loadLabel(packageManager).toString()
        } catch (e: Exception) {
            appInfo.packageName.substringAfterLast('.')
        }
    }
    
    private fun getIcon(appInfo: ApplicationInfo): Drawable? {
        return try {
            appInfo.loadIcon(packageManager)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getPermissions(packageInfo: PackageInfo): List<String> {
        return packageInfo.requestedPermissions?.toList() ?: emptyList()
    }
    
    private fun getVersionCode(packageInfo: PackageInfo): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    }
    
    private fun getMinSdkVersion(appInfo: ApplicationInfo): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            appInfo.minSdkVersion.toInt()
        } else {
            1
        }
    }
    
    private fun getInstalledPackageInfo(packageName: String): PackageInfo? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
        } catch (e: NameNotFoundException) {
            null
        }
    }
    
    private fun checkSignatureMatch(apkPath: String, packageName: String): Boolean {
        return try {
            val apkSignatures = getApkSignatures(apkPath)
            val installedSignatures = getInstalledSignatures(packageName)
            
            if (apkSignatures.isEmpty() || installedSignatures.isEmpty()) {
                return true
            }
            
            apkSignatures.any { apkSig -> installedSignatures.contains(apkSig) }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking signature match", e)
            true
        }
    }
    
    private fun getApkSignatures(apkPath: String): Set<String> {
        return try {
            val flags = PackageManager.GET_SIGNING_CERTIFICATES
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageArchiveInfo(apkPath, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageArchiveInfo(apkPath, flags)
            }
            
            packageInfo?.let { extractSignatures(it) } ?: emptySet()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting APK signatures", e)
            emptySet()
        }
    }
    
    private fun getInstalledSignatures(packageName: String): Set<String> {
        return try {
            val flags = PackageManager.GET_SIGNING_CERTIFICATES
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, flags)
            }
            
            extractSignatures(packageInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installed signatures", e)
            emptySet()
        }
    }
    
    private fun extractSignatures(packageInfo: PackageInfo): Set<String> {
        val signatures = mutableSetOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.signingInfo?.apkContentsSigners?.forEach { signature ->
                signatures.add(signature.toCharsString())
            }
        } else {
            @Suppress("DEPRECATION")
            packageInfo.signatures?.forEach { signature ->
                signatures.add(signature.toCharsString())
            }
        }
        
        return signatures
    }
    
    private fun calculateMd5(file: File): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    md.update(buffer, 0, bytesRead)
                }
            }
            md.digest().joinToString("") { byte ->
                "%02x".format(byte)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating MD5", e)
            ""
        }
    }
    
    companion object {
        private const val TAG = "ApkParser"
    }
}
