package cc.skylock.myapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
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

public class SelfActivity extends AppCompatActivity implements BluetoothDeviceStatus {

    TextView textView_battery, textView_itc_ACC, textView_itc_MAG,
            textView_acce, textView_itc_CAP, textView_itc_motor, textView_status;
    Button button_Next;
    Button button_selfText;
    final String labels[] = {" SET BATTERY=", " SET I2C_ACCE=",
            " SET I2C_MAG=", " SET I2C_CAP=",
            " SET I2C_MOTOR=", " SET ACCE="};
    boolean i2c_value = false, i2c_acc = false, i2c_touch = false, self_value = false, i2c_com_value = false, i2c_mag = false, i2c_motar = false, isI2c_acc_self = false;
    ProgressBar loadProgressBar;
    ImageView imageView_connection;
    BroadcastReceiver mBroadcastReceiver;
    private String[] selfTest = new String[6];
    float battery = 0;
    SkylockBluetoothLEService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self);
        Context context = getApplicationContext();
        getSupportActionBar().setTitle("Skylock Manufacture " + FileUtils.getVersionName(context));
        imageView_connection = (ImageView) findViewById(R.id.iv_connectionStatus);
        loadProgressBar = (ProgressBar) findViewById(R.id.progressbar_load);
        textView_battery = (TextView) findViewById(R.id.tv_battery);
        textView_itc_ACC = (TextView) findViewById(R.id.I2C_ACCE);
        textView_itc_MAG = (TextView) findViewById(R.id.I2C_MAG);
        textView_itc_motor = (TextView) findViewById(R.id.I2C_MOTOR);
        textView_acce = (TextView) findViewById(R.id.opt);
        textView_itc_CAP = (TextView) findViewById(R.id.I2C_CAP);
        button_selfText = (Button) findViewById(R.id.b_title);
        button_Next = (Button) findViewById(R.id.bCapTouchNext);
        loadProgressBar.setVisibility(View.VISIBLE);
        button_Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveTODB();
                    finish();
                    Intent intent = new Intent(SelfActivity.this, SplashActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        button_selfText.setEnabled(false);

        button_selfText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mService.getRSSIvalueForSelf();
                } catch (Exception e) {
                    finish();
                }
            }
        });
    }

    private void saveTODB() {
        collectSelfInfo();
        String data = convertStringToArray();
        FileUtils.writeFile(Myconstants.self_fileName, data);
//        System.out.println(FileUtils.readFromFile(SelfActivity.this, Myconstants.self_fileName));
    }

    private String convertStringToArray() {
        StringBuilder builder = new StringBuilder();
        /*for (String s : Myconstants.captouchState) {
            builder.append(s);
        }*/
        String test_Status = null;
        for (int i = 0; i < selfTest.length; i++) {
            test_Status = "\n" + labels[i] + selfTest[i] + "\n";
            builder.append(test_Status);
        }
        return builder.toString();
    }

    private void collectSelfInfo() {

        selfTest[0] = String.valueOf(battery);
        if (i2c_acc)
            selfTest[1] = "PASS";
        else
            selfTest[1] = "FAIL";
        if (i2c_mag)
            selfTest[2] = "PASS";
        else
            selfTest[2] = "FAIL";
        if (i2c_touch)
            selfTest[3] = "PASS";
        else
            selfTest[3] = "FAIL";

        if (i2c_motar)
            selfTest[4] = "PASS";
        else
            selfTest[4] = "FAIL";
        if (isI2c_acc_self)
            selfTest[5] = "PASS";
        else
            selfTest[5] = "FAIL";


    }

    @Override
    protected void onResume() {
        service_init();
        try {
            if (SkylockBluetoothLEService.mCurrentlyconnectedGatt != null) {
                imageView_connection.setImageResource(R.drawable.green);

            } else {
                disconnection();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    private void disconnection() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SelfActivity.this, "No BLE Connection", Toast.LENGTH_SHORT).show();
                imageView_connection.setImageResource(R.drawable.red);
                loadProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
            if (mService == null) {
                service_init();
            }
            mService.enableHardwareNotification(true);
            mService.registerBluetoothDeviceStatusListener(SelfActivity.this);
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
    protected void onPause() {
        if (mService != null) {
            mService.stopSelfTimer();
        }
        mService.enableHardwareNotification(false);
        super.onPause();
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

    }

    @Override
    public void onDisConnect() {
        disconnection();
    }

    @Override
    public void onGetHardWareInfo(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic mCharacteristic) {
        if (mCharacteristic != null) {

            try {
                final int batvoltage = mCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
                battery = (float) batvoltage / 1000;
                final int one = mCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 11);
                i2c_acc = (1 == ((one >> 0) & 1));
                i2c_mag = (1 == ((one >> 1) & 1));
                i2c_touch = (1 == ((one >> 2) & 1));
                i2c_motar = (1 == ((one >> 3) & 1));
                isI2c_acc_self = (1 == ((one >> 4) & 1));
                if (i2c_motar && i2c_acc) {
                    i2c_value = true;
                }
                if (i2c_touch && i2c_mag) {
                    i2c_com_value = true;
                }
                if (i2c_value && i2c_com_value) {
                    self_value = true;
                }
                //    byte[] solar = CHAR_HW_INFO.getValue();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button_selfText.setEnabled(true);
                        loadProgressBar.setVisibility(View.GONE);
                        textView_battery.setText("Battery = " + battery);
                        textView_itc_ACC.setText("I2C_ACCE = " + i2c_acc);
                        textView_itc_MAG.setText("I2C_MAG = " + i2c_mag);
                        textView_itc_CAP.setText("I2C_CAP = " + i2c_touch);
                        textView_itc_motor.setText("I2C_MOTOR = " + i2c_motar);
                        textView_acce.setText("Acceleration = " + isI2c_acc_self);

                        saveTODB();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onDescriptorWrite(BluetoothGatt mBluetoothGatt) {
        mService.getRSSIvalueForSelf();
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

    @Override
    protected void onStop() {
        finish();
        super.onStop();
    }
}
