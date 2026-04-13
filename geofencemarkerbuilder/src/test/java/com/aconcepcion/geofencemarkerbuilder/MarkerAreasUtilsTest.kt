package com.aconcepcion.geofencemarkerbuilder

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkerAreasUtilsTest {

    @Test
    fun `toRadiusLatLng returns the center when radius is zero`() {
        val center = LatLng(14.5995, 120.9842)

        val result = MarkerAreasUtils.toRadiusLatLng(center, 0.0)

        assertEquals(center, result)
    }

    @Test
    fun `toRadiusMeters approximates the requested radius`() {
        val center = LatLng(14.5995, 120.9842)
        val radiusMeters = 300.0

        val radiusHandle = MarkerAreasUtils.toRadiusLatLng(center, radiusMeters)
        val measuredRadius = MarkerAreasUtils.toRadiusMeters(center, radiusHandle)

        assertEquals(radiusMeters, measuredRadius, 1.0)
    }

    @Test
    fun `toRadiusLatLng stays in the longitude bounds near the poles`() {
        val center = LatLng(89.9, 120.9842)

        val result = MarkerAreasUtils.toRadiusLatLng(center, 400.0)

        assertTrue(result.longitude in -180.0..180.0)
    }
}
