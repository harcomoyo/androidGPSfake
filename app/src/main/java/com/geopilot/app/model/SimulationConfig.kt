package com.geopilot.app.model

data class SimulationConfig(
    val latitude: Double = -6.2088,
    val longitude: Double = 106.8456,
    val altitude: Double = 8.0,
    val accuracy: Float = 5f,
    val speedKmh: Float = 0f,
    val bearing: Float = 0f,
    val updateIntervalMs: Long = 1_000L,
) {
    val isValid: Boolean
        get() = latitude in -90.0..90.0 &&
            longitude in -180.0..180.0 &&
            accuracy > 0f &&
            updateIntervalMs >= 250L
}
