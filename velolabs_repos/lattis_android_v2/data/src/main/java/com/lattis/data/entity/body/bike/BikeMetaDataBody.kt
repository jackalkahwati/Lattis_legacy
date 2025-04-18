package com.lattis.data.entity.body.bike

import com.google.gson.annotations.SerializedName

class BikeMetaDataBody(
    bike_id: Int,
    bike_battery_level: Int?,
    lock_battery_level: Int?,
    firmware_version: String?,
    shackle_jam: Boolean
) {
    @SerializedName("firmware_version")
    private var firmware_version: String? = null
    @SerializedName("shackle_jam")
    private var shackle_jam: Boolean? = null
    @SerializedName("bike_battery_level")
    private var bike_battery_level: Int? = null
    @SerializedName("bike_id")
    private var bike_id: Int? = null
    @SerializedName("lock_battery_level")
    private var lock_battery_level: Int? = null

    init {
        this.firmware_version = firmware_version
        this.shackle_jam = shackle_jam
        if (bike_battery_level!=null && bike_battery_level > 0) {
            this.bike_battery_level = bike_battery_level
        }
        if (lock_battery_level!=null && lock_battery_level > 0) {
            this.lock_battery_level = lock_battery_level
        }
        this.bike_id = bike_id
    }
}