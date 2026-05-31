package com.zgrcan.kalkan.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.zgrcan.kalkan.BuildConfig

object AppVersionUtils {
    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: BuildConfig.VERSION_NAME
        } catch (_: Exception) {
            BuildConfig.VERSION_NAME
        }
    }

    fun getAppVersionCode(): Int = BuildConfig.VERSION_CODE

    fun getAppVersionLabel(context: Context): String =
        "v${getAppVersion(context)} (Build ${getAppVersionCode()})"
}
