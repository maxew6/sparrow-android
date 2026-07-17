package com.mahesh.sparrow.util

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager
import com.mahesh.sparrow.domain.model.SafeBounds

/**
 * Computes the screen area Sparrow may occupy, with system bars and display
 * cutouts subtracted. Call this again whenever the display configuration
 * changes (rotation, fold, external display) rather than caching the result.
 */
class DisplayBoundsProvider(private val context: Context) {

    fun currentSafeBounds(): SafeBounds {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
            )
            val bounds = metrics.bounds
            SafeBounds(
                left = bounds.left + insets.left,
                top = bounds.top + insets.top,
                right = bounds.right - insets.right,
                bottom = bounds.bottom - insets.bottom
            )
        } else {
            @Suppress("DEPRECATION")
            val displayMetrics = DisplayMetrics().also {
                windowManager.defaultDisplay.getRealMetrics(it)
            }
            SafeBounds(
                left = 0,
                top = estimatedStatusBarHeightPx(),
                right = displayMetrics.widthPixels,
                bottom = displayMetrics.heightPixels
            )
        }
    }

    /**
     * Pre-Android-11 fallback. Real inset values are only reliably available
     * from a window's own `onApplyWindowInsets` callback, which a detached
     * bounds calculation doesn't have access to, so this uses the
     * conventional system-resource estimate instead.
     */
    private fun estimatedStatusBarHeightPx(): Int {
        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resId > 0) context.resources.getDimensionPixelSize(resId) else 0
    }
}
