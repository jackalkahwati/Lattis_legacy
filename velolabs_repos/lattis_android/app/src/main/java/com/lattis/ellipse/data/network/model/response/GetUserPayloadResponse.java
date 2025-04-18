package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ssd3 on 5/4/17.
 */

public class GetUserPayloadResponse {

    @SerializedName("user")
    UserResponse userResponse;

    @SerializedName("private_account")
    List<PrivateNetworkResponse> privateNetworksResponse;

    public List<PrivateNetworkResponse> getPrivateNetworkResponse() {
        return privateNetworksResponse;
    }

    public UserResponse getUserResponse() {
        return userResponse;
    }


}
