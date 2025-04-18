package com.lattis.ellipse.data.network.model.body.authentication;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordBody {

    @SerializedName("users_id")
    private String usersId;
    @SerializedName("country_code")
    private String countryCode;
    @SerializedName("user_type")
    private String userType;

    public ResetPasswordBody(String usersId, String countryCode, String userType) {
        this.usersId = usersId;
        this.countryCode = countryCode;
        this.userType = userType;
    }

    @Override
    public String toString() {
        return "ResetPasswordBody{" +
                "usersId=" + usersId +
                ", countryCode='" + countryCode + '\'' +
                ", userType='" + userType + '\'' +
                '}';
    }
}
