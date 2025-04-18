package com.lattis.ellipse.domain.model;

import android.util.Log;

public class Account {

    private String accountName;

    private String accountType;

    private String password;

    private String accessToken;

    private String refreshToken;

    private String userId;

    private String usersId;

    private String userType;

    private boolean verified;

    public Account(String accountName, String accountType) {

        Log.e("Account","Account created with name: " + accountName);
        this.accountName = accountName;
        this.accountType = accountType;
    }

    public Account(String accountType) {
        this.accountType = accountType;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean isVerified() {
        return this.verified;
    }

    public String getAccountName() {
        return this.accountName;
    }

    public String getAccountType() {
        return this.accountType;
    }

    public String getUsersId() {
        return usersId;
    }

    public void setUsersId(String usersId) {
        this.usersId = usersId;
    }

    public String getUserType() {
        return this.userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountName='" + accountName + '\'' +
                ", accountType='" + accountType + '\'' +
                ", password='" + password + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", userId='" + userId + '\'' +
                ", usersId='" + usersId + '\'' +
                ", userType='" + userType + '\'' +
                ", verified=" + verified +
                '}';
    }
}
