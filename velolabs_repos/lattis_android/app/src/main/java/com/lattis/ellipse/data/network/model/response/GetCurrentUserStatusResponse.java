package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/12/17.
 */

public class GetCurrentUserStatusResponse extends AbstractApiResponse{

    @SerializedName("payload")
    GetCurrentUserStatusPayloadResponse getCurrentUserStatusPayloadResponse;

    public GetCurrentUserActiveBookingStatusResponse getCurrentUserActiveBookingStatusResponse(){
        return  getCurrentUserStatusPayloadResponse.getCurrentUserActiveBookingStatusResponse;
    }

    public GetCurrentUserStatusTripResponse getCurrentUserStatusTripResponse(){
        return  getCurrentUserStatusPayloadResponse.getCurrentUserStatusTripResponse;
    }

    public String getSupportPhone(){
        return  getCurrentUserStatusPayloadResponse.getSupport_phone();
    }

    public String getOnCallOperator(){
        return  getCurrentUserStatusPayloadResponse.getOn_call_operator();
    }

}
