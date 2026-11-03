package com.theroute.app.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.theroute.app.audio.VibeRecorder
import com.theroute.app.data.*
import com.theroute.app.engine.Geo
import com.theroute.app.engine.Vibe
import com.theroute.app.service.RouteTrackingService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen { HOME, PARADES, VENDORS, PROFILE, MAP, LEAD, ADMIN, REGISTER, VENDOR_FORM, LIMITS }

class RouteViewModel(app: Application) : AndroidViewModel(app) {
    val repo = Repository.get(app)
    val gps = repo.gps

    private val _screen = MutableStateFlow(Screen.HOME)
    val screen: StateFlow<Screen> = _screen.asStateFlow()
    fun go(s: Screen) { _screen.value = s }

    private val _routes = MutableStateFlow<List<ParadeRoute>>(emptyList())
    val routes: StateFlow<List<ParadeRoute>> = _routes.asStateFlow()
    private val _vendors = MutableStateFlow<List<Vendor>>(emptyList())
    val vendors: StateFlow<List<Vendor>> = _vendors.asStateFlow()
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()
    private val _leaders = MutableStateFlow<List<Leader>>(emptyList())
    val leaders: StateFlow<List<Leader>> = _leaders.asStateFlow()
    private val _points = MutableStateFlow<List<RoutePoint>>(emptyList())
    val points: StateFlow<List<RoutePoint>> = _points.asStateFlow()
    private val _selectedRoute = MutableStateFlow<ParadeRoute?>(null)
    val selectedRoute: StateFlow<ParadeRoute?> = _selectedRoute.asStateFlow()

    private val _role = MutableStateFlow(repo.role)
    val role: StateFlow<String> = _role.asStateFlow()
    fun setRole(r: String) { repo.role = r; _role.value = r }

    private val _allowRotation = MutableStateFlow(repo.allowRotation)
    val allowRotation: StateFlow<Boolean> = _allowRotation.asStateFlow()
    fun setAllowRotation(v: Boolean) { repo.allowRotation = v; _allowRotation.value = v }

