package com.lattis.ellipse.data.network.model.body.authentication;

import com.google.gson.annotations.SerializedName;

public class RefreshTokenBody {

    @SerializedName("user_id")
    public String userId;

    @SerializedName("refresh_token")
    public String refreshToken;

    public RefreshTokenBody(String userId, String refreshToken) {
        this.userId = userId;
        this.refreshToken = refreshToken;
    }
}
