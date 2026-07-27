package com.geopilot.app.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulationConfigTest {
    @Test
    fun defaultConfigurationIsValid() {
        assertTrue(SimulationConfig().isValid)
    }

    @Test
    fun latitudeOutsideWorldBoundsIsInvalid() {
        assertFalse(SimulationConfig(latitude = 90.1).isValid)
    }

    @Test
    fun longitudeOutsideWorldBoundsIsInvalid() {
        assertFalse(SimulationConfig(longitude = -180.1).isValid)
    }

    @Test
    fun nonPositiveAccuracyIsInvalid() {
        assertFalse(SimulationConfig(accuracy = 0f).isValid)
    }

    @Test
    fun excessivelyFrequentUpdatesAreInvalid() {
        assertFalse(SimulationConfig(updateIntervalMs = 100L).isValid)
    }
}
