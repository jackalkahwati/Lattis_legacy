package com.lattis.domain.models

import org.parceler.Parcel

@Parcel
class Location @JvmOverloads constructor(var latitude: Double = 0.0, var longitude: Double = 0.0) {
    var accuracy: Float = 0.toFloat()
    var time: Long = 0
    private var hasSpeed: Boolean = false
    private var hasAccuracy: Boolean = false
    var speed: Float = 0.toFloat()
    var provider: String? = null

    fun hasSpeed(): Boolean {
        return hasSpeed
    }

    fun setHasSpeed(hasSpeed: Boolean) {
        this.hasSpeed = hasSpeed
    }

    fun hasAccuracy(): Boolean {
        return hasAccuracy
    }

    fun setHasAccuracy(hasAccuary: Boolean) {
        this.hasAccuracy = hasAccuary
    }


    override fun toString(): String {
        return latitude.toBigDecimal().toPlainString() + "," + longitude.toBigDecimal().toPlainString()
    }
}
