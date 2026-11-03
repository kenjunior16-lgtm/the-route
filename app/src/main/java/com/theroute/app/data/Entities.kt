package com.theroute.app.data

import androidx.room.*

@Entity(tableName = "routes")
data class ParadeRoute(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val club: String,
    val leaderId: Long? = null,
    val homeLat: Double?,
    val homeLon: Double?,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val status: String = "rolling", // rolling | ended
)

@Entity(tableName = "route_points")
data class RoutePoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeId: Long,
    val lat: Double,
    val lon: Double,
    val at: Long = System.currentTimeMillis(),
)

@Entity(tableName = "vendors")
data class Vendor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val itemType: String, // Plate | Drink | Water
    val priceRange: String,
    val lat: Double,
    val lon: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 4 * 60 * 60 * 1000L,
    val promoted: Boolean = false,
    val promotedUntil: Long? = null,
) {
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean = now >= expiresAt
    fun isPromotedNow(now: Long = System.currentTimeMillis()): Boolean =
        promoted && promotedUntil != null && now < promotedUntil!!
}

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val description: String,
    val lat: Double,
    val lon: Double,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "leaders")
data class Leader(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val title: String,
    val club: String,
    val passwordHash: String,
    val status: String = "pending", // pending | active | banned
    val vouches: Int = 0,
    val vouchedBy: String = "",
    val membershipExpiresAt: Long? = null,
    val isAdmin: Boolean = false,
)

@Dao
interface RouteDao {
    @Insert suspend fun insertRoute(r: ParadeRoute): Long
    @Query("SELECT * FROM routes ORDER BY startedAt DESC") suspend fun allRoutes(): List<ParadeRoute>
    @Query("SELECT * FROM routes WHERE status='rolling' ORDER BY startedAt DESC") suspend fun liveRoutes(): List<ParadeRoute>
    @Query("UPDATE routes SET status='ended', endedAt=:endedAt WHERE id=:id") suspend fun endRoute(id: Long, endedAt: Long)
    @Insert suspend fun insertPoint(p: RoutePoint)
    @Query("SELECT * FROM route_points WHERE routeId=:routeId ORDER BY at ASC") suspend fun points(routeId: Long): List<RoutePoint>
    @Insert suspend fun insertVendor(v: Vendor): Long
    @Query("SELECT * FROM vendors ORDER BY promoted DESC, createdAt DESC") suspend fun allVendors(): List<Vendor>
    @Query("DELETE FROM vendors WHERE expiresAt <= :now") suspend fun deleteExpiredVendors(now: Long)
    @Insert suspend fun insertReport(r: Report)
    @Query("SELECT * FROM reports ORDER BY createdAt DESC") suspend fun allReports(): List<Report>
    @Insert suspend fun insertLeader(l: Leader): Long
    @Query("SELECT * FROM leaders ORDER BY club, name") suspend fun allLeaders(): List<Leader>
    @Query("SELECT * FROM leaders WHERE id=:id") suspend fun leader(id: Long): Leader?
    @Query("UPDATE leaders SET status=:status, vouches=:vouches, vouchedBy=:vouchedBy WHERE id=:id") suspend fun updateLeaderStatus(id: Long, status: String, vouches: Int, vouchedBy: String)
    @Query("UPDATE leaders SET membershipExpiresAt=:until WHERE id=:id") suspend fun setMembership(id: Long, until: Long?)
    @Query("UPDATE vendors SET promoted=1, promotedUntil=:until, expiresAt=:expiresAt WHERE id=:id") suspend fun promoteVendor(id: Long, until: Long, expiresAt: Long)
}

@Database(entities = [ParadeRoute::class, RoutePoint::class, Vendor::class, Report::class, Leader::class], version = 1, exportSchema = false)
abstract class RouteDatabase : RoomDatabase() {
    abstract fun dao(): RouteDao
}
