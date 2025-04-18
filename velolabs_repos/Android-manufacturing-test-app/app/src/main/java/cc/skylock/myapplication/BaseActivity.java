package cc.skylock.myapplication;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

import cc.skylock.myapplication.Uitls.FileUtils;

/**
 * Created by Velo Labs Android on 02-12-2016.
 */

public class BaseActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        getSupportActionBar().setTitle("Skylock Manufacture " + FileUtils.getVersionName(getApplicationContext()));

    }
}
