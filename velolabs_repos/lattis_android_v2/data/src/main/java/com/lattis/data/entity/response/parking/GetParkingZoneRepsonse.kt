package com.lattis.data.entity.response.parking

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse

class GetParkingZoneRepsonse : AbstractApiResponse() {
    @SerializedName("payload")
    var getParkingZonePayloadResponse: GetParkingZonePayloadResponse? = null
    fun getParkingZoneDataResponse(): List<GetParkingZoneDataResponse>?{
        if(getParkingZonePayloadResponse!=null && getParkingZonePayloadResponse?.parkingZoneDataResponses!=null){
            return getParkingZonePayloadResponse!!.parkingZoneDataResponses!!
        }else{
            return null
        }
    }

    override fun toString(): String {
        return "GetParkingZoneRepsonse{" +
                "GetParkingZoneRepsonse=" + getParkingZonePayloadResponse +
                '}'
    }
}