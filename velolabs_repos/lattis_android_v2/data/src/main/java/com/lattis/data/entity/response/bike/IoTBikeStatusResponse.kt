package com.lattis.data.entity.response.bike

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.IoTBikeStatus

data class IoTBikeStatusResponse(
    @SerializedName("payload")
    var iotBikeBikeStatus: IoTBikeStatus
)