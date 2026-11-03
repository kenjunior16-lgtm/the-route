package com.theroute.app.engine

import kotlin.math.*

/** Haversine distance in miles, and metres for the 200m report rule. */
object Geo {
    fun haversineMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 3958.8
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * r * asin(sqrt(a))
    }
    fun metres(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double =
        haversineMiles(lat1, lon1, lat2, lon2) * 1609.344
    fun withinMetres(lat1: Double, lon1: Double, lat2: Double, lon2: Double, metres: Double): Boolean =
        metres(lat1, lon1, lat2, lon2) <= metres
}
