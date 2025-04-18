package com.lattis.data.entity.response.user

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.Bike

class GetUserFleetsResponse (
    @SerializedName("payload")
    var fleets:List<Bike.Fleet>
)