package com.lattis.data.entity.body.v2

import com.google.gson.annotations.SerializedName

data class BikeHubPortBody (
    @SerializedName("bike_id")
    val bike_id:Int?=null,
    @SerializedName("hub_id")
    val hub_id:Int?=null,
    @SerializedName("port_id")
    val port_id:Int?=null,
    val endUrl:String
){
}