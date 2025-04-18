package com.lattis.lattis.presentation.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.annotation.NonNull

object SymbolGenerator {

    /**
     * Generate a Bitmap from an Android SDK View.
     *
     * @param view the View to be drawn to a Bitmap
     * @return the generated bitmap
     */
    fun generate(@NonNull view: View): Bitmap {
        val measureSpec: Int = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(measureSpec, measureSpec)
        val measuredWidth: Int = view.getMeasuredWidth()
        val measuredHeight: Int = view.getMeasuredHeight()
        view.layout(0, 0, measuredWidth, measuredHeight)
        val bitmap: Bitmap =
            Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}