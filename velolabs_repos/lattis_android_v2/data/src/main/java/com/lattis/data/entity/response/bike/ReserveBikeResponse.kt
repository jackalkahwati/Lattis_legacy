package com.lattis.data.entity.response.bike

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse

class ReserveBikeResponse : AbstractApiResponse() {

    @SerializedName("payload")
    var reserveBikeResponse: ReserveBikeDataResponse? = null
        internal set

    override fun toString(): String {
        return "GetUserResponse{" +
                "FindBikeDataResponse=" + reserveBikeResponse +
                '}'.toString()
    }
}
