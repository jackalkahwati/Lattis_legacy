package com.lattis.data.entity.response.ride

import com.google.gson.annotations.SerializedName

class StartRideResponse {
    @SerializedName("payload")
    var startRideDataResponse: StartRideDataResponse? = null

    override fun toString(): String {
        return "GetUserResponse{" +
                "StartRideDataResponse=" + startRideDataResponse +
                '}'
    }
}