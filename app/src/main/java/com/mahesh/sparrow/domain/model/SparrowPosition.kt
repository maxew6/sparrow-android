package com.mahesh.sparrow.domain.model

/**
 * Top-left corner of the overlay window, in pixels, within whatever
 * [SafeBounds] it is being measured against.
 */
data class SparrowPosition(val x: Int, val y: Int)

/**
 * The screen area Sparrow is allowed to occupy: the real display bounds with
 * system bars, cutouts, and any other insets already subtracted. Pass the
 * current bounds every time the display changes (rotation, fold, resize) so
 * clamping always reflects the live layout.
 */
data class SafeBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val width: Int get() = (right - left).coerceAtLeast(0)
    val height: Int get() = (bottom - top).coerceAtLeast(0)
}

enum class HorizontalEdge { LEFT, RIGHT }

/**
 * Clamps this position so a [viewWidth] x [viewHeight] overlay window stays
 * fully inside [bounds]. Safe even when the view is larger than the
 * available space (falls back to the bounds' top-left corner).
 */
fun SparrowPosition.clampTo(bounds: SafeBounds, viewWidth: Int, viewHeight: Int): SparrowPosition {
    val maxX = (bounds.right - viewWidth).coerceAtLeast(bounds.left)
    val maxY = (bounds.bottom - viewHeight).coerceAtLeast(bounds.top)
    return SparrowPosition(
        x = x.coerceIn(bounds.left, maxX),
        y = y.coerceIn(bounds.top, maxY)
    )
}

/** Which horizontal edge of [bounds] this position's center is closer to. */
fun SparrowPosition.nearestHorizontalEdge(bounds: SafeBounds, viewWidth: Int): HorizontalEdge {
    val centerX = x + viewWidth / 2f
    val boundsCenterX = bounds.left + bounds.width / 2f
    return if (centerX <= boundsCenterX) HorizontalEdge.LEFT else HorizontalEdge.RIGHT
}

/**
 * Same position, snapped so the overlay rests flush against whichever
 * horizontal edge of [bounds] it is nearest to — used for the "settle near
 * the side of the screen" behavior after a drag or an automatic relocation.
 */
fun SparrowPosition.snappedToNearestEdge(bounds: SafeBounds, viewWidth: Int, viewHeight: Int): SparrowPosition {
    val clamped = clampTo(bounds, viewWidth, viewHeight)
    val targetX = when (clamped.nearestHorizontalEdge(bounds, viewWidth)) {
        HorizontalEdge.LEFT -> bounds.left
        HorizontalEdge.RIGHT -> (bounds.right - viewWidth).coerceAtLeast(bounds.left)
    }
    return SparrowPosition(x = targetX, y = clamped.y)
}
