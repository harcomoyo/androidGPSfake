package com.geopilot.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF19D3AE),
    onPrimary = Color(0xFF002019),
    primaryContainer = Color(0xFF004E40),
    onPrimaryContainer = Color(0xFF9DF2DC),
    secondary = Color(0xFF87A9FF),
    background = Color(0xFF07111F),
    onBackground = Color(0xFFE5ECF5),
    surface = Color(0xFF0C1929),
    onSurface = Color(0xFFE5ECF5),
    surfaceVariant = Color(0xFF14243A),
    onSurfaceVariant = Color(0xFFAEBDD1),
    error = Color(0xFFFFB4AB),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF006B58),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9DF2DC),
    secondary = Color(0xFF315DA8),
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF17202D),
    surface = Color.White,
    onSurface = Color(0xFF17202D),
    surfaceVariant = Color(0xFFE4EAF2),
    onSurfaceVariant = Color(0xFF4C596B),
)

@Composable
fun GeoPilotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = GeoPilotTypography,
        content = content,
    )
}
