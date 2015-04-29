package com.aconcepcion.geofencemarkerbuilder;

/**
 * This class manages a map circle, with markers
 *
 * Currently there's position marker (in the middle) and resizing marker (in border)
 * Long pressing these markers allow to change circle position or size.
 *
 * @author A-Ar Andrew Concepcion
 *
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by andrewconcepcion on 12/3/14.
 */

public class MapDataPointWrapper {

    public static enum MarkerMoveResult {moved, radiusChange, minRadius, maxRadius, none};
    public static enum MarkerType {move, resize, none}

    private Marker centerMarker;
    private Marker radiusMarker;
    private Circle circle;
    private double radiusMeters;
    private long geofenceCircleId;
    private int count;
    private boolean isEnabled;

    private int minRadiusMeters = -1;
    private int maxRadiusMeters = -1;

    /**
     * Primary constructor
     *
     * @param map
     * @param center center of circle in geo coordinates
     * @param radiusMeters radius of circle in meters
     * @param strokeWidth circle stroke with in pixels
     * @param strokeColor circle stroke color
     * @param fillColor circle fill color
     * @param minRadiusMeters optional - circle min radius in meters (circle will not shrink bellow this, and callback is called when reached)
     * @param maxRadiusMeters optional - circle max radius in meters (circle will not expand above this, and callback is called when reached)
     * @param centerDrawableId drawable ressource id for positioning marker. If not set a default geomarker is used
     * @param radiusDrawableId  drawable ressource id for resizing marker. If not set a default geomarker is used
     * @param moveDrawableAnchorU horizontal anchor for move drawable
     * @param moveDrawableAnchorV vertical anchor for move drawable
     * @param resizeDrawableAnchorU horizontal anchor for resize drawable
     * @param resizeDrawableAnchorV vertical anchor for resize drawable
     */
    public MapDataPointWrapper(Context context, GoogleMap map, boolean isEnabled, LatLng center, int count, int centerDrawableId) {
        this.isEnabled = isEnabled;
        this.count = count;

        centerMarker = map.addMarker(new MarkerOptions()
                .position(center)
                .flat(true)
                .draggable(isEnabled));

        if (centerDrawableId != -1) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) context.getResources().getDrawable(centerDrawableId);
            Bitmap bitmap = bitmapDrawable.getBitmap();
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2, false);
            centerMarker.setIcon(BitmapDescriptorFactory.fromBitmap(scaledBitmap));
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * @return center of circle in geocoordinates
     */
    public LatLng getCenter() {
        return centerMarker.getPosition();
    }

    public long getGeofenceCircleId() { return geofenceCircleId; }

    public void removeArea() {
        centerMarker.remove();
    }

    @Override
    public String toString() {
        return "center: " + getCenter();
    }
}