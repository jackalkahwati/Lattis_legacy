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

import cc.skylock.myapplication.Uitls.FileUtils;
import cc.skylock.myapplication.Uitls.Myconstants;
import cc.skylock.myapplication.Uitls.UtilHelper;
import cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothLEService;

public class ReadSerialActivity extends AppCompatActivity implements BluetoothDeviceStatus {
    ImageView imageView_connection;
    Bundle extras;
    TextView tv_serialnumber;
    RelativeLayout rl_progressBar;
    String mFGResult = "";
    SkylockBluetoothLEService mService;
    boolean isFirstime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_serial);
        Context context = getApplicationContext();
        getSupportActionBar().setTitle("Skylock Manufacture " + FileUtils.getVersionName(context));
        imageView_connection = (ImageView) findViewById(R.id.iv_connectionStatus);
        extras = this.getIntent().getExtras();
        tv_serialnumber = (TextView) findViewById(R.id.tv_serialNumber);
        rl_progressBar = (RelativeLayout) findViewById(R.id.rl_progresslayout);
        rl_progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        service_init();
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
            mService.registerBluetoothDeviceStatusListener(ReadSerialActivity.this);
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
    public void onScanFailed() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
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
    public void onGetHardWareInfo(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic mCharacteristic) {

    }

    @Override
    public void onDescriptorWrite(BluetoothGatt mBluetoothGatt) {
        if (mService != null)
            mService.getSerialInfo();
    }

    @Override
    public void onDeviceStatus(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic mCharacteristic) {

    }

    @Override
    public void onGetSerialInfo(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic != null) {
            mFGResult = "";
            byte[] mode = characteristic.getValue();
            for (int i = 0; i < mode.length; i++) {
                // decimal to hex
                String hex = Integer.toString(mode[i], 16);
                // hexToString
                String result = UtilHelper.hexToString(hex);
                mFGResult = mFGResult + result;
            }
            Log.i("SN MFG result ", "" + mFGResult);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rl_progressBar.setVisibility(View.GONE);
                    tv_serialnumber.setVisibility(View.VISIBLE);
                    tv_serialnumber.setText("Read Serial NO from board = " + mFGResult);
                    if (isFirstime) {
                        saveToDB(mFGResult);
                        isFirstime = false;
                    }

                }
            });
        }

    }

    private void saveToDB(String number) {
        String data = null;
        data = "\n" + "MANUFACTURE SERIAL NUMBER" + "=" + number + "\n";
        FileUtils.writeFile(Myconstants.serial_number, data);
//        System.out.println(FileUtils.readFromFile(ReadSerialActivity.this, Myconstants.serial_number));


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
