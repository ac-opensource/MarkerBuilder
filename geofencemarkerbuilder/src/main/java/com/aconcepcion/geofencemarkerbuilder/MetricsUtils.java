package com.aconcepcion.geofencemarkerbuilder;

import android.content.Context;
import android.util.TypedValue;

/**
 * @author A-Ar Andrew Concepcion
 */
public class MetricsUtils {

    public static int convertDIPsToPixels (Context context, float DIPs) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DIPs, context.getResources().getDisplayMetrics());
    }
}
