package com.mahesh.sparrow.overlay

import android.app.NotificationManager
import android.content.Intent
import android.content.res.Configuration
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.mahesh.sparrow.MainActivity
import com.mahesh.sparrow.R
import com.mahesh.sparrow.SparrowApplication
import com.mahesh.sparrow.data.battery.AndroidBatteryMonitor
import com.mahesh.sparrow.data.battery.BatteryMonitor
import com.mahesh.sparrow.data.preferences.SparrowPreferences
import com.mahesh.sparrow.data.preferences.SparrowUserPreferences
import com.mahesh.sparrow.domain.messages.BatteryMessageKind
import com.mahesh.sparrow.domain.messages.BatteryMessageProvider
import com.mahesh.sparrow.domain.messages.GreetingKind
import com.mahesh.sparrow.domain.messages.GreetingProvider
import com.mahesh.sparrow.domain.model.BatteryState
import com.mahesh.sparrow.domain.model.SafeBounds
import com.mahesh.sparrow.domain.model.SparrowPosition
import com.mahesh.sparrow.domain.model.clampTo
import com.mahesh.sparrow.receiver.SparrowActionReceiver
import com.mahesh.sparrow.util.DisplayBoundsProvider
import com.mahesh.sparrow.util.OverlayPermissionHelper
import com.mahesh.sparrow.util.SystemMotionSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.roundToInt

/**
 * The foreground service that owns Sparrow's floating overlay window from
 * the moment the user starts it until they explicitly stop it. Everything
 * WindowManager-related is delegated to [OverlayWindowController]; this
 * class only coordinates *when* things happen.
 */
class SparrowOverlayService : LifecycleService() {

    private lateinit var preferences: SparrowPreferences
    private lateinit var batteryMonitor: BatteryMonitor
    private lateinit var windowController: OverlayWindowController
    private lateinit var boundsProvider: DisplayBoundsProvider
    private lateinit var notificationManager: SparrowNotificationManager
    private lateinit var menuOverlay: SparrowMenuOverlay

    private var overlayView: SparrowOverlayView? = null
    private var movementController: SparrowMovementController? = null
    private var permissionWatchJob: Job? = null
    private var dragStartPosition: SparrowPosition? = null

    private var isPaused = false
    private var greetingsEnabled = true
    private var batteryMessagesEnabled = true
    private var isDarkTheme = false
    private var latestBattery = BatteryState.Unknown
    private var cachedUserName: String? = null
    private var lastTapShowedGreeting = true
    private var reducedMotionPreference: Boolean = false

    private var birdSizePx = 0
    private var overlayWidthPx = 0
    private var overlayHeightPx = 0

    override fun onCreate() {
        super.onCreate()
        preferences = (application as SparrowApplication).preferences
        batteryMonitor = AndroidBatteryMonitor(applicationContext)
        windowController = OverlayWindowController(applicationContext)
        boundsProvider = DisplayBoundsProvider(applicationContext)
        notificationManager = SparrowNotificationManager(applicationContext)
        menuOverlay = SparrowMenuOverlay(applicationContext, windowController)
        notificationManager.ensureChannel()
        isDarkTheme = isNightMode(resources.configuration)

        observeBattery()
        watchOverlayPermission()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // startForeground must be called promptly after startForegroundService();
        // the notification is cheap to build, so this always happens first.
        startForeground(SparrowNotificationManager.NOTIFICATION_ID, notificationManager.buildNotification(isPaused))

        when (intent?.action) {
            SparrowActionReceiver.ACTION_PAUSE -> handlePauseAction()
            SparrowActionReceiver.ACTION_RESUME -> handleResumeAction()
            SparrowActionReceiver.ACTION_STOP -> stopSparrow()
            else -> ensureOverlayStarted()
        }

        return START_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isDarkTheme = isNightMode(newConfig)
        val view = overlayView ?: return
        view.isDarkTheme = isDarkTheme
        val bounds = boundsProvider.currentSafeBounds()
        val current = windowController.currentPosition(view) ?: return
        val clamped = current.clampTo(bounds, overlayWidthPx, overlayHeightPx)
        if (clamped != current) windowController.updatePosition(view, clamped)
    }

    override fun onDestroy() {
        permissionWatchJob?.cancel()
        movementController?.release()
        overlayView?.let { view ->
            windowController.currentPosition(view)?.let { pos ->
                lifecycleScope.launch {
                    preferences.saveLastPosition(pos.x, pos.y)
                    preferences.setIsRunning(false)
                }
            } ?: lifecycleScope.launch { preferences.setIsRunning(false) }
            view.release()
        }
        menuOverlay.dismiss()
        windowController.removeAll()
        overlayView = null
        movementController = null
        super.onDestroy()
    }

