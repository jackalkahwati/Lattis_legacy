package com.lattis.ellipse.domain.model;

public class VerificationBundle {

    private String userId;

    private boolean isVerified;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    @Override
    public String toString() {
        return "VerificationBundle{" +
                "userId=" + userId +
                ", isVerified=" + isVerified +
                '}';
    }
}
