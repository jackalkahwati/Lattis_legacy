package com.lattis.data.entity.response.bike

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.Bike
import com.lattis.domain.models.DockHub

data class SearchBikeByNameResponse(
    @SerializedName("payload")
    var payload:PayLoad?=null
) {
    data class PayLoad(
        @SerializedName("bikes")
        var bikeList:List<Bike>?=null,
    )
}