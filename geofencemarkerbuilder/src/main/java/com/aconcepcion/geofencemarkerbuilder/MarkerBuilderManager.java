package com.aconcepcion.geofencemarkerbuilder;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;



/**
 * This class manages the map areas
 *
 * On long click on any empty area of the map, a new area will be created
 *
 * The areas can be moved or resized using markers
 *
 * @author aconcepcion
 *
 * Based on functionality of Google APIs v19, com.example.mapdemo.CircleDemoActivity
 *
 */
public class MarkerBuilderManager implements GoogleMap.OnMarkerClickListener, OnMarkerDragListener, OnMapLongClickListener, GoogleMap.OnMapClickListener {

    private static int DEFAULT_FILL_COLOR = 0xff0000ff;
    private static int DEFAULT_STROKE_COLOR = 0xff000000;
    private static int DEFAULT_STROKE_WIDTH = 1;
    private GeofenceCircle geofenceCircle;

    private List<GeofenceCircle> areas = new ArrayList<>(1);
    private List<GeofenceCircle> savedPoints = new ArrayList<>(1);
    private List<GeofenceCircle> savedDataPoints = new ArrayList<>(1);
    private GoogleMap map;

    private int fillColor = DEFAULT_FILL_COLOR;
    private int strokeWidth = DEFAULT_STROKE_WIDTH;
    private int strokeColor = DEFAULT_STROKE_COLOR;

    private int minRadiusMeters = -1;
    private int maxRadiusMeters = -1;

    private MarkerAreaMeasure initRadius;

    private CircleManagerListener circleManagerListener;

    private int moveDrawableId = -1;
    private int radiusDrawableId = -1;

    private float moveDrawableAnchorU;
    private float moveDrawableAnchorV;
    private float resizeDrawableAnchorU;
    private float resizeDrawableAnchorV;

    private Context context;

    private boolean isNew = true;
    private boolean isEnabled = true;

    private Bitmap loadedImage;

    public Bitmap getLoadedImage() {
        return loadedImage;
    }

    public void setLoadedImage(Bitmap loadedImage) {
        this.loadedImage = loadedImage;
    }

    /**
     * Primary constructor
     *
     * @param map
     * @param strokeWidth circle stroke with in pixels
     * @param strokeColor circle stroke color
     * @param fillColor circle fill color
     * @param moveDrawableId
     * @param moveDrawableId drawable resource id for positioning marker. If not set a default geo marker is used
     * @param resizeDrawableId  drawable resource id for resizing marker. If not set a default geo marker is used
     * @param moveDrawableAnchorU horizontal anchor for move drawable
     * @param moveDrawableAnchorV vertical anchor for move drawable
     * @param resizeDrawableAnchorU horizontal anchor for resize drawable
     * @param resizeDrawableAnchorV vertical anchor for resize drawable
     * @param initRadius init radius for all circles, currently supported pixels (constant in all zoom levels) or meters
     * @param circleManagerListener listener for circle events
     *
     */

    public MarkerBuilderManager(GoogleMap map,
                                int strokeWidth, int strokeColor, int circleColor,      //styling
                                int moveDrawableId, int resizeDrawableId,               //custom drawables for move and resize icons
                                float moveDrawableAnchorU,                              //sets anchor point of move / resize drawable in the middle
                                float moveDrawableAnchorV,
                                float resizeDrawableAnchorU,
                                float resizeDrawableAnchorV,
                                MarkerAreaMeasure initRadius,                              //circles will start with 100 pixels (independent of zoom level)
                                CircleManagerListener circleManagerListener,            //listener for all circle events
                                Context context,
                                boolean isNew,
                                boolean isEnabled) {

        this.map = map;
        this.circleManagerListener = circleManagerListener;
        this.strokeWidth = strokeWidth;
        this.strokeColor = strokeColor;
        this.fillColor = circleColor;

        this.moveDrawableId = moveDrawableId;
        this.radiusDrawableId = resizeDrawableId;

        this.moveDrawableAnchorU = moveDrawableAnchorU;
        this.moveDrawableAnchorV = moveDrawableAnchorV;
        this.resizeDrawableAnchorU = resizeDrawableAnchorU;
        this.resizeDrawableAnchorV = resizeDrawableAnchorV;

        this.initRadius = initRadius;

        this.context = context;

        map.setOnMarkerDragListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);

        this.isNew = isNew;
        this.isEnabled = isEnabled;
    }

    public MarkerBuilderManager(Context context, GoogleMap map, CircleManagerListener circleManagerListener, boolean isNew, boolean isEnabled) {
        this.context = context;
        this.map = map;
        this.circleManagerListener = circleManagerListener;

        map.setOnMarkerDragListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
    }

    public MarkerBuilderManager(Context context, GoogleMap map) {
        this.context = context;
        this.map = map;

        map.setOnMarkerDragListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
    }

    public MarkerBuilderManager(Context context, GoogleMap map, GeofenceCircle geofenceCircle) {
        this.context = context;
        this.map = map;
        this.geofenceCircle = geofenceCircle;

        map.setOnMarkerDragListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
    }


    /**
     * Convenience constructor
     *
     * Will pass -1 as move and resize drawable resource id, with means we will use default geo markers
     *
     * @params see primary constructor
     */
