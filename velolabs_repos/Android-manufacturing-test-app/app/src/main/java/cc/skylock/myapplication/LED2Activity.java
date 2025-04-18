package cc.skylock.myapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cc.skylock.myapplication.Uitls.FileUtils;
import cc.skylock.myapplication.Uitls.UtilHelper;
import cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothLEService;

public class LED2Activity extends AppCompatActivity implements BluetoothDeviceStatus, View.OnClickListener {
    TextView textView_title;
    Button button_ledON, button_ledoff, button_next;
    ImageView imageView_bleConnectionStatus;
    boolean ledOnOFF = true;
    Context context;
    ImageButton imageButton1, imageButton2, imageButton3, imageButton4, imageButton5;
    String upToNCharacters;
    boolean isFirstime = true;
    SkylockBluetoothLEService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led1);
        context = getApplicationContext();
        getSupportActionBar().setTitle("Skylock Manufacture " + FileUtils.getVersionName(context));
        textView_title = (TextView) findViewById(R.id.tvTitle);
        button_ledON = (Button) findViewById(R.id.button_ledon);
        button_ledoff = (Button) findViewById(R.id.button_ledoff);
        button_next = (Button) findViewById(R.id.bCapTouchNext);
        imageView_bleConnectionStatus = (ImageView) findViewById(R.id.iv_connectionStatus);
        imageButton1 = (ImageButton) findViewById(R.id.ibCapPin1);
        imageButton2 = (ImageButton) findViewById(R.id.ibCapPin2);
        imageButton3 = (ImageButton) findViewById(R.id.ibCapPin3);
        imageButton4 = (ImageButton) findViewById(R.id.ibCapPin4);
        imageButton5 = (ImageButton) findViewById(R.id.ibCapPin5);
        setLedOffState();
        button_ledON.setOnClickListener(this);
        button_ledoff.setOnClickListener(this);
        button_next.setOnClickListener(this);
        textView_title.setText("LED2 Test");

    }

    private void setLedOnState() {
        imageButton1.setImageResource(R.drawable.grey_emergency);
        imageButton2.setImageResource(R.drawable.grey_emergency);
        imageButton3.setImageResource(R.drawable.grey_emergency);
        imageButton4.setImageResource(R.drawable.grey_emergency);
        imageButton5.setImageResource(R.drawable.red);
    }

    private void setLedOffState() {
        imageButton1.setImageResource(R.drawable.grey_emergency);
        imageButton2.setImageResource(R.drawable.grey_emergency);
        imageButton3.setImageResource(R.drawable.grey_emergency);
        imageButton4.setImageResource(R.drawable.grey_emergency);
        imageButton5.setImageResource(R.drawable.grey_emergency);
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.button_ledon: {
                    if (mService != null) {
                        mService.ledON((byte) 0xE0);
                        ledOnOFF = true;
                    } else
                        Toast.makeText(LED2Activity.this, "No BLE Connection", Toast.LENGTH_SHORT).show();
                }
                break;
                case R.id.button_ledoff: {
                    if (mService != null) {
                        mService.ledON((byte) 0x00);
                        ledOnOFF = false;
                    } else
                        Toast.makeText(LED2Activity.this, "No BLE Connection", Toast.LENGTH_SHORT).show();
                }
                break;
                case R.id.bCapTouchNext: {
                    if (ledOnOFF)
                        mService.ledON((byte) 0x00);
                    finish();
                    Intent intent = new Intent(LED2Activity.this, SplashActivity.class);
                    startActivity(intent);

                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        service_init();
        try {
            if (SkylockBluetoothLEService.mCurrentlyconnectedGatt != null) {
                imageView_bleConnectionStatus.setImageResource(R.drawable.green);
            } else {
                Toast.makeText(LED2Activity.this, "No BLE Connection", Toast.LENGTH_SHORT).show();
                imageView_bleConnectionStatus.setImageResource(R.drawable.red);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
            if (mService == null) {
                service_init();
            }
            mService.ledON((byte) 0x00);
            mService.registerBluetoothDeviceStatusListener(LED2Activity.this);
            mService.enableLEDNotification(true);
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService.unregisterBluetoothDeviceStatusListener();
            mService = null;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

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
                Toast.makeText(LED2Activity.this, "No BLE device", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDisConnect() {

    }

    @Override
    public void onGetHardWareInfo(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic mCharacteristic) {

    }

    @Override
    public void onDescriptorWrite(BluetoothGatt mBluetoothGatt) {
        if (mService != null)
            mService.ledON((byte) 0xE0);
    }

    @Override
    public void onDeviceStatus(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic mCharacteristic) {

    }

    @Override
    public void onGetSerialInfo(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onLedBlink(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic != null) {
            byte[] one = characteristic.getValue();
            String led_temp = UtilHelper.bytesToHex(one);
            upToNCharacters = led_temp.substring(0, Math.min(led_temp.length(), 2));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (upToNCharacters.equals("E0")) {
                        Log.i("Led state in UI", "ON");
                        setLedOnState();
                    } else if (isFirstime) {
                        isFirstime = false;
                        Log.i("isFirstime failed", "Try");
                        mService.ledON((byte) 0xE0);
                    } else {
                        Log.i("Led state in UI", "OFF");
                        setLedOffState();
                    }
                }
            });

        }

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
