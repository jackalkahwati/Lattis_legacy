package com.lattis.ellipse.Utils;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class FirebaseUtil {

    private static FirebaseUtil firebaseUtil;
    private FirebaseAnalytics mFirebaseAnalytics;

    public static FirebaseUtil getInstance(){
        if(firebaseUtil==null) {
            firebaseUtil = new FirebaseUtil();
        }
        return firebaseUtil;
    }

    public void instantiateSDK(Application application){
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(application);
    }

    public void addUserIdAndEmail(@NonNull String userId, @NonNull String email){
        mFirebaseAnalytics.setUserId(userId);
        mFirebaseAnalytics.setUserProperty("email",email);


        FirebaseCrashlytics.getInstance().setUserId(userId);
        FirebaseCrashlytics.getInstance().setCustomKey("email",email);
    }

    public void addSignInEvent(String userId, String email){
        Bundle bundle = new Bundle();
        bundle.putString("user_id",userId);
        bundle.putString("email",email);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);


        FirebaseCrashlytics.getInstance().setUserId(userId);
        FirebaseCrashlytics.getInstance().setCustomKey("email",email);
    }

    public void addSignUpEvent(String userId, String email){
        Bundle bundle = new Bundle();
        bundle.putString("user_id",userId);
        bundle.putString("email",email);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);


        FirebaseCrashlytics.getInstance().setUserId(userId);
        FirebaseCrashlytics.getInstance().setCustomKey("email",email);
    }

    public void addCustomEvent(String event_name, String message){
        Bundle bundle = new Bundle();
        bundle.putString("message",message);
        mFirebaseAnalytics.logEvent(event_name,bundle);
    }

    public void logException(Throwable e){
        FirebaseCrashlytics.getInstance().recordException(e);
    }
}
