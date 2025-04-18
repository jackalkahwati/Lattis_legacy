package com.lattis.lattis.utils.settings

import android.content.SharedPreferences

class IntPref(
    sharedPreferences: SharedPreferences,
    settingKey: String,
    defaultValue: Int
) : AbstractPref<Int?>(sharedPreferences, settingKey, defaultValue) {
    override var value: Int?
        get() = sharedPreferences.getInt(settingKey, defaultValue!!)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(settingKey, value!!)
            editor.apply()
        }

}