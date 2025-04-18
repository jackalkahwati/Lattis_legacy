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
import android.widget.ProgressBar;
import android.widget.Toast;

import cc.skylock.myapplication.Uitls.FileUtils;
import cc.skylock.myapplication.Uitls.Myconstants;
import cc.skylock.myapplication.Uitls.UtilHelper;
import cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothLEService;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothManage;

public class TouchActivity extends AppCompatActivity implements BluetoothDeviceStatus,View.OnClickListener {
    ImageButton ib_1, ib_2, ib_3, ib_4, ib_5;
    ImageView imageView_connection;
    Button button_Next;
    String test_Status;
    private String[] captouchState = new String[5];
    int position;
    ProgressBar mProgressBar;
    SkylockBluetoothLEService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch);
        Context context = this;
        getSupportActionBar().setTitle("Skylock Manufacture " + FileUtils.getVersionName(context));
        ib_1 = (ImageButton) findViewById(R.id.ibCapPin1);
        ib_2 = (ImageButton) findViewById(R.id.ibCapPin2);
        ib_3 = (ImageButton) findViewById(R.id.ibCapPin3);
        ib_4 = (ImageButton) findViewById(R.id.ibCapPin4);
        ib_5 = (ImageButton) findViewById(R.id.ibCapPin5);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_load);
        imageView_connection = (ImageView) findViewById(R.id.iv_connectionStatus);
        button_Next = (Button) findViewById(R.id.bCapTouchNext);
        mProgressBar.setVisibility(View.VISIBLE);
        ib_1.setOnClickListener(this);
        ib_2.setOnClickListener(this);
        ib_3.setOnClickListener(this);
        ib_4.setOnClickListener(this);
        ib_5.setOnClickListener(this);
        button_Next.setOnClickListener(this);
        saveTODB();
        SkylockBluetoothManage.getInstance(this).enableTouchHwInfo(true);
    }
    private void saveTODB() {
        collectInfo();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String data = convertStringToArray();
                FileUtils.writeFile(Myconstants.touch_fileName, data);
//                System.out.println(FileUtils.readFromFile(TouchActivity.this, Myconstants.touch_fileName));

            }
        }).start();


    }
    private void collectInfo() {
        captouchState[0] = ib_1.getTag().toString();
        captouchState[1] = ib_2.getTag().toString();
        captouchState[2] = ib_3.getTag().toString();
        captouchState[3] = ib_4.getTag().toString();
        captouchState[4] = ib_5.getTag().toString();

    }
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private String convertStringToArray() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < captouchState.length; i++) {
            if (captouchState[i].equalsIgnoreCase("on"))
                test_Status = "\n" + " SET TOUCH" + (i + 1) + "=" + "PASS " + "\n";
            else
                test_Status = "\n" + " SET TOUCH" + (i + 1) + "=" + "FAIL " + "\n";
            builder.append(test_Status);
        }
        return builder.toString();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibCapPin1: {
                if (ib_1.getTag().equals("off")) {
                    ib_1.setImageResource(R.drawable.green);
                    ib_1.setTag("on");
                } else {
                    ib_1.setImageResource(R.drawable.grey_emergency);
                    ib_1.setTag("off");
                }
            }
            break;
            case R.id.ibCapPin2: {
                if (ib_2.getTag().equals("off")) {
                    ib_2.setImageResource(R.drawable.green);
                    ib_2.setTag("on");
                } else {
                    ib_2.setImageResource(R.drawable.grey_emergency);
                    ib_2.setTag("off");
                }
            }
            break;
            case R.id.ibCapPin3: {
                if (ib_3.getTag().equals("off")) {
                    ib_3.setImageResource(R.drawable.green);
                    ib_3.setTag("on");
                } else {
                    ib_3.setImageResource(R.drawable.grey_emergency);
                    ib_3.setTag("off");
                }
            }
            break;
            case R.id.ibCapPin4: {
                if (ib_4.getTag().equals("off")) {
                    ib_4.setImageResource(R.drawable.green);
                    ib_4.setTag("on");
                } else {
                    ib_4.setImageResource(R.drawable.grey_emergency);
                    ib_4.setTag("off");
                }

            }
            break;
            case R.id.ibCapPin5: {
                if (ib_5.getTag().equals("off")) {
                    ib_5.setImageResource(R.drawable.green);
                    ib_5.setTag("on");
                } else {
                    ib_5.setImageResource(R.drawable.grey_emergency);
                    ib_5.setTag("off");
                }
            }
            break;
            case R.id.bCapTouchNext: {
                saveTODB();
                finish();
                Intent intent = new Intent(TouchActivity.this, SplashActivity.class);
                startActivity(intent);

            }
            break;
        }
    }

    @Override
    protected void onResume() {
        service_init();
        try {
            if (SkylockBluetoothLEService.mCurrentlyconnectedGatt!=null) {
                imageView_connection.setImageResource(R.drawable.green);

            } else {
                Toast.makeText(TouchActivity.this, "No BLE Connection", Toast.LENGTH_SHORT).show();
                imageView_connection.setImageResource(R.drawable.red);
                mProgressBar.setVisibility(View.GONE);
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
            mService.enableHardwareNotification(true);
            mService.registerBluetoothDeviceStatusListener(TouchActivity.this);
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
                imageView_connection.setImageResource(R.drawable.red);
                Toast.makeText(TouchActivity.this, "No BLE device", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDisConnect() {

    }

    @Override
    public void onGetHardWareInfo(final  BluetoothGatt mBluetoothGatt, final BluetoothGattCharacteristic mCharacteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("ondescriptorWrite ", mBluetoothGatt.getDevice().getName());
                mProgressBar.setVisibility(View.GONE);


            }
        });
        if (mCharacteristic != null) {
            byte[] solar = mCharacteristic.getValue();
            String solar_temp = UtilHelper.bytesToHex(solar);
            Log.i("solar_temp  ", "" + solar_temp);
            String subString = solar_temp.substring(0, Math.min(solar_temp.length(), 12));
            Log.i("substring ", "" + subString);
            String upToNCharacters = subString.substring(subString.length() - 2);
            Log.i("upToNCharacters ", "" + upToNCharacters);
            if (upToNCharacters.equals("81") || upToNCharacters.equals("01") ) {
                Log.i("Button Press ", "TOP");
                position = 1;
            } else if (upToNCharacters.equals("82")||upToNCharacters.equals("02")) {
                Log.i("Button Press ", "RIGHT");
                position = 2;

            } else if (upToNCharacters.equals("84")||upToNCharacters.equals("04")) {
                Log.i("Button Press ", "BOTTOM");
                position = 4;

            } else if (upToNCharacters.equals("88")||upToNCharacters.equals("08")) {
                Log.i("Button Press ", "LEFT");
                position = 8;

            } else if (upToNCharacters.equals("90")||upToNCharacters.equals("10")) {
                Log.i("Button Press ", "MIDDLE");
                position = 10;

            }
            blinLLED(position);
        }

    }
        private void blinLLED(final int position) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (position == 1) {
                        Log.i("Button Press ", "TOP");
                        ib_1.setImageResource(R.drawable.green);
                        ib_1.setTag("on");
                    } else if (position == 2) {
                        Log.i("Button Press ", "RIGHT");
                        ib_2.setImageResource(R.drawable.green);
                        ib_2.setTag("on");
                    } else if (position == 4) {
                        Log.i("Button Press ", "BOTTOM");
                        ib_3.setImageResource(R.drawable.green);
                        ib_3.setTag("on");
                    } else if (position == 8) {
                        Log.i("Button Press ", "LEFT");
                        ib_4.setImageResource(R.drawable.green);
                        ib_4.setTag("on");
                    } else if (position == 10) {
                        Log.i("Button Press ", "MIDDLE");
                        ib_5.setImageResource(R.drawable.green);
                        ib_5.setTag("on");
                    }
                    try {
                        saveTODB();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }

    @Override
    public void onDescriptorWrite(final BluetoothGatt mBluetoothGatt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("ondescriptorWrite ", mBluetoothGatt.getDevice().getName());
                mProgressBar.setVisibility(View.GONE);


            }
        });
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
