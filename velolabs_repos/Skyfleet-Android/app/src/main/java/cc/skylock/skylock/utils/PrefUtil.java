package cc.skylock.skylock.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Set;

public class PrefUtil {
	Context context;
	public PrefUtil(Context context){
		this.context = context.getApplicationContext();
	}
	
	public static SharedPreferences getSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	public void setStringPref(String key, String value){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		editor.commit();
	}
	public void setIntPref(String key, int value){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	public void setBooleanPref(String key, boolean value){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	public void setLongPref(String key, Long value){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(key, value);
		editor.commit();
	}
    public void setStringSetPref(String key,Set<String> value){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(key, value);
        editor.commit();
    }
    public void setObjectPref(String key,Object value){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        Gson gson = new Gson();
        String json = gson.toJson(value);
        editor.putString("MyObject", json);
        editor.commit();
    }
    public String getObjectPref(String key, Object defValue){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        String json = settings.getString("MyObject", "");
        return json;
    }
    public Set<String> getStringSetPref(String key, HashSet<String> defValue){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getStringSet(key,defValue);
    }
	public String getStringPref(String key, String defValue){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getString(key, defValue);
	}
	public int getIntPref(String key, int defValue){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getInt(key, defValue);
	}
	public boolean getBooleanPref(String key, boolean defValue){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getBoolean(key, defValue);
	}
	
	public long getLongPref(String key, Long defValue){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getLong(key, defValue);
	}
	public void removePref(String Key){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(Key);
		editor.commit();
	}
	public void clearAllPref(){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		editor.commit();
	}
}
