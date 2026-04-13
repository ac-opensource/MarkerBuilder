package com.aconcepcion.markerbuilder

import androidx.lifecycle.ViewModel
import com.aconcepcion.geofencemarkerbuilder.MarkerBuilderManagerV2
import com.aconcepcion.geofencemarkerbuilder.MarkerBuilderUiState
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.StateFlow

class MarkerBuilderViewModel : ViewModel() {

    private val manager = MarkerBuilderManagerV2.Builder()
        .enabled(true)
        .radius(220.0)
        .strokeWidth(3f)
        .strokeColor(0xFF0B57D0.toInt())
        .fillColor(0x331B6EF3.toInt())
        .minRadius(50)
        .maxRadius(1_500)
        .build()

    val uiState: StateFlow<MarkerBuilderUiState> = manager.uiState

    private var seededCircleOffset = 0

    init {
        seedSavedCircle()
    }

    fun onMapTap(point: LatLng) {
        manager.markThis(point, isSaved = false)
    }

    fun onSelectCircle(circleId: Long) {
        manager.selectCircle(circleId)
    }

    fun onCenterDragStart(circleId: Long) {
        manager.beginMove(circleId)
    }

    fun onCenterDrag(circleId: Long, center: LatLng) {
        manager.updateCircleCenter(circleId, center)
    }

    fun onCenterDragEnd(circleId: Long, center: LatLng) {
        manager.updateCircleCenter(circleId, center)
        manager.endMove(circleId)
    }

    fun onResizeDragStart(circleId: Long) {
        manager.beginResize(circleId)
    }

    fun onResizeDrag(circleId: Long, handlePosition: LatLng) {
        manager.updateRadiusFromHandle(circleId, handlePosition)
    }

    fun onResizeDragEnd(circleId: Long, handlePosition: LatLng) {
        manager.updateRadiusFromHandle(circleId, handlePosition)
        manager.endResize(circleId)
    }

    fun clearDrafts() {
        manager.clearCircles()
    }

    fun clearSaved() {
        manager.clearSavedCircles()
    }

    fun removeSelected() {
        uiState.value.selectedCircleId?.let(manager::removeCircle)
    }

    fun seedSavedCircle() {
        val center = SAVED_CIRCLE_SEEDS[seededCircleOffset % SAVED_CIRCLE_SEEDS.size]
        manager.plotPoints(
            point = center,
            radius = 280,
            circularGeofenceId = 10_000L + seededCircleOffset,
            fillColor = 0x334CAF50.toInt(),
        )
        seededCircleOffset += 1
    }

    fun toggleEditing() {
        manager.setEnabled(!uiState.value.isEnabled)
    }

    companion object {
        private val SAVED_CIRCLE_SEEDS = listOf(
            LatLng(14.5995, 120.9842),
            LatLng(14.6080, 120.9822),
            LatLng(14.5935, 120.9750),
        )
    }
}
