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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cc.skylock.myapplication.Uitls.FileUtils;
import cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothLEService;

public class SerialActivity extends AppCompatActivity implements BluetoothDeviceStatus {
    ImageView imageView_connection;
    Bundle extras;
    String serialNumber = null;
    TextView tv_serialnumber;
    RelativeLayout rl_progressBar;
    SkylockBluetoothLEService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial);
        Context context = getApplicationContext();
        getSupportActionBar().setTitle("Skylock Manufacture " + FileUtils.getVersionName(context));
        imageView_connection = (ImageView) findViewById(R.id.iv_connectionStatus);
        tv_serialnumber = (TextView) findViewById(R.id.tv_serialNumber);
        extras = this.getIntent().getExtras();
        rl_progressBar = (RelativeLayout) findViewById(R.id.rl_progresslayout);
        tv_serialnumber.setVisibility(View.GONE);


    }

    @Override
    protected void onResume() {
        service_init();
        if (extras != null) {
            if (extras.containsKey("serial_number")) {
                serialNumber = null;

                serialNumber = extras.getString("serial_number");
                Log.i("serial_number", "" + serialNumber);
                tv_serialnumber.setText("Manufacturing Serial Number = " + serialNumber);

            }

        } else {
            rl_progressBar.setVisibility(View.GONE);
        }
        if (SkylockBluetoothLEService.mCurrentlyconnectedGatt != null) {
            imageView_connection.setImageResource(R.drawable.green);
        } else {
            imageView_connection.setImageResource(R.drawable.red);
        }
        super.onResume();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
            if (mService == null) {
                service_init();
            }
            mService.registerBluetoothDeviceStatusListener(SerialActivity.this);
            mService.enableSerialNumberNotification(true);
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
        finish();
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView_connection.setImageResource(R.drawable.red);
                Toast.makeText(SerialActivity.this, "No BLE device", Toast.LENGTH_SHORT).show();
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
        writeSerialNumber();
    }

    @Override
    public void onDeviceStatus(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic mCharacteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rl_progressBar.setVisibility(View.GONE);
                tv_serialnumber.setVisibility(View.VISIBLE);

            }
        });
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

    private void writeSerialNumber() {
        rl_progressBar.setVisibility(View.VISIBLE);
        if (serialNumber != null) {
            Log.i("size :", "" + serialNumber.getBytes().length);
            int length = serialNumber.getBytes().length;
            if (length < 10) {
                for (int i = length; i < 10; i++) {
                    serialNumber = "0" + serialNumber;
                    Log.i("inside serial_number", "" + serialNumber);
                }

            }
            Log.i(" after serial_number", "" + serialNumber);
            mService.writeSerialInfo(serialNumber.trim());
        }
    }
}
