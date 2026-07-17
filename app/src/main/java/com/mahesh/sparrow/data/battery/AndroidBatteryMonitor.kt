package com.mahesh.sparrow.data.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import com.mahesh.sparrow.domain.model.BatteryState
import com.mahesh.sparrow.domain.model.ChargePlug
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidBatteryMonitor(private val context: Context) : BatteryMonitor {

    override fun observe(): Flow<BatteryState> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                trySend(intent.toBatteryState())
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        // ACTION_BATTERY_CHANGED is a sticky broadcast, so registering
        // immediately delivers the current state without polling.
        val sticky = ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        sticky?.let { trySend(it.toBatteryState()) }

        awaitClose { context.unregisterReceiver(receiver) }
    }

    private fun Intent.toBatteryState(): BatteryState {
        val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val plugged = getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        val isFull = status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> ChargePlug.AC
            BatteryManager.BATTERY_PLUGGED_USB -> ChargePlug.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargePlug.WIRELESS
            else -> ChargePlug.NONE
        }

        return BatteryState(
            percentage = BatteryState.normalizePercentage(level, scale),
            isCharging = isCharging,
            isFull = isFull,
            chargePlug = chargePlug
        )
    }
}