//	public MapAreaManager(GoogleMap map, int strokeWidth, int strokeColor, int circleColor,
//			MapAreaMeasure initRadius, CircleManagerListener circleManagerListener, Context context, WSSpiceManager spiceManager, AsyncTaskCompleteListener listener) {
//
//		this(map, strokeWidth, strokeColor, circleColor, -1, -1, initRadius, circleManagerListener, context, spiceManager, listener);
//	}

    /**
     * Convenience constructor
     *
     * Uses default values for marker's drawable anchors
     *
     * @params see primary constructor
     */
//	public MapAreaManager(GoogleMap map, int strokeWidth, int strokeColor, int circleColor,
//			int moveDrawableId, int radiusDrawableId,
//            MapAreaMeasure initRadius, CircleManagerListener circleManagerListener, Context context, WSSpiceManager spiceManager, AsyncTaskCompleteListener listener) {
//
//        this(map, strokeWidth, strokeColor, circleColor, moveDrawableId, radiusDrawableId, 0.5f, 1f, 0.5f, 1f, initRadius, circleManagerListener, context, spiceManager, listener);
//	}

    public List<GeofenceCircle> getCircles() {
        return areas;
    }
    public List<GeofenceCircle> getSavedPoints() {
        return savedPoints;
    }

    /**
     * Set min radius in meters. The circles will shrink bellow this, and onMinRadius will be called when reached
     * @param minRadius
     */
    public void setMinRadius(int minRadius) {
        this.minRadiusMeters = minRadius;
    }

    /**
     * Set min radius in meters. The circles will expand above this, and onMaxRadius will be called when reached
     * @param minRadius
     */
    public void setMaxRadius(int maxRadius) {
        this.maxRadiusMeters = maxRadius;
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

    public void add(GeofenceCircle draggableCircle) {
        areas.add(draggableCircle);
    }

    /**
     * Wrapper for result of gesture with affected circle
     */
    private class MarkerMoveResultWithCircle {
        GeofenceCircle.MarkerMoveResult markerMoveResult;
        GeofenceCircle circle;
        MapDataPointWrapper dataPointWrapper;

        public MarkerMoveResultWithCircle(GeofenceCircle.MarkerMoveResult markerMoveResult, GeofenceCircle circle) {
            this.markerMoveResult = markerMoveResult;
            this.circle = circle;
        }

        public MarkerMoveResultWithCircle(GeofenceCircle.MarkerMoveResult markerMoveResult, MapDataPointWrapper dataPointWrapper) {
            this.markerMoveResult = markerMoveResult;
            this.dataPointWrapper = dataPointWrapper;
        }
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

    @Override
    public void onMapLongClick(LatLng point) {

    }

    @Override
    public void onMapClick(LatLng point) {
        markThis(point, false);
    }

    public void setMarkerIcon(Bitmap loadedImage) {
        setLoadedImage(loadedImage);
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

    public void clearSavedDataPoints() {

        for(int i = 0; i < savedDataPoints.size(); i++) {
            GeofenceCircle oldCircle = savedDataPoints.get(i);
            oldCircle.removeArea();
        }
        savedDataPoints.clear();
    }

    public void markThis(LatLng point, boolean isSaved) {

        if(isNew) {
            map.clear();
        }

        double initRadiusMetersFinal;

        clearCircles();

//        if (initRadius.unit == MapAreaMeasure.Unit.meters) { //init with meters radius
//
//            initRadiusMetersFinal = initRadius.value;
//
//        } else { //init with pixels radius
//            Point screenCenterPoint = map.getProjection().toScreenLocation(point);
//            LatLng radiusLatLng = map.getProjection().fromScreenLocation(new Point(screenCenterPoint.x + 100, screenCenterPoint.y));
//
//            initRadiusMetersFinal = MapAreasUtils.toRadiusMeters(point, radiusLatLng);
//        }

        if(geofenceCircle != null) {
            geofenceCircle.setCenter(point);
        } else {
            geofenceCircle = new GeofenceCircle.Builder(context)
                .map(map)
                .enabled(isEnabled)
                .center(point)
                .circleId(0)
                .centerBitmap(loadedImage)
                .build();
        }

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

    public void plotDataPoints(LatLng point, int count, Boolean shouldExpand, ArrayList<Object> locationData, int moveDrawableId) {

    }

    /**
     * adds a point to non clearable circle
     *
     * @param savedPoint a successfully saved point returned by the server
     */
    public GeofenceCircle addSavedPoint(LatLng savedPoint, Integer radius, long geofenceCircleId, int fillColor) {


        GeofenceCircle circle = new GeofenceCircle.Builder(context)
                .map(map)
                .enabled(isEnabled)
                .center(savedPoint)
                .circleId(geofenceCircleId)
                .centerBitmap(loadedImage)
                .build();
        savedPoints.add(circle);
        return circle;
    }



}
