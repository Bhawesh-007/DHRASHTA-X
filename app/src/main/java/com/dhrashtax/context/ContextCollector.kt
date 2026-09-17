package com.dhrashtax.context

import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.dhrashtax.domain.DeviceRiskContext

class ContextCollector(private val context: Context) {
    fun collect(packageName: String): DeviceRiskContext {
        val packageManager = context.packageManager
        val uid = runCatching { packageManager.getApplicationInfo(packageName, 0).uid }.getOrNull() ?: -1
        val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching { packageManager.getInstallSourceInfo(packageName).installingPackageName }.getOrNull()
        } else {
            @Suppress("DEPRECATION")
            runCatching { packageManager.getInstallerPackageName(packageName) }.getOrNull()
        }
        val sideloaded = installer == null || installer !in TRUSTED_INSTALLERS
        val accessibility = enabledComponents(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            .any { it.packageName == packageName }
        val notificationListener = enabledComponents("enabled_notification_listeners")
            .any { it.packageName == packageName }
        val admins = context.getSystemService(DevicePolicyManager::class.java).activeAdmins.orEmpty()
        val overlayAllowed = if (uid >= 0) {
            val appOps = context.getSystemService(AppOpsManager::class.java)
            runCatching {
                appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, uid, packageName) == AppOpsManager.MODE_ALLOWED
            }.getOrDefault(false)
        } else false
        return DeviceRiskContext(
            sideloaded = sideloaded,
            accessibilityEnabled = accessibility,
            notificationListenerEnabled = notificationListener,
            deviceAdminEnabled = admins.any { it.packageName == packageName },
            overlayAllowed = overlayAllowed,
        )
    }

    private fun enabledComponents(setting: String): List<ComponentName> =
        Settings.Secure.getString(context.contentResolver, setting)
            .orEmpty()
            .split(':')
            .mapNotNull(ComponentName::unflattenFromString)

    private companion object {
        val TRUSTED_INSTALLERS = setOf(
            "com.android.vending",
            "com.bbk.appstore",
            "com.vivo.appstore",
            "com.google.android.packageinstaller",
        )
    }
}
