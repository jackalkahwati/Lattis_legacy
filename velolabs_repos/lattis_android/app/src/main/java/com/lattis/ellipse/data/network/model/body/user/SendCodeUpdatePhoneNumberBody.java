package com.lattis.ellipse.data.network.model.body.user;

import com.google.gson.annotations.SerializedName;

public class SendCodeUpdatePhoneNumberBody {

    @SerializedName("country_code")
    private String countryCode;
    @SerializedName("phone_number")
    private String phoneNumber;



    public SendCodeUpdatePhoneNumberBody(String countryCode, String phoneNumber) {
        this.countryCode = countryCode;
        this.phoneNumber=phoneNumber;
    }
}
