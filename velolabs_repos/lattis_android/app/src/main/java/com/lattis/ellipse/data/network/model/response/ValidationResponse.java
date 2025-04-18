package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

public class ValidationResponse extends AbstractApiResponse {

    @SerializedName("payload")
    private UserResponse userResponse;

    public UserResponse getUserResponse() {
        return userResponse;
    }

    @Override
    public String toString() {
        return "ValidationResponse{" +
                "userResponse=" + userResponse +
                '}';
    }
}
