package com.mahesh.sparrow.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Thin wrapper around the official "Display over other apps" permission
 * flow. Overlay permission can never be granted programmatically — the user
 * must approve it in system Settings.
 */
object OverlayPermissionHelper {

    fun hasOverlayPermission(context: Context): Boolean =
        Settings.canDrawOverlays(context)

    /**
     * Intent that opens Android's own overlay-permission screen for this
     * app. Launch this only after explaining to the user why it's needed.
     */
    fun buildPermissionSettingsIntent(context: Context): Intent =
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
}
