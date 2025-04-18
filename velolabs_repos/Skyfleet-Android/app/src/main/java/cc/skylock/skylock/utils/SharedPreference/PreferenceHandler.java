package cc.skylock.skylock.utils.SharedPreference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Velo Labs Android on 14-01-2016.
 */
public class PreferenceHandler {

    private static SharedPreferences sharedPreferences = null;
    private static SharedPreferences.Editor editor = null;

    public static void storeStringtoPerference(Context context, String key, String data) {
        sharedPreferences = context.getSharedPreferences(Myconstants.SHAREDPREFERENCE_KEY, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(key, data);
        editor.commit();
    }

    public static String getStringFromPreference(Context context, String key) {
        sharedPreferences = context.getSharedPreferences(Myconstants.SHAREDPREFERENCE_KEY, Context.MODE_PRIVATE);
        String values = sharedPreferences.getString(key, null);
        if (values != null) {
            return values;
        }
        return null;
    }

    public static void putBooleanToPerference(Context context, String key,boolean data)
    {
        sharedPreferences = context.getSharedPreferences(Myconstants.SHAREDPREFERENCE_KEY, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putBoolean(key, data);
        editor.commit();

    }

    public static boolean getBooleanToPerference(Context context, String key) {
        sharedPreferences = context.getSharedPreferences(Myconstants.SHAREDPREFERENCE_KEY, Context.MODE_PRIVATE);
        boolean values = sharedPreferences.getBoolean(key,false);
        if (values) {
            return values;
        }
        return false;
    }
}


