package com.lattis.data.entity.body.v2

import com.google.gson.annotations.SerializedName

data class StartTripBody (
    @SerializedName("latitude")
    private val latitude: Double,

    @SerializedName("longitude")
    private val longitude: Double,

    @SerializedName("device_token")
    private val device_token: String,

    @SerializedName("bike_id")
    val bike_id:Int?=null,
    @SerializedName("hub_id")
    val hub_id:Int?=null,
    @SerializedName("port_id")
    val port_id:Int?=null
){
}