package com.lattis.data.entity.response.parking

import com.google.gson.annotations.SerializedName

/**
 * Created by ssd3 on 4/5/17.
 */
class FindParkingPayloadResponse {
    @SerializedName("parking_spots")
    var findParkingDataResponses: List<FindParkingDataResponse>? = null
    fun findParkingDataResponses(): List<FindParkingDataResponse>? {
        return findParkingDataResponses
    }
}