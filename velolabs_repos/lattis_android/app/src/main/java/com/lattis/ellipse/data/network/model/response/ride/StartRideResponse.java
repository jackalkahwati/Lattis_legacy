package com.lattis.ellipse.data.network.model.response.ride;

import com.google.gson.annotations.SerializedName;

public class StartRideResponse {

    @SerializedName("payload")
    StartRideDataResponse startRideDataResponse;

    public StartRideDataResponse getStartRideDataResponse() {
        return startRideDataResponse;
    }

    @Override
    public String toString() {
        return "GetUserResponse{" +
                "StartRideDataResponse=" + startRideDataResponse +
                '}';
    }
}
