package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/25/17.
 */

public class NewTokenResponse {

    @SerializedName("payload")
    TokenResponse tokenResponse;

    public TokenResponse getToken() {
        return this.tokenResponse;
    }
}
