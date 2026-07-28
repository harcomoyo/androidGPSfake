package com.geopilot.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopilot.app.location.SimulationStatus
import com.geopilot.app.model.SimulationConfig
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun GeoPilotApp(
    uiState: MainUiState,
    simulationStatus: SimulationStatus,
    permissionsGranted: Boolean,
    onConfigChange: (SimulationConfig) -> Unit,
    onMapPositionChange: (Double, Double) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onOpenDeveloperSettings: () -> Unit,
    onRequestPermissions: () -> Unit,
) {
    val isExpanded = LocalConfiguration.current.screenWidthDp >= 700
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (!isExpanded) {
                SimulationDock(
                    config = uiState.config,
                    status = simulationStatus,
                    onStart = onStart,
                    onStop = onStop,
                )
            }
        },
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LiveMapScreen(
                config = uiState.config,
                status = simulationStatus,
                permissionsGranted = permissionsGranted,
                expanded = isExpanded,
                onConfigChange = onConfigChange,
                onMapPositionChange = onMapPositionChange,
                onStart = onStart,
                onStop = onStop,
                onOpenDeveloperSettings = onOpenDeveloperSettings,
                onRequestPermissions = onRequestPermissions,
            )
        }
    }
}

@Composable
private fun LiveMapScreen(
    config: SimulationConfig,
    status: SimulationStatus,
    permissionsGranted: Boolean,
    expanded: Boolean,
    onConfigChange: (SimulationConfig) -> Unit,
    onMapPositionChange: (Double, Double) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onOpenDeveloperSettings: () -> Unit,
    onRequestPermissions: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var locationError by remember { mutableStateOf<String?>(null) }
    if (expanded) {
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1.45f).fillMaxHeight()) {
                AppHeader(status)
                RealMap(
                    config = config,
                    scope = scope,
                    onPositionChange = onMapPositionChange,
                    onLocationError = { locationError = it },
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                )
                MapAttribution()
                LocationSearch(
                    onLocationSelected = { latitude, longitude ->
                        onMapPositionChange(latitude, longitude)
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                )
                locationError?.let { MapMessage(it) }
            }
            ControlPanel(
                config = config,
                status = status,
                permissionsGranted = permissionsGranted,
                onConfigChange = onConfigChange,
                onStart = onStart,
                onStop = onStop,
                onOpenDeveloperSettings = onOpenDeveloperSettings,
                onRequestPermissions = onRequestPermissions,
                modifier = Modifier.width(390.dp).fillMaxHeight(),
            )
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            AppHeader(status)
            RealMap(
                config = config,
                scope = scope,
                onPositionChange = onMapPositionChange,
                onLocationError = { locationError = it },
                modifier = Modifier.weight(0.9f).fillMaxWidth().padding(horizontal = 16.dp),
            )
            MapAttribution()
            LocationSearch(
                onLocationSelected = { latitude, longitude ->
                    onMapPositionChange(latitude, longitude)
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            )
            locationError?.let { MapMessage(it) }
            ControlPanel(
                config = config,
                status = status,
                permissionsGranted = permissionsGranted,
                onConfigChange = onConfigChange,
                onStart = onStart,
                onStop = onStop,
                onOpenDeveloperSettings = onOpenDeveloperSettings,
                onRequestPermissions = onRequestPermissions,
                modifier = Modifier.fillMaxWidth().weight(1.1f),
            )
        }
    }
}

@Composable
private fun MapMessage(message: String) {
    Text(
        text = message,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 3.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
    )
}

