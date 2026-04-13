package com.aconcepcion.geofencemarkerbuilder

import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object MarkerAreasUtils {
    private const val MIN_COS_LATITUDE = 1.0e-6

    fun toRadiusLatLng(center: LatLng, radius: Double): LatLng {
        if (radius <= 0.0) {
            return center
        }

        val clampedLatitude = center.latitude.coerceIn(-89.999999, 89.999999)
        val latitudeRadians = Math.toRadians(clampedLatitude)
        val angularDistance = radius / MarkerAreasConstants.RADIUS_OF_EARTH_METERS
        val longitudeOffsetDegrees = Math.toDegrees(
            angularDistance / max(abs(cos(latitudeRadians)), MIN_COS_LATITUDE),
        )

        return LatLng(
            center.latitude,
            normalizeLongitude(center.longitude + longitudeOffsetDegrees),
        )
    }

    fun toRadiusMeters(center: LatLng, radius: LatLng): Double {
        val latitudeDelta = Math.toRadians(radius.latitude - center.latitude)
        val longitudeDelta = Math.toRadians(radius.longitude - center.longitude)

        val centerLatitudeRadians = Math.toRadians(center.latitude)
        val radiusLatitudeRadians = Math.toRadians(radius.latitude)

        val haversine = sin(latitudeDelta / 2.0).pow(2.0) +
            cos(centerLatitudeRadians) * cos(radiusLatitudeRadians) * sin(longitudeDelta / 2.0).pow(2.0)

        val arc = 2.0 * asin(min(1.0, sqrt(haversine)))
        return MarkerAreasConstants.RADIUS_OF_EARTH_METERS * arc
    }

    fun clampRadius(
        proposedRadiusMeters: Double,
        minRadiusMeters: Double?,
        maxRadiusMeters: Double?,
    ): Double {
        val safeMin = max(0.0, minRadiusMeters ?: 0.0)
        val safeMax = maxRadiusMeters ?: Double.MAX_VALUE
        return proposedRadiusMeters.coerceIn(safeMin, safeMax)
    }

    private fun normalizeLongitude(longitude: Double): Double {
        var normalized = longitude
        while (normalized > 180.0) {
            normalized -= 360.0
        }
        while (normalized < -180.0) {
            normalized += 360.0
        }
        return normalized
    }
}
