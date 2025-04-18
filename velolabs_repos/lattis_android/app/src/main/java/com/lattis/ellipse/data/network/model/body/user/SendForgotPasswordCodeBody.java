package com.lattis.ellipse.data.network.model.body.user;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 5/2/17.
 */

public class SendForgotPasswordCodeBody {

    @SerializedName("email")
    private String email;


    public SendForgotPasswordCodeBody(String email) {
        this.email = email;
    }
}
