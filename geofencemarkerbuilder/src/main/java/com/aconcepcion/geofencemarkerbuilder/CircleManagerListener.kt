package com.aconcepcion.geofencemarkerbuilder

interface CircleManagerListener {
    fun onInitCreateCircle(draggableCircle: GeofenceCircle)

    fun onCircleMarkerClick(draggableCircle: GeofenceCircle)

    fun onCreateCircle(draggableCircle: GeofenceCircle)

    fun onResizeCircleEnd(draggableCircle: GeofenceCircle)

    fun onMoveCircleEnd(draggableCircle: GeofenceCircle)

    fun onMoveCircleStart(draggableCircle: GeofenceCircle)

    fun onResizeCircleStart(draggableCircle: GeofenceCircle)

    fun onMinRadius(draggableCircle: GeofenceCircle)

    fun onMaxRadius(draggableCircle: GeofenceCircle)
}
