package com.lattis.data.entity.response.bike

import com.google.gson.annotations.SerializedName


class FindBikePayloadResponse {

    @SerializedName("nearest")
    var findNearestBikeDataResponse: List<FindBikeDataResponse>? = null
        internal set

    @SerializedName("available")
    var findAvailableBikeDataResponse: List<FindBikeDataResponse>? = null
        internal set


}
