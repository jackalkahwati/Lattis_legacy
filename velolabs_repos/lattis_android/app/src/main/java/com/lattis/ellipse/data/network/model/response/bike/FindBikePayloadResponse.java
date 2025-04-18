package com.lattis.ellipse.data.network.model.response.bike;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ssd3 on 4/5/17.
 */

public class FindBikePayloadResponse {

    @SerializedName("nearest")
    List<FindBikeDataResponse> getFindNearestBikeDataResponseList;

    public List<FindBikeDataResponse> getFindNearestBikeDataResponse() {
        return getFindNearestBikeDataResponseList;
    }

    @SerializedName("available")
    List<FindBikeDataResponse> getFindAvailableBikeDataResponseList;

    public List<FindBikeDataResponse> getFindAvailableBikeDataResponse() {
        return getFindAvailableBikeDataResponseList;
    }


}
