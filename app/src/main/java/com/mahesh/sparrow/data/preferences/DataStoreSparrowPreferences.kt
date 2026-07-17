package com.mahesh.sparrow.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mahesh.sparrow.domain.model.SparrowSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sparrow_preferences")

class DataStoreSparrowPreferences(private val context: Context) : SparrowPreferences {

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val OVERLAY_EVER_GRANTED = booleanPreferencesKey("overlay_ever_granted")
        val IS_RUNNING = booleanPreferencesKey("is_running")
        val PET_SIZE = stringPreferencesKey("pet_size")
        val AUTO_MOVEMENT_ENABLED = booleanPreferencesKey("auto_movement_enabled")
        val MOVEMENT_FREQUENCY_MINUTES = intPreferencesKey("movement_frequency_minutes")
        val GREETINGS_ENABLED = booleanPreferencesKey("greetings_enabled")
        val BATTERY_MESSAGES_ENABLED = booleanPreferencesKey("battery_messages_enabled")
        val REDUCED_MOTION_ENABLED = booleanPreferencesKey("reduced_motion_enabled")
        val LAST_POSITION_X = intPreferencesKey("last_position_x")
        val LAST_POSITION_Y = intPreferencesKey("last_position_y")
    }

    override val preferencesFlow: Flow<SparrowUserPreferences> =
        context.dataStore.data.map { prefs ->
            val defaults = SparrowUserPreferences()
            SparrowUserPreferences(
                userName = prefs[Keys.USER_NAME],
                onboardingComplete = prefs[Keys.ONBOARDING_COMPLETE] ?: defaults.onboardingComplete,
                overlayEverGranted = prefs[Keys.OVERLAY_EVER_GRANTED] ?: defaults.overlayEverGranted,
                isRunning = prefs[Keys.IS_RUNNING] ?: defaults.isRunning,
                petSize = SparrowSize.fromName(prefs[Keys.PET_SIZE]),
                autoMovementEnabled = prefs[Keys.AUTO_MOVEMENT_ENABLED] ?: defaults.autoMovementEnabled,
                movementFrequencyMinutes = prefs[Keys.MOVEMENT_FREQUENCY_MINUTES]
                    ?: defaults.movementFrequencyMinutes,
                greetingsEnabled = prefs[Keys.GREETINGS_ENABLED] ?: defaults.greetingsEnabled,
                batteryMessagesEnabled = prefs[Keys.BATTERY_MESSAGES_ENABLED]
                    ?: defaults.batteryMessagesEnabled,
                reducedMotionEnabled = prefs[Keys.REDUCED_MOTION_ENABLED] ?: defaults.reducedMotionEnabled,
                lastPositionX = prefs[Keys.LAST_POSITION_X],
                lastPositionY = prefs[Keys.LAST_POSITION_Y]
            )
        }

    override suspend fun setUserName(name: String) {
        context.dataStore.edit { it[Keys.USER_NAME] = name }
    }

    override suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
    }

    override suspend fun setOverlayEverGranted(granted: Boolean) {
        context.dataStore.edit { it[Keys.OVERLAY_EVER_GRANTED] = granted }
    }

    override suspend fun setIsRunning(running: Boolean) {
        context.dataStore.edit { it[Keys.IS_RUNNING] = running }
    }

    override suspend fun setPetSize(size: SparrowSize) {
        context.dataStore.edit { it[Keys.PET_SIZE] = size.name }
    }

    override suspend fun setAutoMovementEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_MOVEMENT_ENABLED] = enabled }
    }

    override suspend fun setMovementFrequencyMinutes(minutes: Int) {
        val clamped = SparrowUserPreferences.coerceFrequencyMinutes(minutes)
        context.dataStore.edit { it[Keys.MOVEMENT_FREQUENCY_MINUTES] = clamped }
    }

    override suspend fun setGreetingsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.GREETINGS_ENABLED] = enabled }
    }

    override suspend fun setBatteryMessagesEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BATTERY_MESSAGES_ENABLED] = enabled }
    }

    override suspend fun setReducedMotionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REDUCED_MOTION_ENABLED] = enabled }
    }

    override suspend fun saveLastPosition(x: Int, y: Int) {
        context.dataStore.edit {
            it[Keys.LAST_POSITION_X] = x
            it[Keys.LAST_POSITION_Y] = y
        }
    }

    override suspend fun clearLastPosition() {
        context.dataStore.edit {
            it.remove(Keys.LAST_POSITION_X)
            it.remove(Keys.LAST_POSITION_Y)
        }
    }
}
