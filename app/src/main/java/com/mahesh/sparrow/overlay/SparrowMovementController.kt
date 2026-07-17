package com.mahesh.sparrow.overlay

import com.mahesh.sparrow.domain.model.SafeBounds
import com.mahesh.sparrow.domain.model.SparrowAnimationState
import com.mahesh.sparrow.domain.model.SparrowPosition
import com.mahesh.sparrow.domain.model.snappedToNearestEdge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Drives Sparrow's [SparrowAnimationState] machine and, for automatic
 * relocation, picks and animates toward new on-screen positions. Manual
 * drag/tap gestures are reported in via [notifyDragStarted]/[notifyDragEnded]
 * so this controller only ever *reacts* to them rather than owning touch
 * handling itself.
 */
class SparrowMovementController(
    private val scope: CoroutineScope,
    private val boundsProvider: () -> SafeBounds,
    private val overlaySizePx: () -> Pair<Int, Int>,
    private val onPositionChange: (SparrowPosition) -> Unit,
    private val onStateChange: (SparrowAnimationState) -> Unit
) {
    private data class MovementSettings(
        val autoMovementEnabled: Boolean = true,
        val frequencyMinutes: Int = 5,
        val reducedMotion: Boolean = false
    )

    private val _state = MutableStateFlow(SparrowAnimationState.ENTERING)
    val state: StateFlow<SparrowAnimationState> = _state.asStateFlow()

    private var settings = MovementSettings()
    private var autoMovementJob: Job? = null
    private var lastKnownPosition: SparrowPosition = SparrowPosition(0, 0)

    /** Flies Sparrow in from an edge and settles into idle behavior. */
    fun start(initialPosition: SparrowPosition) {
        lastKnownPosition = initialPosition
        setState(SparrowAnimationState.ENTERING)
        scope.launch {
            val (w, h) = overlaySizePx()
            val entryTarget = initialPosition.snappedToNearestEdge(boundsProvider(), w, h)
            movePosition(entryTarget)
            delay(Random.nextLong(1500, 2500))
            settleIntoIdle()
        }
    }

    fun updateSettings(autoMovementEnabled: Boolean, frequencyMinutes: Int, reducedMotion: Boolean) {
        settings = MovementSettings(autoMovementEnabled, frequencyMinutes.coerceAtLeast(2), reducedMotion)
        if (_state.value == SparrowAnimationState.IDLE) {
            scheduleNextRelocation()
        }
        if (reducedMotion) {
            autoMovementJob?.cancel()
        }
    }

    fun notifyDragStarted() {
        autoMovementJob?.cancel()
        setState(SparrowAnimationState.BEING_DRAGGED)
    }

    fun notifyDragMoved(position: SparrowPosition) {
        lastKnownPosition = position
    }

    fun notifyDragEnded(finalPosition: SparrowPosition) {
        lastKnownPosition = finalPosition
        scope.launch {
            setState(SparrowAnimationState.LANDING)
            val (w, h) = overlaySizePx()
            val snapped = finalPosition.snappedToNearestEdge(boundsProvider(), w, h)
            movePosition(snapped)
            delay(if (settings.reducedMotion) 0 else 300)
            settleIntoIdle()
        }
    }

    fun pause() {
        autoMovementJob?.cancel()
        setState(SparrowAnimationState.PAUSED)
    }

    fun resume() {
        settleIntoIdle()
    }

    fun release() {
        autoMovementJob?.cancel()
    }

    private fun settleIntoIdle() {
        setState(SparrowAnimationState.IDLE)
        scheduleNextRelocation()
    }

    private fun scheduleNextRelocation() {
        autoMovementJob?.cancel()
        if (!settings.autoMovementEnabled || settings.reducedMotion) return
        autoMovementJob = scope.launch {
            delay(settings.frequencyMinutes * 60_000L)
            relocateToRandomSafeSpot()
        }
    }

    private suspend fun relocateToRandomSafeSpot() {
        if (_state.value != SparrowAnimationState.IDLE) return

        setState(SparrowAnimationState.FLYING)
        delay(600)

        val bounds = boundsProvider()
        val (w, h) = overlaySizePx()
        val maxX = (bounds.right - w).coerceAtLeast(bounds.left)
        val maxY = (bounds.bottom - h).coerceAtLeast(bounds.top)
        val target = SparrowPosition(
            x = if (maxX > bounds.left) Random.nextInt(bounds.left, maxX + 1) else bounds.left,
            y = if (maxY > bounds.top) Random.nextInt(bounds.top, maxY + 1) else bounds.top
        ).snappedToNearestEdge(bounds, w, h)

        setState(SparrowAnimationState.LANDING)
        movePosition(target)
        delay(300)
        settleIntoIdle()
    }

    private fun movePosition(position: SparrowPosition) {
        lastKnownPosition = position
        onPositionChange(position)
    }

    private fun setState(newState: SparrowAnimationState) {
        val current = _state.value
        if (current == newState || current.canTransitionTo(newState)) {
            _state.value = newState
            onStateChange(newState)
        }
    }
}