@Composable
private fun AppHeader(status: SimulationStatus) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary) {
            Icon(
                Icons.Outlined.MyLocation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(9.dp).size(22.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("GEOPILOT", fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
            Text("Location test workspace", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.weight(1f))
        StatusPill(
            label = when {
                status.starting -> "STARTING"
                status.running -> "RUNNING"
                status.error != null -> "ACTION REQUIRED"
                else -> "READY"
            },
            active = status.running || status.starting,
            error = status.error != null,
        )
    }
}

@Composable
private fun StatusPill(label: String, active: Boolean, error: Boolean) {
    Surface(
        color = when {
            error -> MaterialTheme.colorScheme.errorContainer
            active -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        shape = CircleShape,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(7.dp).background(
                    when {
                        error -> MaterialTheme.colorScheme.error
                        active -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    CircleShape,
                ),
            )
            Spacer(Modifier.width(7.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ControlPanel(
    config: SimulationConfig,
    status: SimulationStatus,
    permissionsGranted: Boolean,
    onConfigChange: (SimulationConfig) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onOpenDeveloperSettings: () -> Unit,
    onRequestPermissions: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Live simulation", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Configure an exact position and publish it through Android's official test provider.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ReadinessCard(permissionsGranted, onOpenDeveloperSettings, onRequestPermissions)
        CoordinateEditor(config, onConfigChange)
        AccuracyControl(config, onConfigChange)
        if (status.error != null) {
            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        status.error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedButton(onClick = onOpenDeveloperSettings) {
                        Icon(Icons.Outlined.DeveloperMode, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Open Developer options")
                    }
                }
            }
        }
        Button(
            onClick = if (status.running || status.starting) onStop else onStart,
            enabled = config.isValid,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = if (status.running || status.starting) ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ) else ButtonDefaults.buttonColors(),
        ) {
            Icon(if (status.running || status.starting) Icons.Outlined.StopCircle else Icons.Outlined.PlayArrow, null)
            Spacer(Modifier.width(8.dp))
            Text(
                when {
                    status.starting -> "Cancel startup"
                    status.running -> "Stop simulation"
                    else -> "Start simulation"
                },
            )
        }
        Text(
            when {
                status.starting -> "Waiting for Android mock-location authorization..."
                status.running -> "Publishing every ${config.updateIntervalMs} ms"
                else -> "No test location is being published"
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ReadinessCard(
    permissionsGranted: Boolean,
    onOpenDeveloperSettings: () -> Unit,
    onRequestPermissions: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("DEVICE READINESS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Outlined.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            ReadinessRow(Icons.Outlined.DeveloperMode, "Mock location app", "Check Developer options", onOpenDeveloperSettings)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ReadinessRow(
                Icons.Outlined.Notifications,
                "Runtime permissions",
                if (permissionsGranted) "Granted" else "Action required",
                if (permissionsGranted) null else onRequestPermissions,
            )
        }
    }
}

@Composable
private fun ReadinessRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    action: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth().let { if (action != null) it.clickable(onClick = action) else it },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CoordinateEditor(config: SimulationConfig, onChange: (SimulationConfig) -> Unit) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("POSITION", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            DecimalField("Latitude", config.latitude) { onChange(config.copy(latitude = it)) }
            DecimalField("Longitude", config.longitude) { onChange(config.copy(longitude = it)) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DecimalField("Altitude (m)", config.altitude, Modifier.weight(1f)) { onChange(config.copy(altitude = it)) }
                DecimalField("Bearing", config.bearing.toDouble(), Modifier.weight(1f)) { onChange(config.copy(bearing = it.toFloat())) }
            }
        }
    }
}

@Composable
private fun DecimalField(
    label: String,
    value: Double,
    modifier: Modifier = Modifier,
    onValueChange: (Double) -> Unit,
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            it.toDoubleOrNull()?.let(onValueChange)
        },
        label = { Text(label) },
        singleLine = true,
        isError = text.toDoubleOrNull() == null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
private fun AccuracyControl(config: SimulationConfig, onChange: (SimulationConfig) -> Unit) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Row {
                Text("ACCURACY PROFILE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("${config.accuracy.roundToInt()} m", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = config.accuracy,
                onValueChange = { onChange(config.copy(accuracy = it)) },
                valueRange = 1f..100f,
            )
            Row {
                Text("Precise", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text("Approximate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MapAttribution() {
    Text(
        text = "Map data © OpenStreetMap contributors",
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 5.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SimulationDock(
    config: SimulationConfig,
    status: SimulationStatus,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(9.dp).background(
                    if (status.running) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    CircleShape,
                ),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    when {
                        status.starting -> "Starting simulation"
                        status.running -> "Publishing test location"
                        else -> "Selected location"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    formatCoordinate(config.latitude, config.longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = if (status.running || status.starting) onStop else onStart,
                enabled = config.isValid,
                colors = if (status.running || status.starting) ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ) else ButtonDefaults.buttonColors(),
            ) {
                Icon(if (status.running || status.starting) Icons.Outlined.StopCircle else Icons.Outlined.PlayArrow, null)
                Spacer(Modifier.width(6.dp))
                Text(if (status.running || status.starting) "Stop" else "Start")
            }
        }
    }
}

private fun formatCoordinate(latitude: Double, longitude: Double): String =
    String.format(Locale.US, "%.6f, %.6f", latitude, longitude)
