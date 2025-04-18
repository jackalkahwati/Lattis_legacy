package com.lattis.data.entity.response.ride

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse


class RideSummaryResponse : AbstractApiResponse() {

    @SerializedName("payload")
    internal var rideSummaryPayloadResponse: RideSummaryPayloadResponse? = null

    val rideSummaryResponse: RideSummaryDataResponse?
        get() = rideSummaryPayloadResponse!!.rideSummaryDataResponse

    override fun toString(): String {
        return "RideSummaryResponse{" +
                "RideSummaryResponse=" + rideSummaryPayloadResponse +
                '}'.toString()
    }
}
