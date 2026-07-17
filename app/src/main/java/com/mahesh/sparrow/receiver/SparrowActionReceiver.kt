package com.mahesh.sparrow.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.mahesh.sparrow.overlay.SparrowOverlayService

/**
 * Receives the pause/resume/stop taps from Sparrow's persistent notification
 * and re-delivers them to [SparrowOverlayService] as an explicit
 * `onStartCommand` action — the service already owns all pet/window state,
 * so this receiver does no work of its own beyond routing.
 */
class SparrowActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in KNOWN_ACTIONS) return

        val serviceIntent = Intent(context, SparrowOverlayService::class.java).setAction(action)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    companion object {
        const val ACTION_PAUSE = "com.mahesh.sparrow.action.PAUSE"
        const val ACTION_RESUME = "com.mahesh.sparrow.action.RESUME"
        const val ACTION_STOP = "com.mahesh.sparrow.action.STOP"

        private val KNOWN_ACTIONS = setOf(ACTION_PAUSE, ACTION_RESUME, ACTION_STOP)
    }
}
