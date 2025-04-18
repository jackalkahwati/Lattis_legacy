package com.lattis.ellipse.presentation.model;

import com.lattis.ellipse.domain.model.Alert;
import com.lattis.ellipse.domain.model.Location;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class LockModel {

    private String lockId;
    private String macAddress;
    private String macId;
    private String userId;
    private String name;
    private String version;
    private String serialNumber;

    private String usersId;
    private String sharedWithUserId;
    private String shareId;
    private boolean sharedWithMe;
    private boolean sharedWithOther;

    private Location lastLocation;
    private Date ConnectedDate;
    private Date LockedDate;
    private boolean isLocked;
    private boolean useDefaultPinCode;
    private Alert alert;

    private boolean autoProximityLock;
    private boolean autoProximityUnlock;

    private String publicKey;
    private String signedMessage;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getSignedMessage() {
        return signedMessage;
    }

    public void setSignedMessage(String signedMessage) {
        this.signedMessage = signedMessage;
    }



    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getMacId() {
        return macId;
    }

    public void setMacId(String macId) {
        this.macId = macId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsersId() {
        return usersId;
    }

    public void setUsersId(String usersId) {
        this.usersId = usersId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSharedWithUserId() {
        return sharedWithUserId;
    }

    public void setSharedWithUserId(String sharedWithUserId) {
        this.sharedWithUserId = sharedWithUserId;
    }

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }


    public boolean isSharedWithMe() {
        return sharedWithMe;
    }

    public void setSharedWithMe(boolean sharedWithMe) {
        this.sharedWithMe = sharedWithMe;
    }

    public boolean isSharedWithOther() {
        return sharedWithOther;
    }

    public void setSharedWithOther(boolean sharedWithOther) {
        this.sharedWithOther = sharedWithOther;
    }

    public boolean isUseDefaultPinCode() {
        return useDefaultPinCode;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public Date getLockedDate() {
        return LockedDate;
    }

    public void setLockedDate(Date lockedDate) {
        LockedDate = lockedDate;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean useDefaultPinCode() {
        return useDefaultPinCode;
    }

    public void setUseDefaultPinCode(boolean useDefaultPinCode) {
        this.useDefaultPinCode = useDefaultPinCode;
    }

    public Alert getAlert() {
        return alert;
    }

    public void setAlert(Alert alert) {
        this.alert = alert;
    }

    public boolean isAutoProximityLock() {
        return autoProximityLock;
    }

    public void setAutoProximityLock(boolean autoProximityLock) {
        this.autoProximityLock = autoProximityLock;
    }

    public boolean isAutoProximityUnlock() {
        return autoProximityUnlock;
    }

    public void setAutoProximityUnlock(boolean autoProximityUnlock) {
        this.autoProximityUnlock = autoProximityUnlock;
    }

    public Date getConnectedDate() {
        return ConnectedDate;
    }

    public void setConnectedDate(Date connectedDate) {
        ConnectedDate = connectedDate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public String toString() {
        return "LockModel{" +
                "lockId='" + lockId + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", macId='" + macId + '\'' +
                ", userId='" + userId + '\'' +
                ", usersId='" + usersId + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", signedMessage=" + signedMessage +
                ", publicKey=" + publicKey +
                ", LockedDate=" + LockedDate +
                '}';
    }
}