    // ---- Startup ----

    private fun ensureOverlayStarted() {
        if (overlayView != null) return

        if (!OverlayPermissionHelper.hasOverlayPermission(this)) {
            stopSparrow()
            return
        }

        lifecycleScope.launch {
            val prefs = preferences.preferencesFlow.first()
            preferences.setOverlayEverGranted(true)
            setUpOverlay(prefs)
            observePreferenceChanges()
        }
    }

    private fun setUpOverlay(prefs: SparrowUserPreferences) {
        recomputeSizes(prefs)
        reducedMotionPreference = prefs.reducedMotionEnabled

        val view = SparrowOverlayView(this).apply {
            this.birdSizePx = this@SparrowOverlayService.birdSizePx
            this.isDarkTheme = this@SparrowOverlayService.isDarkTheme
        }

        val bounds = boundsProvider.currentSafeBounds()
        val initialPosition = restoredOrDefaultPosition(prefs, bounds)

        windowController.addOverlay(view, initialPosition, overlayWidthPx, overlayHeightPx)
        overlayView = view

        SparrowGestureController(this, buildGestureListener(view)).attachTo(view)

        movementController = SparrowMovementController(
            scope = lifecycleScope,
            boundsProvider = { boundsProvider.currentSafeBounds() },
            overlaySizePx = { overlayWidthPx to overlayHeightPx },
            onPositionChange = { pos -> windowController.updatePosition(view, pos) },
            onStateChange = { state -> view.applyMotionState(state, effectiveReducedMotion(reducedMotionPreference)) }
        ).also {
            it.updateSettings(
                prefs.autoMovementEnabled,
                prefs.movementFrequencyMinutes,
                effectiveReducedMotion(reducedMotionPreference)
            )
            it.start(initialPosition)
        }

        greetingsEnabled = prefs.greetingsEnabled
        batteryMessagesEnabled = prefs.batteryMessagesEnabled
        cachedUserName = prefs.userName

        lifecycleScope.launch { preferences.setIsRunning(true) }
    }

    private fun recomputeSizes(prefs: SparrowUserPreferences) {
        val density = resources.displayMetrics.density
        birdSizePx = (prefs.petSize.touchTargetDp * density).roundToInt()
        val bubbleReservePx = (birdSizePx * 1.3f).roundToInt()
        overlayWidthPx = (birdSizePx * 2.4f).roundToInt()
        overlayHeightPx = birdSizePx + bubbleReservePx
    }

    private fun restoredOrDefaultPosition(prefs: SparrowUserPreferences, bounds: SafeBounds): SparrowPosition {
        val restored = if (prefs.lastPositionX != null && prefs.lastPositionY != null) {
            SparrowPosition(prefs.lastPositionX, prefs.lastPositionY)
        } else {
            SparrowPosition(bounds.right - overlayWidthPx, bounds.top + bounds.height / 3)
        }
        return restored.clampTo(bounds, overlayWidthPx, overlayHeightPx)
    }

    // ---- Preferences / battery observers ----

    private fun observePreferenceChanges() {
        lifecycleScope.launch {
            preferences.preferencesFlow.collect { prefs ->
                greetingsEnabled = prefs.greetingsEnabled
                batteryMessagesEnabled = prefs.batteryMessagesEnabled
                cachedUserName = prefs.userName
                reducedMotionPreference = prefs.reducedMotionEnabled

                val controller = movementController
                val view = overlayView
                if (controller != null && view != null) {
                    controller.updateSettings(
                        prefs.autoMovementEnabled,
                        prefs.movementFrequencyMinutes,
                        effectiveReducedMotion(reducedMotionPreference)
                    )
                    view.applyMotionState(controller.state.value, effectiveReducedMotion(reducedMotionPreference))
                }
                view?.let { applySizeIfChanged(it, prefs) }
            }
        }
    }

    private fun applySizeIfChanged(view: SparrowOverlayView, prefs: SparrowUserPreferences) {
        val previousBirdSizePx = birdSizePx
        recomputeSizes(prefs)
        if (birdSizePx == previousBirdSizePx) return
        view.birdSizePx = birdSizePx
        windowController.updateSize(view, overlayWidthPx, overlayHeightPx)
    }

    private fun observeBattery() {
        lifecycleScope.launch {
            batteryMonitor.observe().collect { state -> latestBattery = state }
        }
    }

    private fun watchOverlayPermission() {
        permissionWatchJob = lifecycleScope.launch {
            while (isActive) {
                delay(PERMISSION_CHECK_INTERVAL_MS)
                if (overlayView != null && !OverlayPermissionHelper.hasOverlayPermission(this@SparrowOverlayService)) {
                    stopSparrow()
                }
            }
        }
    }

