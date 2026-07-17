package com.mahesh.sparrow.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryStateTest {

    @Test
    fun `normal level and scale compute a percentage`() {
        assertEquals(50, BatteryState.normalizePercentage(level = 50, scale = 100))
        assertEquals(100, BatteryState.normalizePercentage(level = 10, scale = 10))
        assertEquals(75, BatteryState.normalizePercentage(level = 3, scale = 4))
    }

    @Test
    fun `negative level falls back to the unknown default`() {
        assertEquals(BatteryState.Unknown.percentage, BatteryState.normalizePercentage(level = -1, scale = 100))
    }

    @Test
    fun `zero or negative scale falls back to the unknown default`() {
        assertEquals(BatteryState.Unknown.percentage, BatteryState.normalizePercentage(level = 50, scale = 0))
        assertEquals(BatteryState.Unknown.percentage, BatteryState.normalizePercentage(level = 50, scale = -1))
    }

    @Test
    fun `result is always clamped between 0 and 100`() {
        assertEquals(100, BatteryState.normalizePercentage(level = 500, scale = 100))
    }
}
