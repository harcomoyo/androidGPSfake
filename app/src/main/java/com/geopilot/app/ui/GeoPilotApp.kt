package com.geopilot.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
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
    var section by remember { mutableStateOf(uiState.selectedSection) }
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (!isExpanded) {
                AppBottomNavigation(section = section, onSectionChange = { section = it })
            }
        },
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (isExpanded) {
                AppNavigationRail(section = section, onSectionChange = { section = it })
            }
            when (section) {
                AppSection.Map -> LiveMapScreen(
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
                else -> PlaceholderScreen(section)
            }
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
    if (expanded) {
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1.45f).fillMaxHeight()) {
                AppHeader(status)
                CoordinateCanvas(config, onMapPositionChange, Modifier.weight(1f))
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
            CoordinateCanvas(config, onMapPositionChange, Modifier.weight(0.9f))
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
        IconButton(onClick = {}) { Icon(Icons.Outlined.MoreHoriz, "More") }
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
private fun CoordinateCanvas(
    config: SimulationConfig,
    onPositionChange: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grid = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
    val route = MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(26.dp))
            .pointerInput(Unit) {
                detectTapGestures { tap ->
                    val latitude = 90.0 - (tap.y / size.height) * 180.0
                    val longitude = (tap.x / size.width) * 360.0 - 180.0
                    onPositionChange(latitude.coerceIn(-90.0, 90.0), longitude.coerceIn(-180.0, 180.0))
                }
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val step = size.minDimension / 8f
            var x = 0f
            while (x <= size.width) {
                drawLine(grid, Offset(x, 0f), Offset(x, size.height), 1f)
                x += step
            }
            var y = 0f
            while (y <= size.height) {
                drawLine(grid, Offset(0f, y), Offset(size.width, y), 1f)
                y += step
            }
            val path = Path().apply {
                moveTo(size.width * 0.08f, size.height * 0.72f)
                cubicTo(
                    size.width * 0.31f,
                    size.height * 0.29f,
                    size.width * 0.57f,
                    size.height * 0.85f,
                    size.width * 0.91f,
                    size.height * 0.34f,
                )
            }
            drawPath(path, route, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
            val pinX = (((config.longitude + 180.0) / 360.0) * size.width).toFloat()
            val pinY = (((90.0 - config.latitude) / 180.0) * size.height).toFloat()
            drawCircle(primary.copy(alpha = 0.18f), 26.dp.toPx(), Offset(pinX, pinY))
            drawCircle(primary, 9.dp.toPx(), Offset(pinX, pinY))
            drawCircle(Color.White, 3.dp.toPx(), Offset(pinX, pinY))
        }
        Surface(
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
        ) {
            Text(
                "TAP MAP TO POSITION",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        Surface(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("Selected position", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    formatCoordinate(config.latitude, config.longitude),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                )
            }
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
private fun PlaceholderScreen(section: AppSection) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(section.icon(), null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text(section.label, style = MaterialTheme.typography.headlineSmall)
            Text("Planned for the next delivery", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AppBottomNavigation(section: AppSection, onSectionChange: (AppSection) -> Unit) {
    val sections = listOf(AppSection.Dashboard, AppSection.Map, AppSection.Scenarios, AppSection.Runs, AppSection.Settings)
    NavigationBar(modifier = Modifier.navigationBarsPadding()) {
        sections.forEach { item ->
            NavigationBarItem(
                selected = item == section,
                onClick = { onSectionChange(item) },
                icon = { Icon(item.icon(), item.label) },
                label = { Text(item.label.substringBefore(' ')) },
            )
        }
    }
}

@Composable
private fun AppNavigationRail(section: AppSection, onSectionChange: (AppSection) -> Unit) {
    val sections = listOf(AppSection.Dashboard, AppSection.Map, AppSection.Scenarios, AppSection.Runs, AppSection.Settings)
    NavigationRail {
        Spacer(Modifier.height(24.dp))
        sections.forEach { item ->
            NavigationRailItem(
                selected = item == section,
                onClick = { onSectionChange(item) },
                icon = { Icon(item.icon(), item.label) },
                label = { Text(item.label.substringBefore(' ')) },
            )
        }
    }
}

private fun AppSection.icon() = when (this) {
    AppSection.Dashboard -> Icons.Outlined.Dashboard
    AppSection.Map -> Icons.Outlined.Map
    AppSection.Scenarios -> Icons.Outlined.Route
    AppSection.Runs -> Icons.Outlined.History
    AppSection.Settings -> Icons.Outlined.Settings
}

private fun formatCoordinate(latitude: Double, longitude: Double): String =
    String.format(Locale.US, "%.6f, %.6f", latitude, longitude)
