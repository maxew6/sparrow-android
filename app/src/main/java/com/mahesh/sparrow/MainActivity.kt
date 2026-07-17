package com.mahesh.sparrow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mahesh.sparrow.data.preferences.SparrowPreferences
import com.mahesh.sparrow.overlay.SparrowOverlayService
import com.mahesh.sparrow.receiver.SparrowActionReceiver
import com.mahesh.sparrow.ui.onboarding.OnboardingScreen
import com.mahesh.sparrow.ui.onboarding.OnboardingViewModel
import com.mahesh.sparrow.ui.settings.SettingsScreen
import com.mahesh.sparrow.ui.settings.SettingsViewModel
import com.mahesh.sparrow.ui.theme.SparrowTheme
import com.mahesh.sparrow.util.OverlayPermissionHelper

class MainActivity : ComponentActivity() {

    private val preferences: SparrowPreferences by lazy { (application as SparrowApplication).preferences }

    private val onboardingViewModel: OnboardingViewModel by viewModels {
        LambdaViewModelFactory { OnboardingViewModel(preferences) }
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        LambdaViewModelFactory { SettingsViewModel(preferences) }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op: Sparrow works either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SparrowTheme {
                val prefsState by preferences.preferencesFlow.collectAsState(initial = null)

                Box(Modifier.fillMaxSize()) {
                    val prefs = prefsState
                    if (prefs != null) {
                        if (!prefs.onboardingComplete) {
                            OnboardingScreen(
                                viewModel = onboardingViewModel,
                                onRequestOverlayPermission = ::openOverlayPermissionSettings,
                                onStartSparrow = ::requestSparrowStart,
                                onOpenSettingsShortcut = { /* recomposition follows onboardingComplete automatically */ }
                            )
                        } else {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                isRunning = prefs.isRunning,
                                onStartSparrow = ::requestSparrowStart,
                                onStopSparrow = ::stopSparrowService,
                                onOpenOverlayPermissionSettings = ::openOverlayPermissionSettings
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Overlay permission can only be granted/revoked in system Settings, so
        // re-check every time the user comes back to this Activity.
        onboardingViewModel.onOverlayPermissionChecked(OverlayPermissionHelper.hasOverlayPermission(this))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun requestSparrowStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        ContextCompat.startForegroundService(this, Intent(this, SparrowOverlayService::class.java))
    }

    private fun stopSparrowService() {
        val intent = Intent(this, SparrowOverlayService::class.java).setAction(SparrowActionReceiver.ACTION_STOP)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun openOverlayPermissionSettings() {
        startActivity(OverlayPermissionHelper.buildPermissionSettingsIntent(this))
    }

    companion object {
        const val EXTRA_OPEN_SETTINGS = "com.mahesh.sparrow.extra.OPEN_SETTINGS"
    }
}

/** Minimal [ViewModelProvider.Factory] for constructing a ViewModel from a plain lambda. */
private class LambdaViewModelFactory<T : ViewModel>(private val creator: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator() as VM
}
