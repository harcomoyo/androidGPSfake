package com.geopilot.app

import android.app.Application
import com.geopilot.app.data.SimulationPreferences

class GeoPilotApplication : Application() {
    val simulationPreferences by lazy { SimulationPreferences(this) }
}
