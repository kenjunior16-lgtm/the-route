package com.theroute.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.theroute.app.data.Vendor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MoneyNote() {}

fun fmtTime(t: Long): String = SimpleDateFormat("h:mm a", Locale.US).format(Date(t))

@Composable
fun TopBar(vm: RouteViewModel, title: String) {
    Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}

@Composable
fun BottomNav(vm: RouteViewModel) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        TextButton(onClick = { vm.go(Screen.HOME) }) { Text("🏠 Home") }
        TextButton(onClick = { vm.go(Screen.PARADES) }) { Text("🎵 Parades") }
        TextButton(onClick = { vm.go(Screen.VENDORS) }) { Text("🍽 Vendors") }
        TextButton(onClick = { vm.go(Screen.PROFILE) }) { Text("👤 Profile") }
    }
}

@Composable
fun MessageBox(vm: RouteViewModel) {
    val m by vm.message.collectAsState()
    if (m != null) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text(m!!)
                TextButton(onClick = { vm.clearMessage() }) { Text("Dismiss") }
            }
        }
    }
}

@Composable
fun HomeScreen(vm: RouteViewModel) {
    val routes by vm.routes.collectAsState()
    val gps by vm.gps.collectAsState()
    val live = routes.filter { it.status == "rolling" }
    val ctx = LocalContext.current
    val locPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted.values.all { it }) vm.resumeTrackingIfLive()
    }
    LaunchedEffect(Unit) { vm.refresh() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("The Route", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("NOLA Second Line Tracker", style = MaterialTheme.typography.bodyMedium)
        Text("Community-first: open the app and see the parade. No email needed to watch.", style = MaterialTheme.typography.bodySmall)
        MessageBox(vm)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.go(Screen.LEAD) }) { Text("📢 Lead a Parade") }
            Button(onClick = { vm.go(Screen.VENDOR_FORM) }) { Text("🍽 Drop a Plate") }
            OutlinedButton(onClick = { vm.go(Screen.ADMIN) }) { Text("⚙ Admin") }
        }
        OutlinedButton(onClick = {
            val ok = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!ok) locPerm.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.RECORD_AUDIO))
            else vm.resumeTrackingIfLive()
        }) { Text("Enable location / mic permissions") }
        Text("Live Now", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        if (live.isEmpty()) {
            Text("No parade is live on this phone right now. Start one as a Leader, or check Parades for past routes.")
            Text("Test data note: this build starts empty — no sample parades are invented as if they were live. A 'Test SAPC Parade' from Emergent is not carried over.")
        } else {
            live.forEach { r ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(r.name, fontWeight = FontWeight.Bold)
                        Text("${r.club} • Rolling • started ${fmtTime(r.startedAt)}")
                        Button(onClick = { vm.selectRoute(r) }) { Text("View on map →") }
                    }
                }
            }
        }
        Text("GPS: ${gps.lat?.let { "%.4f".format(it) } ?: "—"}, ${gps.lon?.let { "%.4f".format(it) } ?: "—"}", style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = { vm.go(Screen.LIMITS) }) { Text("What this build can / cannot do yet") }
        BottomNav(vm)
    }
}

@Composable
fun ParadesScreen(vm: RouteViewModel) {
    val routes by vm.routes.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TopBar(vm, "Parades")
        MessageBox(vm)
        if (routes.isEmpty()) Text("No parades yet on this phone.")
        routes.forEach { r ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(r.name, fontWeight = FontWeight.Bold)
                    Text("${r.club} • ${r.status} • ${fmtTime(r.startedAt)}")
                    Button(onClick = { vm.selectRoute(r) }) { Text("View on map") }
                }
            }
        }
        BottomNav(vm)
    }
}

@Composable
fun VendorsScreen(vm: RouteViewModel) {
    val vendors by vm.vendors.collectAsState()
    var filter by remember { mutableStateOf("All") }
    LaunchedEffect(Unit) { vm.refresh() }
    val shown = vendors.filter { filter == "All" || it.itemType == filter }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TopBar(vm, "Vendors")
        Text("Food & Drinks Near the Line", style = MaterialTheme.typography.bodyMedium)
        MessageBox(vm)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Plate", "Drink", "Water").forEach { f ->
                if (f == filter) Button(onClick = {}) { Text(f) } else OutlinedButton(onClick = { filter = f }) { Text(f) }
            }
        }
        if (shown.isEmpty()) {
            Text("No vendors found", fontWeight = FontWeight.Bold)
            Button(onClick = { vm.go(Screen.VENDOR_FORM) }) { Text("Drop a Plate") }
        }
        shown.forEach { v ->
            Card(colors = if (v.isPromotedNow()) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors(), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(v.name, fontWeight = FontWeight.Bold)
                    Text("${v.itemType} • ${v.priceRange} • until ${fmtTime(v.expiresAt)}")
                    if (v.isPromotedNow()) Text("★ FEATURED (promoted)", fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = { vm.promoteVendor(v.id) }) { Text("Promote — \$5 test mode (6 hrs)") }
                }
            }
        }
        Button(onClick = { vm.go(Screen.VENDOR_FORM) }) { Text("+ Add vendor") }
        BottomNav(vm)
    }
}

