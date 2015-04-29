package com.aconcepcion.geofencemarkerbuilder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


/**
 * This class manages a map circle, with markers
 * 
 * Currently there's position marker (in the middle) and resizing marker (in border)
 * Long pressing these markers allow to change circle position or size.
 *
 * @author A-Ar Andrew Concepcion
 * 
 */


public class MarkerAreaWrapper {

    public static final String GEOFENCE_WRAPPER = "geofence";
    public static final String DATA_POINT_WRAPPER = "dataPoint";

    private Marker centerMarker;

    private Marker radiusMarker;
    private Circle circle;

    private static Bitmap childBitmap;

    private double radiusMeters;
    private long geofenceCircleId;
    private boolean isEnabled;
    private int fillColor;

    private String mapAreaType;
    private int count;
    private boolean shouldExpand;
    private ArrayList<Object> locationData;

    private int minRadiusMeters = -1;
    private int maxRadiusMeters = -1;


    private Context context;


    public int getFillColor() {
        return fillColor;
    }

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
    public MarkerAreaWrapper(Context context, GoogleMap map, boolean isEnabled, LatLng center, double radiusMeters, long geofenceCircleId, float strokeWidth, int strokeColor, int fillColor, int minRadiusMeters, int maxRadiusMeters,
                             int centerDrawableId, Bitmap childProfileImage, int radiusDrawableId, float moveDrawableAnchorU, float moveDrawableAnchorV, float resizeDrawableAnchorU, float resizeDrawableAnchorV) {
        this.isEnabled = isEnabled;
        this.radiusMeters = radiusMeters;
        this.minRadiusMeters = minRadiusMeters;
        this.maxRadiusMeters = maxRadiusMeters;
        this.geofenceCircleId = geofenceCircleId;
        this.mapAreaType = GEOFENCE_WRAPPER;
        this.fillColor = fillColor;

        this.context = context;

        radiusMarker = map.addMarker(new MarkerOptions()
                .position(MarkerAreasUtils.toRadiusLatLng(center, radiusMeters))
                .anchor(resizeDrawableAnchorU, resizeDrawableAnchorV)
                .draggable(isEnabled));
        radiusMarker.setVisible(isEnabled);

        centerMarker = map.addMarker(new MarkerOptions()
                .position(center)
                .flat(true)
                .anchor(moveDrawableAnchorU, moveDrawableAnchorV)
                .draggable(isEnabled));

        if (radiusDrawableId != 0 && radiusDrawableId != -1) {

            int px = context.getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
            Bitmap mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mDotMarkerBitmap);
            Drawable shape = ResourcesCompat.getDrawable(context.getResources(), radiusDrawableId, null);
            shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
            shape.draw(canvas);

            radiusMarker.setIcon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap));
        }

        if (centerDrawableId != -1) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) context.getResources().getDrawable(centerDrawableId);
            Bitmap bitmap = bitmapDrawable.getBitmap();
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth() / 2), (int)(bitmap.getHeight() / 2), false);
            //            centerMarker.setIcon(BitmapDescriptorFactory.fromBitmap(scaledBitmap));

            int imageSize = (MetricsUtils.convertDIPsToPixels(context, 30f));

            if (childProfileImage == null) {
                if(childBitmap == null) {
                    childProfileImage = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
                    Drawable drawable = context.getResources().getDrawable(android.R.drawable.sym_contact_card);
                    Canvas canvas = new Canvas(childProfileImage);
                    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    drawable.draw(canvas);
                    childBitmap = childProfileImage;
                } else {
                    childProfileImage = childBitmap;
                }

            }

            centerMarker.setIcon(BitmapDescriptorFactory.fromBitmap(overlay(scaledBitmap, getRoundedCornerBitmap(childProfileImage, imageSize))));
        }

        circle = map.addCircle(new CircleOptions()
                .center(center)
                .radius(radiusMeters)
                .strokeWidth(strokeWidth)
                .strokeColor(strokeColor)
                .fillColor(fillColor));
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(pixels, pixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, pixels, pixels);
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(Bitmap.createScaledBitmap(bitmap, pixels, pixels, false), rect, rect, paint);

        return output;
    }

    public Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);

        int offset = MetricsUtils.convertDIPsToPixels(context, 5);

        canvas.drawBitmap(bmp2, offset, offset, null);
        return bmOverlay;
    }


    public MarkerAreaWrapper(Context context, GoogleMap map, boolean isEnabled, LatLng center, int count, Boolean shouldExpand, ArrayList<Object> locationData, int centerDrawableId) {
        this.isEnabled = isEnabled;
        this.count = count;
        this.mapAreaType = DATA_POINT_WRAPPER;
        this.shouldExpand = shouldExpand;
        this.locationData = locationData;

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
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
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

    public Marker getCenterMarker() {
        return centerMarker;
    }

    public long getGeofenceCircleId() { return geofenceCircleId; }

    /**
     * @return radius of circle in meters
     */
    public double getRadius() {
        return radiusMeters;
    }

    public String getMapAreaType() {
        return mapAreaType;
    }

    public void setMapAreaType(String mapAreaType) {
        this.mapAreaType = mapAreaType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isShouldExpand() {
        return shouldExpand;
    }

    public void setShouldExpand(boolean shouldExpand) {
        this.shouldExpand = shouldExpand;
    }

    public ArrayList<Object> getLocationData() {
        return locationData;
    }

    public void setLocationData(ArrayList<Object> locationData) {
        this.locationData = locationData;
    }

    public void setStokeWidth(float strokeWidth) {
        circle.setStrokeWidth(strokeWidth);
    }

    public void setStokeColor(int strokeColor) {
        circle.setStrokeColor(strokeColor);
    }

    public void setFillColor(int fillColor) {
        circle.setFillColor(fillColor);
    }

    public void setCenter(LatLng center) {
        centerMarker.setPosition(center);
        onCenterUpdated(center);
    }

    public void removeArea() {
        if(radiusMarker != null) radiusMarker.remove();
        if(centerMarker != null) centerMarker.remove();
        if(circle != null) circle.remove();
    }

    public void removeDataPoint() {
        if(centerMarker != null) centerMarker.remove();
    }

    /**
     * Called after update position of center marker, to update the circle and the radius marker
     * @param center
     */
    public void onCenterUpdated(LatLng center) {
        if(circle != null) circle.setCenter(center);
        if(radiusMarker != null) radiusMarker.setPosition(MarkerAreasUtils.toRadiusLatLng(center, radiusMeters));
    }

    /**
     * Set the radius of circle
     * the map circle will be updated immediately
     *
     * @param radiusMeters
     */
    public void setRadius(double radiusMeters) {
        this.radiusMeters = radiusMeters;
        circle.setRadius(radiusMeters);
    }

    @Override
    public String toString() {
        return "center: " + getCenter() + " radius: " + getRadius() + " type: " + getMapAreaType() + " count: " + getCount();
    }
}