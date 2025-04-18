package com.lattis.data.entity.response.parking

import com.google.gson.annotations.SerializedName

class GetParkingFeeForFleetDataResponse {
    @SerializedName("outside")
    val isOutside = false

    @SerializedName("not_allowed")
    val isNot_allowed = false

    @SerializedName("fee")
    val fee = 0f

    @SerializedName("currency")
    val currency: String? = null

}