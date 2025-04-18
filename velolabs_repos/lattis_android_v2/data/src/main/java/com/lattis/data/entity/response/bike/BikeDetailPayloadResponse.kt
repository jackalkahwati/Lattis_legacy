package com.lattis.data.entity.response.bike

import com.google.gson.annotations.SerializedName

/**
 * Created by ssd3 on 4/13/17.
 */

class BikeDetailPayloadResponse {

    @SerializedName("bike")
    var bikeDetailDataResponse: FindBikeDataResponse? = null
}
