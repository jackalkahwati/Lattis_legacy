package com.lattis.ellipse.data.network.model.body.user;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 3/16/17.
 */

public class ChangePasswordBody {

    @SerializedName("new_password")
    private String new_password;
    @SerializedName("password")
    private String password;

    public ChangePasswordBody(String password, String new_password) {
        this.new_password = new_password;
        this.password=password;
    }
}
