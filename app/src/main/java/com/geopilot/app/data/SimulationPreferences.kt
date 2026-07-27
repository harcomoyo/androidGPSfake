package com.geopilot.app.data

import android.content.Context
import com.geopilot.app.model.SimulationConfig

class SimulationPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun load(): SimulationConfig = SimulationConfig(
        latitude = preferences.getString(KEY_LATITUDE, null)?.toDoubleOrNull() ?: -6.2088,
        longitude = preferences.getString(KEY_LONGITUDE, null)?.toDoubleOrNull() ?: 106.8456,
        altitude = preferences.getString(KEY_ALTITUDE, null)?.toDoubleOrNull() ?: 8.0,
        accuracy = preferences.getFloat(KEY_ACCURACY, 5f),
        speedKmh = preferences.getFloat(KEY_SPEED, 0f),
        bearing = preferences.getFloat(KEY_BEARING, 0f),
        updateIntervalMs = preferences.getLong(KEY_INTERVAL, 1_000L),
    )

    fun save(config: SimulationConfig) {
        preferences.edit()
            .putString(KEY_LATITUDE, config.latitude.toString())
            .putString(KEY_LONGITUDE, config.longitude.toString())
            .putString(KEY_ALTITUDE, config.altitude.toString())
            .putFloat(KEY_ACCURACY, config.accuracy)
            .putFloat(KEY_SPEED, config.speedKmh)
            .putFloat(KEY_BEARING, config.bearing)
            .putLong(KEY_INTERVAL, config.updateIntervalMs)
            .apply()
    }

    private companion object {
        const val FILE_NAME = "simulation_preferences"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
        const val KEY_ALTITUDE = "altitude"
        const val KEY_ACCURACY = "accuracy"
        const val KEY_SPEED = "speed"
        const val KEY_BEARING = "bearing"
        const val KEY_INTERVAL = "interval"
    }
}
