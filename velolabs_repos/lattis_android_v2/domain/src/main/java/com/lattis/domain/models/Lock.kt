package com.lattis.domain.models

import java.util.Date

open class Lock : ScannedLock() {


    var lockId: String? = null
    var userId: String? = null
    var usersId: String? = null
    var publicKey: String? = null
    var signedMessage: String? = null

    var isSharedWithOther: Boolean? = false
    var isSharedWithMe: Boolean? = false

    var sharedWithUserId: String? = null
    var shareId: String? = null
    var sharedTill: Date? = null

    var lastLocation: Location? = null
    var lockedDate: Date? = null
    var connectedDate: Date? = null
    var isLocked: Boolean? = false
    var isLockType: Boolean = false
    var alertMode: Alert? = null
    var isAutoProximityLock: Boolean? = false
    var isAutoProximityUnlock: Boolean? = false
    var version: Version? = null
    var serialNumber: String? = null
    var isDefaultPinCode: Boolean? = false
    var activityClassName: String? = null

    override fun toString(): String {
        return "Lock{" +
                "lockId='" + lockId + '\''.toString() +
                ", userId='" + userId + '\''.toString() +
                ", lastLocation=" + lastLocation +
                ", LockedDate=" + lockedDate +
                ", connectedDate=" + connectedDate +
                ", version=" + version +
                ", serialNumber='" + serialNumber + '\''.toString() +
                '}'.toString()
    }

    class Connection {

        enum class Status {

            SCANNING,
            DEVICE_FOUND,
            DISCOVER_SERVICE,
            DISCONNECTED,
            SERVICE_DISCOVERED,
            OWNER_REQUEST,
            GUEST_REQUEST,
            OWNER_VERIFIED,
            GUEST_VERIFIED,
            ACCESS_DENIED,
            UPDATING_FIRMWARE,
            ERROR;

            var lock: Lock? = null
                internal set

            val isAuthenticated: Boolean
                get() = this == OWNER_VERIFIED ||
                        this == GUEST_VERIFIED ||
                        this == UPDATING_FIRMWARE

            fun forLock(lock: Lock?): Status {
                this.lock = lock
                return this
            }
        }

        enum class Rssi {

            ANDROID, LOCK;

            var value: Int = 0
                internal set

            fun withValue(value: Int): Rssi {
                this.value = value
                return this
            }
        }
    }

    class Hardware {

        class State(val position: Position, val batteryLevel: Int?, val rssiLevel: Int?, val temperature: Int?) {

            override fun toString(): String {
                return "Lock{" +
                        "position=" + position +
                        ", batteryLevel=" + batteryLevel +
                        ", rssiLevel=" + rssiLevel +
                        ", temperature=" + temperature +
                        '}'.toString()
            }
        }

        enum class Position {
            LOCKED,
            UNLOCKED,
            BETWEEN_LOCKED_AND_UNLOCKED,
            INVALID
        }
    }


