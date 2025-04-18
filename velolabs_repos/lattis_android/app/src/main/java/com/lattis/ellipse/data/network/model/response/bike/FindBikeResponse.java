package com.lattis.ellipse.data.network.model.response.bike;

import com.google.gson.annotations.SerializedName;
import com.lattis.ellipse.data.network.model.response.AbstractApiResponse;

import java.util.List;


public class FindBikeResponse extends AbstractApiResponse {


    @SerializedName("payload")
    FindBikePayloadResponse findBikePayloadResponse;

    public List<FindBikeDataResponse> getFindNearestBikeResponse() {
        return findBikePayloadResponse.getFindNearestBikeDataResponse();
    }

    public List<FindBikeDataResponse> getFindAvailableBikeResponse() {
        return findBikePayloadResponse.getFindAvailableBikeDataResponse();
    }

    @Override
    public String toString() {
        return "GetUserResponse{" +
                "FindBikeDataResponse=" + findBikePayloadResponse +
                '}';
    }

    public FindBikePayloadResponse getFindBikePayloadResponse() {
        return findBikePayloadResponse;
    }

    public void setFindBikePayloadResponse(FindBikePayloadResponse findBikePayloadResponse) {
        this.findBikePayloadResponse = findBikePayloadResponse;
    }

}
