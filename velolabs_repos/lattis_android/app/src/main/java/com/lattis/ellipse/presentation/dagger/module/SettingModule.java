package com.lattis.ellipse.presentation.dagger.module;

import android.content.SharedPreferences;

import com.lattis.ellipse.presentation.setting.BooleanPref;
import com.lattis.ellipse.presentation.setting.IntPref;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class SettingModule {

    public static final String PREF_KEY__ACCEPT_TERMS_AND_CONDITION = "cc.skylock.skylock.ACCEPT.TERMS.AND.CONDITION";
    public static final String KEY_FIRST_TIME_WALK_THROUGH_STRING = "key_first_time_walk_through_string";
    public static final String KEY_FIRST_TIME_SHARING_GUIDE_STRING = "key_first_time_sharing_guide_string";
    public static final String KEY_HAS_ACCEPTED_TERMS_CONDITIONS = "KEY_HAS_ACCEPTED_TERMS_CONDITIONS";
    public static final String KEY_SHOW_ON_BOARDING_FLOW = "KEY_SHOW_ON_BOARDING_FLOW";

    public static final String KEY_RIDE_WALK_THROUGH_STRING = "RIDE_WALK_THROUGH";
    public static final String KEY_RIDE_COUNT = "KEY_RIDE_COUNT";
    public static final String KEY_QR_CODE_HELP_COUNT = "KEY_QR_CODE_HELP_COUNT";


    @Provides
    @Singleton
    @Named(PREF_KEY__ACCEPT_TERMS_AND_CONDITION)
    BooleanPref provideAcceptTCUseCase(SharedPreferences sharedPreferences){
        return new BooleanPref(sharedPreferences,PREF_KEY__ACCEPT_TERMS_AND_CONDITION,false);
    }

    @Provides
    @Singleton
    @Named(KEY_FIRST_TIME_WALK_THROUGH_STRING)
    BooleanPref provideFirstTimeWalkThrough(SharedPreferences sharedPreferences) {
        return new BooleanPref(sharedPreferences,KEY_FIRST_TIME_WALK_THROUGH_STRING,true);
    }

    @Provides
    @Singleton
    @Named(KEY_FIRST_TIME_SHARING_GUIDE_STRING)
    BooleanPref provideFirstTimeSharingGuide(SharedPreferences sharedPreferences) {
        return new BooleanPref(sharedPreferences,KEY_FIRST_TIME_SHARING_GUIDE_STRING,true);
    }

    @Provides
    @Singleton
    @Named(KEY_HAS_ACCEPTED_TERMS_CONDITIONS)
    BooleanPref provideHasSeenTermsConditions(SharedPreferences sharedPreferences) {
        return new BooleanPref(sharedPreferences, KEY_HAS_ACCEPTED_TERMS_CONDITIONS, false);
    }

    @Provides
    @Singleton
    @Named(KEY_SHOW_ON_BOARDING_FLOW)
    BooleanPref provideShowOnBoardingFlow(SharedPreferences sharedPreferences) {
        return new BooleanPref(sharedPreferences, KEY_SHOW_ON_BOARDING_FLOW, false);
    }

    @Provides
    @Singleton
    @Named(KEY_RIDE_WALK_THROUGH_STRING)
    IntPref provideRideWalkThrough(SharedPreferences sharedPreferences) {
        return new IntPref(sharedPreferences, KEY_SHOW_ON_BOARDING_FLOW, 0);
    }
    @Provides
    @Singleton
    @Named(KEY_QR_CODE_HELP_COUNT)
    IntPref provideQRCodeHelpCount(SharedPreferences sharedPreferences) {
        return new IntPref(sharedPreferences, KEY_QR_CODE_HELP_COUNT, 0);
    }

    @Provides
    @Singleton
    @Named(KEY_RIDE_COUNT)
    IntPref provideRideCount(SharedPreferences sharedPreferences) {
        return new IntPref(sharedPreferences, KEY_RIDE_COUNT, 0);
    }



}
