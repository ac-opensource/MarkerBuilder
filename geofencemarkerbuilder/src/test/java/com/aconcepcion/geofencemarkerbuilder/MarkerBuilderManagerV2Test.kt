package com.aconcepcion.geofencemarkerbuilder

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkerBuilderManagerV2Test {

    @Test
    fun `markThis replaces the existing draft circle`() {
        val manager = MarkerBuilderManagerV2.Builder()
            .radius(250.0)
            .build()

        manager.markThis(LatLng(14.5995, 120.9842), isSaved = false)
        manager.markThis(LatLng(14.6080, 120.9822), isSaved = false)

        val state = manager.uiState.value

        assertEquals(1, state.draftCircles.size)
        assertEquals(LatLng(14.6080, 120.9822), state.draftCircles.single().center)
        assertEquals(250.0, state.draftCircles.single().radiusMeters, 0.0)
    }

    @Test
    fun `clearCircles only removes drafts`() {
        val manager = MarkerBuilderManagerV2.Builder().build()

        manager.plotPoints(LatLng(14.5995, 120.9842), 200, 777L, 0x3300FF00.toInt())
        manager.markThis(LatLng(14.6080, 120.9822), isSaved = false)

        manager.clearCircles()

        val state = manager.uiState.value

        assertTrue(state.draftCircles.isEmpty())
        assertEquals(1, state.savedCircles.size)
    }

    @Test
    fun `removeCircle uses the requested id`() {
        val manager = MarkerBuilderManagerV2.Builder().build()

        manager.plotPoints(LatLng(14.5995, 120.9842), 200, 101L, 0x3300FF00.toInt())
        manager.plotPoints(LatLng(14.6080, 120.9822), 220, 202L, 0x3300FF00.toInt())

        manager.removeCircle(101L)

        val remainingIds = manager.uiState.value.savedCircles.map { it.circleId }
        assertEquals(listOf(202L), remainingIds)
    }

    @Test
    fun `resize clamps to configured minimum radius`() {
        val manager = MarkerBuilderManagerV2.Builder()
            .radius(200.0)
            .minRadius(150)
            .build()

        val circle = manager.markThis(LatLng(14.5995, 120.9842), isSaved = false)

        val updatedCircle = manager.updateRadiusFromHandle(circle.circleId, circle.center)

        assertEquals(150.0, updatedCircle!!.radiusMeters, 0.0)
    }

    @Test
    fun `removeCircle clears the selected circle when it is deleted`() {
        val manager = MarkerBuilderManagerV2.Builder().build()
        val circle = manager.markThis(LatLng(14.5995, 120.9842), isSaved = false)

        manager.removeCircle(circle.circleId)

        assertNull(manager.uiState.value.selectedCircleId)
    }
}
