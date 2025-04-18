package com.lattis.data.entity.response.user.current_status

import com.google.gson.annotations.SerializedName

class GetCurrentUserActiveBookingStatusResponse {
    @SerializedName("bike_id")
    val bike_id:Int?=null
    @SerializedName("port_id")
    val port_id:Int?=null
    @SerializedName("hub_id")
    val hub_id:Int?=null
    @SerializedName("device_type")
    val device_type:String?=null
    @SerializedName("booking_id")
    val booking_id:Int?=null
    @SerializedName("booked_on")
    val booked_on: Long = 0
    @SerializedName("till")
    val till: Long = 0

}