@Composable
fun VendorFormScreen(vm: RouteViewModel) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Plate") }
    var price by remember { mutableStateOf("\$\$") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Drop a Plate", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Pin drops at your current GPS location and disappears after 4 hours, so ghost pins don't linger after the parade.", style = MaterialTheme.typography.bodySmall)
        MessageBox(vm)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Vendor name (e.g. Ms. Linda's Yakamein)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Plate", "Drink", "Water").forEach { t ->
                if (t == type) Button(onClick = {}) { Text(t) } else OutlinedButton(onClick = { type = t }) { Text(t) }
            }
        }
        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price range (e.g. \$\$, \$5–\$10)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(onClick = { vm.addVendor(name, type, price) }, modifier = Modifier.fillMaxWidth()) { Text("Drop this plate") }
        TextButton(onClick = { vm.go(Screen.VENDORS) }) { Text("Back to vendors") }
    }
}

@Composable
fun MapScreen(vm: RouteViewModel) {
    val gps by vm.gps.collectAsState()
    val route by vm.selectedRoute.collectAsState()
    val points by vm.points.collectAsState()
    val vendors by vm.vendors.collectAsState()
    val reports by vm.reports.collectAsState()
    val bpm by vm.vibeBpm.collectAsState()
    val busy by vm.vibeBusy.collectAsState()
    var reportType by remember { mutableStateOf("Crowd is THICK") }
    var reportDesc by remember { mutableStateOf("") }
    var showHelp by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { vm.refresh() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(route?.name ?: "Map", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Live pin + breadcrumbs + Home Base + vendors + reports. Map tiles: OpenStreetMap, no key.", style = MaterialTheme.typography.bodySmall)
        MessageBox(vm)
        RouteMap(gps.lat, gps.lon, points, route?.homeLat, route?.homeLon, vendors, reports, Modifier.fillMaxWidth().height(320.dp))
        Text("Vibe Check: ${vm.vibeIcon()} ${vm.vibeLabel()}${bpm?.let { " (${"%.0f".format(it)} BPM)" } ?: ""}", fontWeight = FontWeight.Bold)
        Text("Mic sample is 5 seconds, analysed on this phone only, and discarded. No audio is recorded to a file or sent anywhere.", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { vm.checkVibe() }, enabled = !busy) { Text(if (busy) "Listening… (5s)" else "🎵 Vibe Check (mic)") }
        Text("Street report — only within 200m of the live parade pin", fontWeight = FontWeight.Bold)
        OutlinedTextField(value = reportType, onValueChange = { reportType = it }, label = { Text("Report type") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = reportDesc, onValueChange = { reportDesc = it }, label = { Text("Details (optional)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { vm.addReport(reportType, reportDesc) }) { Text("Send report") }
        if (gps.activeRouteId != null) {
            OutlinedButton(onClick = { vm.endRoute() }) { Text("End live route") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { showHelp = true }) { Text("Help & Info") }
            TextButton(onClick = { vm.go(Screen.HOME) }) { Text("Back home") }
        }
        if (showHelp) {
            AlertDialog(onDismissRequest = { showHelp = false }, title = { Text("How this map works") }, text = {
                Text("A Leader starts a live route and their GPS draws breadcrumbs. Vendors drop plates that vanish after 4 hours. Community reports only count if you're within 200m of the live pin, to cut fake reports. Vibe Check listens for 5 seconds on-device and shows 🎺 under 90 BPM, 🔥 over 120 BPM.")
            }, confirmButton = { TextButton(onClick = { showHelp = false }) { Text("Got it") } })
        }
    }
}

@Composable
fun LeadScreen(vm: RouteViewModel) {
    var name by remember { mutableStateOf("") }
    var club by remember { mutableStateOf("") }
    val gps by vm.gps.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Lead a Parade", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        MessageBox(vm)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Parade / route name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = club, onValueChange = { club = it }, label = { Text("Club / band name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Text("Home Base will be your current GPS location when you start. Keep the app's tracking notification visible while you parade.", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { vm.startRoute(name, club) }, modifier = Modifier.fillMaxWidth()) { Text("Start live route") }
        if (gps.activeRouteId != null) OutlinedButton(onClick = { vm.endRoute() }) { Text("End live route") }
        TextButton(onClick = { vm.go(Screen.REGISTER) }) { Text("Register as a Leader") }
        TextButton(onClick = { vm.go(Screen.HOME) }) { Text("Back") }
    }
}

@Composable
fun RegisterScreen(vm: RouteViewModel) {
    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var club by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Leader Registration", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("New leaders start as Pending. You need 2 vouches from verified leaders to become Active. Admins can also verify or ban.", style = MaterialTheme.typography.bodySmall)
        MessageBox(vm)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title (e.g. President)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = club, onValueChange = { club = it }, label = { Text("Club name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = pw, onValueChange = { pw = it }, label = { Text("Leader password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
        Button(onClick = { vm.registerLeader(name, title, club, pw) }) { Text("Register") }
        TextButton(onClick = { vm.go(Screen.HOME) }) { Text("Back") }
    }
}

@Composable
fun AdminScreen(vm: RouteViewModel) {
    val leaders by vm.leaders.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Admin", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Verify, ban, and manage memberships. In this first build, leaders and admins are local to this phone — cross-phone admin needs a backend later.", style = MaterialTheme.typography.bodySmall)
        MessageBox(vm)
        if (leaders.isEmpty()) Text("No leaders registered yet on this phone.")
        leaders.forEach { l ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${l.name} — ${l.title}, ${l.club}", fontWeight = FontWeight.Bold)
                    Text("Status: ${l.status} • Vouches: ${l.vouches}/2 • Membership: ${l.membershipExpiresAt?.let { "until ${fmtTime(it)}" } ?: "not paid"}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { vm.vouch(l.id) }) { Text("Vouch") }
                        OutlinedButton(onClick = { vm.setLeaderStatus(l.id, "active") }) { Text("Verify") }
                        OutlinedButton(onClick = { vm.setLeaderStatus(l.id, "banned") }) { Text("Ban") }
                    }
                    OutlinedButton(onClick = { vm.membershipTestMode(l.id) }) { Text("\$10 annual membership — test mode (no charge)") }
                }
            }
        }
        TextButton(onClick = { vm.go(Screen.HOME) }) { Text("Back") }
    }
}

@Composable
fun ProfileScreen(vm: RouteViewModel) {
    val role by vm.role.collectAsState()
    val allowRot by vm.allowRotation.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        MessageBox(vm)
        Text("Role", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (role == "Community") Button(onClick = {}) { Text("Community") } else OutlinedButton(onClick = { vm.setRole("Community") }) { Text("Community") }
            if (role == "Leader") Button(onClick = {}) { Text("Leader") } else OutlinedButton(onClick = { vm.setRole("Leader") }) { Text("Leader") }
        }
        Text("Community can watch immediately, no email needed. Leaders register with a password to start a route.", style = MaterialTheme.typography.bodySmall)
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = allowRot, onCheckedChange = { vm.setAllowRotation(it) })
            Text("Allow rotation (off = portrait only)")
        }
        Text("Portrait is the default. Turning this on lets the phone rotate the app; turning it off locks portrait again on next open.", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { vm.go(Screen.REGISTER) }) { Text("Register as a Leader") }
        OutlinedButton(onClick = { vm.go(Screen.ADMIN) }) { Text("Admin panel") }
        TextButton(onClick = { vm.go(Screen.LIMITS) }) { Text("Limits & privacy") }
        BottomNav(vm)
    }
}

@Composable
fun LimitsScreen(vm: RouteViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("What this build can and cannot do yet", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("• CAN: live GPS route with breadcrumbs and Home Base on OpenStreetMap, vendor pins that expire after 4 hours, 200m-checked reports, leader registration with 2-vouch activation, admin verify/ban, on-device Vibe Check, portrait with a rotation option.")
        Text("• CANNOT YET: share a live route with other people's phones. That needs a backend server, which is not in this first Android build. Everything here is local to this phone, so test it with routes, vendors and reports on one phone first.")
        Text("• PAYMENTS ARE TEST MODE ONLY: the \$10 leader membership and \$5 vendor promotion buttons do not charge anything in this build. Real Square checkout needs a backend and sandbox keys set up securely, not pasted in chat.")
        Text("• VIBE CHECK: mic audio is analysed for 5 seconds on-device and discarded. No audio file is saved or sent anywhere.")
        Text("• MAP TILES: OpenStreetMap tiles only, no key. If you're offline, the map may show a blank grid, but GPS tracking still works.")
        TextButton(onClick = { vm.go(Screen.HOME) }) { Text("Back") }
    }
}
