package com.lattis.data.entity.response.bike

import com.google.gson.annotations.SerializedName


class BikeDetailResponse {

    @SerializedName("payload")
    internal var bikeDetailPayloadResponse: BikeDetailPayloadResponse? = null

    val bikeDetailResponse: FindBikeDataResponse?
        get() = bikeDetailPayloadResponse!!.bikeDetailDataResponse
}
