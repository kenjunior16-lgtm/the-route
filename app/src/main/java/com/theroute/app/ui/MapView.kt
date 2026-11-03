package com.theroute.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.theroute.app.data.Report
import com.theroute.app.data.RoutePoint
import com.theroute.app.data.Vendor
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * OpenStreetMap, no API key. Tiles come from OSM servers (tile
 * coordinates only); parade, vendor and report data stay on this phone
 * in this first build — there is no backend sync yet.
 */
@Composable
fun RouteMap(
    lat: Double?, lon: Double?,
    points: List<RoutePoint>, homeLat: Double?, homeLon: Double?,
    vendors: List<Vendor>, reports: List<Report>,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(14.0)
                controller.setCenter(GeoPoint(lat ?: 29.95, lon ?: -90.07))
            }
        },
        update = { map ->
            map.overlays.clear()
            if (points.size >= 2) {
                val line = Polyline().apply {
                    setPoints(points.map { GeoPoint(it.lat, it.lon) })
                    outlinePaint.strokeWidth = 8f
                }
                map.overlays.add(line)
            }
            if (homeLat != null && homeLon != null) {
                map.overlays.add(Marker(map).apply { position = GeoPoint(homeLat, homeLon); title = "Home Base"; setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) })
            }
            vendors.forEach { v ->
                map.overlays.add(Marker(map).apply { position = GeoPoint(v.lat, v.lon); title = "${v.name} (${v.itemType})"; setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) })
            }
            reports.forEach { r ->
                map.overlays.add(Marker(map).apply { position = GeoPoint(r.lat, r.lon); title = r.type; snippet = r.description; setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) })
            }
            if (lat != null && lon != null) {
                map.overlays.add(Marker(map).apply { position = GeoPoint(lat, lon); title = "You / Live pin"; setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) })
                map.controller.setCenter(GeoPoint(lat, lon))
            }
            map.invalidate()
        },
    )
}
