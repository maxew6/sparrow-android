package com.mahesh.sparrow.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mahesh.sparrow.R
import com.mahesh.sparrow.domain.model.SafeBounds
import com.mahesh.sparrow.domain.model.SparrowPosition
import com.mahesh.sparrow.domain.model.clampTo

/**
 * Sparrow's compact long-press menu. Built as a second small
 * `TYPE_APPLICATION_OVERLAY` window (rather than a [android.widget.PopupWindow])
 * because a `PopupWindow` normally anchors to an Activity's window token,
 * which the overlay view doesn't have — reusing [OverlayWindowController]
 * keeps this on the exact same, already-proven code path as the bird itself.
 */
class SparrowMenuOverlay(
    private val context: Context,
    private val windowController: OverlayWindowController
) {
    data class MenuItem(val label: String, val onClick: () -> Unit)

    private var menuView: LinearLayout? = null

    fun isShowing(): Boolean = menuView != null

    fun show(
        anchor: SparrowPosition,
        anchorSizePx: Int,
        bounds: SafeBounds,
        isDarkTheme: Boolean,
        items: List<MenuItem>
    ) {
        dismiss()

        val density = context.resources.displayMetrics.density
        val itemHeightPx = (48 * density).toInt()
        val widthPx = (220 * density).toInt()
        val heightPx = itemHeightPx * items.size

        val view = buildMenuView(items, itemHeightPx, isDarkTheme)

        val preferAbove = anchor.y > bounds.top + bounds.height / 2
        val targetY = if (preferAbove) anchor.y - heightPx else anchor.y + anchorSizePx
        val targetX = anchor.x + anchorSizePx / 2 - widthPx / 2
        val position = SparrowPosition(targetX, targetY).clampTo(bounds, widthPx, heightPx)

        windowController.addOverlay(view, position, widthPx, heightPx)
        menuView = view
    }

    fun dismiss() {
        menuView?.let { windowController.removeOverlay(it) }
        menuView = null
    }

    private fun buildMenuView(items: List<MenuItem>, itemHeightPx: Int, isDarkTheme: Boolean): LinearLayout {
        val backgroundColor = ContextCompat.getColor(
            context,
            if (isDarkTheme) R.color.bubble_dark_bg else R.color.bubble_light_bg
        )
        val textColor = ContextCompat.getColor(
            context,
            if (isDarkTheme) R.color.bubble_dark_text else R.color.bubble_light_text
        )

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(backgroundColor)
                cornerRadius = 8f * context.resources.displayMetrics.density
            }
        }

        items.forEach { item ->
            val row = TextView(context).apply {
                text = item.label
                setTextColor(textColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
                val paddingPx = (16 * context.resources.displayMetrics.density).toInt()
                setPadding(paddingPx, 0, paddingPx, 0)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    itemHeightPx
                )
                isClickable = true
                isFocusable = false
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener {
                    item.onClick()
                    dismiss()
                }
            }
            container.addView(row)
        }

        return container
    }
}
