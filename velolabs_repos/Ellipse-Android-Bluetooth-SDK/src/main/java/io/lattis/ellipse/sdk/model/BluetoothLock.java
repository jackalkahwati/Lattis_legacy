package io.lattis.ellipse.sdk.model;

public class BluetoothLock {

    private String lockId;
    private String macId;
    private String macAddress;
    private String name;
    private String publicKey;
    private String signedMessage;
    private String userId;
    private Alert alertMode;
    private boolean autoLockActive;
    private boolean autoUnLockActive;

    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    public String getMacId() {
        return macId;
    }

    public void setMacId(String macId) {
        this.macId = macId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Alert getAlertMode() {
        return alertMode;
    }

    public void setAlertMode(Alert alertMode) {
        this.alertMode = alertMode;
    }

    public void setAutoLockActive(boolean autoLockActive) {
        this.autoLockActive = autoLockActive;
    }

    public void setAutoUnLockActive(boolean autoUnLockActive) {
        this.autoUnLockActive = autoUnLockActive;
    }

    public boolean isAutoLockActive() {
        return autoLockActive;
    }

    public boolean isAutoUnLockActive() {
        return autoUnLockActive;
    }

    @Override
    public String toString() {
        return "BluetoothLock{" +
                "lockId='" + lockId + '\'' +
                ", macId='" + macId + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", name='" + name + '\'' +
                ", userId='" + userId + '\'' +
                ", alertMode=" + alertMode +
                '}';
    }
}
