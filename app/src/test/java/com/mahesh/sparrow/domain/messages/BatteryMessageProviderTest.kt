package com.mahesh.sparrow.domain.messages

import com.mahesh.sparrow.domain.model.BatteryState
import com.mahesh.sparrow.domain.model.ChargePlug
import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryMessageProviderTest {

    private fun state(
        percentage: Int,
        isCharging: Boolean = false,
        isFull: Boolean = false
    ) = BatteryState(percentage, isCharging, isFull, ChargePlug.NONE)

    @Test
    fun `full battery flag always yields full message`() {
        assertEquals(BatteryMessageKind.FULL, BatteryMessageProvider.select(state(percentage = 60, isFull = true)))
    }

    @Test
    fun `charging at or above 95 percent yields full message`() {
        assertEquals(
            BatteryMessageKind.FULL,
            BatteryMessageProvider.select(state(percentage = 95, isCharging = true))
        )
        assertEquals(
            BatteryMessageKind.FULL,
            BatteryMessageProvider.select(state(percentage = 100, isCharging = true))
        )
    }

    @Test
    fun `charging below 95 percent yields charging message`() {
        assertEquals(
            BatteryMessageKind.CHARGING,
            BatteryMessageProvider.select(state(percentage = 94, isCharging = true))
        )
        assertEquals(
            BatteryMessageKind.CHARGING,
            BatteryMessageProvider.select(state(percentage = 10, isCharging = true))
        )
    }

    @Test
    fun `50 to 94 percent not charging yields high message`() {
        assertEquals(BatteryMessageKind.HIGH, BatteryMessageProvider.select(state(percentage = 50)))
        assertEquals(BatteryMessageKind.HIGH, BatteryMessageProvider.select(state(percentage = 94)))
    }

    @Test
    fun `20 to 49 percent yields mid message`() {
        assertEquals(BatteryMessageKind.MID, BatteryMessageProvider.select(state(percentage = 20)))
        assertEquals(BatteryMessageKind.MID, BatteryMessageProvider.select(state(percentage = 49)))
    }

    @Test
    fun `below 20 percent yields low message`() {
        assertEquals(BatteryMessageKind.LOW, BatteryMessageProvider.select(state(percentage = 19)))
        assertEquals(BatteryMessageKind.LOW, BatteryMessageProvider.select(state(percentage = 0)))
    }
}
