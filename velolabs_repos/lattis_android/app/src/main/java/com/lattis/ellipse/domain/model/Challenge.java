package com.lattis.ellipse.domain.model;

public class Challenge {

    private String macId;

    private String signedMessage;

    private String publicKey;

    public Challenge(String macId, String signedMessage, String publicKey) {
        this.macId = macId;
        this.signedMessage = signedMessage;
        this.publicKey = publicKey;
    }

    public String getMacId() {
        return macId;
    }

    public String getSignedMessage() {
        return signedMessage;
    }

    public void setSignedMessage(String signedMessage) {
        this.signedMessage = signedMessage;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "Challenge{" +
                "signedMessage='" + signedMessage + '\'' +
                ", publicKey='" + publicKey + '\'' +
                '}';
    }
}
