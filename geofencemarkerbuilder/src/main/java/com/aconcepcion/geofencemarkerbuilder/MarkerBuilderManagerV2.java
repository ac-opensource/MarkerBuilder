package com.aconcepcion.geofencemarkerbuilder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrewconcepcion on 4/28/15.
 */
public class MarkerBuilderManagerV2 implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener {


    private final Context context;
    private final GoogleMap googleMap;
    private final boolean isEnabled;
    private final double radius;
    private final long circleId;
    private final float strokeWidth;
    private final int strokeColor;
    private final int fillColor;
    private final int minRadius;
    private final int maxRadius;
    private final int centerIcon;
    private final Bitmap centerBitmap;
    private final int resizerIcon;
    private final float centerOffsetHorizontal;
    private final float centerOffsetVertical;
    private final CircleManagerListener circleManagerListener;

    private List<GeofenceCircle> areas = new ArrayList<>(1);
    private List<GeofenceCircle> savedPoints = new ArrayList<>(1);
    private List<GeofenceCircle> savedDataPoints = new ArrayList<>(1);

    public static class Builder {
        private Context context;
        private GoogleMap googleMap;
        private boolean isEnabled;
        private double radius = 0;
        private long circleId;
        private int fillColor = Color.HSVToColor(70, new float[]{1, 1, 200});
        private float strokeWidth = 4f;
        private int strokeColor = Color.RED;
        private int minRadius = -1;
        private int maxRadius = -1;
        private int centerIcon = android.R.drawable.ic_menu_mylocation;
        private int resizerIcon = android.R.drawable.ic_menu_mylocation;
        private Bitmap centerBitmap;
        private float centerOffsetHorizontal = 0.5f;
        private float centerOffsetVertical = 0.5f;
        private float resizerOffsetHorizontal = 0.5f;
        private float resizerOffsetVertical = 0.5f;
        private CircleManagerListener circleManagerListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder map(GoogleMap googleMap) {
            this.googleMap = googleMap;
            return this;
        }

        public Builder listener(CircleManagerListener circleManagerListener) {
            this.circleManagerListener = circleManagerListener;
            return this;
        }

        public Builder enabled(boolean isEnabled) {
            this.isEnabled = isEnabled;
            return this;
        }

        public Builder radius(double radius) {
            this.radius = radius;
            return this;
        }

        public Builder circleId(long circleId) {
            this.circleId = circleId;
            return this;
        }

        public Builder strokeWidth(float strokeWidth) {
            this.strokeWidth = strokeWidth;
            return this;
        }

        public Builder strokeColor(int strokeColor) {
            this.strokeColor = strokeColor;
            return this;
        }

        public Builder fillColor(int fillColor) {
            this.fillColor = fillColor;
            return this;
        }

        public Builder minRadius(int minRadius) {
            this.minRadius = minRadius;
            return this;
        }

        public Builder maxRadius(int maxRadius) {
            this.maxRadius = maxRadius;
            return this;
        }

        public Builder centerIcon(int centerIcon) {
            this.centerIcon = centerIcon;
            return this;
        }

        public Builder centerBitmap(Bitmap centerBitmap) {
            this.centerBitmap = centerBitmap;
            return this;
        }

        public Builder resizerIcon(int resizerIcon) {
            this.resizerIcon = resizerIcon;
            return this;
        }

        public Builder centerOffsetHorizontal(float centerOffsetHorizontal) {
            this.centerOffsetHorizontal = centerOffsetHorizontal;
            return this;
        }

        public Builder centerOffsetVertical(float centerOffsetVertical) {
            this.centerOffsetVertical = centerOffsetVertical;
            return this;
        }

        public Builder resizerOffsetHorizontal(float resizerOffsetHorizontal) {
            this.resizerOffsetHorizontal = resizerOffsetHorizontal;
            return this;
        }

        public Builder resizerOffsetVertical(float resizerOffsetVertical) {
            this.resizerOffsetVertical = resizerOffsetVertical;
            return this;
        }
        public MarkerBuilderManagerV2 build(){
            return new MarkerBuilderManagerV2(this);
        }
    }

    private MarkerBuilderManagerV2(Builder b) {
        this.context                =   b.context;
        this.googleMap              =   b.googleMap;
        this.circleManagerListener  =   b.circleManagerListener;
        this.isEnabled              =   b.isEnabled;
        this.radius                 =   b.radius;
        this.circleId               =   b.circleId;
        this.strokeWidth            =   b.strokeWidth;
        this.strokeColor            =   b.strokeColor;
        this.fillColor              =   b.fillColor;
        this.minRadius              =   b.minRadius;
        this.maxRadius              =   b.maxRadius;
        this.centerIcon             =   b.centerIcon;
        this.centerBitmap           =   b.centerBitmap;
        this.resizerIcon            =   b.resizerIcon;
        this.centerOffsetHorizontal =   b.centerOffsetHorizontal;
        this.centerOffsetVertical   =   b.centerOffsetVertical;

        googleMap.setOnMarkerDragListener(this);
        googleMap.setOnMapLongClickListener(this);
        googleMap.setOnMapClickListener(this);
        googleMap.setOnMarkerClickListener(this);
    }

    /**
     * When marker is moved, notify circles
     * The circle containing the marker will execute necessary actions
     *
     * @param marker
     * @return
     */
    private MarkerMoveResultWithCircle onMarkerMoved(Marker marker) {
        GeofenceCircle.MarkerMoveResult result = GeofenceCircle.MarkerMoveResult.none;
        GeofenceCircle affectedDraggableCircle = null;

        ArrayList<GeofenceCircle> allMarkers = new ArrayList<>();
        allMarkers.addAll(areas);
        allMarkers.addAll(savedPoints);
        allMarkers.addAll(savedDataPoints);

        for(int i = 0; i < allMarkers.size(); i++) {
            GeofenceCircle draggableCircle = allMarkers.get(i);
            result = draggableCircle.onMarkerMoved(marker);
            if (result != GeofenceCircle.MarkerMoveResult.none) {
                affectedDraggableCircle = draggableCircle;
                break;
            }
        }

        return new MarkerMoveResultWithCircle(result, affectedDraggableCircle);
    }

