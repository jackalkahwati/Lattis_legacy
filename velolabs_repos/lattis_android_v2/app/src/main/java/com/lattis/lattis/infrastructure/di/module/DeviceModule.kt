package com.lattis.lattis.infrastructure.di.module

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import com.google.i18n.phonenumbers.PhoneNumberUtil
import dagger.Module
import dagger.Provides
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

@Module
class DeviceModule {
    @Provides
    @Singleton
    fun provideTelephonyManager(context: Context): TelephonyManager {
        return context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    @Provides
    @Singleton
    @Named("ISO31662Code")
    fun provideCountryCode(telephonyManager: TelephonyManager): String {
        val countryIsoCode = telephonyManager.networkCountryIso.toUpperCase()
        return if (countryIsoCode.equals("", ignoreCase = true)) {
            Locale.getDefault().country
        } else countryIsoCode
    }

    @Provides
    @Singleton
    @Named("ISDCode")
    fun provideRegionCode(
        phoneNumberUtil: PhoneNumberUtil,
        @Named("ISO31662Code") countryCode: String
    ): Int {
        return phoneNumberUtil.getCountryCodeForRegion(countryCode)
    }

    @Provides
    @Singleton
    fun providePhoneNumberUtil(): PhoneNumberUtil {
        return PhoneNumberUtil.getInstance()
    }

    @Provides
    @Singleton
    @Named("DeviceModel")
    fun provideDeviceModel(): String {
        return try {
            Build.MODEL
        } catch (e: Exception) {
            ""
        }
    }

    @Provides
    @Singleton
    @Named("DeviceOS")
    fun provideDeviceOS(): String {
        return try {
            "Android-OS:" + Build.VERSION.RELEASE + "-API:" + Build.VERSION.SDK_INT
        } catch (e: Exception) {
            ""
        }
    }

    @Provides
    @Singleton
    @Named("DeviceLanguage")
    fun provideDeviceLanguage(): String {
        return Locale.getDefault().language.toString()
    }
}