    private fun effectiveReducedMotion(userPreference: Boolean): Boolean =
        userPreference || SystemMotionSettings.isReducedMotionEnabled(this)

    private fun isNightMode(config: Configuration): Boolean =
        (config.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    // ---- Gestures ----

    private fun buildGestureListener(view: SparrowOverlayView) = object : SparrowGestureController.Listener {
        override fun onTap() {
            menuOverlay.dismiss()
            view.playTapHop()
            showTapMessage(view)
        }

        override fun onLongPress() {
            showLongPressMenu(view)
        }

        override fun onDragStart() {
            menuOverlay.dismiss()
            dragStartPosition = windowController.currentPosition(view)
            movementController?.notifyDragStarted()
        }

        override fun onDrag(dx: Int, dy: Int) {
            val origin = dragStartPosition ?: windowController.currentPosition(view) ?: return
            val bounds = boundsProvider.currentSafeBounds()
            val target = SparrowPosition(origin.x + dx, origin.y + dy).clampTo(bounds, overlayWidthPx, overlayHeightPx)
            windowController.updatePosition(view, target)
            movementController?.notifyDragMoved(target)
        }

        override fun onDragEnd() {
            dragStartPosition = null
            val end = windowController.currentPosition(view) ?: return
            movementController?.notifyDragEnded(end)
            lifecycleScope.launch { preferences.saveLastPosition(end.x, end.y) }
        }
    }

    private fun showTapMessage(view: SparrowOverlayView) {
        val message = when {
            greetingsEnabled && batteryMessagesEnabled -> {
                lastTapShowedGreeting = !lastTapShowedGreeting
                if (lastTapShowedGreeting) greetingText() else batteryText()
            }
            greetingsEnabled -> greetingText()
            batteryMessagesEnabled -> batteryText()
            else -> null
        }
        message?.let { view.showSpeechBubble(it) }
    }

    private fun greetingText(): String {
        val name = cachedUserName?.takeIf { it.isNotBlank() } ?: getString(R.string.default_friend_name)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (GreetingProvider.select(hour)) {
            GreetingKind.MORNING -> getString(R.string.greeting_morning, name)
            GreetingKind.HELLO -> getString(R.string.greeting_hello, name)
        }
    }

    private fun batteryText(): String {
        val percentage = latestBattery.percentage
        return when (BatteryMessageProvider.select(latestBattery)) {
            BatteryMessageKind.FULL -> getString(R.string.battery_full)
            BatteryMessageKind.CHARGING -> getString(R.string.battery_charging, percentage)
            BatteryMessageKind.HIGH -> getString(R.string.battery_high, percentage)
            BatteryMessageKind.MID -> getString(R.string.battery_mid, percentage)
            BatteryMessageKind.LOW -> getString(R.string.battery_low, percentage)
        }
    }

    private fun showLongPressMenu(view: SparrowOverlayView) {
        val position = windowController.currentPosition(view) ?: return
        val bounds = boundsProvider.currentSafeBounds()

        val items = buildList {
            add(SparrowMenuOverlay.MenuItem(getString(R.string.menu_greet_me)) {
                view.showSpeechBubble(greetingText())
            })
            add(SparrowMenuOverlay.MenuItem(getString(R.string.menu_battery_status)) {
                view.showSpeechBubble(batteryText())
            })
            add(
                if (isPaused) {
                    SparrowMenuOverlay.MenuItem(getString(R.string.menu_resume_movement)) { handleResumeAction() }
                } else {
                    SparrowMenuOverlay.MenuItem(getString(R.string.menu_pause_movement)) { handlePauseAction() }
                }
            )
            add(SparrowMenuOverlay.MenuItem(getString(R.string.menu_open_settings)) { openSettingsActivity() })
            add(SparrowMenuOverlay.MenuItem(getString(R.string.menu_stop_sparrow)) { stopSparrow() })
        }

        menuOverlay.show(position, birdSizePx, bounds, isDarkTheme, items)
    }

    private fun openSettingsActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_SETTINGS, true)
        }
        startActivity(intent)
    }

    // ---- Notification actions ----

    private fun handlePauseAction() {
        isPaused = true
        movementController?.pause()
        refreshNotification()
    }

    private fun handleResumeAction() {
        isPaused = false
        movementController?.resume()
        refreshNotification()
    }

    private fun refreshNotification() {
        getSystemService(NotificationManager::class.java)
            ?.notify(SparrowNotificationManager.NOTIFICATION_ID, notificationManager.buildNotification(isPaused))
    }

    private fun stopSparrow() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        private const val PERMISSION_CHECK_INTERVAL_MS = 30_000L
    }
}
