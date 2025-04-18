package com.lattis.data.entity.response.parking

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse

class FindParkingResponse : AbstractApiResponse() {
    @SerializedName("payload")
    var findParkingPayloadResponse: FindParkingPayloadResponse? = null
    fun findParkingDataResponse(): List<FindParkingDataResponse>?
    {
        if(findParkingPayloadResponse!=null && findParkingPayloadResponse?.findParkingDataResponses!=null){
            return findParkingPayloadResponse!!.findParkingDataResponses()
        }else{
            return null
        }

    }

    override fun toString(): String {
        return "GetUserResponse{" +
                "FindBikeDataResponse=" + findParkingPayloadResponse +
                '}'
    }
}