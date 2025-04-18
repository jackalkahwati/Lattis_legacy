package com.lattis.data.entity.response.reservation

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.Reservation

data class GetReservationsResponse(
    @SerializedName("payload")
    var reservations:List<Reservation>
)