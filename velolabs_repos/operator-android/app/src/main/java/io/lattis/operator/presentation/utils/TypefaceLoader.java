package io.lattis.operator.presentation.utils;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by raverat on 3/7/17.
 */

public class TypefaceLoader {

    private final static Map<String, Typeface> TYPEFACE_MAP;

    static {
        TYPEFACE_MAP = new HashMap<>();
    }

    public static Typeface getTypeface(Context context, String path) {
        Typeface tf = TYPEFACE_MAP.get(path);

        if (tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), path);
                TYPEFACE_MAP.put(path, tf);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return tf;
    }

}
