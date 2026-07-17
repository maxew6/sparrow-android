package com.mahesh.sparrow.data.preferences

import com.mahesh.sparrow.domain.model.SparrowSize
import kotlinx.coroutines.flow.Flow

/** Everything Sparrow remembers locally between launches. */
data class SparrowUserPreferences(
    val userName: String? = null,
    val onboardingComplete: Boolean = false,
    val overlayEverGranted: Boolean = false,
    val isRunning: Boolean = false,
    val petSize: SparrowSize = SparrowSize.Default,
    val autoMovementEnabled: Boolean = true,
    val movementFrequencyMinutes: Int = DEFAULT_MOVEMENT_FREQUENCY_MINUTES,
    val greetingsEnabled: Boolean = true,
    val batteryMessagesEnabled: Boolean = true,
    val reducedMotionEnabled: Boolean = false,
    val lastPositionX: Int? = null,
    val lastPositionY: Int? = null
) {
    companion object {
        const val DEFAULT_MOVEMENT_FREQUENCY_MINUTES = 5
        const val MIN_MOVEMENT_FREQUENCY_MINUTES = 2
        const val MAX_MOVEMENT_FREQUENCY_MINUTES = 30

        /** Keeps a user- or restored-supplied frequency inside the allowed range. */
        fun coerceFrequencyMinutes(minutes: Int): Int =
            minutes.coerceIn(MIN_MOVEMENT_FREQUENCY_MINUTES, MAX_MOVEMENT_FREQUENCY_MINUTES)
    }
}

/** Local, on-device preference storage. No account, no sync, no analytics. */
interface SparrowPreferences {
    val preferencesFlow: Flow<SparrowUserPreferences>

    suspend fun setUserName(name: String)
    suspend fun setOnboardingComplete(complete: Boolean)
    suspend fun setOverlayEverGranted(granted: Boolean)
    suspend fun setIsRunning(running: Boolean)
    suspend fun setPetSize(size: SparrowSize)
    suspend fun setAutoMovementEnabled(enabled: Boolean)
    suspend fun setMovementFrequencyMinutes(minutes: Int)
    suspend fun setGreetingsEnabled(enabled: Boolean)
    suspend fun setBatteryMessagesEnabled(enabled: Boolean)
    suspend fun setReducedMotionEnabled(enabled: Boolean)
    suspend fun saveLastPosition(x: Int, y: Int)
    suspend fun clearLastPosition()
}
