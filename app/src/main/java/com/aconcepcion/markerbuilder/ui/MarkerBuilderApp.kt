package com.aconcepcion.markerbuilder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aconcepcion.geofencemarkerbuilder.GeofenceCircle
import com.aconcepcion.geofencemarkerbuilder.MarkerBuilderUiState
import com.aconcepcion.markerbuilder.BuildConfig
import com.aconcepcion.markerbuilder.MarkerBuilderViewModel
import com.aconcepcion.markerbuilder.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap as GmsGoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker as GoogleMarker
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun MarkerBuilderApp(
    viewModel: MarkerBuilderViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MarkerBuilderTheme {
        MarkerBuilderScreen(
            uiState = uiState,
            onMapTap = viewModel::onMapTap,
            onSelectCircle = viewModel::onSelectCircle,
            onCenterDragStart = viewModel::onCenterDragStart,
            onCenterDrag = viewModel::onCenterDrag,
            onCenterDragEnd = viewModel::onCenterDragEnd,
            onResizeDragStart = viewModel::onResizeDragStart,
            onResizeDrag = viewModel::onResizeDrag,
            onResizeDragEnd = viewModel::onResizeDragEnd,
            onClearDrafts = viewModel::clearDrafts,
            onClearSaved = viewModel::clearSaved,
            onRemoveSelected = viewModel::removeSelected,
            onSeedSavedCircle = viewModel::seedSavedCircle,
            onToggleEditing = viewModel::toggleEditing,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarkerBuilderScreen(
    uiState: MarkerBuilderUiState,
    onMapTap: (LatLng) -> Unit,
    onSelectCircle: (Long) -> Unit,
    onCenterDragStart: (Long) -> Unit,
    onCenterDrag: (Long, LatLng) -> Unit,
    onCenterDragEnd: (Long, LatLng) -> Unit,
    onResizeDragStart: (Long) -> Unit,
    onResizeDrag: (Long, LatLng) -> Unit,
    onResizeDragEnd: (Long, LatLng) -> Unit,
    onClearDrafts: () -> Unit,
    onClearSaved: () -> Unit,
    onRemoveSelected: () -> Unit,
    onSeedSavedCircle: () -> Unit,
    onToggleEditing: () -> Unit,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_CAMERA_POSITION, 12f)
    }

    LaunchedEffect(uiState.selectedCircleId) {
        uiState.selectedCircle?.let { circle ->
            cameraPositionState.animate(CameraUpdateFactory.newLatLng(circle.center))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_title)) },
                actions = {
                    FilledTonalButton(
                        modifier = Modifier.padding(end = 12.dp),
                        onClick = onToggleEditing,
                    ) {
                        Text(
                            text = stringResource(
                                if (uiState.isEnabled) {
                                    R.string.lock_markers
                                } else {
                                    R.string.unlock_markers
                                },
                            ),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(minZoomPreference = 3f),
                uiSettings = MapUiSettings(
                    compassEnabled = true,
                    mapToolbarEnabled = false,
                    zoomControlsEnabled = false,
                ),
                onMapClick = onMapTap,
            ) {
                MapEffect(onCenterDragStart, onCenterDrag, onCenterDragEnd, onResizeDragStart, onResizeDrag, onResizeDragEnd) { googleMap ->
                    googleMap.setOnMarkerDragListener(
                        object : GmsGoogleMap.OnMarkerDragListener {
                            override fun onMarkerDragStart(marker: GoogleMarker) {
                                marker.markerTag()?.let { tag ->
                                    when (tag.type) {
                                        MarkerHandleType.CENTER -> onCenterDragStart(tag.circleId)
                                        MarkerHandleType.RESIZE -> onResizeDragStart(tag.circleId)
                                    }
                                }
                            }

                            override fun onMarkerDrag(marker: GoogleMarker) {
                                marker.markerTag()?.let { tag ->
                                    when (tag.type) {
                                        MarkerHandleType.CENTER -> onCenterDrag(tag.circleId, marker.position)
                                        MarkerHandleType.RESIZE -> onResizeDrag(tag.circleId, marker.position)
                                    }
                                }
                            }

                            override fun onMarkerDragEnd(marker: GoogleMarker) {
                                marker.markerTag()?.let { tag ->
                                    when (tag.type) {
                                        MarkerHandleType.CENTER -> onCenterDragEnd(tag.circleId, marker.position)
                                        MarkerHandleType.RESIZE -> onResizeDragEnd(tag.circleId, marker.position)
                                    }
                                }
                            }
                        },
                    )
                }

                uiState.circles.forEach { circle ->
                    Circle(
                        center = circle.center,
                        radius = circle.radiusMeters,
                        strokeColor = Color(circle.strokeColor),
                        strokeWidth = circle.strokeWidth,
                        fillColor = Color(circle.fillColor),
                    )

                    Marker(
                        state = rememberUpdatedMarkerState(position = circle.center),
                        draggable = uiState.isEnabled,
                        title = circle.markerTitle(),
                        snippet = circle.radiusMeters.asRadiusLabel(),
                        tag = MarkerHandleTag(circle.circleId, MarkerHandleType.CENTER),
                        onClick = {
                            onSelectCircle(circle.circleId)
                            false
                        },
                    )

                    Marker(
                        state = rememberUpdatedMarkerState(position = circle.resizeHandlePosition),
                        draggable = uiState.isEnabled,
                        title = stringResource(R.string.circle_radius),
                        snippet = circle.radiusMeters.asRadiusLabel(),
                        tag = MarkerHandleTag(circle.circleId, MarkerHandleType.RESIZE),
                        onClick = {
                            onSelectCircle(circle.circleId)
                            false
                        },
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (BuildConfig.MAPS_API_KEY.isBlank()) {
                    InfoCard(
                        title = stringResource(R.string.map_title),
                        body = stringResource(R.string.maps_api_key_missing),
                    )
                }

                InfoCard(
                    title = stringResource(R.string.map_title),
                    body = stringResource(R.string.map_subtitle),
                    footer = "${stringResource(R.string.saved_count)} ${uiState.savedCircles.size} • ${stringResource(R.string.draft_count)} ${uiState.draftCircles.size}",
                )
            }

            SelectedCircleSheet(
                modifier = Modifier.align(Alignment.BottomCenter),
                selectedCircle = uiState.selectedCircle,
                onClearDrafts = onClearDrafts,
                onClearSaved = onClearSaved,
                onRemoveSelected = onRemoveSelected,
                onSeedSavedCircle = onSeedSavedCircle,
            )
        }
    }
}

@Composable
private fun SelectedCircleSheet(
    modifier: Modifier = Modifier,
    selectedCircle: GeofenceCircle?,
    onClearDrafts: () -> Unit,
    onClearSaved: () -> Unit,
    onRemoveSelected: () -> Unit,
    onSeedSavedCircle: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.selected_circle),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            if (selectedCircle == null) {
                Text(
                    text = stringResource(R.string.no_selection),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = if (selectedCircle.isSaved) {
                                stringResource(R.string.saved_circle)
                            } else {
                                stringResource(R.string.draft_circle)
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "${stringResource(R.string.circle_center)} ${selectedCircle.center.latitude.formatCoordinate()}, ${selectedCircle.center.longitude.formatCoordinate()}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "${stringResource(R.string.circle_radius)} ${selectedCircle.radiusMeters.asRadiusLabel()}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = onClearDrafts,
                ) {
                    Text(stringResource(R.string.clear_draft))
                }
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = onClearSaved,
                ) {
                    Text(stringResource(R.string.clear_saved))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onSeedSavedCircle,
                ) {
                    Text(stringResource(R.string.seed_saved_circle))
                }
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = onRemoveSelected,
                    enabled = selectedCircle != null,
                ) {
                    Text(stringResource(R.string.remove_selected))
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    body: String,
    footer: String? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            footer?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun GeofenceCircle.markerTitle(): String = if (isSaved) "Saved geofence" else "Draft geofence"

private fun Double.asRadiusLabel(): String = "${roundToInt()} m"

private fun Double.formatCoordinate(): String = String.format(Locale.US, "%.5f", this)

private fun GoogleMarker.markerTag(): MarkerHandleTag? = tag as? MarkerHandleTag

private val DEFAULT_CAMERA_POSITION = LatLng(14.5995, 120.9842)

private data class MarkerHandleTag(
    val circleId: Long,
    val type: MarkerHandleType,
)

private enum class MarkerHandleType {
    CENTER,
    RESIZE,
}
