package cc.skylock.skylock.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import cc.skylock.skylock.Bean.FriendBean;
import cc.skylock.skylock.Bean.LockDataBeen;
import cc.skylock.skylock.Bean.UserRegistrationParameter;

/**
 * Created by Velo Labs Android on 29-01-2016.
 */
public class Dbfunction {

    public static final int DATABASE_VERSION = 3;
    public static final String SKYLOCK_DATABASE_NAME = "skylock.db";
    public static final String SKYLOCK_USER_TABLE_NAME = "skylockUserTable";
    public static final String SKYLOCK_LOCK_TABLE_NAME = "skylockLockTable";
    public static final String SKYLOCK_FRIEND_TABLE_NAME = "skylockFriendTable";
    private static final String USER_COLUMN_FIRST_NAME = "First_Name";
    private static final String USER_COLUMN_LAST_NAME = "Last_Name";
    private static final String USER_COLUMN_USER_ID = "User_Id";
    private static final String USER_COLUMN_PASSWORD = "Password";
    private static final String USER_COLUMN_GCM_REGISTRATION_ID = "Gcm_Reg_ID";
    private static final String USER_COLUMN_FACEBOOK_FLAG = "Facebook_Flag";
    private static final String USER_COLUMN_COUNTRY_CODE = "country_code";


//    "latitude": null,
//            "lockshared_time": null,
//            "longitude": null,
//            "mac_id": "ABCD123",
//            "user_id": "4156767922"
//}
//],
//        "shared_locks": [
//        {
//        "latitude": 1.2,
//        "longitude": 1.3,
//        "mac_id": "ABCDEF",
//        "shared_by": "4156767921",
//        "shared_on": "Wed, 03 Feb 2016 08:00:23 GMT",
//        "shared_till": "Sun, 03 Apr 2016 01:00:23 GMT"


    public static final String SKYLOCK_USER_TABLE = "CREATE TABLE IF NOT EXISTS "
            + SKYLOCK_USER_TABLE_NAME
            + " ("
            + USER_COLUMN_USER_ID + "  TEXT  PRIMARY KEY,"
            + USER_COLUMN_FIRST_NAME + " TEXT,"
            + USER_COLUMN_LAST_NAME + "  TEXT,"
            + USER_COLUMN_PASSWORD + " TEXT,"
            + USER_COLUMN_GCM_REGISTRATION_ID + " TEXT,"
            + USER_COLUMN_COUNTRY_CODE + " TEXT,"
            + USER_COLUMN_FACEBOOK_FLAG + " BOOLEAN"
            + ")";

    public static final String SKYLOCK_LOCKS_TABLE = "CREATE TABLE IF NOT EXISTS "
            + SKYLOCK_LOCK_TABLE_NAME
            + " ("
            + LockDataBeen.LOCK_COLUME_MAC_ID + "  TEXT  NOT NULL PRIMARY KEY,"
            + LockDataBeen.LOCK_COLUME_LATITUDE + " TEXT,"
            + LockDataBeen.LOCK_COLUME_LONGITUDE + "  TEXT,"
            + LockDataBeen.LOCK_COLUME_USER_ID + " TEXT,"
            + LockDataBeen.LOCK_COLUME_SHARED_ON + " TEXT,"
            + LockDataBeen.LOCK_COLUME_SHARED_TILL + " TEXT,"
            + LockDataBeen.LOCK_COLUME_SHARED_TO + " TEXT,"
            + LockDataBeen.LOCK_COLUME_LOCK_TYPE_FLAG + " BOOLEAN"
            + ")";
    public static final String SKYLOCK_FRIEND_TABLE = "CREATE TABLE IF NOT EXISTS "
            + SKYLOCK_FRIEND_TABLE_NAME
            + " ("
            + FriendBean.FIRST_ID + "  TEXT NOT NULL PRIMARY KEY,"
            + FriendBean.FIRST_NAME + " TEXT,"
            + FriendBean.LAST_NAME + "  TEXT"
            + ")";


    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    private boolean opened = false;

    public Dbfunction(Context aContext) {

        databaseHelper = new DatabaseHelper(aContext);
    }

    public Dbfunction open() throws SQLException {
        db = databaseHelper.getWritableDatabase();
        opened = true;
        return this;
    }

    /**
     * Close database
     */
    public void close() {
        databaseHelper.close();
        opened = false;
    }

    public boolean isOpen() {
        return opened;
    }

    public static boolean doesDatabaseExist(Context context, String dbName) {
        File dbFile = context.getDatabasePath(SKYLOCK_DATABASE_NAME);
        if (!dbFile.exists())
            Log.i("Database", "Not Found");
        else
            Log.i("Database", "Found");
        return dbFile.exists();
    }

    public void insertUserDetails(UserRegistrationParameter userRegistrationParameter) {
        ContentValues values = new ContentValues();
        values.put(USER_COLUMN_USER_ID, userRegistrationParameter.getUsers_id());
        values.put(USER_COLUMN_PASSWORD, userRegistrationParameter.getPassword());
        values.put(USER_COLUMN_FACEBOOK_FLAG, userRegistrationParameter.getUser_type());
        values.put(USER_COLUMN_GCM_REGISTRATION_ID, userRegistrationParameter.getReg_id());
        db.insert(SKYLOCK_USER_TABLE_NAME, null, values);
        //  Log.i("prabhu","insert notification" +	db.insert(MESSAGE_NOTIFICATION_TABLE_NAME, "messageNotificationTableId" ,notificationMessages.getDbContentValues(notificationMessages))+"::"+notificationMessages.getMessageType()+"::"+notificationMessages.getMessageSubject());
    }

    public void insertLockDetails(LockDataBeen lockDataBeen) {

        db.insert(SKYLOCK_LOCKS_TABLE, null, lockDataBeen.getLockContentValue());
        //  Log.i("prabhu","insert notification" +	db.insert(MESSAGE_NOTIFICATION_TABLE_NAME, "messageNotificationTableId" ,notificationMessages.getDbContentValues(notificationMessages))+"::"+notificationMessages.getMessageType()+"::"+notificationMessages.getMessageSubject());
    }

    public void insertFriendDetails(FriendBean friendBean) {

        db.insert(SKYLOCK_FRIEND_TABLE, null, friendBean.getFriendContentValue());
        //  Log.i("prabhu","insert notification" +	db.insert(MESSAGE_NOTIFICATION_TABLE_NAME, "messageNotificationTableId" ,notificationMessages.getDbContentValues(notificationMessages))+"::"+notificationMessages.getMessageType()+"::"+notificationMessages.getMessageSubject());
    }

    public ArrayList<FriendBean> getFriendListFromDb() {

        ArrayList<FriendBean> friendList = new ArrayList<FriendBean>();
        String selectQuery = "SELECT  * FROM " + SKYLOCK_FRIEND_TABLE;
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                FriendBean friendBean = new FriendBean();
                friendBean.setFriend_id(cursor.getString(0));
                friendBean.setFirst_name(cursor.getString(1));
                friendBean.setLast_name(cursor.getString(2));
                // Adding contact to list
                friendList.add(friendBean);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return friendList;
    }

    // Deleting single friend
    public void deleteContact(FriendBean friendBean) {
        db.delete(SKYLOCK_FRIEND_TABLE, FriendBean.FIRST_ID + " = ?",
                new String[]{String.valueOf(friendBean.getFriend_id())});
        db.close();
    }

    // Deleting single friend
    public void deleteAccount() {
        db.delete(SKYLOCK_USER_TABLE_NAME, null, null);
        db.close();
    }

}
