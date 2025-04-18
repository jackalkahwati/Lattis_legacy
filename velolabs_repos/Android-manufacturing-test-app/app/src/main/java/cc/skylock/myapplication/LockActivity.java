package cc.skylock.myapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import cc.skylock.myapplication.Uitls.FileUtils;
import cc.skylock.myapplication.Uitls.Myconstants;
import cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothLEService;

public class LockActivity extends AppCompatActivity implements BluetoothDeviceStatus, View.OnClickListener {
    Button button_lock, button_nextText, button_unlock, bLockPass;
    TextView textView_status;
    ImageView imageView_bleConnectionStatus;
    TextView textView_latchposition;
    Handler delayHandler = new Handler();
    int latch_position = 0;
    private static ProgressBar loadProgressBar;
    Context mContext;
    SkylockBluetoothLEService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        Context mContext = getApplicationContext();
        getSupportActionBar().setTitle("Skylock Manufacture " + FileUtils.getVersionName(mContext));
        button_lock = (Button) findViewById(R.id.button_lock);
        button_nextText = (Button) findViewById(R.id.bCapTouchNext);
        button_unlock = (Button) findViewById(R.id.button_unlock);
        bLockPass = (Button) findViewById(R.id.bLockPass);
        loadProgressBar = (ProgressBar) findViewById(R.id.progressbar_load);
        textView_status = (TextView) findViewById(R.id.textView_loading);
        imageView_bleConnectionStatus = (ImageView) findViewById(R.id.iv_connectionStatus);
        textView_latchposition = (TextView) findViewById(R.id.tv_latch);
        button_lock.setOnClickListener(this);
        button_nextText.setOnClickListener(this);
        button_unlock.setOnClickListener(this);
        bLockPass.setOnClickListener(this);
    }

    private void saveToDB(int latch_position) {
        String data = null;
        if (latch_position == 1) {
            data = "\n" + " SET LOCK" + "=" + "PASS" + "\n";
        } else if (latch_position == 2) {
            data = "\n" + " SET LOCK" + "=" + "PASS" + "\n";
        } else {
            data = "\n" + " SET LOCK" + "=" + "FAIL" + "\n";
        }
        FileUtils.writeFile(Myconstants.lock_fileName, data);
//        System.out.println(FileUtils.readFromFile(LockActivity.this, Myconstants.lock_fileName));


    }

    @Override
    protected void onResume() {
        service_init();
        try {
            if (SkylockBluetoothLEService.mCurrentlyconnectedGatt != null) {
                imageView_bleConnectionStatus.setImageResource(R.drawable.green);

            } else {
                imageView_bleConnectionStatus.setImageResource(R.drawable.red);
                loadProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(LockActivity.this, "No BLE device", Toast.LENGTH_SHORT).show();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bCapTouchNext: {
                saveToDB(latch_position);
                finish();
                Intent intent = new Intent(LockActivity.this, SplashActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.bLockPass: {
                saveToDB(latch_position);
                finish();
            }
            break;
            case R.id.button_lock: {
                try {
                    mService.lock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case R.id.button_unlock: {
                try {
                    mService.unLock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;

        }

    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
            if (mService == null) {
                service_init();
            }
            mService.registerBluetoothDeviceStatusListener(LockActivity.this);
            mService.enableHardwareNotification(true);
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService.unregisterBluetoothDeviceStatusListener();
            mService = null;
        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, SkylockBluetoothLEService.class);
        getApplicationContext().bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onScanFailed() {

    }

    @Override
    public void onScanedDevice(BluetoothDevice device) {

    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onConnected(BluetoothGatt mBluetoothGatt) {

    }

    @Override
    public void onConnectionFailed(boolean shippingModeEnabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView_bleConnectionStatus.setImageResource(R.drawable.red);
                Toast.makeText(LockActivity.this, "No BLE device", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onDisConnect() {

    }

    @Override
    public void onGetHardWareInfo(BluetoothGatt mBluetoothGatt, final BluetoothGattCharacteristic mCharacteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadProgressBar.setVisibility(View.INVISIBLE);
                latch_position = mCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 4);
                Log.i("Latch Position ", " " + latch_position);
                textView_latchposition.setText("Latch Position = " + latch_position);
                loadProgressBar.setVisibility(View.INVISIBLE);
                saveToDB(latch_position);

            }
        });

    }

    @Override
    public void onDescriptorWrite(BluetoothGatt mBluetoothGatt) {
        if (mService != null) {
            mService.lock();
        }
    }

    @Override
    protected void onPause() {

        finish();
        super.onPause();

    }


    @Override
    public void onDeviceStatus(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic mCharacteristic) {

    }

    @Override
    public void onGetSerialInfo(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onLedBlink(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onFWUpgradeProgress(FWUpgradeProgress fwUpgradeProgress) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unbindService(mServiceConnection);
        getApplicationContext().stopService(new Intent(this, SkylockBluetoothLEService.class));
    }

}
