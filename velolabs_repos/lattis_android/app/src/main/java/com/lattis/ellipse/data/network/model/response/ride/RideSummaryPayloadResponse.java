package com.lattis.ellipse.data.network.model.response.ride;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/11/17.
 */

public class RideSummaryPayloadResponse {

    @SerializedName("trip")
    RideSummaryDataResponse rideSummaryDataResponse;

    RideSummaryDataResponse getRideSummaryDataResponse(){
        return rideSummaryDataResponse;
    }
}
