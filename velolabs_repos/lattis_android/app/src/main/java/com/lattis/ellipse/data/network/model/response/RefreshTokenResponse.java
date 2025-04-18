package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

public class RefreshTokenResponse extends AbstractApiResponse {

    @SerializedName("payload")
    public TokenResponse tokenResponse;

    public TokenResponse getTokenResponse() {
        return tokenResponse;
    }
}
