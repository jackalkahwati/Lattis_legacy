package com.lattis.data.entity.response.parking

import com.google.gson.annotations.SerializedName

class GetParkingZonePayloadResponse {
    @SerializedName("parking_zones")
    var parkingZoneDataResponses: List<GetParkingZoneDataResponse>? = null
}