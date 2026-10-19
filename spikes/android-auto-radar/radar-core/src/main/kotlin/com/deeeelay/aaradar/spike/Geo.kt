package com.deeeelay.aaradar.spike

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan

private const val EARTH_RADIUS_KM = 6371.0088
private const val WEB_MERCATOR_HALF = 20037508.342789244

data class GeoBounds(
    val west: Double,
    val south: Double,
    val east: Double,
    val north: Double,
)

data class TileCoord(val zoom: Int, val x: Int, val y: Int) {
    val bounds: GeoBounds = WebMercator.tileBounds(x, y, zoom)
    val url: String = "https://tile.openstreetmap.org/$zoom/$x/$y.png"
}

object WebMercator {
    fun tileContaining(latitude: Double, longitude: Double, zoom: Int): TileCoord {
        val n = 2.0.pow(zoom)
        val x = (((longitude + 180.0) / 360.0) * n).toInt().coerceIn(0, n.toInt() - 1)
        val latRad = Math.toRadians(latitude)
        val yFrac = (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0
        val y = (yFrac * n).toInt().coerceIn(0, n.toInt() - 1)
        return TileCoord(zoom, x, y)
    }

    fun tileBounds(x: Int, y: Int, zoom: Int): GeoBounds {
        val n = 2.0.pow(zoom)
        val west = x / n * 360.0 - 180.0
        val east = (x + 1) / n * 360.0 - 180.0
        val north = latitudeAt((y / n))
        val south = latitudeAt((y + 1) / n)
        return GeoBounds(west, south, east, north)
    }

    fun mercatorX(longitude: Double): Double = longitude * WEB_MERCATOR_HALF / 180.0

    fun mercatorY(latitude: Double): Double {
        val clamped = latitude.coerceIn(-85.05112878, 85.05112878)
        val rad = Math.toRadians(clamped)
        return ln(tan(PI / 4.0 + rad / 2.0)) * WEB_MERCATOR_HALF / PI
    }

    fun lonLatAt(bounds: GeoBounds, x: Double, y: Double, width: Int, height: Int): Pair<Double, Double> {
        val longitude = bounds.west + (x / width) * (bounds.east - bounds.west)
        val northY = mercatorY(bounds.north)
        val southY = mercatorY(bounds.south)
        val projected = northY - (y / height) * (northY - southY)
        val latitude = Math.toDegrees(atan(sinh(projected * PI / WEB_MERCATOR_HALF)))
        return latitude to longitude
    }

    private fun latitudeAt(yFraction: Double): Double {
        val mercator = PI * (1.0 - 2.0 * yFraction)
        return Math.toDegrees(atan(sinh(mercator)))
    }
}

object Geodesy {
    fun destination(latitude: Double, longitude: Double, bearingDeg: Double, distanceKm: Double): Pair<Double, Double> {
        val delta = distanceKm / EARTH_RADIUS_KM
        val theta = Math.toRadians(bearingDeg)
        val lat1 = Math.toRadians(latitude)
        val lon1 = Math.toRadians(longitude)
        val lat2 = asin(sin(lat1) * cos(delta) + cos(lat1) * sin(delta) * cos(theta))
        val lon2 = lon1 + atan2(
            sin(theta) * sin(delta) * cos(lat1),
            cos(delta) - sin(lat1) * sin(lat2),
        )
        return Math.toDegrees(lat2) to wrapLongitude(Math.toDegrees(lon2))
    }

    fun rangeAzimuthKm(lat1Deg: Double, lon1Deg: Double, lat2Deg: Double, lon2Deg: Double): Pair<Double, Double> {
        val lat1 = Math.toRadians(lat1Deg)
        val lat2 = Math.toRadians(lat2Deg)
        val dLon = Math.toRadians(lon2Deg - lon1Deg)
        val haversine = sin((lat2 - lat1) / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val distanceKm = 2 * EARTH_RADIUS_KM * asin(sqrt(haversine))
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        var azimuth = Math.toDegrees(atan2(y, x))
        if (azimuth < 0) {
            azimuth += 360.0
        }
        return distanceKm to azimuth
    }

    fun boundsAround(latitude: Double, longitude: Double, radiusKm: Double): GeoBounds {
        val north = destination(latitude, longitude, 0.0, radiusKm)
        val east = destination(latitude, longitude, 90.0, radiusKm)
        val south = destination(latitude, longitude, 180.0, radiusKm)
        val west = destination(latitude, longitude, 270.0, radiusKm)
        return GeoBounds(west.second, south.first, east.second, north.first)
    }

    private fun wrapLongitude(longitude: Double): Double {
        var wrapped = longitude
        while (wrapped > 180.0) wrapped -= 360.0
        while (wrapped < -180.0) wrapped += 360.0
        return wrapped
    }
}
