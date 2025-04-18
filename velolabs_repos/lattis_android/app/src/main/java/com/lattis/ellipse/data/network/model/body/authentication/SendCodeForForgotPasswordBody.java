package com.lattis.ellipse.data.network.model.body.authentication;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 3/17/17.
 */

public class SendCodeForForgotPasswordBody {


        @SerializedName("phone_number")
        private String phoneNumber;

    @SerializedName("country_code")
    private String country_code;


        public SendCodeForForgotPasswordBody(String phoneNumber,String country_code) {
            this.phoneNumber = phoneNumber;
            this.country_code=country_code;
        }

        @Override
        public String toString() {
            return "ResetPasswordBody{" +
                    "phoneNumber=" + phoneNumber +
                    '}';
        }

}
