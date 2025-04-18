package com.lattis.ellipse.presentation.setting;

import android.content.SharedPreferences;

public class BooleanPref extends AbstractPref<Boolean> {

    public BooleanPref(SharedPreferences sharedPreferences, String settingKey, Boolean defaultValue) {
        super(sharedPreferences, settingKey, defaultValue);
    }

    @Override
    public Boolean getValue() {
        return getSharedPreferences().getBoolean(getSettingKey(),getDefaultValue());
    }

    @Override
    public void setValue(Boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(getSettingKey(),value);
        editor.apply();
    }
}
