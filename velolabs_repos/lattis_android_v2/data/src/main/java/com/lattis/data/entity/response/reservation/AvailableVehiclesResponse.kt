package com.lattis.data.entity.response.reservation

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse
import com.lattis.domain.models.Bike

data class AvailableVehiclesResponse(
    @SerializedName("payload")
    var bikes:List<Bike>?
) : AbstractApiResponse() {


}