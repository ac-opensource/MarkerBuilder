package com.aconcepcion.geofencemarkerbuilder

import androidx.annotation.ColorInt
import com.google.android.gms.maps.model.LatLng

data class GeofenceCircle(
    val circleId: Long,
    val center: LatLng,
    val radiusMeters: Double,
    @param:ColorInt val strokeColor: Int,
    @param:ColorInt val fillColor: Int,
    val strokeWidth: Float,
    val minRadiusMeters: Double?,
    val maxRadiusMeters: Double?,
    val isSaved: Boolean,
) {
    val resizeHandlePosition: LatLng
        get() = MarkerAreasUtils.toRadiusLatLng(center, radiusMeters)
}
