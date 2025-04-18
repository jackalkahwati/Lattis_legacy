package com.lattis.ellipse.data.network.model.body.authentication;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/25/17.
 */

public class GetNewTokensBody {

    @SerializedName("user_id")
    public String userId;

    @SerializedName("password")
    public String password;

    public GetNewTokensBody(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }
}
