package com.lattis.ellipse.data.network.model.body.user;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lattis on 03/05/17.
 */

public class ValidateCodeUpdateEmailBody {
    @SerializedName("email")
    String email;
    @SerializedName("confirmation_code")
    String confirmation_code;
    public ValidateCodeUpdateEmailBody(String confirmation_code, String email) {
        this.email = email;
        this.confirmation_code = confirmation_code;
    }

}
