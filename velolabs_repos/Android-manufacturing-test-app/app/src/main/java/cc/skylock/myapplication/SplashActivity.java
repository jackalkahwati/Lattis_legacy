package cc.skylock.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import cc.skylock.myapplication.Uitls.FileUtils;
import cc.skylock.myapplication.Uitls.Myconstants;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothLEService;

public class SplashActivity extends AppCompatActivity {
    Context context;
    ImageView imageView_bleConnectionStatus;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        context = SplashActivity.this;
        getSupportActionBar().setTitle("Skylock Manufacture " + FileUtils.getVersionName(context));
        imageView_bleConnectionStatus = (ImageView) findViewById(R.id.iv_connectionStatus);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SkylockBluetoothLEService.mCurrentlyconnectedGatt!=null) {
            imageView_bleConnectionStatus.setImageResource(R.drawable.green);
        } else {
            imageView_bleConnectionStatus.setImageResource(R.drawable.red);
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        finish();
        super.onPause();
    }

}
