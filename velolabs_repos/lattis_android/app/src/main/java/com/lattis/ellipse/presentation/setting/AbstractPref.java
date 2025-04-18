package com.lattis.ellipse.presentation.setting;

import android.content.SharedPreferences;

abstract class AbstractPref<T> {

    private SharedPreferences sharedPreferences;
    private String settingKey;
    private T defaultValue;

    AbstractPref(SharedPreferences sharedPreferences, String settingKey, T defaultValue) {
        this.sharedPreferences = sharedPreferences;
        this.settingKey = settingKey;
        this.defaultValue = defaultValue;
    }

    public abstract T getValue();

    T getDefaultValue(){
        return defaultValue;
    }

    public abstract void setValue(T value);

    SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    String getSettingKey() {
        return settingKey;
    }

    public void remove() {
        sharedPreferences.edit().remove(getSettingKey()).commit();
    }
}
