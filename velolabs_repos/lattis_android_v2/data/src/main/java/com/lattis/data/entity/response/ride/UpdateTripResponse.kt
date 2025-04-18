package com.lattis.data.entity.response.ride

import com.google.gson.annotations.SerializedName

class UpdateTripResponse {

    @SerializedName("payload")
    var updateTripDataResponse: UpdateTripDataResponse? = null
        internal set


}
