package com.lattis.data.entity.response.parking

import com.google.gson.annotations.SerializedName

class FindParkingDataResponse {
    @SerializedName("parking_spot_id")
    val parking_spot_id = 0

    @SerializedName("latitude")
    val latitude = 0.0

    @SerializedName("longitude")
    val longitude = 0.0

    @SerializedName("name")
    val name: String? = null

    @SerializedName("description")
    val description: String? = null

    @SerializedName("pic")
    val pic: String? = null

    @SerializedName("type")
    val type: String? = null

    @SerializedName("fleet_id")
    val fleet_id = 0

    @SerializedName("operator_id")
    val operator_id = 0

    @SerializedName("customer_id")
    val customer_id = 0

    @SerializedName("parking_area_id")
    val parking_area_id = 0

}