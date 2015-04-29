package com.aconcepcion.geofencemarkerbuilder;

/**
 * Created by andrewconcepcion on 4/28/15.
 */
public interface CircleManagerListener {

    /**
     * Called when location was initially loaded
     * @param draggableCircle created circle
     */
    void onInitCreateCircle(GeofenceCircle draggableCircle);

    /**
     * Called when a circle was placed on the map
     * @param draggableCircle created circle
     */
    void onCircleMarkerClick(GeofenceCircle draggableCircle);

    void onCreateCircle(GeofenceCircle draggableCircle);

    /**
     * Called when resizing gesture finishes (user lifts the finger)
     * @param draggableCircle resized circle
     */
    void onResizeCircleEnd(GeofenceCircle draggableCircle);

    /**
     * Called when move gesture finishes (user lifts the finger)
     * @param draggableCircle move circle
     */
    void onMoveCircleEnd(GeofenceCircle draggableCircle);

    /**
     * Called when move gesture starts (user long presses the position marker)
     * @param draggableCircle circle about to be moved
     */
    void onMoveCircleStart(GeofenceCircle draggableCircle);

    /**
     * Called when resize gesture starts (user long presses the resizing marker)
     * @param draggableCircle circle about to be resized
     */
    void onResizeCircleStart(GeofenceCircle draggableCircle);

    /**
     * Called when the circle reaches the min possible radius (meters), if it was initialized with a min radius value
     * This happens during resizing gesture
     * Reducing size is automatically blocked when reached this value - no extra action required for this
     *
     * @param draggableCircle circle
     */
    void onMinRadius(GeofenceCircle draggableCircle);

    /**
     * Called when the circle reaches the max possible radius (meters), if it was initialized with a max radius value
     * This happens during resizing gesture
     * Increasing size is automatically blocked when reached this value - no extra action required for this
     *
     * @param draggableCircle circle
     */

    void onMaxRadius(GeofenceCircle draggableCircle);
}