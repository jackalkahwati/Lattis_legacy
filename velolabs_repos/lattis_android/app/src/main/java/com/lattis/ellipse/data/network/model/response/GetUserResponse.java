package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetUserResponse extends AbstractApiResponse {
    @SerializedName("payload")
    GetUserPayloadResponse getUserPayloadResponse;

    public UserResponse getUserResponse() {
        return getUserPayloadResponse.getUserResponse();
    }

    public List<PrivateNetworkResponse> getPrivateNetworkResponse() {
        return getUserPayloadResponse.getPrivateNetworkResponse();
    }

    @Override
    public String toString() {
        return "GetUserResponse{" +
                "userResponse=" + getUserPayloadResponse +
                '}';
    }
}
