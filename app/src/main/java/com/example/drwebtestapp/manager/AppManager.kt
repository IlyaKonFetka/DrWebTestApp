package com.example.drwebtestapp.manager

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.example.drwebtestapp.model.AppInfo
import com.example.drwebtestapp.model.AppDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class AppManager(private val context: Context) {
    
    private val packageManager: PackageManager = context.packageManager
    
    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        
        packages.mapNotNull { app ->
            try {
                val packageInfo = packageManager.getPackageInfo(app.packageName, 0)
                AppInfo(
                    name = app.loadLabel(packageManager).toString(),
                    packageName = app.packageName,
                    versionName = packageInfo.versionName ?: "Unknown",
                    versionCode = packageInfo.longVersionCode,
                    icon = app.loadIcon(packageManager),
                    isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    installTime = packageInfo.firstInstallTime,
                    updateTime = packageInfo.lastUpdateTime,
                    apkPath = app.sourceDir
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.name.lowercase() }
    }
    
    suspend fun getAppDetails(packageName: String): AppDetails? = withContext(Dispatchers.IO) {
        try {
            val app = packageManager.getApplicationInfo(packageName, 0)
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            
            val appInfo = AppInfo(
                name = app.loadLabel(packageManager).toString(),
                packageName = app.packageName,
                versionName = packageInfo.versionName ?: "Unknown",
                versionCode = packageInfo.longVersionCode,
                icon = app.loadIcon(packageManager),
                isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                installTime = packageInfo.firstInstallTime,
                updateTime = packageInfo.lastUpdateTime,
                apkPath = app.sourceDir
            )
            
            val checksum = calculateSHA256(app.sourceDir)
            val apkSize = File(app.sourceDir).length()
            val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()
            val activities = getActivities(packageName)
            
            AppDetails(
                appInfo = appInfo,
                sha256Checksum = checksum,
                apkSize = apkSize,
                permissions = permissions,
                activities = activities
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun calculateSHA256(filePath: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val fis = FileInputStream(filePath)
            val buffer = ByteArray(8192)
            var bytesRead: Int
            
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            fis.close()
            
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "Error calculating checksum"
        }
    }
    
    private fun getActivities(packageName: String): List<String> {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            packageInfo.activities?.map { it.name } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun launchApp(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
