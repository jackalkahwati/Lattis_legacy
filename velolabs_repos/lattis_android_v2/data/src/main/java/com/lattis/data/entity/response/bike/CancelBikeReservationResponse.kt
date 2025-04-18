package com.lattis.data.entity.response.bike

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse
import com.lattis.domain.models.Bike

class CancelBikeReservationResponse : AbstractApiResponse() {
    @SerializedName("bike_id")
    var bikeId:Int?=0
}