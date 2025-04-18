package cc.skylock.skylock;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Base64;
import android.util.Log;

import cc.skylock.skylock.cc.skylock.skylock.sharedpreference.Myconstants;
import cc.skylock.skylock.cc.skylock.skylock.sharedpreference.PreferenceHandler;
import cc.skylock.skylock.generator.HashGenerator;

/**
 * Created by AlexVijayRaj on 8/11/2015.
 */
public class SplashActivity extends Activity {

    Context context = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        Intent mainIntent = new Intent(context, MainActivity.class);
        context.startActivity(mainIntent);
        SplashActivity.this.finish();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            //    HashGenerator.checkSumSHA256("Velolabsindia");
                HashGenerator.aTOb();
            }
        }, 1000);

    }


}
