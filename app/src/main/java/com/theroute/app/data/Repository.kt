package com.theroute.app.data

import android.content.Context
import androidx.room.Room
import com.theroute.app.engine.Geo
import java.security.MessageDigest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GpsState(val lat: Double? = null, val lon: Double? = null, val activeRouteId: Long? = null)

/** Local-only repository for this first Android build.
 *  Live sharing between different phones needs a backend, which is not
 *  in this build — routes, vendors, reports and leaders here live on
 *  this phone only. That limit is stated in the app's Limits screen. */
class Repository private constructor(context: Context) {
    private val db = Room.databaseBuilder(context.applicationContext, RouteDatabase::class.java, "route.db").build()
    private val prefs = context.getSharedPreferences("route_prefs", Context.MODE_PRIVATE)

    private val _gps = MutableStateFlow(GpsState(activeRouteId = prefs.getLong("activeRouteId", -1).takeIf { it > 0 }))
    val gps: StateFlow<GpsState> = _gps.asStateFlow()

    var role: String
        get() = prefs.getString("role", "Community") ?: "Community"
        set(v) { prefs.edit().putString("role", v).apply() }
    var allowRotation: Boolean
        get() = prefs.getBoolean("allowRotation", false)
        set(v) { prefs.edit().putBoolean("allowRotation", v).apply() }
    var currentLeaderId: Long?
        get() = prefs.getLong("leaderId", -1).takeIf { it > 0 }
        set(v) { prefs.edit().putLong("leaderId", v ?: -1).apply() }

    fun onLocation(lat: Double, lon: Double) {
        _gps.value = _gps.value.copy(lat = lat, lon = lon)
    }
    fun setActiveRoute(id: Long?) {
        _gps.value = _gps.value.copy(activeRouteId = id)
        prefs.edit().putLong("activeRouteId", id ?: -1).apply()
    }

    suspend fun startRoute(name: String, club: String, leaderId: Long?, homeLat: Double?, homeLon: Double?): Long {
        val id = db.dao().insertRoute(ParadeRoute(name = name, club = club, leaderId = leaderId, homeLat = homeLat, homeLon = homeLon))
        setActiveRoute(id)
        return id
    }
    suspend fun endRoute(id: Long) {
        db.dao().endRoute(id, System.currentTimeMillis())
        if (_gps.value.activeRouteId == id) setActiveRoute(null)
    }
    suspend fun addPoint(routeId: Long, lat: Double, lon: Double) {
        // Ignore GPS jumps over 2 miles between fixes.
        val pts = db.dao().points(routeId)
        val last = pts.lastOrNull()
        if (last != null && Geo.haversineMiles(last.lat, last.lon, lat, lon) > 2.0) return
        db.dao().insertPoint(RoutePoint(routeId = routeId, lat = lat, lon = lon))
    }
    suspend fun routes(): List<ParadeRoute> = db.dao().allRoutes()
    suspend fun liveRoutes(): List<ParadeRoute> = db.dao().liveRoutes()
    suspend fun points(routeId: Long): List<RoutePoint> = db.dao().points(routeId)
    suspend fun addVendor(v: Vendor): Long = db.dao().insertVendor(v)
    suspend fun vendors(): List<Vendor> {
        db.dao().deleteExpiredVendors(System.currentTimeMillis())
        return db.dao().allVendors().filter { !it.isExpired() }
    }
    suspend fun promoteVendor(id: Long) {
        val now = System.currentTimeMillis()
        // $5 promotion: highlighted for 6 hours. Test mode only in this
        // build — no real Square charge is made here.
        db.dao().promoteVendor(id, now + 6 * 60 * 60 * 1000L, now + 6 * 60 * 60 * 1000L)
    }
    suspend fun addReport(r: Report) = db.dao().insertReport(r)
    suspend fun reports(): List<Report> = db.dao().allReports()
    suspend fun registerLeader(name: String, title: String, club: String, password: String): Long =
        db.dao().insertLeader(Leader(name = name, title = title, club = club, passwordHash = sha256(password)))
    suspend fun leaders(): List<Leader> = db.dao().allLeaders()
    suspend fun leader(id: Long): Leader? = db.dao().leader(id)
    suspend fun vouch(leaderId: Long, byLeaderId: Long): Leader? {
        val l = db.dao().leader(leaderId) ?: return null
        val by = l.vouchedBy.split(",").filter { it.isNotBlank() }
        if (by.contains(byLeaderId.toString())) return l
        val newBy = (by + byLeaderId.toString()).joinToString(",")
        val newVouches = l.vouches + 1
        val newStatus = if (newVouches >= 2) "active" else l.status
        db.dao().updateLeaderStatus(leaderId, newStatus, newVouches, newBy)
        return db.dao().leader(leaderId)
    }
    suspend fun setLeaderStatus(id: Long, status: String) {
        val l = db.dao().leader(id) ?: return
        db.dao().updateLeaderStatus(id, status, l.vouches, l.vouchedBy)
    }
    suspend fun setMembershipTestMode(id: Long) {
        // $10 annual fee placeholder: test mode only, no charge.
        db.dao().setMembership(id, System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000L)
    }

    companion object {
        @Volatile private var instance: Repository? = null
        fun get(context: Context): Repository =
            instance ?: synchronized(this) { instance ?: Repository(context).also { instance = it } }
        fun sha256(s: String): String =
            MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
