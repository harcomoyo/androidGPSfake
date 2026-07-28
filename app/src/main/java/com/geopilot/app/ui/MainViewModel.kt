package com.geopilot.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geopilot.app.data.SimulationPreferences
import com.geopilot.app.model.SimulationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainUiState(
    val config: SimulationConfig = SimulationConfig(),
)

class MainViewModel(private val preferences: SimulationPreferences) : ViewModel() {
    private val mutableUiState = MutableStateFlow(MainUiState(config = preferences.load()))
    val uiState = mutableUiState.asStateFlow()

    fun updateConfig(config: SimulationConfig) {
        mutableUiState.update { it.copy(config = config) }
    }

    fun updateMapPosition(latitude: Double, longitude: Double) {
        mutableUiState.update {
            it.copy(config = it.config.copy(latitude = latitude, longitude = longitude))
        }
    }

    fun persist() = preferences.save(mutableUiState.value.config)
}

class MainViewModelFactory(
    private val preferences: SimulationPreferences,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(preferences) as T
}
