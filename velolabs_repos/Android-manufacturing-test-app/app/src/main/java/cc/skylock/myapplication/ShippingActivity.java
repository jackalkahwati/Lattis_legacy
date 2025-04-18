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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cc.skylock.myapplication.Uitls.FileUtils;
import cc.skylock.myapplication.Uitls.Myconstants;
import cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothLEService;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothManage;

public class ShippingActivity extends AppCompatActivity implements BluetoothDeviceStatus, View.OnClickListener {

    Button button_beginTest, button_next;
    TextView textView_shipping_status, textView_status;
    ImageView imageView_connection;
    String result = "0";
    RelativeLayout cirProgressBar_RelativeLayout;
    SkylockBluetoothLEService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping);
        Context context = this;
        SkylockBluetoothManage.getInstance(this).registerBluetoothDeviceStatusListener(this);
        getSupportActionBar().setTitle("Skylock Manufacture " + FileUtils.getVersionName(context));
        button_beginTest = (Button) findViewById(R.id.blifeCycleBegin);
        button_next = (Button) findViewById(R.id.bCapTouchNext);
        textView_shipping_status = (TextView) findViewById(R.id.tv_shipping_status);
        button_beginTest.setOnClickListener(this);
        button_next.setOnClickListener(this);
        cirProgressBar_RelativeLayout = (RelativeLayout) findViewById(R.id.relativelayout_circular);
        textView_status = (TextView) findViewById(R.id.textView_loading);
        imageView_connection = (ImageView) findViewById(R.id.iv_connectionStatus);
        textView_shipping_status.setVisibility(View.GONE);
        saveToDB();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bCapTouchNext: {
                saveToDB();
                finish();
                Intent intent = new Intent(ShippingActivity.this, SplashActivity.class);
                startActivity(intent);


            }
            break;
            case R.id.blifeCycleBegin: {
                try {
                    SkylockBluetoothManage.getInstance().putShippingMode();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            break;
        }
    }

    private void saveToDB() {
        String data = "\n" + " SET SHIP_MODE" + "=" + result + "\n";
        FileUtils.writeFile(Myconstants.shipping_fileName, data);
//        System.out.println(FileUtils.readFromFile(ShippingActivity.this, Myconstants.shipping_fileName));

    }

    @Override
    protected void onResume() {
        service_init();
        try {
            if (SkylockBluetoothLEService.mCurrentlyconnectedGatt != null) {
                imageView_connection.setImageResource(R.drawable.green);


            } else {
                Toast.makeText(ShippingActivity.this, "No BLE Connection", Toast.LENGTH_SHORT).show();
                imageView_connection.setImageResource(R.drawable.red);
                cirProgressBar_RelativeLayout.setVisibility(View.INVISIBLE);

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
            mService.registerBluetoothDeviceStatusListener(ShippingActivity.this);
            mService.putShippingMode();
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
        if (shippingModeEnabled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView_connection.setImageResource(R.drawable.red);
                    result = "1";
                    cirProgressBar_RelativeLayout.setVisibility(View.GONE);
                    textView_shipping_status.setVisibility(View.VISIBLE);
                    textView_shipping_status.setText("Shipping Mode Status = 1");
                    saveToDB();
                    SkylockBluetoothManage.getInstance().disconnect();
                    SkylockBluetoothManage.getInstance().close();

                }
            });
        }
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
        finish();
        getApplicationContext().unbindService(mServiceConnection);
        getApplicationContext().stopService(new Intent(this, SkylockBluetoothLEService.class));
    }

}
