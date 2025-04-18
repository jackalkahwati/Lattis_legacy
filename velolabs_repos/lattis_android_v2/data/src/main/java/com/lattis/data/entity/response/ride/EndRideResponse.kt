package com.lattis.data.entity.response.ride

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse

class EndRideResponse : AbstractApiResponse(){

    @SerializedName("payload")
    var rideSummaryDataResponse: RideSummaryDataResponse? = null

}