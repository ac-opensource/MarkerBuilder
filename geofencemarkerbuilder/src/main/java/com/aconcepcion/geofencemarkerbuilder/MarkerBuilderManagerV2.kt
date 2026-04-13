package com.aconcepcion.geofencemarkerbuilder

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

class MarkerBuilderManagerV2 private constructor(
    builder: Builder,
) {
    private val listener = builder.circleManagerListener
    private val defaultRadius = builder.radius.takeIf { it > 0.0 } ?: DEFAULT_RADIUS_METERS
    private val strokeWidth = builder.strokeWidth
    private val strokeColor = builder.strokeColor
    private val fillColor = builder.fillColor
    private val minRadius = builder.minRadius.takeIf { it >= 0 }?.toDouble()
    private val maxRadius = builder.maxRadius.takeIf { it >= 0 }?.toDouble()

    private val _uiState = MutableStateFlow(
        MarkerBuilderUiState(isEnabled = builder.isEnabled),
    )

    private var nextCircleId = max(1L, builder.circleId.takeIf { it > 0 } ?: 1L)

    val uiState: StateFlow<MarkerBuilderUiState> = _uiState.asStateFlow()

    fun clearCircles() {
        val updatedState = _uiState.value.copy(
            draftCircles = emptyList(),
            selectedCircleId = _uiState.value.selectedCircleId.takeUnless { selectedId ->
                _uiState.value.draftCircles.any { it.circleId == selectedId }
            },
        )
        _uiState.value = updatedState
    }

    fun removeCircle(geofenceCircleId: Long) {
        updateState { state ->
            val updatedDrafts = state.draftCircles.filterNot { it.circleId == geofenceCircleId }
            val updatedSaved = state.savedCircles.filterNot { it.circleId == geofenceCircleId }
            state.copy(
                draftCircles = updatedDrafts,
                savedCircles = updatedSaved,
                selectedCircleId = if (state.selectedCircleId == geofenceCircleId) {
                    null
                } else {
                    state.selectedCircleId
                },
            )
        }
    }

    fun clearSavedCircles() {
        updateState { state ->
            state.copy(
                savedCircles = emptyList(),
                selectedCircleId = state.selectedCircleId.takeUnless { selectedId ->
                    state.savedCircles.any { it.circleId == selectedId }
                },
            )
        }
    }

    fun setEnabled(isEnabled: Boolean) {
        updateState { state ->
            state.copy(isEnabled = isEnabled)
        }
    }

    fun markThis(point: LatLng, isSaved: Boolean): GeofenceCircle {
        val newCircle = buildCircle(
            center = point,
            radiusMeters = defaultRadius,
            isSaved = isSaved,
            explicitCircleId = null,
            fillColor = fillColor,
        )

        updateState { state ->
            if (isSaved) {
                state.copy(
                    savedCircles = state.savedCircles + newCircle,
                    selectedCircleId = newCircle.circleId,
                )
            } else {
                state.copy(
                    draftCircles = listOf(newCircle),
                    selectedCircleId = newCircle.circleId,
                )
            }
        }

        listener?.onCreateCircle(newCircle)
        return newCircle
    }

    fun plotPoints(point: LatLng, radius: Int, circularGeofenceId: Long, fillColor: Int) {
        val circle = addSavedPoint(point, radius, circularGeofenceId, fillColor)
        listener?.onInitCreateCircle(circle)
    }

    fun addSavedPoint(
        savedPoint: LatLng,
        radius: Int,
        geofenceCircleId: Long,
        fillColor: Int,
    ): GeofenceCircle {
        val circle = buildCircle(
            center = savedPoint,
            radiusMeters = radius.toDouble(),
            isSaved = true,
            explicitCircleId = geofenceCircleId,
            fillColor = fillColor,
        )

        updateState { state ->
            state.copy(
                savedCircles = state.savedCircles + circle,
                selectedCircleId = circle.circleId,
            )
        }

        return circle
    }

    fun selectCircle(circleId: Long) {
        val circle = findCircle(circleId) ?: return

        updateState { state ->
            state.copy(selectedCircleId = circleId)
        }

        listener?.onCircleMarkerClick(circle)
    }

    fun beginMove(circleId: Long) {
        findCircle(circleId)?.let { circle ->
            listener?.onMoveCircleStart(circle)
            selectCircle(circleId)
        }
    }

    fun updateCircleCenter(circleId: Long, center: LatLng): GeofenceCircle? {
        if (!_uiState.value.isEnabled) {
            return findCircle(circleId)
        }

        var updatedCircle: GeofenceCircle? = null
        updateState { state ->
            val updatedDrafts = state.draftCircles.map { circle ->
                if (circle.circleId == circleId) {
                    circle.copy(center = center).also { updatedCircle = it }
                } else {
                    circle
                }
            }
            val updatedSaved = state.savedCircles.map { circle ->
                if (circle.circleId == circleId) {
                    circle.copy(center = center).also { updatedCircle = it }
                } else {
                    circle
                }
            }
            state.copy(
                draftCircles = updatedDrafts,
                savedCircles = updatedSaved,
                selectedCircleId = circleId,
            )
        }
        return updatedCircle
    }

    fun endMove(circleId: Long) {
        findCircle(circleId)?.let { circle ->
            listener?.onMoveCircleEnd(circle)
        }
    }

    fun beginResize(circleId: Long) {
        findCircle(circleId)?.let { circle ->
            listener?.onResizeCircleStart(circle)
            selectCircle(circleId)
        }
    }

    fun updateRadiusFromHandle(circleId: Long, handlePosition: LatLng): GeofenceCircle? {
        if (!_uiState.value.isEnabled) {
            return findCircle(circleId)
        }

        val existingCircle = findCircle(circleId) ?: return null
        val proposedRadius = MarkerAreasUtils.toRadiusMeters(existingCircle.center, handlePosition)
        val clampedRadius = MarkerAreasUtils.clampRadius(
            proposedRadiusMeters = proposedRadius,
            minRadiusMeters = existingCircle.minRadiusMeters,
            maxRadiusMeters = existingCircle.maxRadiusMeters,
        )

        val updatedCircle = updateCircle(circleId) { circle ->
            circle.copy(radiusMeters = clampedRadius)
        }

        updatedCircle?.let { circle ->
            when {
                existingCircle.minRadiusMeters != null && proposedRadius < existingCircle.minRadiusMeters ->
                    listener?.onMinRadius(circle)

                existingCircle.maxRadiusMeters != null && proposedRadius > existingCircle.maxRadiusMeters ->
                    listener?.onMaxRadius(circle)
            }
        }

        return updatedCircle
    }

    fun endResize(circleId: Long) {
        findCircle(circleId)?.let { circle ->
            listener?.onResizeCircleEnd(circle)
        }
    }

    private fun updateCircle(
        circleId: Long,
        transform: (GeofenceCircle) -> GeofenceCircle,
    ): GeofenceCircle? {
        var updatedCircle: GeofenceCircle? = null
        updateState { state ->
            val updatedDrafts = state.draftCircles.map { circle ->
                if (circle.circleId == circleId) {
                    transform(circle).also { updatedCircle = it }
                } else {
                    circle
                }
            }
            val updatedSaved = state.savedCircles.map { circle ->
                if (circle.circleId == circleId) {
                    transform(circle).also { updatedCircle = it }
                } else {
                    circle
                }
            }
            state.copy(
                draftCircles = updatedDrafts,
                savedCircles = updatedSaved,
                selectedCircleId = circleId,
            )
        }
        return updatedCircle
    }

    private fun buildCircle(
        center: LatLng,
        radiusMeters: Double,
        isSaved: Boolean,
        explicitCircleId: Long?,
        fillColor: Int,
    ): GeofenceCircle {
        val circleId = reserveCircleId(explicitCircleId)
        return GeofenceCircle(
            circleId = circleId,
            center = center,
            radiusMeters = MarkerAreasUtils.clampRadius(radiusMeters, minRadius, maxRadius),
            strokeColor = strokeColor,
            fillColor = fillColor,
            strokeWidth = strokeWidth,
            minRadiusMeters = minRadius,
            maxRadiusMeters = maxRadius,
            isSaved = isSaved,
        )
    }

    private fun reserveCircleId(explicitCircleId: Long?): Long {
        if (explicitCircleId != null && explicitCircleId > 0) {
            nextCircleId = max(nextCircleId, explicitCircleId + 1)
            return explicitCircleId
        }

        val generatedCircleId = nextCircleId
        nextCircleId += 1
        return generatedCircleId
    }

    private fun findCircle(circleId: Long): GeofenceCircle? {
        val state = _uiState.value
        return state.circles.firstOrNull { it.circleId == circleId }
    }

    private fun updateState(transform: (MarkerBuilderUiState) -> MarkerBuilderUiState) {
        _uiState.value = transform(_uiState.value)
    }

    class Builder @JvmOverloads constructor(
        @Suppress("unused") private val context: Context? = null,
    ) {
        internal var isEnabled: Boolean = true
        internal var radius: Double = DEFAULT_RADIUS_METERS
        internal var circleId: Long = 1L
        internal var fillColor: Int = 0x401B6EF3.toInt()
        internal var strokeWidth: Float = 4f
        internal var strokeColor: Int = 0xFFFF0000.toInt()
        internal var minRadius: Int = -1
        internal var maxRadius: Int = -1
        internal var circleManagerListener: CircleManagerListener? = null

        fun map(@Suppress("unused") googleMap: GoogleMap): Builder = this

        fun listener(circleManagerListener: CircleManagerListener): Builder {
            this.circleManagerListener = circleManagerListener
            return this
        }

        fun enabled(isEnabled: Boolean): Builder {
            this.isEnabled = isEnabled
            return this
        }

        fun radius(radius: Double): Builder {
            this.radius = radius
            return this
        }

        fun circleId(circleId: Long): Builder {
            this.circleId = circleId
            return this
        }

        fun strokeWidth(strokeWidth: Float): Builder {
            this.strokeWidth = strokeWidth
            return this
        }

        fun strokeColor(strokeColor: Int): Builder {
            this.strokeColor = strokeColor
            return this
        }

        fun fillColor(fillColor: Int): Builder {
            this.fillColor = fillColor
            return this
        }

        fun minRadius(minRadius: Int): Builder {
            this.minRadius = minRadius
            return this
        }

        fun maxRadius(maxRadius: Int): Builder {
            this.maxRadius = maxRadius
            return this
        }

        fun centerIcon(@Suppress("unused") centerIcon: Int): Builder = this

        fun centerBitmap(@Suppress("unused") centerBitmap: Bitmap?): Builder = this

        fun resizerIcon(@Suppress("unused") resizerIcon: Int): Builder = this

        fun centerOffsetHorizontal(@Suppress("unused") centerOffsetHorizontal: Float): Builder = this

        fun centerOffsetVertical(@Suppress("unused") centerOffsetVertical: Float): Builder = this

        fun resizerOffsetHorizontal(@Suppress("unused") resizerOffsetHorizontal: Float): Builder = this

        fun resizerOffsetVertical(@Suppress("unused") resizerOffsetVertical: Float): Builder = this

        fun build(): MarkerBuilderManagerV2 {
            return MarkerBuilderManagerV2(this)
        }
    }

    private companion object {
        const val DEFAULT_RADIUS_METERS = 200.0
    }
}
