package com.geopilot.app.ui

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.geopilot.app.model.SimulationConfig
import com.geopilot.app.search.LocationSearchResult
import com.geopilot.app.search.LocationSearchService
import kotlinx.coroutines.launch
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun RealMap(
    config: SimulationConfig,
    onPositionChange: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context).apply { onCreate(Bundle()) } }
    val controller = remember { RealMapController(mapView, onPositionChange) }
    val searchService = remember { LocationSearchService() }
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<LocationSearchResult>>(emptyList()) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var searching by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    LaunchedEffect(config.latitude, config.longitude) {
        controller.updatePosition(config.latitude, config.longitude)
    }

    Box(modifier = modifier.clip(RoundedCornerShape(26.dp))) {
        AndroidView(
            factory = {
                controller.initialize(config.latitude, config.longitude)
                mapView
            },
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Cari lokasi di Indonesia") },
                placeholder = { Text("Monas, Jakarta") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = {
                    if (searching) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                supportingText = searchError?.let { message -> { Text(message) } },
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = {
                        if (searchQuery.isNotBlank() && !searching) {
                            searching = true
                            searchError = null
                            scope.launch {
                                searchService.search(searchQuery)
                                    .onSuccess {
                                        searchResults = it
                                        if (it.isEmpty()) searchError = "Lokasi tidak ditemukan di Indonesia"
                                    }
                                    .onFailure { searchError = "Pencarian gagal. Periksa koneksi internet." }
                                searching = false
                            }
                        }
                    },
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search,
                ),
            )
            if (searchResults.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                    tonalElevation = 8.dp,
                ) {
                    Column {
                        searchResults.forEach { result ->
                            Text(
                                text = result.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchQuery = result.name.substringBefore(',')
                                        searchResults = emptyList()
                                        onPositionChange(result.latitude, result.longitude)
                                        controller.moveTo(result.latitude, result.longitude)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MapButton(Icons.Outlined.Add, "Zoom in") { controller.zoomBy(1.0) }
            MapButton(Icons.Outlined.Remove, "Zoom out") { controller.zoomBy(-1.0) }
            MapButton(Icons.Outlined.CenterFocusStrong, "Center selected location") {
                controller.centerOn(config.latitude, config.longitude)
            }
        }
    }
}

@Composable
private fun MapButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Icon(icon, description)
    }
}

private class RealMapController(
    private val mapView: MapView,
    private val onPositionChange: (Double, Double) -> Unit,
) {
    private var map: MapLibreMap? = null
    private var marker: Marker? = null
    private var pendingPosition: LatLng? = null

    fun initialize(latitude: Double, longitude: Double) {
        pendingPosition = LatLng(latitude, longitude)
        mapView.getMapAsync { mapLibreMap ->
            map = mapLibreMap
            mapLibreMap.uiSettings.apply {
                isCompassEnabled = true
                isAttributionEnabled = true
                isLogoEnabled = true
                isZoomGesturesEnabled = true
                isScrollGesturesEnabled = true
            }
            mapLibreMap.setStyle(Style.Builder().fromUri(STYLE_URI)) {
                val selected = pendingPosition ?: LatLng(latitude, longitude)
                marker = mapLibreMap.addMarker(
                    MarkerOptions()
                        .position(selected)
                        .title("Test location"),
                )
                mapLibreMap.cameraPosition = CameraPosition.Builder()
                    .target(selected)
                    .zoom(DEFAULT_ZOOM)
                    .build()
                mapLibreMap.addOnMapClickListener { point ->
                    pendingPosition = point
                    marker?.position = point
                    onPositionChange(point.latitude, point.longitude)
                    true
                }
            }
        }
    }

    fun updatePosition(latitude: Double, longitude: Double) {
        val position = LatLng(latitude, longitude)
        pendingPosition = position
        marker?.position = position
    }

    fun centerOn(latitude: Double, longitude: Double) {
        map?.animateCamera(CameraUpdateFactory.newLatLng(LatLng(latitude, longitude)), 450)
    }

    fun moveTo(latitude: Double, longitude: Double) {
        val position = LatLng(latitude, longitude)
        pendingPosition = position
        marker?.position = position
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM), 650)
    }

    fun zoomBy(amount: Double) {
        map?.animateCamera(CameraUpdateFactory.zoomBy(amount), 250)
    }

    private companion object {
        const val STYLE_URI = "asset://osm_style.json"
        const val DEFAULT_ZOOM = 13.0
    }
}
