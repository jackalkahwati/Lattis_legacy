package com.lattis.data.entity.response.user.current_status

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.DockHub
import com.lattis.domain.models.Reservation
import com.lattis.domain.models.UserCurrentStatus

class GetCurrentUserStatusPayloadResponse {
    @SerializedName("trip")
    var getCurrentUserStatusTripResponse: GetCurrentUserStatusTripResponse? = null
    @SerializedName("active_booking")
    var getCurrentUserActiveBookingStatusResponse: GetCurrentUserActiveBookingStatusResponse? = null
    @SerializedName("support_phone")
    val support_phone: String? = null
    @SerializedName("on_call_operator")
    val on_call_operator: String? = null

    @SerializedName("reservation")
    val reservation:Reservation?=null

    @SerializedName("vehicle")
    val vehicle:UserCurrentStatus.Vehicle?=null

    @SerializedName("hub")
    val dockHub:DockHub?=null


}