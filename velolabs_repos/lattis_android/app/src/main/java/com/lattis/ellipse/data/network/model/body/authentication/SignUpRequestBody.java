package com.lattis.ellipse.data.network.model.body.authentication;

import com.google.gson.annotations.SerializedName;

public class SignUpRequestBody {

    @SerializedName("user_type")
    private String userType;
    @SerializedName("users_id")
    private String usersId;
    @SerializedName("reg_id")
    private String regId;
    @SerializedName("password")
    private String password;
    @SerializedName("is_signing_up")
    private boolean isSigningUp;


    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;

    public SignUpRequestBody(String userType,
                             String usersId,
                             String regId,
                             String password,
                             boolean isSigningUp,
                             String firstName,
                             String lastName
                             ) {
        this.userType = userType;
        this.usersId = usersId;
        this.regId = regId;
        this.password = password;
        this.isSigningUp = isSigningUp;
        this.firstName = firstName;
        this.lastName=lastName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUsersId() {
        return usersId;
    }

    public void setUsersId(String usersId) {
        this.usersId = usersId;
    }

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public boolean isSigningUp() {
        return isSigningUp;
    }

    public void setSigningUp(boolean signingUp) {
        isSigningUp = signingUp;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "SignInRequestBody{" +
                "userType='" + userType + '\'' +
                ", usersId='" + usersId + '\'' +
                ", regId='" + regId + '\'' +
                ", password='" + password + '\'' +
                ", isSigningUp=" + isSigningUp +
                '}';
    }
}
