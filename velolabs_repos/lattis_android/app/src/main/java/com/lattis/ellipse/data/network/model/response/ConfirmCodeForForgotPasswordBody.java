package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;


public class ConfirmCodeForForgotPasswordBody {


        @SerializedName("email")
        private String email;

        @SerializedName("password")
        private String password;

        @SerializedName("confirmation_code")
        private String confirmation_code;


        public ConfirmCodeForForgotPasswordBody(String email,String confirmation_code,String password) {
            this.email = email;
            this.password = password;
            this.confirmation_code = confirmation_code;
        }

        @Override
        public String toString() {
            return "ResetPasswordBody{" +
                    "phoneNumber=" + email +
                    '}';
        }



}
