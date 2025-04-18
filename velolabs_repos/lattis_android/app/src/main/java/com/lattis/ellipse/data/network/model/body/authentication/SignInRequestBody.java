package com.lattis.ellipse.data.network.model.body.authentication;

import com.google.gson.annotations.SerializedName;

import static android.R.attr.phoneNumber;

public class SignInRequestBody {

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

    public SignInRequestBody(String userType, String usersId, String regId, String password,boolean isSigningUp) {
        this.userType = userType;
        this.usersId = usersId;
        this.regId = regId;
        this.password = password;
        this.isSigningUp = isSigningUp;
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

    @Override
    public String toString() {
        return "SignInRequestBody{" +
                "userType='" + userType + '\'' +
                ", usersId='" + usersId + '\'' +
                ", regId='" + regId + '\'' +
                ", password='" + password + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", isSigningUp=" + isSigningUp +
                '}';
    }
}
