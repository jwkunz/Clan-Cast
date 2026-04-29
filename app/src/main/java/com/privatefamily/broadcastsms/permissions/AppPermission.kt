package com.privatefamily.broadcastsms.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

enum class AppPermission(val manifestValue: String) {
    Contacts(Manifest.permission.READ_CONTACTS),
    SendSms(Manifest.permission.SEND_SMS),
}

data class PermissionUiState(
    val isGranted: Boolean,
    val shouldShowRationale: Boolean,
) {
    val requiresSettings: Boolean
        get() = !isGranted && !shouldShowRationale
}

object PermissionHelper {
    fun state(activity: Activity, permission: AppPermission): PermissionUiState {
        val granted = ContextCompat.checkSelfPermission(
            activity,
            permission.manifestValue,
        ) == PackageManager.PERMISSION_GRANTED
        return PermissionUiState(
            isGranted = granted,
            shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                permission.manifestValue,
            ),
        )
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        )
        context.startActivity(intent)
    }
}
