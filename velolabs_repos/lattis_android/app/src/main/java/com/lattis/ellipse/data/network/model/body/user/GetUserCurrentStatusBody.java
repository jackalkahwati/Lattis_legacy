package com.lattis.ellipse.data.network.model.body.user;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 9/6/17.
 */


public class GetUserCurrentStatusBody {

    @SerializedName("device_model")
    private String device_model;

    @SerializedName("device_os")
    private String device_os;

    public GetUserCurrentStatusBody(String deviceModel, String deviceOS) {
        this.device_os = deviceOS;
        this.device_model = deviceModel;
    }
}
