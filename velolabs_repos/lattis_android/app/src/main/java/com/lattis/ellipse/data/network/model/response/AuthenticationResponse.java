package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

public class AuthenticationResponse extends AbstractApiResponse {
    @SerializedName("payload")
    private UserResponse user;

    public UserResponse getUser() {
        return user;
    }
}
