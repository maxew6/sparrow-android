package com.mahesh.sparrow.util

import android.content.Context
import android.provider.Settings

/**
 * Reads Android's system-wide "Remove animations" / reduced-motion setting
 * (the same one that backs `ValueAnimator.areAnimatorsEnabled()`), so
 * Sparrow's own automatic movement can respect it in addition to the
 * in-app "Reduced motion" preference.
 */
object SystemMotionSettings {
    fun isReducedMotionEnabled(context: Context): Boolean {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
        return scale == 0f
    }
}
