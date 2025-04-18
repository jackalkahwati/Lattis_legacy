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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import cc.skylock.myapplication.Uitls.FileUtils;
import cc.skylock.myapplication.Uitls.Myconstants;
import cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothLEService;

public class RFActivity extends AppCompatActivity implements BluetoothDeviceStatus {
    TextView textView_connection, textView_rssi, textView_mac;
    String labels[] = {" SET CONNECTION=", " SET MAC=", " SET RSSI=", "SET RSSI_VALUE="
    };
    ImageView imageView_connection;
    ProgressBar progressBar;
    Button button_next;
    SkylockBluetoothLEService mService;
    String macID;
    int rssiValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rf);
        Context context = getApplicationContext();
        getSupportActionBar().setTitle("Skylock Manufacture " + FileUtils.getVersionName(context));
        textView_connection = (TextView) findViewById(R.id.tv_connection);
        textView_rssi = (TextView) findViewById(R.id.tv_Rssi);
        textView_mac = (TextView) findViewById(R.id.tv_macaddress);
        imageView_connection = (ImageView) findViewById(R.id.iv_connectionStatus);
        progressBar = (ProgressBar) findViewById(R.id.progressbar_load);
        button_next = (Button) findViewById(R.id.bCapTouchNext);
        progressBar.setVisibility(View.VISIBLE);
        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTODB();
                finish();
                Intent intent = new Intent(RFActivity.this, SplashActivity.class);
                startActivity(intent);

            }
        });
        saveTODB();
    }

    private void saveTODB() {
        collectSelfInfo();
        String data = convertStringToArray();
        FileUtils.writeFile(Myconstants.rf_fileName, data);
//        System.out.println("one" + FileUtils.readFromFile(RFActivity.this, Myconstants.rf_fileName));

    }

    private void collectSelfInfo() {
        if (SkylockBluetoothLEService.mCurrentlyconnectedGatt != null)
            Myconstants.rFTest[0] = "PASS";
        else
            Myconstants.rFTest[0] = "FAIL";
        Myconstants.rFTest[1] = macID;
        if (Myconstants.rssi < Myconstants.higer_rssi || rssiValue > Myconstants.lower_rssi)
            Myconstants.rFTest[2] = "PASS";
        else
            Myconstants.rFTest[2] = "FAIL";

        Myconstants.rFTest[3] = "" + rssiValue;
    }

    private String convertStringToArray() {
        StringBuilder builder = new StringBuilder();

        String test_Status = null;
        for (int i = 0; i < Myconstants.rFTest.length; i++) {
            test_Status = "\n" + labels[i] + Myconstants.rFTest[i] + "\n";
            builder.append(test_Status);
        }
        return builder.toString();
    }

    @Override
    protected void onPause() {
        mService.enableHardwareNotification(false);
        super.onPause();
       RFActivity.this.finish();


    }

    @Override
    protected void onResume() {
        service_init();
        if (SkylockBluetoothLEService.mCurrentlyconnectedGatt != null) {
            imageView_connection.setImageResource(R.drawable.green);

        } else {
            imageView_connection.setImageResource(R.drawable.red);
            textView_rssi.setText("RSSI = FAIL");
        }
        super.onResume();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
            mService.registerBluetoothDeviceStatusListener(RFActivity.this);
            if (mService == null) {
                service_init();
            }
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
    public void onGetHardWareInfo(final BluetoothGatt mBluetoothGatt, final BluetoothGattCharacteristic mCharacteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCharacteristic != null) {
                    final Integer temp2 = mCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 3);
                    textView_connection.setText("Connection = PASS");
                    progressBar.setVisibility(View.GONE);
                    macID = mBluetoothGatt.getDevice().getAddress().replace(":", "");
                    textView_mac.setText("Mac address = " + macID);
                    rssiValue = temp2;
                    if (temp2 < Myconstants.higer_rssi || temp2 > Myconstants.lower_rssi)
                        textView_rssi.setText("RSSI = PASS" + "  " + temp2);
                    else
                        textView_rssi.setText("RSSI = FAIL" + "  " + temp2);
                    saveTODB();
                }
            }
        });

    }

    @Override
    public void onDescriptorWrite(BluetoothGatt mBluetoothGatt) {
        if (mService != null) {
            mService.getHardwareInfo();
        }
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
        RFActivity.this.finish();
    }

}
