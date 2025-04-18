package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/12/17.
 */

public class GetCurrentUserStatusPayloadResponse {

    @SerializedName("trip")
    public GetCurrentUserStatusTripResponse getCurrentUserStatusTripResponse;

    @SerializedName("active_booking")
    public GetCurrentUserActiveBookingStatusResponse getCurrentUserActiveBookingStatusResponse;

    @SerializedName("support_phone")
    private String support_phone;

    @SerializedName("on_call_operator")
    private String on_call_operator;

    public String getSupport_phone() {
        return support_phone;
    }

    public String getOn_call_operator() {
        return on_call_operator;
    }

}
