package com.lattis.data.entity.response.ride

import com.google.gson.annotations.SerializedName

class UpdateTripDataResponse {

    @SerializedName("duration")
    val duration: Double = 0.toDouble()

    @SerializedName("charge_for_duration")
    val charge_for_duration: Float = 0.toFloat()

    @SerializedName("currency")
    val currency: String? = null

    @SerializedName("bike_battery_level")
    val bike_battery_level: String? = null


    @SerializedName("ended_trip")
    var updateTripEndedResponse: UpdateTripEndedResponse? = null
        internal set


}
