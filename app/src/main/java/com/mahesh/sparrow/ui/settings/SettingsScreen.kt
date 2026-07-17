package com.mahesh.sparrow.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mahesh.sparrow.R
import com.mahesh.sparrow.data.preferences.SparrowUserPreferences
import com.mahesh.sparrow.domain.model.SparrowSize

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    isRunning: Boolean,
    onStartSparrow: () -> Unit,
    onStopSparrow: () -> Unit,
    onOpenOverlayPermissionSettings: () -> Unit
) {
    val prefs by viewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            StatusSection(isRunning, onStartSparrow, onStopSparrow)
            SectionDivider()

            NameSection(prefs.userName.orEmpty(), viewModel::updateName)
            SectionDivider()

            PetSizeSection(prefs.petSize, viewModel::updatePetSize)
            SectionDivider()

            MovementSection(prefs, viewModel::updateAutoMovement, viewModel::updateMovementFrequency)
            SectionDivider()

            ToggleRow(
                title = stringResource(R.string.settings_greetings),
                checked = prefs.greetingsEnabled,
                onCheckedChange = viewModel::updateGreetings
            )
            ToggleRow(
                title = stringResource(R.string.settings_battery_messages),
                checked = prefs.batteryMessagesEnabled,
                onCheckedChange = viewModel::updateBatteryMessages
            )
            ToggleRow(
                title = stringResource(R.string.settings_reduced_motion),
                checked = prefs.reducedMotionEnabled,
                onCheckedChange = viewModel::updateReducedMotion
            )
            SectionDivider()

            OutlinedButton(onClick = viewModel::resetPosition, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_reset_position))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onOpenOverlayPermissionSettings, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_overlay_permission))
            }
            SectionDivider()

            Text(stringResource(R.string.settings_privacy_info), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.settings_privacy_body), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun StatusSection(isRunning: Boolean, onStart: () -> Unit, onStop: () -> Unit) {
    Text(
        text = stringResource(
            if (isRunning) R.string.settings_status_running else R.string.settings_status_stopped
        ),
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(Modifier.height(12.dp))
    if (isRunning) {
        Button(onClick = onStop, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.menu_stop_sparrow))
        }
    } else {
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_start_sparrow))
        }
    }
}

@Composable
private fun NameSection(name: String, onNameChanged: (String) -> Unit) {
    Text(stringResource(R.string.settings_change_name), style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = name,
        onValueChange = onNameChanged,
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PetSizeSection(selected: SparrowSize, onSelect: (SparrowSize) -> Unit) {
    Text(stringResource(R.string.settings_pet_size), style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SparrowSize.entries.forEach { size ->
            val label = when (size) {
                SparrowSize.SMALL -> stringResource(R.string.settings_pet_size_small)
                SparrowSize.MEDIUM -> stringResource(R.string.settings_pet_size_medium)
                SparrowSize.LARGE -> stringResource(R.string.settings_pet_size_large)
            }
            FilterChip(selected = selected == size, onClick = { onSelect(size) }, label = { Text(label) })
        }
    }
}

@Composable
private fun MovementSection(
    prefs: SparrowUserPreferences,
    onAutoMovementChanged: (Boolean) -> Unit,
    onFrequencyChanged: (Int) -> Unit
) {
    ToggleRow(
        title = stringResource(R.string.settings_auto_movement),
        checked = prefs.autoMovementEnabled,
        onCheckedChange = onAutoMovementChanged
    )
    if (prefs.autoMovementEnabled) {
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.settings_movement_frequency) + ": ${prefs.movementFrequencyMinutes} min",
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = prefs.movementFrequencyMinutes.toFloat(),
            onValueChange = { onFrequencyChanged(it.toInt()) },
            valueRange = SparrowUserPreferences.MIN_MOVEMENT_FREQUENCY_MINUTES.toFloat()..
                SparrowUserPreferences.MAX_MOVEMENT_FREQUENCY_MINUTES.toFloat(),
            steps = SparrowUserPreferences.MAX_MOVEMENT_FREQUENCY_MINUTES -
                SparrowUserPreferences.MIN_MOVEMENT_FREQUENCY_MINUTES - 1
        )
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SectionDivider() {
    Spacer(Modifier.height(16.dp))
    HorizontalDivider()
    Spacer(Modifier.height(16.dp))
}
