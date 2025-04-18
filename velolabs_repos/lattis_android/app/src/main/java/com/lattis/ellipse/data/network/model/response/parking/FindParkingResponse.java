package com.lattis.ellipse.data.network.model.response.parking;

import com.google.gson.annotations.SerializedName;
import com.lattis.ellipse.data.network.model.response.AbstractApiResponse;

import java.util.List;

/**
 * Created by ssd3 on 3/21/17.
 */

public class FindParkingResponse extends AbstractApiResponse {

    @SerializedName("payload")
    FindParkingPayloadResponse findParkingPayloadResponse;

    public List<FindParkingDataResponse> getFindParkingResponse() {
        return findParkingPayloadResponse.findParkingDataResponses();
    }

    @Override
    public String toString() {
        return "GetUserResponse{" +
                "FindBikeDataResponse=" + findParkingPayloadResponse +
                '}';
    }
}
