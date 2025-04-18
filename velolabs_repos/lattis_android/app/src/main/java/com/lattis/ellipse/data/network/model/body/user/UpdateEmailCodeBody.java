package com.lattis.ellipse.data.network.model.body.user;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lattis on 03/05/17.
 */

public class UpdateEmailCodeBody {
    @SerializedName("email")
    String email;
    public UpdateEmailCodeBody(String email) {
        this.email = email;
    }

}
