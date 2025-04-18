package com.lattis.ellipse.presentation.setting;

import android.content.SharedPreferences;

/**
 * Created by ssd3 on 7/24/17.
 */

public class IntPref extends AbstractPref<Integer> {

    public IntPref(SharedPreferences sharedPreferences, String settingKey, int defaultValue) {
        super(sharedPreferences, settingKey, defaultValue);
    }

    @Override
    public Integer getValue() {
        return getSharedPreferences().getInt(getSettingKey(),getDefaultValue());
    }

    @Override
    public void setValue(Integer value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putInt(getSettingKey(),value);
        editor.apply();
    }
}