    //    private String lockId;
    //    private String userId;
    //    private String usersId;
    //    private String publicKey;
    //    private String signedMessage;
    //
    //    private boolean isSharedWithOther;
    //    private boolean isSharedWithMe;
    //
    //    private String sharedWithUserId;
    //    private String shareId;
    //    private Date sharedTill;
    //
    //    private Location lastLocation;
    //    private Date LockedDate;
    //    private Date connectedDate;
    //    private boolean isLocked;
    //    private boolean lockType;
    //    private Alert alertMode;
    //    private boolean autoProximityLock;
    //    private boolean autoProximityUnlock;
    //    private Version version;
    //    private String serialNumber;
    //    private boolean isDefaultPinCode;
    //
    //    public String getLockId() {
    //        return lockId;
    //    }
    //
    //    public void setLockId(String lockId) {
    //        this.lockId = lockId;
    //    }
    //
    //    public String getUserId() {
    //        return userId;
    //    }
    //
    //    public void setUserId(String userId) {
    //        this.userId = userId;
    //    }
    //
    //    public String getUsersId() {
    //        return usersId;
    //    }
    //
    //    public void setUsersId(String usersId) {
    //        this.usersId = usersId;
    //    }
    //
    //    public String getPublicKey() {
    //        return publicKey;
    //    }
    //
    //    public void setPublicKey(String publicKey) {
    //        this.publicKey = publicKey;
    //    }
    //
    //    public String getSignedMessage() {
    //        return signedMessage;
    //    }
    //
    //    public void setSignedMessage(String signedMessage) {
    //        this.signedMessage = signedMessage;
    //    }
    //
    //    public String getSharedWithUserId() {
    //        return sharedWithUserId;
    //    }
    //
    //    public void setSharedWithUserId(String sharedWithUserId) {
    //        this.sharedWithUserId = sharedWithUserId;
    //    }
    //
    //    public boolean isSharedWithOther() {
    //        return isSharedWithOther;
    //    }
    //
    //    public void setIsSharedWithOther(boolean isSharedWithOther) {
    //        this.isSharedWithOther = isSharedWithOther;
    //    }
    //
    //    public boolean isSharedWithMe() {
    //        return isSharedWithMe;
    //    }
    //
    //    public void setIsSharedWithMe(boolean isSharedWithMe) {
    //        this.isSharedWithMe = isSharedWithMe;
    //    }
    //
    //    public boolean isDefaultPinCode() {
    //        return isDefaultPinCode;
    //    }
    //
    //    public void setDefaultPinCode(boolean defaultPinCode) {
    //        isDefaultPinCode = defaultPinCode;
    //    }
    //
    //    public String getShareId() {
    //        return shareId;
    //    }
    //
    //    public void setShareId(String shareId) {
    //        this.shareId = shareId;
    //    }
    //
    //    public Date getSharedTill() {
    //        return sharedTill;
    //    }
    //
    //    public void setSharedTill(Date sharedTill) {
    //        this.sharedTill = sharedTill;
    //    }
    //
    //    public Location getLastLocation() {
    //        return lastLocation;
    //    }
    //
    //    public void setLastLocation(Location lastLocation) {
    //        this.lastLocation = lastLocation;
    //    }
    //
    //    public Date getLockedDate() {
    //        return LockedDate;
    //    }
    //
    //    public void setLockedDate(Date lockedDate) {
    //        LockedDate = lockedDate;
    //    }
    //
    //    public Date getConnectedDate() {
    //        return connectedDate;
    //    }
    //
    //    public void setConnectedDate(Date connectedDate) {
    //        this.connectedDate = connectedDate;
    //    }
    //
    //    public boolean isLocked() {
    //        return isLocked;
    //    }
    //
    //    public void setLocked(boolean locked) {
    //        isLocked = locked;
    //    }
    //
    //    public boolean isLockType() {
    //        return lockType;
    //    }
    //
    //    public void setLockType(boolean lockType) {
    //        this.lockType = lockType;
    //    }
    //
    //    public Alert getAlertMode() {
    //        return alertMode;
    //    }
    //
    //    public void setAlertMode(Alert alertMode) {
    //        this.alertMode = alertMode;
    //    }
    //
    //    public boolean isAutoProximityLock() {
    //        return autoProximityLock;
    //    }
    //
    //    public void setAutoProximityLock(boolean autoProximityLock) {
    //        this.autoProximityLock = autoProximityLock;
    //    }
    //
    //    public boolean isAutoProximityUnlock() {
    //        return autoProximityUnlock;
    //    }
    //
    //    public void setAutoProximityUnlock(boolean autoProximityUnlock) {
    //        this.autoProximityUnlock = autoProximityUnlock;
    //    }
    //
    //    public Version getVersion() {
    //        return version;
    //    }
    //
    //    public void setVersion(Version version) {
    //        this.version = version;
    //    }
    //
    //    public String getSerialNumber() {
    //        return serialNumber;
    //    }
    //
    //    public void setSerialNumber(String serialNumber) {
    //        this.serialNumber = serialNumber;
    //    }
    //
    //    public boolean useDefaultPinCode() {
    //        return isDefaultPinCode;
    //    }
    //
    //    public void setUseDefaultPinCode(boolean defaultPinCode) {
    //        isDefaultPinCode = defaultPinCode;
    //    }
    //
    //    @Override
    //    public String toString() {
    //        return "Lock{" +
    //                "id='" + id + '\'' +
    //                ", macId='" + getMacId() + '\'' +
    //                ", userId='" + userId + '\'' +
    //                ", signedMessage='" + signedMessage + '\'' +
    //                ", publicKey=" + publicKey +
    //                '}';
    //    }
    //
    //    public static class Connection {
    //
    //        public enum Status {
    //
    //            SCANNING,
    //            DEVICE_FOUND,
    //            DISCOVER_SERVICE,
    //            DISCONNECTED,
    //            SERVICE_DISCOVERED,
    //            OWNER_REQUEST,
    //            GUEST_REQUEST,
    //            OWNER_VERIFIED,
    //            GUEST_VERIFIED,
    //            ACCESS_DENIED,
    //            UPDATING_FIRMWARE,
    //            ERROR;
    //
    //            Lock lock;
    //
    //            public Lock getLock() {
    //                return lock;
    //            }
    //
    //            public Status forLock(Lock lock) {
    //                this.lock = lock;
    //                return this;
    //            }
    //
    //            public boolean isAuthenticated(){
    //                return this.equals(OWNER_VERIFIED)||
    //                        this.equals(GUEST_VERIFIED)||
    //                        this.equals(UPDATING_FIRMWARE);
    //            }
    //        }
    //
    //        public enum  Rssi {
    //
    //            ANDROID, LOCK;
    //
    //            int value;
    //
    //            public Rssi withValue(int value) {
    //                this.value = value;
    //                return this;
    //            }
    //
    //            public int getValue() {
    //                return value;
    //            }
    //        }
    //    }
    //
    //    public static class Hardware {
    //
    //        public static class State {
    //
    //            public State(Position position, int batteryLevel, int rssiLevel, int temperature) {
    //                this.position = position;
    //                this.batteryLevel = batteryLevel;
    //                this.rssiLevel = rssiLevel;
    //                this.temperature = temperature;
    //            }
    //
    //            private Position position;
    //
    //            private int batteryLevel;
    //
    //            private int rssiLevel;
    //
    //            private int temperature;
    //
    //            public Position getPosition() {
    //                return position;
    //            }
    //
    //            public int getBatteryLevel() {
    //                return batteryLevel;
    //            }
    //
    //            public int getRssiLevel() {
    //                return rssiLevel;
    //            }
    //
    //            public int getTemperature() {
    //                return temperature;
    //            }
    //
    //            @Override
    //            public String toString() {
    //                return "State{" +
    //                        "position=" + position +
    //                        ", batteryLevel=" + batteryLevel +
    //                        ", rssiLevel=" + rssiLevel +
    //                        ", temperature=" + temperature +
    //                        '}';
    //            }
    //        }
    //
    //        public enum Position {
    //            LOCKED,
    //            UNLOCKED,
    //            BETWEEN_LOCKED_AND_UNLOCKED,
    //            INVALID
    //        }
    //    }
}