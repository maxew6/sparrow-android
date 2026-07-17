package com.mahesh.sparrow.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Test

class SparrowUserPreferencesTest {

    @Test
    fun `frequency within range is unchanged`() {
        assertEquals(10, SparrowUserPreferences.coerceFrequencyMinutes(10))
    }

    @Test
    fun `frequency below minimum is clamped up`() {
        assertEquals(
            SparrowUserPreferences.MIN_MOVEMENT_FREQUENCY_MINUTES,
            SparrowUserPreferences.coerceFrequencyMinutes(0)
        )
    }

    @Test
    fun `frequency above maximum is clamped down`() {
        assertEquals(
            SparrowUserPreferences.MAX_MOVEMENT_FREQUENCY_MINUTES,
            SparrowUserPreferences.coerceFrequencyMinutes(999)
        )
    }

    @Test
    fun `defaults are sane out of the box`() {
        val defaults = SparrowUserPreferences()
        assertEquals(SparrowUserPreferences.DEFAULT_MOVEMENT_FREQUENCY_MINUTES, defaults.movementFrequencyMinutes)
        assertEquals(false, defaults.onboardingComplete)
        assertEquals(true, defaults.autoMovementEnabled)
    }
}
