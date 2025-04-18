package com.lattis.ellipse.presentation.setting;

import android.content.SharedPreferences;

public class StringPref extends AbstractPref<String> {

    public StringPref(SharedPreferences sharedPreferences, String settingKey, String defaultValue) {
        super(sharedPreferences, settingKey, defaultValue);
    }

    @Override
    public String getValue() {
        return getSharedPreferences().getString(getSettingKey(),getDefaultValue());
    }

    @Override
    public void setValue(String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(getSettingKey(),value);
        editor.apply();
    }
}
