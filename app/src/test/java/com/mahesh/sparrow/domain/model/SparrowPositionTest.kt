package com.mahesh.sparrow.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SparrowPositionTest {

    private val bounds = SafeBounds(left = 0, top = 100, right = 1000, bottom = 2000)

    @Test
    fun `position already inside bounds is unchanged`() {
        val position = SparrowPosition(x = 400, y = 500)
        assertEquals(position, position.clampTo(bounds, viewWidth = 100, viewHeight = 100))
    }

    @Test
    fun `position left of bounds clamps to left edge`() {
        val position = SparrowPosition(x = -500, y = 500)
        val clamped = position.clampTo(bounds, viewWidth = 100, viewHeight = 100)
        assertEquals(0, clamped.x)
    }

    @Test
    fun `position right of bounds clamps so the view stays fully inside`() {
        val position = SparrowPosition(x = 5000, y = 500)
        val clamped = position.clampTo(bounds, viewWidth = 100, viewHeight = 100)
        assertEquals(900, clamped.x) // bounds.right (1000) - viewWidth (100)
    }

    @Test
    fun `position above bounds clamps to top edge`() {
        val position = SparrowPosition(x = 400, y = 0)
        val clamped = position.clampTo(bounds, viewWidth = 100, viewHeight = 100)
        assertEquals(100, clamped.y)
    }

    @Test
    fun `position below bounds clamps so the view stays fully inside`() {
        val position = SparrowPosition(x = 400, y = 9000)
        val clamped = position.clampTo(bounds, viewWidth = 100, viewHeight = 200)
        assertEquals(1800, clamped.y) // bounds.bottom (2000) - viewHeight (200)
    }

    @Test
    fun `view larger than bounds falls back to the bounds origin`() {
        val position = SparrowPosition(x = 500, y = 500)
        val clamped = position.clampTo(bounds, viewWidth = 5000, viewHeight = 5000)
        assertEquals(SparrowPosition(bounds.left, bounds.top), clamped)
    }

    @Test
    fun `position left of center snaps to the left edge`() {
        val position = SparrowPosition(x = 100, y = 500)
        val snapped = position.snappedToNearestEdge(bounds, viewWidth = 100, viewHeight = 100)
        assertEquals(0, snapped.x)
    }

    @Test
    fun `position right of center snaps to the right edge`() {
        val position = SparrowPosition(x = 800, y = 500)
        val snapped = position.snappedToNearestEdge(bounds, viewWidth = 100, viewHeight = 100)
        assertEquals(900, snapped.x)
    }

    @Test
    fun `nearest edge is left when view center is before the bounds midpoint`() {
        // bounds width 1000 -> midpoint at x=500; a 100-wide view at x=300 has
        // its center at 350, which is left of the midpoint.
        val position = SparrowPosition(x = 300, y = 500)
        assertEquals(HorizontalEdge.LEFT, position.nearestHorizontalEdge(bounds, viewWidth = 100))
    }

    @Test
    fun `nearest edge is right when view center is past the bounds midpoint`() {
        val position = SparrowPosition(x = 700, y = 500)
        assertEquals(HorizontalEdge.RIGHT, position.nearestHorizontalEdge(bounds, viewWidth = 100))
    }

    @Test
    fun `a view centered exactly on the midpoint resolves to the left edge`() {
        // x=450 + viewWidth/2 (50) = center at 500, exactly the bounds midpoint.
        val position = SparrowPosition(x = 450, y = 500)
        assertEquals(HorizontalEdge.LEFT, position.nearestHorizontalEdge(bounds, viewWidth = 100))
    }
}
