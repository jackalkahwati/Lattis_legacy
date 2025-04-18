package cc.skylock.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cc.skylock.myapplication.Uitls.FileUtils;
import cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothLEService;

@TargetApi(23)
public class HomeActivity extends BaseActivity implements BluetoothDeviceStatus {
    private BluetoothAdapter mBtAdapter = null;
    private static final int REQUEST_ENABLE_BT = 2;
    private SkylockBluetoothLEService mService = null;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private String mac_ID;
    TextView tv_loadingStatus;
    RelativeLayout rl_Content, loading_layout;
    ImageView iv_ConnectionStatus;
    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        activity = this;
        iv_ConnectionStatus = (ImageView) findViewById(R.id.iv_connectionStatus);
        rl_Content = (RelativeLayout) findViewById(R.id.content_layout);
        loading_layout = (RelativeLayout) findViewById(R.id.loading_layout);
        tv_loadingStatus = (TextView) findViewById(R.id.textView_status);
        tv_loadingStatus.setText("Loading...");
        getSupportActionBar().setTitle("Skylock Manufacture " + FileUtils.getVersionName(getApplicationContext()));
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

    }

    @Override
    protected void onResume() {
        service_init();
        super.onResume();
    }

    private void service_init() {
        Intent bindIntent = new Intent(this, SkylockBluetoothLEService.class);
        getApplicationContext().bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
            if (mService == null) {
                service_init();
            }
            mService.registerBluetoothDeviceStatusListener(HomeActivity.this);
            if (!mBtAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            } else {
                final Intent intent = getIntent();
                if (intent != null)
                    onNewIntent(intent);
                handlePremisssion();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            mService.unregisterBluetoothDeviceStatusListener();
            mService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unbindService(mServiceConnection);
        getApplicationContext().stopService(new Intent(this, SkylockBluetoothLEService.class));
        finish();
    }

    @Override
    public void onScanFailed() {

    }

    private void intializeBluetoothLE() {
        if (!mService.initialize()) {
            Log.e("", "Unable to initialize Bluetooth");
            finish();
        }
    }

    @Override
    public void onScanedDevice(BluetoothDevice device) {
        bleconnection(device);
    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onConnected(BluetoothGatt mBluetoothGatt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading_layout.setVisibility(View.GONE);
                iv_ConnectionStatus.setImageResource(R.drawable.green);
                rl_Content.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onConnectionFailed(boolean shippingModeEnabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading_layout.setVisibility(View.GONE);
                iv_ConnectionStatus.setImageResource(R.drawable.red);
                rl_Content.setVisibility(View.GONE);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            } else {
                handlePremisssion();
            }
        }
    }


    private void handlePremisssion() {
        if (Build.VERSION.SDK_INT >= 23) {
            getPermissionForBluetoothWrapper();
        } else {
            intializeBluetoothLE();
        }
    }

    private void getPermissionForBluetoothWrapper() {
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_LOCATION_PERMISSION);
            return;
        } else {
            intializeBluetoothLE();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // granted
                    intializeBluetoothLE();
                } else {
                    // no granted
                    finish();
                }
                return;
            }


        }
    }

    private void bleconnection(BluetoothDevice device) {

        final Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("mac_id")) {
                tv_loadingStatus.setText("Connecting...");
                mac_ID = extras.getString("mac_id");
                mac_ID = FileUtils.macAddColon(extras.getString("mac_id").trim());
                Log.e("Intent mac_id", mac_ID);
                Log.e("Bluetooth device mac_id", device.getAddress());
                Log.e("Bluetooth device mac_id", device.getName());

                if(!mac_ID.contains(":") && device.getAddress().contains(":")){
                    device.getAddress().replaceAll(":","");
                }

                if(device.getAddress().equals(mac_ID)){
                    mService.stopScan();
                    mService.connect(device);
                }
//                final BluetoothDevice device = mBtAdapter.getRemoteDevice(mac_ID);

            }
        }

    }

}
