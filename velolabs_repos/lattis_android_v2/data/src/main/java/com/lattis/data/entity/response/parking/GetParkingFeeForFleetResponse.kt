package com.lattis.data.entity.response.parking

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse

class GetParkingFeeForFleetResponse : AbstractApiResponse() {
    @SerializedName("payload")
    var getParkingFeeForFleetDataResponse: GetParkingFeeForFleetDataResponse? = null
    fun getParkingFeeForFleetData(): GetParkingFeeForFleetDataResponse? {
        return getParkingFeeForFleetDataResponse
    }
}