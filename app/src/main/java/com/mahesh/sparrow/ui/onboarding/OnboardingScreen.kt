package com.mahesh.sparrow.ui.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mahesh.sparrow.R
import com.mahesh.sparrow.ui.components.SparrowIllustration

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onRequestOverlayPermission: () -> Unit,
    onStartSparrow: () -> Unit,
    onOpenSettingsShortcut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (uiState.step) {
                OnboardingStep.MEET -> MeetSparrowStep(
                    state = uiState,
                    onNameChanged = viewModel::onNameChanged,
                    onContinue = viewModel::onContinueFromName
                )
                OnboardingStep.PERMISSION -> PermissionStep(
                    onAllow = onRequestOverlayPermission,
                    onNotNow = viewModel::onSkipPermissionForNow
                )
                OnboardingStep.READY -> ReadyStep(
                    onStart = { viewModel.completeOnboarding(); onStartSparrow() },
                    onOpenSettings = { viewModel.completeOnboarding(); onOpenSettingsShortcut() }
                )
            }
        }
    }
}

@Composable
private fun MeetSparrowStep(
    state: OnboardingUiState,
    onNameChanged: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SparrowIllustration()
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_meet_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.nameInput,
            onValueChange = onNameChanged,
            label = { Text(stringResource(R.string.onboarding_name_hint)) },
            singleLine = true,
            isError = state.nameError != null,
            supportingText = {
                when (val error = state.nameError) {
                    is NameFieldError.Blank -> Text(stringResource(R.string.onboarding_name_error_blank))
                    is NameFieldError.TooLong -> Text(
                        stringResource(R.string.onboarding_name_error_length, error.maxLength)
                    )
                    null -> {}
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_continue))
        }
    }
}

@Composable
private fun PermissionStep(onAllow: () -> Unit, onNotNow: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SparrowIllustration(sizeDp = 96)
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_permission_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_permission_privacy),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAllow, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_allow_overlay))
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onNotNow, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_not_now))
        }
    }
}

@Composable
private fun ReadyStep(onStart: () -> Unit, onOpenSettings: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SparrowIllustration()
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_ready_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_start_sparrow))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_open_settings))
        }
    }
}
