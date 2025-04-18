package com.lattis.data.entity.response.bike

import com.google.gson.annotations.SerializedName

/**
 * Created by ssd3 on 4/3/17.
 */

class ReserveBikeDataResponse {

    @SerializedName("booked_on")
    var booked_on: Long = 0
    @SerializedName("expires_in")
    var expires_in: Int = 0
    @SerializedName("on_call_operator")
    var on_call_operator: String? = null

    @SerializedName("booking_id")
    var booking_id: Int? = null


    @SerializedName("bike_id")
    var bike_id: Int? = null
    @SerializedName("port_id")
    var port_id: Int? = null
    @SerializedName("hub_id")
    var hub_id: Int? = null

    @SerializedName("device_type")
    var device_type: String? = null

    @SerializedName("trip_id")
    var trip_id: Int? = null

    @SerializedName("do_not_track_trip")
    var do_not_track_trip: String? = null


}
