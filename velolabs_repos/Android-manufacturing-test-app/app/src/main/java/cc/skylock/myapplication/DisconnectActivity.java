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
import android.widget.ImageView;

import cc.skylock.myapplication.Uitls.FileUtils;
import cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothLEService;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothManage;

public class DisconnectActivity extends AppCompatActivity implements BluetoothDeviceStatus {
    ImageView imageView_bleConnectionStatus;
    SkylockBluetoothLEService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rf);
        Context context = this;
        SkylockBluetoothManage.getInstance(this).registerBluetoothDeviceStatusListener(this);
        getSupportActionBar().setTitle("Skylock Manufacture " + FileUtils.getVersionName(context));
        imageView_bleConnectionStatus = (ImageView) findViewById(R.id.iv_connectionStatus);
        imageView_bleConnectionStatus.setImageResource(R.drawable.green);
    }

    @Override
    protected void onResume() {
        service_init();
        if (SkylockBluetoothLEService.mCurrentlyconnectedGatt != null) {
            imageView_bleConnectionStatus.setImageResource(R.drawable.green);
        } else {
            imageView_bleConnectionStatus.setImageResource(R.drawable.red);
        }
        super.onResume();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
            if (mService == null) {
                service_init();
            }
            mService.registerBluetoothDeviceStatusListener(DisconnectActivity.this);
            mService.clearBLeStack();
            SkylockBluetoothLEService.mCurrentlyconnectedGatt = null;
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

                mService.close();
                Intent intent = new Intent(DisconnectActivity.this, SplashActivity.class);
                startActivity(intent);
                finish();
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
