package com.lattis.data.entity.response.user.current_status

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse
import com.lattis.domain.models.DockHub

/**
 * Created by ssd3 on 4/12/17.
 */
class GetCurrentUserStatusResponse : AbstractApiResponse() {
    @SerializedName("payload")
    var getCurrentUserStatusPayloadResponse: GetCurrentUserStatusPayloadResponse? = null

    val currentUserActiveBookingStatusResponse: GetCurrentUserActiveBookingStatusResponse?
        get() = getCurrentUserStatusPayloadResponse?.getCurrentUserActiveBookingStatusResponse

    val currentUserStatusTripResponse: GetCurrentUserStatusTripResponse?
        get() = getCurrentUserStatusPayloadResponse?.getCurrentUserStatusTripResponse

    val supportPhone: String?
        get() = getCurrentUserStatusPayloadResponse?.support_phone

    val onCallOperator: String?
        get() = getCurrentUserStatusPayloadResponse?.on_call_operator

    val dockHub:DockHub?
    get() = getCurrentUserStatusPayloadResponse?.dockHub
}