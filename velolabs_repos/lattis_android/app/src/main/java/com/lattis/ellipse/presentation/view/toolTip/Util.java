
package com.lattis.ellipse.presentation.view.toolTip;

import android.content.res.Resources;
import android.graphics.RectF;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Tooltip utils
 */
final class Util {

    public static RectF calculateRectOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new RectF(location[0], location[1], location[0] + view.getMeasuredWidth(), location[1] + view.getMeasuredHeight());
    }

    public static RectF calculateRectInWindow(View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        return new RectF(location[0], location[1], location[0] + view.getMeasuredWidth(), location[1] + view.getMeasuredHeight());
    }

    public static float pxToDp(float px) {
        return px / Resources.getSystem().getDisplayMetrics().density;
    }

    public static float dpToPx(float dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }

    public static int gravityToArrowDirection(int gravity) {
        switch (gravity) {
            case Gravity.START:
                return Gravity.END;
            case Gravity.TOP:
                return Gravity.BOTTOM;
            case Gravity.END:
                return Gravity.START;
            case Gravity.BOTTOM:
                return Gravity.TOP;
            default:
                return gravity;
        }
    }

    public static void removeOnGlobalLayoutListener(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        } else {
            //noinspection deprecation
            view.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        }
    }
}
