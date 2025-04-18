package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/25/17.
 */

public class TokenResponse {

    @SerializedName("rest_token")
    public String restToken;
    @SerializedName("refresh_token")
    public String refreshToken;

    public String getRestToken() {
        return restToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
