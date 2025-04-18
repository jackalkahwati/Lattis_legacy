package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

public class UserResponse {

    @SerializedName("user_id")
    private String userId;
    @SerializedName("users_id")
    private String usersId;
    @SerializedName("rest_token")
    private String restToken;
    @SerializedName("refresh_token")
    private String refreshToken;
    @SerializedName("username")
    private String username;
    @SerializedName("user_type")
    private String userType;
    @SerializedName("verified")
    private boolean verified;
    @SerializedName("max_locks")
    private int maxLocks;
    @SerializedName("title")
    private String title;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;
    @SerializedName("phone_number")
    private String phoneNumber;
    @SerializedName("email")
    private String email;
    @SerializedName("country_code")
    private String countryCode;

    public String getUserId() {
        return userId;
    }

    public String getUsersId() {
        return usersId;
    }

    public String getRestToken() {
        return restToken;
    }

    public String getUsername() {
        return username;
    }

    public String getUserType() {
        return userType;
    }

    public boolean isVerified() {
        return verified;
    }

    public int getMaxLocks() {
        return maxLocks;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRestToken(String restToken) {
        this.restToken = restToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "userId=" + userId +
                ", usersId='" + usersId + '\'' +
                ", restToken='" + restToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", username=" + username +
                ", userType='" + userType + '\'' +
                ", verified=" + verified +
                ", maxLocks=" + maxLocks +
                ", title='" + title + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", countryCode='" + countryCode + '\'' +
                '}';
    }
}
