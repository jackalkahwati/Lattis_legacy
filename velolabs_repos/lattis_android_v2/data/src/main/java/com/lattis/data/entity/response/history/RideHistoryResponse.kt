package com.lattis.data.entity.response.history

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse

class RideHistoryResponse : AbstractApiResponse() {
    @SerializedName("payload")
    var rideHistoryDataResponse: List<RideHistoryDataResponse>? = null
}