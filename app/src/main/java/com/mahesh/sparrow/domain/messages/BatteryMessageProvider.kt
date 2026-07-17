package com.mahesh.sparrow.domain.messages

import com.mahesh.sparrow.domain.model.BatteryState

/** Which battery string to show; the caller resolves this to actual text. */
enum class BatteryMessageKind { FULL, CHARGING, HIGH, MID, LOW }

/**
 * Chooses a battery message from a [BatteryState] using the fixed rule table
 * from the product spec:
 *  - full, or charging at >=95%      -> FULL
 *  - charging below 95%              -> CHARGING
 *  - 50-100%, not charging/full      -> HIGH
 *  - 20-49%                          -> MID
 *  - below 20%                       -> LOW
 */
object BatteryMessageProvider {
    private const val FULL_THRESHOLD = 95
    private const val HIGH_THRESHOLD = 50
    private const val MID_THRESHOLD = 20

    fun select(state: BatteryState): BatteryMessageKind {
        val percentage = state.percentage.coerceIn(0, 100)
        return when {
            state.isFull || (state.isCharging && percentage >= FULL_THRESHOLD) -> BatteryMessageKind.FULL
            state.isCharging -> BatteryMessageKind.CHARGING
            percentage >= HIGH_THRESHOLD -> BatteryMessageKind.HIGH
            percentage >= MID_THRESHOLD -> BatteryMessageKind.MID
            else -> BatteryMessageKind.LOW
        }
    }
}
