package com.lattis.ellipse.data.network.model.response.bike;

import com.google.gson.annotations.SerializedName;
import com.lattis.ellipse.data.network.model.response.AbstractApiResponse;

/**
 * Created by ssd3 on 4/3/17.
 */

public class ReserveBikeResponse extends AbstractApiResponse {

    @SerializedName("payload")
    ReserveBikeDataResponse reserveBikeDataResponse;

    public ReserveBikeDataResponse getReserveBikeResponse() {
        return reserveBikeDataResponse;
    }

    @Override
    public String toString() {
        return "GetUserResponse{" +
                "FindBikeDataResponse=" + reserveBikeDataResponse +
                '}';
    }
}
