package com.example.drwebtestapp.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val installTime: Long,
    val updateTime: Long,
    val apkPath: String
)

data class AppDetails(
    val appInfo: AppInfo,
    val sha256Checksum: String,
    val apkSize: Long,
    val permissions: List<String>,
    val activities: List<String>
)
