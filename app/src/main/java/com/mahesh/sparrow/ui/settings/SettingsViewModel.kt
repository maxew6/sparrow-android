package com.mahesh.sparrow.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahesh.sparrow.data.preferences.SparrowPreferences
import com.mahesh.sparrow.data.preferences.SparrowUserPreferences
import com.mahesh.sparrow.domain.model.SparrowSize
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val preferences: SparrowPreferences) : ViewModel() {

    val uiState: StateFlow<SparrowUserPreferences> = preferences.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SparrowUserPreferences()
    )

    fun updateName(name: String) {
        if (name.length > com.mahesh.sparrow.util.NameValidator.MAX_LENGTH) return
        viewModelScope.launch { preferences.setUserName(name) }
    }

    fun updatePetSize(size: SparrowSize) {
        viewModelScope.launch { preferences.setPetSize(size) }
    }

    fun updateAutoMovement(enabled: Boolean) {
        viewModelScope.launch { preferences.setAutoMovementEnabled(enabled) }
    }

    fun updateMovementFrequency(minutes: Int) {
        viewModelScope.launch { preferences.setMovementFrequencyMinutes(minutes) }
    }

    fun updateGreetings(enabled: Boolean) {
        viewModelScope.launch { preferences.setGreetingsEnabled(enabled) }
    }

    fun updateBatteryMessages(enabled: Boolean) {
        viewModelScope.launch { preferences.setBatteryMessagesEnabled(enabled) }
    }

    fun updateReducedMotion(enabled: Boolean) {
        viewModelScope.launch { preferences.setReducedMotionEnabled(enabled) }
    }

    fun resetPosition() {
        viewModelScope.launch { preferences.clearLastPosition() }
    }
}
