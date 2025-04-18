package com.lattis.data.entity.response.ride

import com.google.gson.annotations.SerializedName

class StartRideDataResponse {
    @SerializedName("trip_id")
    var tripId = 0

    @SerializedName("do_not_track_trip")
    val do_not_track_trip: Boolean? = null

}