package com.lattis.data.entity.body.bike

import com.google.gson.annotations.SerializedName

class BookBikeBody(@field:SerializedName("bike_id")
                   private val bike_id: Int, by_scan: Boolean, latitude: Double?, longitude: Double?,device_token:String,pricing_option_id:Int?) {

    @SerializedName("by_scan")
    private var by_scan: Boolean? = null

    @SerializedName("latitude")
    private var latitude: Double? = null

    @SerializedName("longitude")
    private var longitude: Double? = null

    @SerializedName("device_token")
    private val device_token: String


    @SerializedName("pricing_option_id")
    private var pricing_option_id: Int?



    init {

        if (by_scan) {
            this.by_scan = by_scan
            this.latitude = latitude
            this.longitude = longitude
        }
        this.pricing_option_id = pricing_option_id
        this.device_token = device_token
    }
}
