package com.lattis.data.entity.response.reservation

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.Reserve
import com.lattis.domain.models.StartReservation

data class StartReservationResponse(
    @SerializedName("payload")
    var startReservation: StartReservation
){
}