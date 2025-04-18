package com.lattis.data.entity.response.reservation

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.CostEstimate

data class CostEstimationResponse(
    @SerializedName("payload")
    var costEstimate: CostEstimate
){
}