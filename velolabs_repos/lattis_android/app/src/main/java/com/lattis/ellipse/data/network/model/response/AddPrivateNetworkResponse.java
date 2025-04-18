package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

public class AddPrivateNetworkResponse {
    @SerializedName("payload")
    AddPrivateNetworkDataResponse addPrivateNetworkDataResponse;

    public AddPrivateNetworkDataResponse getAddPrivateNetworkDataResponse() {
        return addPrivateNetworkDataResponse;
    }
}
