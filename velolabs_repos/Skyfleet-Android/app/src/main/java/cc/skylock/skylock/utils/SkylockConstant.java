package cc.skylock.skylock.utils;

import java.util.ArrayList;

/**
 * Created by Velo Labs Android on 22-01-2016.
 */
public class SkylockConstant {
    // PRODUCTION URL
    public static final String BASE_URL = "https://oval.lattisapi.io/api/";
    // DEVELOPMENT URL
    //  public static final String BASE_URL = "http://oval-dev.us-west-1.elasticbeanstalk.com/api/";

    public static final String BASE_URL_HELP = "https://lattis.helpscoutdocs.com/";

    public static final String BASE_URL_ORDER = "https://www.lattis.io/";

    public static final String GCM_SENDER_ID = "638979384071";

    public static String userToken = "";

    public static final int NOTIFICATION_ID = 1000;

    public static final String PREF_USER_TOKEN = "cc.skylock.skylock.userToken";

    public static final String PREF_USER_ID = "cc.skylock.skylock.userId";

    public static final String PREF_USERS_ID = "cc.skylock.skylock.usersId";

    public static final String PREF_USER_DETAILS = "cc.skylock.skylock.userdetails";

    public static final String PREF_USER_EMAIL = "cc.skylock.skylock.user.email";

    public static final String PREF_EMERGENCY_CONTACT_LIST = "cc.skylock.skylock.emergencyContact";

    public static final String PREF_GCM_NOTIFICATIONI_KEY = "cc.skylock.skylock.GCMNotificationKey";

    public static final String PREF_LOCK_LIST = "cc.skylock.skylock.LockList";

    public static final String PREF_LOCK_DETAILS = "cc.skylock.skylock.LockDetails";

    public static final String PREF_LOCK_ID = "cc.skylock.skylock.LockId";

    public static final String SKYLOCK_PUBLIC_KEYS = "cc.skylock.skylock.publicmessages";

    public static final String SKYLOCK_SIGNED_MESSAGES = "cc.skylock.skylock.signedmessage";

    public static final int SKYLOCK_CRASHSELECTION = 1;

    public static final int SKYLOCK_THEFTSELECTION = 2;

    public static final String FW_VERSION = "cc.skylock.skylock.firmwareVersion";

    public static final String BOARD_MANUFACTURING_NUMBER = "cc.skylock.skylock.manufacturing.number";

    public static final String PREF_LOCK_LOCATION = "cc.skylock.skylock.lock.location";

    public static final String LAST_CONNECTED_TIMESTAMP = "cc.skylock.skylock.last.connected.timestamp";

    public static final String SKYLOCK_CRASH_ENABLE = "cc.skylock.skylock.crash.enable";

    public static final String SKYLOCK_THEFT_ENABLE = "cc.skylock.skylock.theft.enable";

    public static final String SKYLOCK_PROXIMITY_LOCK_ENABLE = "cc.skylock.skylock.proximity.lock.enable";

    public static final String SKYLOCK_PROXIMITY_UNLOCK_ENABLE = "cc.skylock.skylock.proximity.unlock.enable";

    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";

    public final static String THEFT_DETECTION_SENSITIVITY_LEVEL =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";

    public final static String CRYPT_KEY = "MAID";

    public static final String SKYLOCK_PRIMARY = "cc.skylock.skylock.primary";

    public static final String PREF_KEY_SHARED_LOCK = "cc.skylock.skylock.shared.key";

    public static final String PREF_KEY_LOCK_SHARED_TO = "cc.skylock.skylock.lock.shared.to";

    public static final String PREF_KEY_FIRST_NAME = "cc.skylock.skylock.lock.first.name";

    public static final String PREF_KEY_LAST_NAME = "cc.skylock.skylock.lock.last.name";

    public static final String PREF_LOCK_THEFT_SENSITIVITY = "cc.skylock.skylock.lock.theft.sensitivity";

    public static final String PREF_KEY__TERMS_AND_CONDITION = "cc.skylock.skylock.TERMS.AND.CONDITION";

    public static int LOCK_CRASH_ID = 0;

    public static int LOCK_THEFT_ID = 0;

    public static final String PREF_KEY__ACCEPT_TERMS_AND_CONDITION = "cc.skylock.skylock.ACCEPT.TERMS.AND.CONDITION";

    public static ArrayList<String> mLockMacIdList = new ArrayList<>();
}
