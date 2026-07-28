package com.geopilot.app

import android.app.Application
import com.geopilot.app.data.SimulationPreferences
import org.maplibre.android.MapLibre

class GeoPilotApplication : Application() {
    val simulationPreferences by lazy { SimulationPreferences(this) }

    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
    }
}
