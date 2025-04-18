package com.lattis.ellipse.data.database.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class RealmLock extends RealmObject{

    public static final String COLUMN_NAME_LOCK_ID = "lockId";
    public static final String COLUMN_NAME_MAC_ID = "macId";
    public static final String COLUMN_NAME_USER_ID = "userId";
    public static final String COLUMN_NAME_LAST_LOCKED_DATE = "lockedDate";
    public static final String COLUMN_NAME_LAST_CONNECTED_DATE = "connectedDate";


    @Required
    @PrimaryKey
    private String id;
    private String macId;
    private String lockId;
    private String macAddress;
    private String publicKey;
    private String signedMessage;
    private String name;
    private String version;
    private String revision;
    private String serialNumber;
    private String userId;

    private boolean sharedWithMe;
    private boolean sharedWithOther;

    private String shareWithUserId;
    private String shareId;
    private String usersId;

    private RealmLocation lastLocation;
    private boolean lockType;
    private Date connectedDate;
    private Date lockedDate;
    private boolean isLocked;
    private boolean useDefaultPinCode = true;

    private boolean autoProximityLock;
    private boolean autoProximityUnlock;
    private String alertMode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getRevision() {
        return revision;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getMacId() {
        return macId;
    }

    public void setMacId(String macId) {
        this.macId = macId;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getShareWithUserId() {
        return shareWithUserId;
    }

    public void setShareWithUserId(String shareWithUserId) {
        this.shareWithUserId = shareWithUserId;
    }

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public String getUsersId() {
        return usersId;
    }

    public void setUsersId(String usersId) {
        this.usersId = usersId;
    }

    public boolean isUseDefaultPinCode() {
        return useDefaultPinCode;
    }

    public RealmLocation getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(RealmLocation lastLocation) {
        this.lastLocation = lastLocation;
    }

    public boolean isLockType() {
        return lockType;
    }

    public void setLockType(boolean lockType) {
        this.lockType = lockType;
    }

    public Date getLockedDate() {
        return lockedDate;
    }

    public void setLockedDate(Date lockedDate) {
        this.lockedDate = lockedDate;
    }

    public Date getConnectedDate() {
        return connectedDate;
    }

    public void setConnectedDate(Date connectedDate) {
        this.connectedDate = connectedDate;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
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

    public String getAlertMode() {
        return alertMode;
    }

    public void setAlertMode(String alertMode) {
        this.alertMode = alertMode;
    }

    public boolean useDefaultPinCode() {
        return useDefaultPinCode;
    }

    public void setUseDefaultPinCode(boolean useDefaultPinCode) {
        useDefaultPinCode = useDefaultPinCode;
    }


    @Override
    public String toString() {
        return "RealmLock{" +
                "id='" + id + '\'' +
                ", macId='" + macId + '\'' +
                ", userId='" + userId + '\'' +
                ", signedMessage='" + signedMessage + '\'' +
                ", publicKey=" + publicKey +
                '}';
    }




}
