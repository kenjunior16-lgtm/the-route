package com.theroute.app.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.theroute.app.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Foreground GPS tracking for a live route. Visible and stoppable. */
class RouteTrackingService : Service() {
    private lateinit var fused: FusedLocationProviderClient
    private lateinit var callback: LocationCallback
    private lateinit var repo: Repository
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        repo = Repository.get(this)
        fused = LocationServices.getFusedLocationProviderClient(this)
        Notify.ensureChannel(this)
        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    repo.onLocation(loc.latitude, loc.longitude)
                    val routeId = repo.gps.value.activeRouteId
                    if (routeId != null) scope.launch { repo.addPoint(routeId, loc.latitude, loc.longitude) }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification: Notification = NotificationCompat.Builder(this, Notify.CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("The Route is live")
            .setContentText("Your parade route is being tracked. Tap to open the app.")
            .setOngoing(true).build()
        startForeground(1, notification)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L)
            .setMinUpdateIntervalMillis(5_000L).build()
        try { fused.requestLocationUpdates(request, callback, Looper.getMainLooper()) } catch (_: SecurityException) {}
        return START_STICKY
    }

    override fun onDestroy() {
        try { fused.removeLocationUpdates(callback) } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
