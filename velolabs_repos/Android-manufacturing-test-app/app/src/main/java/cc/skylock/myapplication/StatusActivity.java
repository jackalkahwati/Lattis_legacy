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
import android.widget.ProgressBar;
import android.widget.TextView;

import cc.skylock.myapplication.Uitls.FileUtils;
import cc.skylock.myapplication.Uitls.Myconstants;
import cc.skylock.myapplication.Uitls.UtilHelper;
import cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothLEService;

@TargetApi(23)
public class StatusActivity extends BaseActivity implements BluetoothDeviceStatus {
    String mac_ID;
    BluetoothAdapter mBluetoothAdapter;
    int REQUEST_ENABLE_BT = 1;
    ImageView imageView_bleConnectionStatus;
    ProgressBar progressBar;
    boolean foundBluetoothdevice = false;
    Bundle extras;
    Context context;
    TextView mode_TextView;
    SkylockBluetoothLEService mService;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        context = this;
        getSupportActionBar().setTitle("Skylock Manufacture " + FileUtils.getVersionName(context));
        imageView_bleConnectionStatus = (ImageView) findViewById(R.id.iv_connectionStatus);
        progressBar = (ProgressBar) findViewById(R.id.progressbar_load);
        mode_TextView = (TextView) findViewById(R.id.tv_mode);
        progressBar.setVisibility(View.VISIBLE);
        extras = this.getIntent().getExtras();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onResume() {
        service_init();
        if (SkylockBluetoothLEService.mCurrentlyconnectedGatt!=null) {
            imageView_bleConnectionStatus.setImageResource(R.drawable.green);
        } else {
            imageView_bleConnectionStatus.setImageResource(R.drawable.red);
        }
        super.onResume();
    }

    private void service_init() {
        Intent bindIntent = new Intent(this, SkylockBluetoothLEService.class);
        getApplicationContext().bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    private void saveToDB(String mode) {
        String data = null;
        data = "\n" + " LOCK MODE" + "=" + mode + "\n";
        FileUtils.writeFile(Myconstants.status_fileName, data);
//        System.out.println(FileUtils.readFromFile(StatusActivity.this, Myconstants.status_fileName));


    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
            if (mService == null) {
                service_init();
            }
            mService.registerBluetoothDeviceStatusListener(StatusActivity.this);
            if (!mBluetoothAdapter.isEnabled()) {
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
        bleconnection();
    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onConnected(BluetoothGatt mBluetoothGatt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView_bleConnectionStatus.setImageResource(R.drawable.green);
                mService.enableSecurityNotificationCharaterisitics(true);
            }
        });
    }

    @Override
    public void onConnectionFailed(boolean shippingModeEnabled) {

    }

    @Override
    protected void onPause() {
        finish();
        super.onPause();
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
        if (mCharacteristic != null) {
            foundBluetoothdevice = true;
            byte[] mode = mCharacteristic.getValue();
            final String lockMode = UtilHelper.bytesToHex(mode);
            Log.i("lock_Mode", lockMode);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String lock_mode = lockMode.substring(0, Math.min(lockMode.length(), 2));
                    progressBar.setVisibility(View.GONE);
                    mode_TextView.setText("Status = " + lock_mode);
                    mode_TextView.setVisibility(View.VISIBLE);
                    saveToDB(lock_mode);
                }
            });
        }
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

    private void bleconnection() {

        final Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("mac_id")) {
                mac_ID = extras.getString("mac_id");
                mac_ID = FileUtils.macAddColon(extras.getString("mac_id").trim());
                Log.i("mac_id", mac_ID);
                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac_ID);
                mService.connect(device);
            }
        }

    }

}
