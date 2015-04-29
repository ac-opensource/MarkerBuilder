package com.aconcepcion.geofencemarkerbuilder;

/**
 * Created by andrewconcepcion on 4/28/15.
 */
public class MarkerMoveResultWithCircle {
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