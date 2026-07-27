package com.geopilot.app.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.geopilot.app.GeoPilotApplication
import com.geopilot.app.MainActivity
import com.geopilot.app.R
import com.geopilot.app.model.SimulationConfig
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MockLocationService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var simulationJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopSimulation()
            ACTION_START, null -> startSimulation()
        }
        return START_NOT_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startSimulation() {
        val config = (application as GeoPilotApplication).simulationPreferences.load()
        if (!config.isValid) {
            stopSelf()
            return
        }
        if (!hasLocationPermission()) {
            SimulationState.fail("Precise location permission is required")
            stopSelf()
            return
        }

        startForeground(NOTIFICATION_ID, buildNotification(config))
        SimulationState.start(config)
        simulationJob?.cancel()
        simulationJob = scope.launch {
            try {
                fusedLocationClient.setMockMode(true).awaitTask()
                fusedLocationClient.setMockLocation(config.toLocation()).awaitTask()
                SimulationState.update(config)
                while (isActive) {
                    delay(config.updateIntervalMs)
                    fusedLocationClient.setMockLocation(config.toLocation()).awaitTask()
                    SimulationState.update(config)
                }
            } catch (error: Exception) {
                SimulationState.fail(error.toUserMessage())
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        scope.launch {
            if (hasLocationPermission()) {
                runCatching { fusedLocationClient.setMockMode(false).awaitTask() }
            }
            SimulationState.stop()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun SimulationConfig.toLocation() = Location("fused").apply {
        latitude = this@toLocation.latitude
        longitude = this@toLocation.longitude
        altitude = this@toLocation.altitude
        accuracy = this@toLocation.accuracy
        speed = speedKmh / 3.6f
        bearing = this@toLocation.bearing
        time = System.currentTimeMillis()
        elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            verticalAccuracyMeters = accuracy
            speedAccuracyMetersPerSecond = 0.5f
            bearingAccuracyDegrees = 1f
        }
    }

    private fun buildNotification(config: SimulationConfig) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_app)
        .setContentTitle(getString(R.string.notification_title))
        .setContentText("%.5f, %.5f".format(config.latitude, config.longitude))
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )
        .addAction(
            0,
            "Stop",
            PendingIntent.getService(
                this,
                1,
                Intent(this, MockLocationService::class.java).setAction(ACTION_STOP),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = getString(R.string.notification_channel_description) }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        simulationJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private fun Exception.toUserMessage(): String {
        val detail = localizedMessage.orEmpty()
        return if (this is SecurityException || detail.contains("mock", ignoreCase = true)) {
            "GeoPilot is not authorized as the mock location app. Open Developer options, choose Select mock location app, then select GeoPilot."
        } else {
            detail.ifBlank { "Android rejected the test location. Check Developer options and try again." }
        }
    }

    companion object {
        const val ACTION_START = "com.geopilot.app.action.START"
        const val ACTION_STOP = "com.geopilot.app.action.STOP"
        private const val CHANNEL_ID = "simulation"
        private const val NOTIFICATION_ID = 1001
    }
}
