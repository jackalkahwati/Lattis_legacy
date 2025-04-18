package com.lattis.data.entity.response.parking

import com.google.gson.annotations.SerializedName

class GetParkingZoneDataResponse {
    @SerializedName("parking_area_id")
    val parking_area_id = 0

    @SerializedName("name")
    val zone_Name: String? = null

    @SerializedName("type")
    val type: String? = null

    @SerializedName("zone")
    val zone: String? = null

    @SerializedName("geometry")
    var parkingZoneGeometry: List<ParkingZoneGeometryResponse>? = null

    @SerializedName("operator_id")
    val operatorID = 0

    @SerializedName("customer_id")
    val customerID = 0

    @SerializedName("fleet_id")
    val fleetID = 0

}