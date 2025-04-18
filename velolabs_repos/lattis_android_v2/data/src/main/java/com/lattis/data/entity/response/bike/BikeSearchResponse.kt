package com.lattis.data.entity.response.bike

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Rentals

class BikeSearchResponse : AbstractApiResponse() {
    @SerializedName("payload")
    var rentals:Rentals?=null
}