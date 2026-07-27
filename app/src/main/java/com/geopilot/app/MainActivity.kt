package com.geopilot.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geopilot.app.location.SimulationState
import com.geopilot.app.ui.GeoPilotApp
import com.geopilot.app.ui.MainViewModel
import com.geopilot.app.ui.MainViewModelFactory
import com.geopilot.app.ui.theme.GeoPilotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeoPilotTheme {
                val application = application as GeoPilotApplication
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(application.simulationPreferences),
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val simulationStatus by SimulationState.status.collectAsStateWithLifecycle()
                var permissionsGranted by remember { mutableStateOf(hasRequiredPermissions()) }
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions(),
                ) { permissionsGranted = hasRequiredPermissions() }

                LaunchedEffect(Unit) {
                    if (!permissionsGranted) launcher.launch(requiredPermissions())
                }

                GeoPilotApp(
                    uiState = uiState,
                    simulationStatus = simulationStatus,
                    permissionsGranted = permissionsGranted,
                    onConfigChange = viewModel::updateConfig,
                    onMapPositionChange = viewModel::updateMapPosition,
                    onStart = {
                        if (!permissionsGranted) {
                            launcher.launch(requiredPermissions())
                        } else {
                            viewModel.persist()
                            ContextCompat.startForegroundService(
                                this,
                                Intent(this, com.geopilot.app.location.MockLocationService::class.java)
                                    .setAction(com.geopilot.app.location.MockLocationService.ACTION_START),
                            )
                        }
                    },
                    onStop = {
                        startService(
                            Intent(this, com.geopilot.app.location.MockLocationService::class.java)
                                .setAction(com.geopilot.app.location.MockLocationService.ACTION_STOP),
                        )
                    },
                    onOpenDeveloperSettings = {
                        startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                    },
                    onRequestPermissions = { launcher.launch(requiredPermissions()) },
                )
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean = requiredPermissions().all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requiredPermissions(): Array<String> = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()
}
