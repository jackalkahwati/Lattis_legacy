package com.lattis.ellipse.data.network.model.response.history;

import com.google.gson.annotations.SerializedName;
import com.lattis.ellipse.data.network.model.response.AbstractApiResponse;

import java.util.List;

/**
 * Created by ssd3 on 8/16/17.
 */

public class RideHistoryResponse extends AbstractApiResponse {

    @SerializedName("payload")
    List<RideHistoryDataResponse> rideHistoryPayloadResponses;

    public List<RideHistoryDataResponse> getRideHistoryDataResponse() {
        return rideHistoryPayloadResponses;
    }
}
