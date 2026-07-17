package com.mahesh.sparrow.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.mahesh.sparrow.domain.model.SparrowPosition
import java.util.Collections
import java.util.WeakHashMap

/**
 * Owns every direct [WindowManager] call for Sparrow's overlay windows —
 * the bird itself, and the small long-press menu. Kept separate from
 * [SparrowOverlayService] so overlay-window plumbing never leaks into an
 * Activity or composable, per the project's architecture. Supports more
 * than one independently-tracked overlay window at once.
 */
class OverlayWindowController(context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val attachedViews: MutableSet<View> = Collections.newSetFromMap(WeakHashMap())

    fun addOverlay(view: View, position: SparrowPosition, widthPx: Int, heightPx: Int) {
        if (view in attachedViews) return

        val params = WindowManager.LayoutParams(
            widthPx,
            heightPx,
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = position.x
            y = position.y
        }

        runCatching { windowManager.addView(view, params) }
            .onFailure { Log.w(TAG, "Failed to add overlay view", it) }
            .onSuccess { attachedViews += view }
    }

    fun updatePosition(view: View, position: SparrowPosition) {
        if (view !in attachedViews) return
        val params = view.layoutParams as? WindowManager.LayoutParams ?: return
        params.x = position.x
        params.y = position.y
        runCatching { windowManager.updateViewLayout(view, params) }
            .onFailure { Log.w(TAG, "Failed to update overlay position", it) }
    }

    fun updateSize(view: View, widthPx: Int, heightPx: Int) {
        if (view !in attachedViews) return
        val params = view.layoutParams as? WindowManager.LayoutParams ?: return
        params.width = widthPx
        params.height = heightPx
        runCatching { windowManager.updateViewLayout(view, params) }
            .onFailure { Log.w(TAG, "Failed to update overlay size", it) }
    }

    fun currentPosition(view: View): SparrowPosition? {
        val params = view.layoutParams as? WindowManager.LayoutParams ?: return null
        return SparrowPosition(params.x, params.y)
    }

    fun isAttached(view: View): Boolean = view in attachedViews

    /** Safe to call even if the view was never added or was already removed. */
    fun removeOverlay(view: View) {
        if (view !in attachedViews) return
        runCatching { windowManager.removeViewImmediate(view) }
            .onFailure { Log.w(TAG, "Failed to remove overlay view", it) }
        attachedViews -= view
    }

    /** Removes every overlay window this controller currently tracks (used on shutdown). */
    fun removeAll() {
        attachedViews.toList().forEach { removeOverlay(it) }
    }

    companion object {
        private const val TAG = "OverlayWindowController"
    }
}
