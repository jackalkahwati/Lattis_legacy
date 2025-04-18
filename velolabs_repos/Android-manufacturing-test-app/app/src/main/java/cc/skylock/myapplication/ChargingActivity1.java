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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import cc.skylock.myapplication.Uitls.FileUtils;
import cc.skylock.myapplication.Uitls.Myconstants;
import cc.skylock.myapplication.Uitls.UtilHelper;
import cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothLEService;

public class ChargingActivity1 extends AppCompatActivity implements BluetoothDeviceStatus {
    private Button button_beginTest, button_NextTest;
    private TextView textView_status, textView_usb;
    private ImageView imageView_bleConnectionStatus;
    private BroadcastReceiver mBroadcastReceiver;
    private ProgressBar loading_ProgressBar;
    private int result = 0;
    Context context;
    private String charge_status;
    TextView textView_Instruction;
    SkylockBluetoothLEService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charging1);
        context = getApplicationContext();
        getSupportActionBar().setTitle("Skylock Manufacture " + FileUtils.getVersionName(context));
        button_beginTest = (Button) findViewById(R.id.blifeCycleBegin);
        button_NextTest = (Button) findViewById(R.id.bUSBChargingNext);
        textView_status = (TextView) findViewById(R.id.textView_loading);
        imageView_bleConnectionStatus = (ImageView) findViewById(R.id.iv_connectionStatus);
        textView_usb = (TextView) findViewById(R.id.tv_usb);
        textView_Instruction = (TextView) findViewById(R.id.tvInstructions);
        textView_Instruction.setText("Insert the USB charger to the lock's charging port. Press the Begin Test button to start the test. It will tell the result at the end of the test.");
        loading_ProgressBar = (ProgressBar) findViewById(R.id.progressbar_load);
        button_NextTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToDB();
                finish();
                Intent intent = new Intent(ChargingActivity1.this, SplashActivity.class);
                startActivity(intent);


            }
        });

    }

    private void saveToDB() {
        String data = "\n" + "SET USB_CHARGING" + "=" + charge_status + "\n";
        FileUtils.writeFile(Myconstants.usb_charging_fileName + "1", data);
//        System.out.println(FileUtils.readFromFile(ChargingActivity1.this, Myconstants.usb_charging_fileName + "1"));
    }

    @Override
    protected void onResume() {
        service_init();
        try {
            if (SkylockBluetoothLEService.mCurrentlyconnectedGatt != null) {
                imageView_bleConnectionStatus.setImageResource(R.drawable.green);

            } else {
                imageView_bleConnectionStatus.setImageResource(R.drawable.red);
                loading_ProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(ChargingActivity1.this, "No BLE device", Toast.LENGTH_SHORT).show();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
            if (mService == null) {
                service_init();
            }
            mService.registerBluetoothDeviceStatusListener(ChargingActivity1.this);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView_bleConnectionStatus.setImageResource(R.drawable.red);
                loading_ProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(ChargingActivity1.this, "No BLE device", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDisConnect() {

    }

    @Override
    public void onGetHardWareInfo(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic mCharacteristic) {
        if (mCharacteristic != null) {
            byte[] solar = mCharacteristic.getValue();
            Log.i("solar ", "" + solar);
            final String solar_temp = UtilHelper.bytesToHex(solar);
            final String tempstring = solar_temp.substring(Math.max(solar_temp.length() - 6, 0));
            final String upToNCharacters = tempstring.substring(0, Math.min(tempstring.length(), 2));
            final int a = Integer.parseInt(upToNCharacters, 16);
            final String b = Integer.toBinaryString(a);
            final String test = String.format("%08d", Integer.parseInt(b));
            final String a_letter = Character.toString(test.charAt(6));
            result = Integer.parseInt(a_letter);
            Log.i("result", "" + result);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loading_ProgressBar.setVisibility(View.GONE);
                    if (result == 0)
                        charge_status = "PASS";
                    else
                        charge_status = "FAIL";
                    textView_usb.setText("USB Charging = " + charge_status);
                    saveToDB();
                }
            });
        }

    }

    @Override
    public void onDescriptorWrite(BluetoothGatt mBluetoothGatt) {
        if (mService != null)
            mService.writeHardwareInfo();
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
