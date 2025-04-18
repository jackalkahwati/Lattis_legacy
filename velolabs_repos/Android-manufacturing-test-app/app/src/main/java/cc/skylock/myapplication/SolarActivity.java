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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import cc.skylock.myapplication.Uitls.FileUtils;
import cc.skylock.myapplication.Uitls.Myconstants;
import cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothLEService;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothManage;

public class SolarActivity extends AppCompatActivity implements BluetoothDeviceStatus, View.OnClickListener {
    Button button_beginTest, button_next;
    TextView textView_solar_status, textView_delta, textView_reference;
    TextView textView_status, textView_status_p_f;
    ImageView imageView_connection;
    int solar_result = 0;
    BroadcastReceiver mBroadcastReceiver;
    public static Timer timer, timer1;
    boolean battReferenceFirstFlag = false;
    public float battVolt = 0, tempBattVolt1 = 0, tempBattVolt2 = 0, reference_battery_voltage = 0, battery_voltage = 0;

    SkylockBluetoothLEService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solar);
        Context context = getApplicationContext();
        getSupportActionBar().setTitle("Skylock Manufacture " + FileUtils.getVersionName(context));
        button_beginTest = (Button) findViewById(R.id.blifeCycleBegin);
        button_next = (Button) findViewById(R.id.bCapTouchNext);
        textView_solar_status = (TextView) findViewById(R.id.tvsolar);
        textView_reference = (TextView) findViewById(R.id.tvreference);
        button_beginTest.setOnClickListener(this);
        button_next.setOnClickListener(this);
        textView_status = (TextView) findViewById(R.id.textView_loading);
        textView_delta = (TextView) findViewById(R.id.tvdelta);
        textView_status_p_f = (TextView) findViewById(R.id.tvstatus);
        imageView_connection = (ImageView) findViewById(R.id.iv_connectionStatus);
        timer = new Timer();
        timer1 = new Timer();
        saveToDB();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bCapTouchNext: {
                saveToDB();
                finish();
                Intent intent = new Intent(SolarActivity.this, SplashActivity.class);
                startActivity(intent);

            }
            break;
            case R.id.blifeCycleBegin: {
                try {

                    mService.getHardwareInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
            break;
        }

    }

    private void saveToDB() {
        String data = "\n" + " SET SOLAR" + "=" + solar_result + "\n";
        FileUtils.writeFile(Myconstants.solar_fileName, data);
//        System.out.println(FileUtils.readFromFile(SolarActivity.this, Myconstants.solar_fileName));

    }

    @Override
    protected void onResume() {
        service_init();
        try {
            if (SkylockBluetoothLEService.mCurrentlyconnectedGatt != null) {
                imageView_connection.setImageResource(R.drawable.green);

            } else {
                Toast.makeText(SolarActivity.this, "No BLE Connection", Toast.LENGTH_SHORT).show();
                imageView_connection.setImageResource(R.drawable.red);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        mService.enableHardwareNotification(false);
        super.onPause();
        timer1.cancel();
        timer.cancel();
        tempBattVolt1 = 0;
        tempBattVolt2 = 0;
        battVolt = 0;
        finish();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
            if (mService == null) {
                service_init();
            }
            mService.registerBluetoothDeviceStatusListener(SolarActivity.this);
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

    }

    @Override
    public void onDisConnect() {

    }

    @Override
    public void onFWUpgradeProgress(FWUpgradeProgress fwUpgradeProgress) {

    }

    @Override
    public void onGetHardWareInfo(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic mCharacteristic) {
        if (mCharacteristic != null) {
            try {
                Integer batvoltage = mCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                if (battReferenceFirstFlag) {
                    reference_battery_voltage = (float) batvoltage / 1000;
                    battReferenceFirstFlag = false;
                }
                if (batvoltage != null) {
                    float tempf = (float) batvoltage / 1000;
                    battery_voltage = tempf;

                    if (battVolt == 0) {
                        battVolt = tempf;
                    } else {
                        tempBattVolt1 = tempf - reference_battery_voltage;
                        if (tempBattVolt1 > Myconstants.batteryThreshold) {
                            solar_result = 1;
                        } else {
                            solar_result = 0;
                        }

                    }


                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DecimalFormat df = new DecimalFormat("#.####");
                        textView_reference.setText("Reference volt = " + reference_battery_voltage);
                        textView_solar_status.setText("Battery Voltage = " + battery_voltage);
                        textView_delta.setText("Delta = " + df.format(tempBattVolt1));
                        textView_status_p_f.setText("status = " + solar_result);
                        if (solar_result == 1) {
                            saveToDB();
                            timer.cancel();
                        }


                    }
                });
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       mService.writeHardwareInfo();
                    }
                }, 1000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onDescriptorWrite(BluetoothGatt mBluetoothGatt) {
        getSolarInfoCall();
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
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unbindService(mServiceConnection);
        getApplicationContext().stopService(new Intent(this, SkylockBluetoothLEService.class));
    }

    private void getSolarInfoCall() {
        battReferenceFirstFlag = true;
        SkylockBluetoothManage.getInstance().getHWInfo();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                battVolt = 0;
            }
        }, 0, 5000);
    }
}
