package com.mahesh.sparrow.data.battery

import com.mahesh.sparrow.domain.model.BatteryState
import kotlinx.coroutines.flow.Flow

/**
 * Observes device battery state. Implementations should only hold a
 * broadcast receiver (or similar OS hook) registered while [observe]'s
 * returned flow has an active collector.
 */
interface BatteryMonitor {
    fun observe(): Flow<BatteryState>
}
