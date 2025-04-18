package com.lattis.lattis.utils.settings

import android.content.SharedPreferences

abstract class AbstractPref<T>(
    val sharedPreferences: SharedPreferences,
    val settingKey: String,
    val defaultValue: T
) {
    abstract var value: T

    fun remove() {
        sharedPreferences.edit().remove(settingKey).commit()
    }

}