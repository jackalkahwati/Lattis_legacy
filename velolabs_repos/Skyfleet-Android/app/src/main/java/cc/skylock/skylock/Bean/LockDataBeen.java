package cc.skylock.skylock.Bean;

import android.content.ContentValues;

/**
 * Created by prabhu on 2/5/16.
 */
public class LockDataBeen {

    public static final String LOCK_COLUME_LATITUDE = "Lock_Latitude";
    public static final String LOCK_COLUME_LONGITUDE = "Lock_Longitude";
    public static final String LOCK_COLUME_MAC_ID = "Lock_MacId";
    public static final String LOCK_COLUME_USER_ID = "User_Id";
    public static final String LOCK_COLUME_LOCK_TYPE_FLAG ="Lock_Type_Flag";
    public static final String LOCK_COLUME_SHARED_ON = "Lock_Shared_On";
    public static final String LOCK_COLUME_SHARED_TILL ="Lock_Shared_Till";
    public static final String LOCK_COLUME_SHARED_TO = "Lock_Shared_To";

    private String lockLatitude;
    private String lockLongitude;
    private String lockMacID;
    private String lockUserID;
    private String lockSharedOn;
    private String lockSharedTill;
    private String lockSharedTo;
    private Boolean lockTypeFlag;

    public String getLockLatitude() {
        return lockLatitude;
    }

    public void setLockLatitude(String lockLatitude) {
        this.lockLatitude = lockLatitude;
    }

    public String getLockLongitude() {
        return lockLongitude;
    }

    public void setLockLongitude(String lockLongitude) {
        this.lockLongitude = lockLongitude;
    }

    public String getLockMacID() {
        return lockMacID;
    }

    public void setLockMacID(String lockMacID) {
        this.lockMacID = lockMacID;
    }

    public String getLockUserID() {
        return lockUserID;
    }

    public void setLockUserID(String lockUserID) {
        this.lockUserID = lockUserID;
    }

    public String getLockSharedOn() {
        return lockSharedOn;
    }

    public void setLockSharedOn(String lockSharedOn) {
        this.lockSharedOn = lockSharedOn;
    }

    public String getLockSharedTill() {
        return lockSharedTill;
    }

    public void setLockSharedTill(String lockSharedTill) {
        this.lockSharedTill = lockSharedTill;
    }

    public String getLockSharedTo() {
        return lockSharedTo;
    }

    public void setLockSharedTo(String lockSharedTo) {
        this.lockSharedTo = lockSharedTo;
    }

    public Boolean getLockTypeFlag() {
        return lockTypeFlag;
    }

    public void setLockTypeFlag(Boolean lockTypeFlag) {
        this.lockTypeFlag = lockTypeFlag;
    }

    public ContentValues getLockContentValue(){
        ContentValues values = new ContentValues();
        values.put(LOCK_COLUME_LATITUDE,getLockLatitude());
        values.put(LOCK_COLUME_LONGITUDE,getLockLongitude());
        values.put(LOCK_COLUME_LOCK_TYPE_FLAG,getLockTypeFlag());
        values.put(LOCK_COLUME_MAC_ID, getLockMacID());
        values.put(LOCK_COLUME_SHARED_ON, getLockSharedOn());
        values.put(LOCK_COLUME_SHARED_TILL, getLockSharedTill());
        values.put(LOCK_COLUME_SHARED_TO,getLockSharedTo());
        values.put(LOCK_COLUME_USER_ID, getLockUserID());
        return  values;
    }
}
