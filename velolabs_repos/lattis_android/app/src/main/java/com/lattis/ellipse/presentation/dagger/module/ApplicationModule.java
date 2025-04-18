package com.lattis.ellipse.presentation.dagger.module;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.lattis.ellipse.data.network.base.ApiEndpoints;
import com.lattis.ellipse.domain.executor.JobExecutor;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.presentation.UIThread;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.lattis.ellipse.BuildConfig;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

@Module
public class ApplicationModule {

    private Application application;

    public ApplicationModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return application;
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @Singleton
    //@DeviceId
    @SuppressLint("HardwareIds")
    String provideDeviceId() {
        return Settings.Secure.getString(application.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Provides
    @Singleton
    ThreadExecutor provideThreadExecutor(JobExecutor jobExecutor) {
        return jobExecutor;
    }

    @Provides
    @Singleton
    PostExecutionThread providePostExecutionThread(UIThread uiThread) {
        return uiThread;
    }

    @Provides
    @Singleton
    ApiEndpoints provideApiEndpoints() {

        if(BuildConfig.FLAVOR=="lattisBeta"){
            return ApiEndpoints.PRODUCTION;
        }else if(BuildConfig.FLAVOR=="lattisDev"){
            return ApiEndpoints.STAGING;
        }else{
            return ApiEndpoints.PRODUCTION;
        }

    }

    @Provides
    @Singleton
    AppUpdateManager getAppUpdateManager(Context context){
        return AppUpdateManagerFactory.create(context);
    }
}
