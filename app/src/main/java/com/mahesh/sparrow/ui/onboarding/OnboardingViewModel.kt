package com.mahesh.sparrow.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahesh.sparrow.data.preferences.SparrowPreferences
import com.mahesh.sparrow.util.NameValidationResult
import com.mahesh.sparrow.util.NameValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingStep { MEET, PERMISSION, READY }

sealed class NameFieldError {
    data object Blank : NameFieldError()
    data class TooLong(val maxLength: Int) : NameFieldError()
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.MEET,
    val nameInput: String = "",
    val nameError: NameFieldError? = null,
    val overlayGranted: Boolean = false
)

class OnboardingViewModel(private val preferences: SparrowPreferences) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(nameInput = value, nameError = null) }
    }

    fun onContinueFromName() {
        when (val result = NameValidator.validate(_uiState.value.nameInput)) {
            is NameValidationResult.Valid -> {
                viewModelScope.launch {
                    preferences.setUserName(result.trimmedName)
                    _uiState.update { it.copy(step = OnboardingStep.PERMISSION) }
                }
            }
            is NameValidationResult.Blank ->
                _uiState.update { it.copy(nameError = NameFieldError.Blank) }
            is NameValidationResult.TooLong ->
                _uiState.update { it.copy(nameError = NameFieldError.TooLong(result.maxLength)) }
        }
    }

    /** Called whenever the host Activity re-checks Settings.canDrawOverlays(), e.g. on resume. */
    fun onOverlayPermissionChecked(granted: Boolean) {
        val wasAlreadyReady = _uiState.value.step == OnboardingStep.READY
        _uiState.update { it.copy(overlayGranted = granted) }
        if (granted && !wasAlreadyReady && _uiState.value.step == OnboardingStep.PERMISSION) {
            _uiState.update { it.copy(step = OnboardingStep.READY) }
        }
    }

    fun onSkipPermissionForNow() {
        _uiState.update { it.copy(step = OnboardingStep.READY) }
    }

    fun completeOnboarding() {
        viewModelScope.launch { preferences.setOnboardingComplete(true) }
    }
}