    /**
     * Clear current circles plotted on the map
     */
    public void clearCircles() {

        for(int i = 0; i < areas.size(); i++) {
            getCircles().get(i).removeArea();
        }
    }

    public void removeCircle(long geofenceCircleId) {

        for(int i = 0; i < savedPoints.size(); i++) {

            GeofenceCircle savedCircle = savedPoints.get(i);
            if(savedCircle.getCircleId() == geofenceCircleId){
                savedCircle.removeArea();
                break;
            }
        }
    }

    public void clearSavedCircles() {

        for(int i = 0; i < savedPoints.size(); i++) {
            GeofenceCircle oldCircle = savedPoints.get(i);
            oldCircle.removeArea();
        }
        savedPoints.clear();
    }

    public void markThis(LatLng point, boolean isSaved) {

        clearCircles();
        double initRadiusMetersFinal;

        if(radius == 0) {
            Point screenCenterPoint = googleMap.getProjection().toScreenLocation(point);
            LatLng radiusLatLng = googleMap.getProjection().fromScreenLocation(new Point(screenCenterPoint.x + (int)radius, screenCenterPoint.y));
            initRadiusMetersFinal = MarkerAreasUtils.toRadiusMeters(point, radiusLatLng);
        } else {
            initRadiusMetersFinal = radius;
        }

        GeofenceCircle geofenceCircle = new GeofenceCircle.Builder(context)
                .map(googleMap)
                .enabled(isEnabled)
                .radius(initRadiusMetersFinal)
                .circleId(circleId)
                .strokeWidth(strokeWidth)
                .strokeColor(strokeColor)
                .fillColor(fillColor)
                .minRadius(minRadius)
                .maxRadius(maxRadius)
                .centerIcon(centerIcon)
                .centerBitmap(centerBitmap)
                .resizerIcon(resizerIcon)
                .centerOffsetHorizontal(centerOffsetHorizontal)
                .centerOffsetVertical(centerOffsetVertical)
                .center(point)
                .build();

        if(!isSaved) {
            areas.add(geofenceCircle);
        } else {
            savedPoints.add(geofenceCircle);
        }

        if(circleManagerListener != null) circleManagerListener.onCreateCircle(geofenceCircle);
    }

    public void plotPoints(LatLng point, Integer radius, long circularGeofenceId, int fillColor) {

        GeofenceCircle circle = addSavedPoint(point, radius, circularGeofenceId, fillColor);

        if(circleManagerListener != null) circleManagerListener.onInitCreateCircle(circle);

    }

    /**
     * adds a point to non clearable circle
     *
     * @param savedPoint a successfully saved point returned by the server
     */
    public GeofenceCircle addSavedPoint(LatLng savedPoint, Integer radius, long geofenceCircleId, int fillColor) {

        GeofenceCircle circle = new GeofenceCircle.Builder(context)
                .map(googleMap)
                .enabled(isEnabled)
                .radius(radius)
                .circleId(circleId)
                .circleId(circleId)
                .strokeWidth(strokeWidth)
                .strokeColor(strokeColor)
                .fillColor(fillColor)
                .minRadius(minRadius)
                .maxRadius(maxRadius)
                .centerIcon(centerIcon)
                .centerBitmap(centerBitmap)
                .resizerIcon(resizerIcon)
                .centerOffsetHorizontal(centerOffsetHorizontal)
                .centerOffsetVertical(centerOffsetVertical)
                .center(savedPoint)
                .build();

        savedPoints.add(circle);
        return circle;
    }

    public List<GeofenceCircle> getCircles() {
        return areas;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        markThis(latLng, false);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if(circleManagerListener == null) return false;
        MarkerMoveResultWithCircle result = onMarkerMoved(marker);
        circleManagerListener.onCircleMarkerClick(result.circle);
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        MarkerMoveResultWithCircle result = onMarkerMoved(marker);
        if(circleManagerListener == null) return;
        switch (result.markerMoveResult) {
            case minRadius: {
                circleManagerListener.onMinRadius(result.circle);
                break;
            }
            case maxRadius: {
                circleManagerListener.onMaxRadius(result.circle);
                break;
            }
            case radiusChange: {
                circleManagerListener.onResizeCircleStart(result.circle);
                break;
            }
            case moved: {
                circleManagerListener.onMoveCircleStart(result.circle);
                break;
            }
            default: break;
        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        MarkerMoveResultWithCircle result = onMarkerMoved(marker);
        if(circleManagerListener == null) return;
        switch (result.markerMoveResult) {
            case minRadius: {
                circleManagerListener.onMinRadius(result.circle);
                break;
            }
            case maxRadius: {
                circleManagerListener.onMaxRadius(result.circle);
                break;
            }
            default: break;
        }
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        MarkerMoveResultWithCircle result = onMarkerMoved(marker);
        if(circleManagerListener == null) return;
        switch (result.markerMoveResult) {
            case minRadius: {
                circleManagerListener.onMinRadius(result.circle);
                break;
            }
            case maxRadius: {
                circleManagerListener.onMaxRadius(result.circle);
                break;
            }
            case radiusChange: {
                circleManagerListener.onResizeCircleEnd(result.circle);
                break;
            }
            case moved: {
                circleManagerListener.onMoveCircleEnd(result.circle);
                break;
            }
            default: break;
        }
    }
}
