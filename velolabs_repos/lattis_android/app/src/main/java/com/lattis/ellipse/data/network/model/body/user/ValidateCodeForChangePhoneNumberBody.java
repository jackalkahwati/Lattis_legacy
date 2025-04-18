package com.lattis.ellipse.data.network.model.body.user;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 3/15/17.
 */

public class ValidateCodeForChangePhoneNumberBody {

    @SerializedName("confirmation_code")
    private String confirmation_code;
    @SerializedName("phone_number")
    private String phoneNumber;

    public ValidateCodeForChangePhoneNumberBody(String confirmation_code, String phoneNumber) {
        this.confirmation_code = confirmation_code;
        this.phoneNumber=phoneNumber;
    }
}
