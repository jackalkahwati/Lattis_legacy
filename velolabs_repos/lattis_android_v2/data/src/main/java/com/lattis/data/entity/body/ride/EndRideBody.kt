package com.lattis.data.entity.body.ride

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.Location

class EndRideBody(
    @field:SerializedName("trip_id")
    private val trip_id: Int,

    location: Location?,
    parkingId: Int?,
    imageURL: String?,
    isBikeDamage: Boolean,
    lock_battery: Int?,
    bike_battery: Int?
) {

    @SerializedName("latitude")
    private val latitude: Double?

    @SerializedName("longitude")
    private val longitude: Double?

    @SerializedName("parking_spot_id")
    private var parking_spot_id: Int? = null

    @SerializedName("parking_image")
    private val parking_image: String?

    @SerializedName("bike_damaged")
    private val isBikeDamage: Boolean

    @SerializedName("bike_battery_level")
    private var bike_battery_level: Int? = null

    @SerializedName("lock_battery_level")
    private var lock_battery_level: Int? = null

    @SerializedName("accuracy")
    private var accuracy:Double?

    init {
        latitude = location?.latitude
        longitude = location?.longitude
        this.isBikeDamage = isBikeDamage
        if (parkingId != -1) parking_spot_id = parkingId else parking_spot_id = null
        if (lock_battery != null) {
            lock_battery_level = lock_battery
        }
        if (bike_battery != null) {
            bike_battery_level = bike_battery
        }
        if (location?.hasAccuracy()?:false) {
            accuracy = location?.accuracy?.toDouble()
        } else {
            accuracy = 0.0
        }
        parking_image = imageURL
    }
}