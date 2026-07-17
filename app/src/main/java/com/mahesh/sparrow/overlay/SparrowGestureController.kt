package com.mahesh.sparrow.overlay

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

/**
 * Interprets raw touch events on the overlay view as a tap, a long press, or
 * a drag. Kept independent of [OverlayWindowController] / the service so it
 * only ever reports gestures — callers decide what a tap or drag *means*.
 */
class SparrowGestureController(
    context: Context,
    private val listener: Listener
) {
    interface Listener {
        fun onTap()
        fun onLongPress()
        fun onDragStart()
        /** Cumulative delta, in px, from where the drag started. */
        fun onDrag(dx: Int, dy: Int)
        fun onDragEnd()
    }

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val longPressTimeoutMs = ViewConfiguration.getLongPressTimeout().toLong()
    private val handler = Handler(Looper.getMainLooper())

    private var downRawX = 0f
    private var downRawY = 0f
    private var isDragging = false
    private var longPressFired = false

    private val longPressRunnable = Runnable {
        if (!isDragging) {
            longPressFired = true
            listener.onLongPress()
        }
    }

    fun attachTo(view: View) {
        view.setOnTouchListener { v, event -> handleTouch(v, event) }
    }

    @Suppress("ClickableViewAccessibility")
    private fun handleTouch(view: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = event.rawX
                downRawY = event.rawY
                isDragging = false
                longPressFired = false
                handler.postDelayed(longPressRunnable, longPressTimeoutMs)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = (event.rawX - downRawX)
                val dy = (event.rawY - downRawY)
                if (!isDragging && (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop)) {
                    isDragging = true
                    handler.removeCallbacks(longPressRunnable)
                    listener.onDragStart()
                }
                if (isDragging) {
                    listener.onDrag(dx.toInt(), dy.toInt())
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(longPressRunnable)
                if (isDragging) {
                    listener.onDragEnd()
                } else if (!longPressFired) {
                    listener.onTap()
                }
                isDragging = false
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(longPressRunnable)
                if (isDragging) {
                    listener.onDragEnd()
                }
                isDragging = false
                return true
            }
        }
        return false
    }
}
