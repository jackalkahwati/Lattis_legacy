package io.lattis.operator.utils

import android.content.Context

object GeneralUtils {

    fun dpToPx(context: Context,dp: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}