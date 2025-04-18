package com.lattis.ellipse.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class AccountAuthenticatorService extends Service {

    AccountAuthenticator accountAuthenticator;

    @Override
    public void onCreate() {
       accountAuthenticator = new AccountAuthenticator(getApplicationContext());
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) {
        return accountAuthenticator.getIBinder();
    }
}
