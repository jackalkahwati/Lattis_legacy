package com.lattis.ellipse.data.network.model.body.lock;

import com.google.gson.annotations.SerializedName;


public class SignedMessagePublicKeyBody {

    @SerializedName("bike_id")
    private int bike_id;

    public SignedMessagePublicKeyBody(int bike_id){
        this.bike_id=bike_id;
    }
}
