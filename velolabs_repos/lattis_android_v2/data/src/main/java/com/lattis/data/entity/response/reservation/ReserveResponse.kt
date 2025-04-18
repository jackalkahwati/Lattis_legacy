package com.lattis.data.entity.response.reservation

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.CostEstimate
import com.lattis.domain.models.Reserve

data class ReserveResponse(
    @SerializedName("payload")
    var reserve: Reserve
){
}