package com.lattis.ellipse.data.network.model.body.authentication;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/25/17.
 */

public class GetRefreshTokensBody {

    @SerializedName("user_id")
    public String userId;

    @SerializedName("refresh_token")
    public String refresh_token;

    public GetRefreshTokensBody(String userId, String refresh_token) {
        this.userId = userId;
        this.refresh_token = refresh_token;
    }
}