    private val _vibeBpm = MutableStateFlow<Double?>(null)
    val vibeBpm: StateFlow<Double?> = _vibeBpm.asStateFlow()
    private val _vibeBusy = MutableStateFlow(false)
    val vibeBusy: StateFlow<Boolean> = _vibeBusy.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    fun clearMessage() { _message.value = null }
    fun say(m: String) { _message.value = m }

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _routes.value = repo.routes()
            _vendors.value = repo.vendors()
            _reports.value = repo.reports()
            _leaders.value = repo.leaders()
            val sel = _selectedRoute.value
            if (sel != null) _points.value = repo.points(sel.id)
            val active = repo.gps.value.activeRouteId
            if (active != null && sel?.id != active) {
                val live = repo.liveRoutes().firstOrNull { it.id == active }
                if (live != null) { _selectedRoute.value = live; _points.value = repo.points(live.id) }
            }
        }
    }

    fun selectRoute(r: ParadeRoute) {
        _selectedRoute.value = r
        viewModelScope.launch { _points.value = repo.points(r.id) }
        go(Screen.MAP)
    }

    fun startRoute(name: String, club: String) {
        viewModelScope.launch {
            val g = repo.gps.value
            val id = repo.startRoute(name.ifBlank { "Second Line Parade" }, club, repo.currentLeaderId, g.lat, g.lon)
            getApplication<Application>().startForegroundService(Intent(getApplication(), RouteTrackingService::class.java))
            refresh()
            val r = repo.routes().firstOrNull { it.id == id }
            if (r != null) _selectedRoute.value = r
            go(Screen.MAP)
        }
    }
    fun endRoute() {
        val id = repo.gps.value.activeRouteId ?: return
        viewModelScope.launch {
            repo.endRoute(id)
            getApplication<Application>().stopService(Intent(getApplication(), RouteTrackingService::class.java))
            refresh(); go(Screen.HOME)
        }
    }
    fun resumeTrackingIfLive() {
        if (repo.gps.value.activeRouteId != null) {
            getApplication<Application>().startForegroundService(Intent(getApplication(), RouteTrackingService::class.java))
        }
    }

    /** Last known location, so community users can drop a plate or report
     *  without leading a live route (which is what starts GPS tracking). */
    private fun withLocation(onDone: (Double, Double) -> Unit) {
        val g = repo.gps.value
        if (g.lat != null && g.lon != null) { onDone(g.lat!!, g.lon!!); return }
        try {
            LocationServices.getFusedLocationProviderClient(getApplication()).lastLocation
                .addOnSuccessListener { loc ->
                    if (loc != null) { repo.onLocation(loc.latitude, loc.longitude); onDone(loc.latitude, loc.longitude) }
                    else say("No GPS fix yet — step outside, wait a moment, and try again.")
                }
                .addOnFailureListener { say("Could not get a GPS fix. Check location permission.") }
        } catch (_: SecurityException) { say("Location permission is needed to place this on the map.") }
        catch (_: Exception) { say("Could not get a GPS fix. Check location permission and that GPS is on.") }
    }

    fun addVendor(name: String, type: String, price: String) {
        if (name.isBlank()) { say("Enter a vendor name first."); return }
        try {
            withLocation { lat, lon ->
                viewModelScope.launch {
                    try {
                        repo.addVendor(Vendor(name = name, itemType = type, priceRange = price, lat = lat, lon = lon))
                        refresh(); go(Screen.VENDORS)
                    } catch (e: Exception) {
                        // Never let a failed drop kill the app: stay on the
                        // form and show what went wrong instead of closing.
                        say("Couldn't drop that plate: ${e.message ?: "unknown error"}. Check location permission and try again.")
                    }
                }
            }
        } catch (e: Exception) {
            say("Couldn't drop that plate: ${e.message ?: "unknown error"}. Check location permission and try again.")
        }
    }
    fun promoteVendor(id: Long) {
        viewModelScope.launch { repo.promoteVendor(id); refresh(); say("Vendor highlighted for 6 hours (test mode — no charge made).") }
    }
    fun addReport(type: String, desc: String) {
        val live = _routes.value.firstOrNull { it.status == "rolling" }
        withLocation { lat, lon ->
            viewModelScope.launch {
                val route = live
                val pts = if (route != null) repo.points(route.id) else emptyList()
                val pinLat = pts.lastOrNull()?.lat ?: route?.homeLat
                val pinLon = pts.lastOrNull()?.lon ?: route?.homeLon
                if (pinLat == null || pinLon == null) { say("No live parade pin yet, so a 200m check can't be done. Start a live route first."); return@launch }
                val dist = Geo.metres(lat, lon, pinLat, pinLon)
                if (dist > 200) { say("You're about ${dist.toInt()}m from the live parade pin. Reports are only allowed within 200m, to prevent fake reports."); return@launch }
                repo.addReport(Report(type = type, description = desc, lat = lat, lon = lon))
                refresh(); say("Report added. Thanks for helping the line.")
            }
        }
    }
    fun registerLeader(name: String, title: String, club: String, password: String) {
        if (password.length < 4) { say("Choose a leader password of at least 4 characters."); return }
        viewModelScope.launch {
            val id = repo.registerLeader(name, title, club, password)
            repo.currentLeaderId = id
            refresh(); say("Leader registered as Pending. You need 2 vouches from verified leaders to go Active.")
            go(Screen.PROFILE)
        }
    }
    fun vouch(leaderId: Long) {
        val by = repo.currentLeaderId
        if (by == null) { say("Register as a leader first, then you can vouch."); return }
        viewModelScope.launch { repo.vouch(leaderId, by); refresh() }
    }
    fun setLeaderStatus(id: Long, status: String) {
        viewModelScope.launch { repo.setLeaderStatus(id, status); refresh() }
    }
    fun membershipTestMode(id: Long) {
        viewModelScope.launch { repo.setMembershipTestMode(id); refresh(); say("Membership marked for 1 year (test mode — no Square charge made in this build).") }
    }
    fun checkVibe() {
        if (_vibeBusy.value) return
        _vibeBusy.value = true
        viewModelScope.launch {
            val bpm = VibeRecorder.sampleBpm()
            _vibeBpm.value = bpm
            _vibeBusy.value = false
            if (bpm == null) say("No clear beat found in that 5-second sample. Try again closer to the band.")
        }
    }
    fun vibeIcon(): String = Vibe.iconFor(_vibeBpm.value)
    fun vibeLabel(): String = Vibe.labelFor(_vibeBpm.value)
}
