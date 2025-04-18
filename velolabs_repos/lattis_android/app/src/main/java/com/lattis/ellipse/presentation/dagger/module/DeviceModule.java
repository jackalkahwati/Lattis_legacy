package com.lattis.ellipse.presentation.dagger.module;

import android.accounts.AccountManager;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.lattis.ellipse.data.platform.AndroidAccountRepository;
import com.lattis.ellipse.data.platform.mapper.AccountMapper;
import com.lattis.ellipse.domain.repository.AccountRepository;
import com.lattis.ellipse.presentation.dagger.qualifier.AccountType;
import com.lattis.ellipse.presentation.dagger.qualifier.AuthenticationTokenType;
import com.lattis.ellipse.presentation.dagger.qualifier.DeviceModel;
import com.lattis.ellipse.presentation.dagger.qualifier.DeviceOS;
import com.lattis.ellipse.presentation.dagger.qualifier.ISDCode;
import com.lattis.ellipse.presentation.dagger.qualifier.ISO31662Code;

import java.util.Locale;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DeviceModule {

    @Provides
    @Singleton
    TelephonyManager provideTelephonyManager(Context context){
        return (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Provides
    @Singleton
    @ISO31662Code
    String provideCountryCode(TelephonyManager telephonyManager){
        String countryIsoCode = telephonyManager.getNetworkCountryIso().toUpperCase();
        if (countryIsoCode.equalsIgnoreCase("")) {
            return Locale.getDefault().getCountry();
        }
        return countryIsoCode;
    }

    @Provides
    @Singleton
    @ISDCode
    int provideRegionCode(PhoneNumberUtil phoneNumberUtil,
                          @ISO31662Code String countryCode){
        return phoneNumberUtil.getCountryCodeForRegion(countryCode);
    }

    @Provides
    @Singleton
    PhoneNumberUtil providePhoneNumberUtil(){
        return PhoneNumberUtil.getInstance();
    }


    @Provides
    @Singleton
    @DeviceModel
    String provideDeviceModel(){
        try{
            return android.os.Build.MODEL;
        }catch (Exception e){
            return "";
        }

    }

    @Provides
    @Singleton
    @DeviceOS
    String provideDeviceOS(){
        try{
            return "Android-OS:"+android.os.Build.VERSION.RELEASE+"-API:"+android.os.Build.VERSION.SDK_INT;
        }catch (Exception e){
            return "";
        }
    }



    @Provides
    @Singleton
    AccountRepository provideAccountRepository(AccountManager accountManager,
                                               AccountMapper accountMapper,
                                               @AccountType String accountType,
                                               @AuthenticationTokenType String authTokenType){
        return new AndroidAccountRepository(accountManager,accountMapper,accountType,authTokenType);
    }
}
