package com.lattis.ellipse.data.network.model.body.user;

import com.google.gson.annotations.SerializedName;

public class UpdateUserBody {

    @SerializedName("properties")
    private UserBody userBody;

    public UpdateUserBody(UserBody userBody) {
        this.userBody = userBody;
    }

    @Override
    public String toString() {
        return "UpdateUserBody{" +
                "userBody=" + userBody +
                '}';
    }
}
