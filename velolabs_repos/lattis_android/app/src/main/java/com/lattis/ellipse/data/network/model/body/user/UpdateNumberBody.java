package com.lattis.ellipse.data.network.model.body.user;

import com.google.gson.annotations.SerializedName;

public class UpdateNumberBody {

    public static class Body {
        @SerializedName("user_id")
        private int userId;
        @SerializedName("country_code")
        private String countryCode;
        @SerializedName("phone_number")
        private String phoneNumber;

        public Body(String userId, String countryCode, String phoneNumber) {
            this.userId = Integer.parseInt(userId);
            this.countryCode = countryCode;
            this.phoneNumber = phoneNumber;
        }
    }

    @SerializedName("properties")
    Body properties;

    public UpdateNumberBody(String userId, String countryCode, String phoneNumber) {
        this.properties = new Body(userId, countryCode, phoneNumber);
    }

}
