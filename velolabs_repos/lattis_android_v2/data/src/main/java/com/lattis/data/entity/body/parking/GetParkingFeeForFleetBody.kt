package com.lattis.data.entity.body.parking

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.Location

class GetParkingFeeForFleetBody(
    location: Location,
    fleet_id: Int
) {
    @SerializedName("latitude")
    private val latitude: Double

    @SerializedName("longitude")
    private val longitude: Double

    @SerializedName("accuracy")
    private var accuracy = 0.0

    @SerializedName("fleet_id")
    private val fleet_id: Int

    init {
        latitude = location.latitude
        longitude = location.longitude
        if (location.hasAccuracy()) {
            accuracy = location.accuracy.toDouble()
        } else {
            accuracy = 0.0
        }
        this.fleet_id = fleet_id
    }
}