package com.aconcepcion.geofencemarkerbuilder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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

/**
 * Created by andrewconcepcion on 4/20/15.
 */
public class GeofenceCircle {
//    private final int size;

    public static final String GEOFENCE_WRAPPER = "geofence";
    public static final String DATA_POINT_WRAPPER = "dataPoint";

    public long getCircleId() {
        return circleId;
    }

    public static enum MarkerMoveResult {moved, radiusChange, minRadius, maxRadius, none;};
    public static enum MarkerType {move, resize, none;}


    private static Bitmap centerBitmapCache;

    private Context context;
    private GoogleMap map;
    private boolean isEnabled;
    private LatLng center;
    private double radius;
    private long circleId;
    private float strokeWidth;
    private int strokeColor;
    private int fillColor;
    private int minRadius;
    private int maxRadius;
    private int centerIcon;
    private Bitmap centerBitmap;
    private int resizerIcon;
    private float centerOffsetHorizontal;
    private float centerOffsetVertical;

    private Marker centerMarker;
    private Marker resizerMarker;
    private Circle circle;


    public static class Builder {


        private static int DEFAULT_FILL_COLOR = 0xff0000ff;
        private static int DEFAULT_STROKE_COLOR = 0xff000000;
        private static int DEFAULT_STROKE_WIDTH = 1;


        //required
//        private final int size;

        //optional
        private Context context;
        private GoogleMap map;
        private boolean isEnabled;
        private LatLng center;
        private double radius = new MarkerAreaMeasure(200, MarkerAreaMeasure.Unit.meters).value;
        private long circleId;
        private int fillColor = Color.HSVToColor(70, new float[] {1, 1, 200});
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

        private Marker centerMarker;
        private Marker resizerMarker;
        private Circle circle;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder map(GoogleMap map) {
            this.map = map;
            return this;
        }

        public Builder enabled(boolean isEnabled) {
            this.isEnabled = isEnabled;
            return this;
        }

        public Builder center(LatLng center) {
            this.center = center;
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

        public Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
            Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
            Canvas canvas = new Canvas(bmOverlay);
            canvas.drawBitmap(bmp1, new Matrix(), null);

            int offset = MetricsUtils.convertDIPsToPixels(context, 5);

            canvas.drawBitmap(bmp2, offset, offset, null);
            return bmOverlay;
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

        public GeofenceCircle build(){

            centerMarker = map.addMarker(new MarkerOptions()
                    .position(center)
                    .flat(true)
                    .anchor(centerOffsetHorizontal, centerOffsetVertical)
                    .draggable(isEnabled));


            resizerMarker = map.addMarker(new MarkerOptions()
                    .position(MarkerAreasUtils.toRadiusLatLng(center, radius))
                    .anchor(resizerOffsetHorizontal, resizerOffsetVertical)
                    .draggable(isEnabled));

            if (resizerIcon != 0 && resizerIcon != -1) {

                int px = 120;
                Bitmap mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(mDotMarkerBitmap);
                Drawable shape = ResourcesCompat.getDrawable(context.getResources(), resizerIcon, null);
                shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
                shape.draw(canvas);

                resizerMarker.setIcon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap));
            }



            if (centerIcon != -1) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) context.getResources().getDrawable(centerIcon);
                Bitmap bitmap = bitmapDrawable.getBitmap();
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth() / 2), (int)(bitmap.getHeight() / 2), false);
                //            centerMarker.setIcon(BitmapDescriptorFactory.fromBitmap(scaledBitmap));

                int imageSize = (MetricsUtils.convertDIPsToPixels(context, 30f));

                if (centerBitmap == null) {
                    if(centerBitmapCache == null) {
                        centerBitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.RGB_565);
                        Drawable drawable = context.getResources().getDrawable(android.R.drawable.sym_def_app_icon);
                        Canvas canvas = new Canvas(centerBitmap);
                        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        drawable.draw(canvas);
                        centerBitmapCache = centerBitmap;
                    } else {
                        centerBitmap = centerBitmapCache;
                    }

                }

//                centerMarker.setIcon(BitmapDescriptorFactory.fromBitmap(overlay(scaledBitmap, getRoundedCornerBitmap(centerBitmap, imageSize))));
            }

            circle = map.addCircle(new CircleOptions()
                    .center(center)
                    .radius(radius)
                    .strokeWidth(strokeWidth)
                    .strokeColor(strokeColor)
                    .fillColor(fillColor));

            return new GeofenceCircle(this);
        }
    }

    private GeofenceCircle(Builder b) {
        this.context                =   b.context;
        this.map                    =   b.map;
        this.isEnabled              =   b.isEnabled;
        this.center                 =   b.center;
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
        this.centerMarker           =   b.centerMarker;
        this.resizerMarker          =   b.resizerMarker;
        this.circle                 =   b.circle;

    }

    public void setCenter(LatLng center) {
        centerMarker.setPosition(center);
        onCenterUpdated(center);
    }

    public void removeArea() {
        if(resizerMarker != null) resizerMarker.remove();
        if(centerMarker != null) centerMarker.remove();
        if(circle != null) circle.remove();
    }


    /**
     * This modifies circle according to marker's type and position
     *
     * if the marker is position marker (from this circle), the circle will be moved according to marker's position
     * if the marker is resizing marker (from this circle), the circle will be resized according to marker's position
     *
     * If the marker is not in this circle (it's not the position or resizing marker) no action will be done
     *
     * @param marker
     *
     * @return flag indicating which action was done.
     * When the marker is not in this circle returned action is MarkerMoveResult.none
     */
    public MarkerMoveResult onMarkerMoved(Marker marker) {
        if (marker.equals(centerMarker)) {
            onCenterUpdated(marker.getPosition());
            return MarkerMoveResult.moved;
        }

        if (marker.equals(resizerMarker)) {
            double newRadius = MarkerAreasUtils.toRadiusMeters(centerMarker.getPosition(), marker.getPosition());

            if (minRadius != -1 && newRadius < minRadius) {
                return MarkerMoveResult.minRadius;

            } else if (maxRadius != -1 && newRadius > maxRadius) {
                return MarkerMoveResult.maxRadius;

            } else {
                setRadius(newRadius);

                return MarkerMoveResult.radiusChange;
            }

        }
        return MarkerMoveResult.none;
    }



    /**
     * Called after update position of center marker, to update the circle and the radius marker
     * @param center
     */
    public void onCenterUpdated(LatLng center) {
        if(circle != null) circle.setCenter(center);
        if(resizerMarker != null) resizerMarker.setPosition(MarkerAreasUtils.toRadiusLatLng(center, radius));
    }

    /**
     * Set the radius of circle
     * the map circle will be updated immediately
     *
     * @param radius
     */
    public void setRadius(double radius) {
        this.radius = radius;
        circle.setRadius(radius);
    }

    public LatLng getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }



    @Override
    public String toString() {
        return "center: " + getCenter() + " radius: " + getRadius();
    }


}