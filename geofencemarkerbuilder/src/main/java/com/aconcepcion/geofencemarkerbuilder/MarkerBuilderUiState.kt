package com.aconcepcion.geofencemarkerbuilder

data class MarkerBuilderUiState(
    val draftCircles: List<GeofenceCircle> = emptyList(),
    val savedCircles: List<GeofenceCircle> = emptyList(),
    val selectedCircleId: Long? = null,
    val isEnabled: Boolean = true,
) {
    val circles: List<GeofenceCircle>
        get() = draftCircles + savedCircles

    val selectedCircle: GeofenceCircle?
        get() = circles.firstOrNull { it.circleId == selectedCircleId }
}
