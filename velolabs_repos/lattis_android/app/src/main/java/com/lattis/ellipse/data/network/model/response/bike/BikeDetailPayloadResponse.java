package com.lattis.ellipse.data.network.model.response.bike;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/13/17.
 */

public class BikeDetailPayloadResponse {

    @SerializedName("bike")
    FindBikeDataResponse findBikeDataResponse;

    public FindBikeDataResponse getBikeDetailDataResponse() {
        return findBikeDataResponse;
    }
}
