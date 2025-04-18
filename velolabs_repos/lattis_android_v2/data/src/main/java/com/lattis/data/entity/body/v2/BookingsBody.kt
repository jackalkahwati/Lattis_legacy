package com.lattis.data.entity.body.v2

import com.google.gson.annotations.SerializedName

data class BookingsBody(
    @SerializedName("by_scan")
    private var by_scan: Boolean? = null,

    @SerializedName("latitude")
    private var latitude: Double? = null,

    @SerializedName("longitude")
    private var longitude: Double? = null,

    @SerializedName("device_token")
    private val device_token: String?=null,


    @SerializedName("pricing_option_id")
    private var pricing_option_id: Int?=null,

    @SerializedName("bike_id")
    val bike_id:Int?=null,
    @SerializedName("hub_id")
    val hub_id:Int?=null,
    @SerializedName("port_id")
    val port_id:Int?=null
) {
}