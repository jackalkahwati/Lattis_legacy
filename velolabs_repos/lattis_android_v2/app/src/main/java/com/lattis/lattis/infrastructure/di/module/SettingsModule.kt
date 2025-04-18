package com.lattis.lattis.infrastructure.di.module

import android.content.Context
import android.content.SharedPreferences
import com.lattis.lattis.utils.settings.IntPref
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class SettingsModule {

    companion object {
        const val KEY_SHOWING_CONNECT_TO_LOCK = "KEY_SHOWING_CONNECT_TO_LOCK"
        const val KEY_SHOWING_TUTORIAL = "KEY_SHOWING_TUTORIAL"
    }

    @Provides
    @Singleton
    @Named(KEY_SHOWING_CONNECT_TO_LOCK)
    fun provideConnectToLock(sharedPreferences: SharedPreferences): IntPref? {
        return IntPref(
            sharedPreferences,
            KEY_SHOWING_CONNECT_TO_LOCK,
            0
        )
    }

    @Provides
    @Singleton
    @Named(KEY_SHOWING_TUTORIAL)
    fun provideTutorial(sharedPreferences: SharedPreferences): IntPref? {
        return IntPref(
            sharedPreferences,
            KEY_SHOWING_TUTORIAL,
            0
        )
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
    }
}