package com.lattis.lattis.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach

object Extensions {

    fun ViewGroup.deepForEach(function: View.() -> Unit) {
        this.forEach { child ->
            child.function()
            if (child is ViewGroup) {
                child.deepForEach(function)
            }
        }
    }
}