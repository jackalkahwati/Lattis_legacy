package com.lattis.data.entity.body.ride

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.body.v2.BikeHubPortBody
import com.lattis.domain.models.Location

class StartRideBody(
    @field:SerializedName("bike_id") private val bike_id: Int,
    location: Location,
    device_token:String
) {

    @SerializedName("latitude")
    private val latitude: Double

    @SerializedName("longitude")
    private val longitude: Double

    @SerializedName("device_token")
    private val device_token: String

    init {
        latitude = location.latitude
        longitude = location.longitude
        this.device_token = device_token
    }
}