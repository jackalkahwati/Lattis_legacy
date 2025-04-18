package com.lattis.ellipse.data.network.model.response.ride;

import com.google.gson.annotations.SerializedName;
import com.lattis.ellipse.data.network.model.response.AbstractApiResponse;

/**
 * Created by ssd3 on 4/11/17.
 */

public class RideSummaryResponse extends AbstractApiResponse {

    @SerializedName("payload")
    RideSummaryPayloadResponse rideSummaryPayloadResponse;

    public RideSummaryDataResponse getRideSummaryResponse() {
        return rideSummaryPayloadResponse.getRideSummaryDataResponse();
    }

    @Override
    public String toString() {
        return "RideSummaryResponse{" +
                "RideSummaryResponse=" + rideSummaryPayloadResponse +
                '}';
    }
}
