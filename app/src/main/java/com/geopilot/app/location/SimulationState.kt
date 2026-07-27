package com.geopilot.app.location

import com.geopilot.app.model.SimulationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SimulationStatus(
    val running: Boolean = false,
    val starting: Boolean = false,
    val config: SimulationConfig? = null,
    val error: String? = null,
)

object SimulationState {
    private val mutableStatus = MutableStateFlow(SimulationStatus())
    val status = mutableStatus.asStateFlow()

    fun start(config: SimulationConfig) {
        mutableStatus.value = SimulationStatus(starting = true, config = config)
    }

    fun update(config: SimulationConfig) {
        mutableStatus.value = SimulationStatus(running = true, config = config)
    }

    fun stop() {
        mutableStatus.value = SimulationStatus()
    }

    fun fail(message: String) {
        mutableStatus.value = SimulationStatus(error = message)
    }
}
