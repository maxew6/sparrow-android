package com.mahesh.sparrow.domain.model

/**
 * Immutable snapshot of the device's battery, normalized from whatever the
 * platform reports so the rest of the app never touches raw [android.os.BatteryManager]
 * intents directly.
 */
data class BatteryState(
    val percentage: Int,
    val isCharging: Boolean,
    val isFull: Boolean,
    val chargePlug: ChargePlug
) {
    companion object {
        /** Used before the first real battery broadcast arrives. */
        val Unknown = BatteryState(
            percentage = 100,
            isCharging = false,
            isFull = false,
            chargePlug = ChargePlug.NONE
        )

        /**
         * Clamps a raw level/scale pair (as reported by
         * `ACTION_BATTERY_CHANGED`) into a safe 0..100 percentage, guarding
         * against the -1/0 values some emulators and edge cases report.
         */
        fun normalizePercentage(level: Int, scale: Int): Int {
            if (level < 0 || scale <= 0) return Unknown.percentage
            val raw = (level * 100f / scale).toInt()
            return raw.coerceIn(0, 100)
        }
    }
}

enum class ChargePlug {
    NONE,
    AC,
    USB,
    WIRELESS
}
