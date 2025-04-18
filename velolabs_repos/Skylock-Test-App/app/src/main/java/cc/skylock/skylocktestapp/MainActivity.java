package cc.skylock.skylocktestapp;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity {

    //JSON size(0 to 292 = 293)
    int fileSize = 293;

    Button connectNew, disconnect, LED_ON, LED_OFF, LOCK, UNLOCK,scanBluetooth, bLEDState, bBattVolt, bReset,
            bMag, bMagClose, bAcc, bAccClose, bTxPower, bTxClose, bTxRead, bFirmware, bMOT, bLED, bKEY, bScenario,
            bReadCommand, bShipping;
    TextView textView1, textView2, tvLEDState, tvBattVolt, tvTemp, tvRssi, tvLockState, tvXDisplay,
             tvYDisplay, tvZDisplay, tvMagOutput, tvAccMag, tvXSD, tvYSD, tvZSD, tvTxDisplay,
             tvNotify, tvLS, tvStartTest, tvStartTest1, tvReadVerify, tvLedsOnOff, tvLocksUnlocks, tvScenarioStatus;
    EditText etTX, etStartTest2, etScenarioTime, etScenarioCount;
    Switch swAutoLock;
    Context context = this;
    BluetoothDevice Device;
    BluetoothAdapter myBluetoothAdapter;
    ArrayAdapter<String> BTArrayAdapter;
    ListView listView;
    AlertDialog dialog, dialog1, dialog2, dialog3;
    ProgressDialog pdBluetooth, pdLock;
    Timer timer, timerBluetooth, timerLED;
    final MainActivity myContext = this;
    BluetoothGatt mBluetoothGatt;
    BluetoothManager manager;
    BluetoothGattService sLED, sLOCK;
    BluetoothGattCharacteristic cLED_ON, cLED_OFF, cLOCK, cUNLOCK, cLED_STATE, c_TX, c_Testing, cAcc, cMag, cNotify, cBattVolt,
            cPUB, cSIGN, cMSG, cVerify, cChallengeKey, cChallengeData, cLockAdjust;
    BluetoothGattDescriptor descriptorVerify;

    private int AUTO_LOCK = 0;
    private int LOCK_COUNT = 0;
    private int[] countArray = new int[3];
    private String[] fwUpdateArray;
    private int fwCounter = 0;
    private int motCount1 = 0;
    private int motCount2 = 0;
    private int motCount3 = 0;
    int timerScenario = 0;
    int timerCount = 0;
    int countLED = 0;
    int LedsOnOff = 0;
    int maxScenarioCount = 0;
    int LocksUnlocks = 0;
    int ConnectsDisconnects = 0;
    Boolean booleanLED = true;
    Boolean booleanLock = true;
    Boolean booleanVerify = false;


    private static final String TAG_MESSAGE = "signed_message";
    private static final String TAG_PUBLIC_KEY = "public_key";
    private static final String TAG_CHALLENGE_KEY = "challenge_key";
    private static final String TAG_CHALLENGE_RESULT = "challenge_data";
    private static final String TAG_PAYLOAD = "payload";

    String PUB_KEY = null;
    String MESSAGE = null;
    String SIGNATURE = null;

    List<NameValuePair> nameValuePair;
    StringEntity se = null;
    int GET = 1;
    int POST = 2;
    String challengeKeyLocal = "1122334455";
    String challengeKey = null;
    String challengeData = null;
    String challengeResult = null;

    private UUID UUID_TEST_SER = UUID.fromString("d3995e40-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_LED_STATE = UUID.fromString("d3995e41-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_LOCK_STATE = UUID.fromString("d3995e42-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_BAT_VOLT = UUID.fromString("d3995e43-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_TEMP = UUID.fromString("d3995e44-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_TX_PWR = UUID.fromString("d3995e45-fa57-11e4-ae59-0002a5d5c51b");

    private UUID UUID_ACC_MAG_SER = UUID.fromString("d3995e40-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_MAG = UUID.fromString("d3995e44-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_ACC = UUID.fromString("d3995e46-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_ACC_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    //private UUID UUID_ACC_DESC_ACC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fc");

    private UUID UUID_CONFIG_SER = UUID.fromString("d3995e80-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_RESET = UUID.fromString("d3995e81-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_LOCK_ADJUST = UUID.fromString("d3995e82-fa57-11e4-ae59-0002a5d5c51b");

    private UUID UUID_BOOT_SER = UUID.fromString("d3995d00-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_BOOT_VERSION = UUID.fromString("d3995d01-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_BOOT_WRITE = UUID.fromString("d3995d02-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_BOOT_NOTIFY = UUID.fromString("d3995d03-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_BOOT_DONE = UUID.fromString("d3995d04-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_NOTIFY_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private UUID UUID_CRYPTO_SER = UUID.fromString("d3995e00-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_CRYPTO_MSG = UUID.fromString("d3995e01-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_CRYPTO_PUB = UUID.fromString("d3995e02-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_CHALLENGE_KEY = UUID.fromString("d3995e03-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_CHALLENGE_DATA = UUID.fromString("d3995e04-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_CRYPTO_VERIFY = UUID.fromString("d3995e05-fa57-11e4-ae59-0002a5d5c51b");


    String MacAddress1 = "FB:7D:23:F3:73:B5";
    String MacAddress2 = "DA:31:F8:C5:1A:F0";
    String MacAddress;
    int count = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //initializations
        init();

        //setOnclick listeners
        onClickListenerMethod();
    }


    private void init() {
        // TODO Auto-generated method stub
        connectNew = (Button) findViewById(R.id.button1);
        disconnect = (Button) findViewById(R.id.button2);
        LED_ON = (Button) findViewById(R.id.button3);
        LED_OFF = (Button) findViewById(R.id.button4);
        LOCK = (Button) findViewById(R.id.button5);
        UNLOCK = (Button) findViewById(R.id.button6);
        bLEDState = (Button) findViewById(R.id.button9);
        bBattVolt = (Button) findViewById(R.id.bBattVolt);
        bReset = (Button) findViewById(R.id.bReset);
        bMag = (Button) findViewById(R.id.bMag);
        bAcc = (Button) findViewById(R.id.bACC);
        bTxPower = (Button) findViewById(R.id.bTxPower);
        bFirmware = (Button) findViewById(R.id.bFirmware);
        bMOT = (Button) findViewById(R.id.bMOT);
        bLED = (Button) findViewById(R.id.bLED);
        bKEY = (Button) findViewById(R.id.bKEY);
        bScenario = (Button) findViewById(R.id.bScenario);
        bReadCommand = (Button) findViewById(R.id.bReadCommand);
        bShipping = (Button) findViewById(R.id.bShipping);
        swAutoLock = (Switch) findViewById(R.id.swAutoLock);
        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        tvLEDState = (TextView) findViewById(R.id.tvLEDState);
        tvBattVolt = (TextView) findViewById(R.id.tvBattVolt);
        tvTemp = (TextView) findViewById(R.id.tvTemp);
        tvRssi = (TextView) findViewById(R.id.tvRssi);
        tvLockState = (TextView) findViewById(R.id.tvLockState);
        tvLS = (TextView) findViewById(R.id.tvLS);
        manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        myBluetoothAdapter = manager.getAdapter();

        BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);


    }
    private void onClickListenerMethod() {
        // TODO Auto-generated method stub
        connectNew.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.bluetooth_list, null);
                scanBluetooth = (Button) view.findViewById(R.id.scanBluetooth);
                listView = (ListView) view.findViewById(R.id.bluetoothList);
                builder.setView(view);
                dialog_set();
                dialog = builder.create();
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();


            }


        });

        //disconnect button - Bluetooth disconnect, textview resets, throws exception when no device is connected
        disconnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try{
                    booleanVerify = false;
                    myBluetoothAdapter.stopLeScan(mLeScanCallback);
                    textView2.setText("");
                    mBluetoothGatt.close();
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt = null;
                    Method m = Device.getClass().getMethod("removeBond", (Class[]) null);
                    m.invoke(Device, (Object[]) null);
                    timer.cancel();
                } catch(Exception e){
                    Toast.makeText(getApplicationContext(),"No device is connected",
                            Toast.LENGTH_LONG).show();
                }


            }
        });

        //LED_ON button - Turns on LED, throws exception when no device is connected
        LED_ON.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try{
                    cLED_STATE= mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_LED_STATE);
                    cLED_STATE.setValue(new byte[] {(byte) 0xFF});
                    mBluetoothGatt.writeCharacteristic(cLED_STATE);
                    Toast.makeText(getApplicationContext(),"LED Status = ON",
                            Toast.LENGTH_LONG).show();
                } catch(Exception e){
                    Toast.makeText(getApplicationContext(),"No device is connected",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        //LED_OFF button - Turns off LED, throws exception when no device is connected
        LED_OFF.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try{
                    cLED_STATE= mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_LED_STATE);
                    cLED_STATE.setValue(new byte[] {0x00});
                    mBluetoothGatt.writeCharacteristic(cLED_STATE);
                    Toast.makeText(getApplicationContext(),"LED Status = OFF",
                            Toast.LENGTH_LONG).show();
                } catch(Exception e){
                    Toast.makeText(getApplicationContext(),"No device is connected",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        //LOCK button - LOCKS, throws exception when no device is connected
        LOCK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try{
                    cLOCK= mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_LOCK_STATE);
                    cLOCK.setValue(new byte[]{0x01});
                    mBluetoothGatt.writeCharacteristic(cLOCK);

                    pdLock = new ProgressDialog(context);                         //setup Lock progress dialog
                    pdLock.setMessage("Locking...");
                    pdLock.show();
                } catch(Exception e){
                    Toast.makeText(getApplicationContext(),"No device is connected",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        //UNLOCK button - UNLOCKS, throws exception when no device is connected
        UNLOCK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    cUNLOCK= mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_LOCK_STATE);
                    cUNLOCK.setValue(new byte[]{0x00});
                    mBluetoothGatt.writeCharacteristic(cUNLOCK);
                    pdLock = new ProgressDialog(context);                         //setup Lock progress dialog
                    pdLock.setMessage("Unlocking...");
                    pdLock.show();

                } catch(Exception e){
                    Toast.makeText(getApplicationContext(),"No device is connected",
                            Toast.LENGTH_LONG).show();
                }
            }
        });


        //Reads LED state and displays in textView , throws exception when no device is connected
        bLEDState.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try{
                    BluetoothGattCharacteristic cLED_STATE= mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_LED_STATE);
                    mBluetoothGatt.readCharacteristic(cLED_STATE);

                    Integer temp = cLED_STATE.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

                    if(temp == 0){
                        tvLEDState.setText("LED is OFF");
                    }else if(temp >= 1) {
                        tvLEDState.setText("LED is ON");
                    }
                } catch(Exception e){
                    Toast.makeText(getApplicationContext(),""+e.toString(),
                            Toast.LENGTH_LONG).show();
                }

            }
        });

        bBattVolt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try{
                    cBattVolt= mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_BAT_VOLT);
                    mBluetoothGatt.readCharacteristic(cBattVolt);

                } catch(Exception e){
                    Toast.makeText(getApplicationContext(),""+e.toString(),
                            Toast.LENGTH_LONG).show();
                }

            }
        });

        bReset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    BluetoothGattCharacteristic cReset= mBluetoothGatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_RESET);
                    cReset.setValue(new byte[] {0x00});
                    mBluetoothGatt.writeCharacteristic(cReset);

                } catch(Exception e){
                    Toast.makeText(getApplicationContext(),"No device is connected",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        bMag.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub



                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.pop_up, null);
                bMagClose = (Button) view.findViewById(R.id.bMagClose);
                tvXDisplay = (TextView) view.findViewById(R.id.tvXDisplay);
                tvYDisplay = (TextView) view.findViewById(R.id.tvYDisplay);
                tvZDisplay = (TextView) view.findViewById(R.id.tvZDisplay);
                tvMagOutput = (TextView) view.findViewById(R.id.tvMagOutput);
                builder.setView(view);
                bMagClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        dialog1.cancel();
                        try {
                            cMag = mBluetoothGatt.getService(UUID_ACC_MAG_SER).getCharacteristic(UUID_MAG);
                            mBluetoothGatt.setCharacteristicNotification(cMag, true);
                            BluetoothGattDescriptor descriptor = cMag.getDescriptor(UUID_ACC_DESC);
                            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                            mBluetoothGatt.writeDescriptor(descriptor);
                        }catch(Exception e){}
                    }
                });
                dialog1 = builder.create();
                dialog1.setCanceledOnTouchOutside(false);
                try {
                    cMag = mBluetoothGatt.getService(UUID_ACC_MAG_SER).getCharacteristic(UUID_MAG);
                    mBluetoothGatt.setCharacteristicNotification(cMag, true);
                    BluetoothGattDescriptor descriptor = cMag.getDescriptor(UUID_ACC_DESC);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                    dialog1.show();


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "No device is connected",
                            Toast.LENGTH_LONG).show();
                }


            }
        });

        bAcc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                booleanVerify = false;

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.pop_up, null);
                bAccClose = (Button) view.findViewById(R.id.bMagClose);
                tvAccMag = (TextView) view.findViewById(R.id.tvAccMag);
                tvAccMag.setText("Accelerometer Data");
                tvXDisplay = (TextView) view.findViewById(R.id.tvXDisplay);
                tvYDisplay = (TextView) view.findViewById(R.id.tvYDisplay);
                tvZDisplay = (TextView) view.findViewById(R.id.tvZDisplay);
                tvXSD = (TextView) view.findViewById(R.id.tvXSD);
                tvYSD = (TextView) view.findViewById(R.id.tvYSD);
                tvZSD = (TextView) view.findViewById(R.id.tvZSD);

                tvMagOutput = (TextView) view.findViewById(R.id.tvMagOutput);
                builder.setView(view);
                bAccClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        dialog1.cancel();
                        try {
                            cAcc = mBluetoothGatt.getService(UUID_ACC_MAG_SER).getCharacteristic(UUID_ACC);
                            mBluetoothGatt.setCharacteristicNotification(cAcc, true);
                            BluetoothGattDescriptor descriptor = cAcc.getDescriptor(UUID_ACC_DESC);
                            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                            mBluetoothGatt.writeDescriptor(descriptor);
                            //booleanVerify = true;
                        }catch(Exception e){}
                    }
                });
                dialog1 = builder.create();
                dialog1.setCanceledOnTouchOutside(false);
                try {
                    cAcc = mBluetoothGatt.getService(UUID_ACC_MAG_SER).getCharacteristic(UUID_ACC);
                    mBluetoothGatt.setCharacteristicNotification(cAcc, true);
                    BluetoothGattDescriptor descriptor = cAcc.getDescriptor(UUID_ACC_DESC);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                    dialog1.show();


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "No device is connected",
                            Toast.LENGTH_LONG).show();
                }


            }
        });

        bTxPower.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub



                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.tx_power, null);
                bTxClose = (Button) view.findViewById(R.id.bTxClose);
                bTxRead = (Button) view.findViewById(R.id.bTxRead);
                etTX = (EditText) view.findViewById(R.id.etTX);
                Button bTx04 = (Button) view.findViewById(R.id.bTX04);
                tvTxDisplay = (TextView) view.findViewById(R.id.tvTXOutput);
                builder.setView(view);
                bTxClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        dialog2.cancel();

                    }
                });
                bTxRead.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        try{
                            BluetoothGattCharacteristic cTX_POWER= mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_TX_PWR);
                            mBluetoothGatt.readCharacteristic(cTX_POWER);

                            Integer temp = cTX_POWER.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
                            tvTxDisplay.setText("TX Power: "+temp);

                        } catch(Exception e){
                            Toast.makeText(getApplicationContext(),""+e.toString(),
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });

                bTx04.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        try {
                            BluetoothGattCharacteristic cTX_POWER = mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_TX_PWR);
                            if ("-40".equals(etTX.getText().toString())) {
                                cTX_POWER.setValue(new byte[]{(byte) 0xD8});
                                mBluetoothGatt.writeCharacteristic(cTX_POWER);
                            } else if ("-30".equals(etTX.getText().toString())) {
                                cTX_POWER.setValue(new byte[]{(byte) 0xE2});
                                mBluetoothGatt.writeCharacteristic(cTX_POWER);
                            } else if ("-20".equals(etTX.getText().toString())) {
                                cTX_POWER.setValue(new byte[]{(byte) 0xEC});
                                mBluetoothGatt.writeCharacteristic(cTX_POWER);
                            } else if ("-8".equals(etTX.getText().toString())) {
                                cTX_POWER.setValue(new byte[]{(byte) 0xF0});
                                mBluetoothGatt.writeCharacteristic(cTX_POWER);
                            } else if ("-12".equals(etTX.getText().toString())) {
                                cTX_POWER.setValue(new byte[]{(byte) 0xF8});
                                mBluetoothGatt.writeCharacteristic(cTX_POWER);
                            } else if ("-4".equals(etTX.getText().toString())) {
                                cTX_POWER.setValue(new byte[]{(byte) 0xFC});
                                mBluetoothGatt.writeCharacteristic(cTX_POWER);
                            } else if ("0".equals(etTX.getText().toString())) {
                                cTX_POWER.setValue(new byte[]{(byte) 0x00});
                                mBluetoothGatt.writeCharacteristic(cTX_POWER);
                            } else if ("4".equals(etTX.getText().toString())) {
                                cTX_POWER.setValue(new byte[]{(byte) 0x04});
                                mBluetoothGatt.writeCharacteristic(cTX_POWER);
                            } else {
                                Toast.makeText(getApplicationContext(), "Invalid Value",
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                        }
                    }
                });
                dialog2 = builder.create();
                dialog2.setCanceledOnTouchOutside(false);

                dialog2.show();


            }
        });

        bFirmware.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.firmware_update, null);
                builder.setView(view);
                dialog3 = builder.create();
                dialog3.setCanceledOnTouchOutside(false);
                dialog3.show();
                tvNotify = (TextView) view.findViewById(R.id.tvNotify);
                Button bFwClose = (Button) view.findViewById(R.id.bFwClose);
                Button bFwVersion = (Button) view.findViewById(R.id.bFwVersion);
                Button bFwReset = (Button) view.findViewById(R.id.bFwReset);
                Button bgetFW = (Button) view.findViewById(R.id.bTest);
                Button bNotify = (Button) view.findViewById(R.id.bNotify);
                Button bWrite = (Button) view.findViewById(R.id.bWrite);

                bFwClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        dialog3.dismiss();
                    }
                });
                bFwReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        try {
                            BluetoothGattCharacteristic cReset= mBluetoothGatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_RESET);
                            cReset.setValue(new byte[] {(byte) 0xBB});
                            mBluetoothGatt.writeCharacteristic(cReset);
                        } catch(Exception e){
                            Toast.makeText(getApplicationContext(),"No device is connected",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });
                bFwVersion.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        try{
                            BluetoothGattCharacteristic cBootVersion= mBluetoothGatt.getService(UUID_BOOT_SER).getCharacteristic(UUID_BOOT_VERSION);
                            mBluetoothGatt.readCharacteristic(cBootVersion);
                            Integer temp = cBootVersion.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 9);
                            Integer temp1 = cBootVersion.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 11);
                            Toast.makeText(getApplicationContext(),""+temp+"."+temp1,
                                    Toast.LENGTH_LONG).show();

                        } catch(Exception e){
                            Toast.makeText(getApplicationContext(),""+e.toString(),
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });
                bgetFW.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        FirmwareUpdate objFirmwareUpdate = new FirmwareUpdate(context);
                        fwUpdateArray = objFirmwareUpdate.getStringArray();
                    }
                });
                bNotify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        try {
                            cNotify = mBluetoothGatt.getService(UUID_BOOT_SER).getCharacteristic(UUID_BOOT_NOTIFY);
                            mBluetoothGatt.setCharacteristicNotification(cNotify, true);
                            BluetoothGattDescriptor descriptor = cNotify.getDescriptor(UUID_NOTIFY_DESC);
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            mBluetoothGatt.writeDescriptor(descriptor);

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "No device is connected",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });
                bWrite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        try {
                            BluetoothGattCharacteristic cWrite = mBluetoothGatt.getService(UUID_BOOT_SER).getCharacteristic(UUID_BOOT_WRITE);
                            String s = fwUpdateArray[fwCounter];
                            int len = s.length();
                            byte[] byteTemp = new byte[len / 2];
                            for (int i = 0; i < len; i += 2) {
                                byteTemp[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                        + Character.digit(s.charAt(i + 1), 16));
                            }
                            cWrite.setValue(byteTemp);
                            mBluetoothGatt.writeCharacteristic(cWrite);

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "" + e.toString(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });



            }
        });

        swAutoLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AUTO_LOCK = 1;
                } else {
                    AUTO_LOCK = 0;
                }
            }
        });

        bMOT.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.mot, null);
                builder.setView(view);
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                Button bStartTest = (Button) view.findViewById(R.id.bStartTest);
                tvStartTest = (TextView) view.findViewById(R.id.tvStartTest);
                tvStartTest1 = (TextView) view.findViewById(R.id.tvStartTest1);
                etStartTest2 = (EditText) view.findViewById(R.id.etStartTest2);

                bStartTest.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub

                        try {
                            String temp = Integer.toString(Integer.parseInt(String.valueOf(etStartTest2.getText())), 16);
                            byte[] bytes = temp.getBytes("UTF-8");
                            String temp1 = new String(bytes, "UTF-8");
                            Log.i("EditText Value", "" + temp1);
                            cLockAdjust = mBluetoothGatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_LOCK_ADJUST);
                            cLockAdjust.setValue(bytes);
                            mBluetoothGatt.writeCharacteristic(cLockAdjust);

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Enter only numbers from 0 to 255",
                                    Toast.LENGTH_LONG).show();
                        }


                    }
                });
            }
        });

        bLED.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.led, null);
                builder.setView(view);
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                Button bStart = (Button) view.findViewById(R.id.bStart);
                Button bStop = (Button) view.findViewById(R.id.bStop);

                bStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            timerLED = new Timer();
                            timerLED.schedule(new TimerTask()
                            {
                                @Override
                                public void run()
                                {
                                    LEDDisplay(countLED);
                                }
                            }, 0, 200);
                    }
                    });

                bStop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timerLED.cancel();
                    }
                });

            }
        });

        bKEY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.key, null);
                builder.setView(view);
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                Button bPUB = (Button) view.findViewById(R.id.bPub);
                Button bReadVerify = (Button) view.findViewById(R.id.bReadVerify);
                Button bMSG = (Button) view.findViewById(R.id.bMsg);
                Button bChallengeKey = (Button) view.findViewById(R.id.bChallengeKey);
                Button bTestChallenge = (Button) view.findViewById(R.id.bTestChallenge);
                Button bReadChallenge = (Button) view.findViewById(R.id.bReadChallenge);
                Button bChallengeData = (Button) view.findViewById(R.id.bChallengeData);
                tvReadVerify = (TextView) view.findViewById(R.id.tvReadVerify);
                booleanVerify = true;
                enableVerify();
                //getKeys();

                bPUB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        writePUBKey();
                    }
                });

                bMSG.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        writeMSG();
                    }
                });

                bChallengeKey.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        //writeChallKey();
                    }
                });

                bReadChallenge.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        //readChallData();
                    }
                });

                bReadVerify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //enableVerify();
                    }
                });

                bTestChallenge.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        //getKeys();
                    }
                });

                bChallengeData.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub

                        //writeChallResult();

                    }
                });

            }
        });

        bScenario.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.scenario, null);
                builder.setView(view);
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                LedsOnOff = 0;
                motCount1 = 0;

                Button bStartTest = (Button) view.findViewById(R.id.bStartTest);
                Button bStopTest = (Button) view.findViewById(R.id.bStopTest);
                tvLedsOnOff = (TextView) view.findViewById(R.id.tvLedsOnOff);
                tvLocksUnlocks = (TextView) view.findViewById(R.id.tvLocksUnlocks);
                tvScenarioStatus = (TextView) view.findViewById(R.id.textView_status);
                etScenarioTime = (EditText) view.findViewById(R.id.edittext1);
                etScenarioCount = (EditText) view.findViewById(R.id.edittext2);

                tvLedsOnOff.setText("LEDs On/ Off : " + LedsOnOff);

                bStartTest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if((etScenarioTime.getText().toString().trim().length()>0) && (etScenarioCount.getText().toString().trim().length()>0)){
                            int time = Integer.valueOf(etScenarioTime.getText().toString().trim())*60*1000;
                            int count = Integer.valueOf(etScenarioCount.getText().toString().trim());
                            maxScenarioCount = count;
                            timerScenario = time/(count*2);
                            Log.i("timer value",""+timerScenario);
                            LOCK_COUNT = 1;
                            lock();
                        }else{
                            Toast.makeText(getApplicationContext(),"Enter Valid Values",
                                    Toast.LENGTH_SHORT).show();
                        }



                    }
                });

                bStopTest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //timerLED.cancel();
                        LOCK_COUNT=0;
                    }
                });

            }
        });

        bReadCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cVerify= mBluetoothGatt.getService(UUID_CRYPTO_SER).getCharacteristic(UUID_CRYPTO_VERIFY);
                mBluetoothGatt.readCharacteristic(cVerify);

            }
        });

        bShipping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    BluetoothGattCharacteristic cReset= mBluetoothGatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_RESET);
                    cReset.setValue(new byte[] {(byte) 0xBC});
                    mBluetoothGatt.writeCharacteristic(cReset);

                } catch(Exception e){
                    Toast.makeText(getApplicationContext(),"No device is connected",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private List<BluetoothDevice> tmpBtChecker = new ArrayList<BluetoothDevice>();

    protected void discoverServices() {
        // TODO Auto-generated method stub
        int count = 10;
        while(true){
            mBluetoothGatt.discoverServices();
            count = mBluetoothGatt.getServices().size();
            if(count >= 4){

                timer = new Timer();
                timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        //mBluetoothGatt.readRemoteRssi();
                    }
                }, 0, 1000);

                break;
            }
        }
        Toast.makeText(getApplicationContext(),"Services Discovered!!!",
                Toast.LENGTH_LONG).show();
    }

    private void setupChars(){
        try {
            cLOCK = mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_LOCK_STATE);
            cLED_STATE = mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_LED_STATE);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    protected void bluetoothConnect() {
        // TODO Auto-generated method stub
        mBluetoothGatt = Device.connectGatt(myContext, true, mGattCallback);
        Device.createBond();
        if(mBluetoothGatt.connect()==true){

            MacAddress = Device.getAddress().replace(":","");
            textView2.setText(""+MacAddress);
        }
        pdBluetooth = new ProgressDialog(context);
        pdBluetooth.setMessage("Connecting...");
        pdBluetooth.setCancelable(true);
        pdBluetooth.show();

        timerBluetooth = new Timer();
        timerBluetooth.schedule(new TimerTask() {
            @Override
            public void run() {
                timerCount++;
                if (timerCount >= 150) {
                    if (pdBluetooth.isShowing()) {
                        pdBluetooth.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Device not connected",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    timerBluetooth.cancel();
                    timerCount = 0;
                }
            }
        }, 0, 1000);



    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void dialog_set(){
        scanBluetooth.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                tmpBtChecker.clear();

                if (!myBluetoothAdapter.isEnabled()) {
                    Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOnIntent, 1);
                }
                try {

                    if (myBluetoothAdapter.isDiscovering()) {
                        // the button is pressed when it discovers, so cancel the discovery
                        myBluetoothAdapter.cancelDiscovery();
                    }
                    else {
                        BTArrayAdapter.clear();
                        tmpBtChecker.clear();
                        //myBluetoothAdapter.startDiscovery();
                        myBluetoothAdapter.startLeScan(mLeScanCallback);
                        //registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                myBluetoothAdapter.stopLeScan(mLeScanCallback);
                            }
                        }, 10000);

                    }

                    BTArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);

                    listView.setAdapter(BTArrayAdapter);

                } catch(Exception e){
                    String temp = e.toString();
                    Toast.makeText(getApplicationContext(),""+temp,
                            Toast.LENGTH_LONG).show();

                }
            }
        });

        listView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                myBluetoothAdapter.cancelDiscovery();
                myBluetoothAdapter.stopLeScan(mLeScanCallback);
                String[] lines = ((String) listView.getItemAtPosition(position)).split("\\n");
                String line2 = lines[1];
                Toast.makeText(getApplicationContext(),"Connecting to "+line2 ,
                        Toast.LENGTH_LONG).show();
                Device = myBluetoothAdapter.getRemoteDevice(""+line2);
                try{
                    bluetoothConnect();

                } catch(Exception e){
                    Toast.makeText(getApplicationContext(),""+e.toString() ,
                            Toast.LENGTH_LONG).show();
                }

                dialog.dismiss();
            }
        });
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!tmpBtChecker.contains(device)){
                                if(device.getName() != null){
                                    if(device.getName().contains("Sky")){
                                        tmpBtChecker.add(device);
                                        BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                                        BTArrayAdapter.notifyDataSetChanged();
                                    }
                                }
                            }

                        }
                    });
                }
            };

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Device.createBond();
                mBluetoothGatt.discoverServices();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pdBluetooth.setMessage("Discovering Services...");
                    }
                });


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                timer.cancel();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView2.setText("   " + MacAddress + "\n Disconnected. Get Closer!!! ");

                    }
                });

            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            //Boolean bondState = Device.createBond();
            /*if(!bondState){
                Method m = null;
                try {
                    m = Device.getClass().getMethod("removeBond", (Class[]) null);
                    m.invoke(Device, (Object[]) null);
                    mBluetoothGatt.close();
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt = null;
                    bluetoothConnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }*/
            timer = new Timer();
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    mBluetoothGatt.readRemoteRssi();
                }
            }, 0, 3000);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Connection Successful",
                            Toast.LENGTH_LONG).show();
                    setupChars();
                    enableVerify();
                    //getKeys();
                    //writeMSG();

                    //if (pdBluetooth.isShowing()) pdBluetooth.dismiss();
                    if (pdBluetooth.isShowing()) //pdBluetooth.setMessage("Writing_Keys");
                        pdBluetooth.dismiss();
                }
            });
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(characteristic.equals(cBattVolt)){
                displayBattTemp(characteristic);
                //displayMotorTest(characteristic);
            }else if(characteristic.equals(cChallengeData)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(cChallengeData != null){
                            byte[] temp = cChallengeData.getValue();
                            challengeData = bytesToHex(temp);
                            getKeys();
                            writeChallResult();
                        }
                    }
                });

            }else if(characteristic.equals(cVerify)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer temp = cVerify.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                        Log.i("Notification", "Value: " + temp);
                        Toast.makeText(context, "Command Status: "+temp,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(motCount1>(maxScenarioCount-1)){
                LOCK_COUNT = 0;
            }
            if (characteristic.equals(cLED_STATE)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (LOCK_COUNT == 1) {
                            if (booleanLock) {
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        lock();
                                        booleanLock = false;
                                    }
                                }, timerScenario);
                            } else {
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        unlock();
                                        booleanLock = true;
                                        motCount1++;
                                        tvLocksUnlocks.setText("Locks/ Unlocks : " + motCount1);
                                    }
                                }, timerScenario);
                            }
                        }
                    }
                });

            } else if (characteristic.equals(cLOCK)) {
                //getValues();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(LOCK_COUNT == 1) {
                            if (booleanLED) {
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i("On Char write", "LED ON");
                                        ledOn();
                                        LedsOnOff++;
                                        tvLedsOnOff.setText("LEDs On/ Off : " + LedsOnOff);
                                        booleanLED = false;
                                    }
                                }, timerScenario);

                            } else {
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i("On Char write", "LED OFF");
                                        ledOff();
                                        booleanLED = true;
                                    }
                                }, timerScenario);

                            }
                        }
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //cBattVolt = mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_BAT_VOLT);
                                //mBluetoothGatt.readCharacteristic(cBattVolt);

                            }
                        }, 300);
                    }
                });


            } else if (characteristic.equals(cUNLOCK)) {
                //getValues();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                cBattVolt = mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_BAT_VOLT);
                                mBluetoothGatt.readCharacteristic(cBattVolt);
                                Log.i("On Char write : UNLOCK", "");
                            }
                        }, 300);
                    }
                });
            }
            else if(characteristic.equals(cPUB)){
                writeChallKey();
            }else if (characteristic.equals(cChallengeKey)){
                writeMSG();
            }else if(characteristic.equals(cMSG)){
                readChallData();
            }


        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            if(characteristic.equals(cVerify)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer temp = cVerify.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                        if (temp == 4) {
                            pdBluetooth.dismiss();
                        }
                        Log.i("Notification", "Value: " + temp);
//                        tvReadVerify.setText("" + temp);
                    }
                });

            }else{
                displayTV(characteristic);
            }

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if(descriptor.equals(descriptor)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(booleanVerify){
                        if (Device.getName().contains("Skylock-")) {
                            PreferenceHandler.storeStringtoPerference(context,MacAddress,null);
                            getKeys();
                            writePUBKey();
                        } else {
                            writeMSG();
                        }
                        }

                    }
                });
            }

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            display(rssi);
        }

        private void display(final int rssi) {
            // TODO Auto-generated method stub
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //textView2.setText("   "+Device.getAddress()+"\n          Rssi:  "+rssi);

                    if(AUTO_LOCK == 1){
                        countArray[LOCK_COUNT] = rssi;
                        LOCK_COUNT++;
                        if(LOCK_COUNT >= 2){
                            LOCK_COUNT = 0;
                            int meanRssi = (Math.abs(countArray[0])+Math.abs(countArray[1])+Math.abs(countArray[2]))/3;
                            MacAddress = Device.getAddress().replace(":","");
                            textView2.setText("   "+MacAddress+"\n meanRssi:  "+meanRssi);
                            if(meanRssi <= 50){
                                auto_unlock();
                            }else if(meanRssi > 50 ){
                                auto_lock();
                            }
                        }

                    }else{
                        MacAddress = Device.getAddress().replace(":","");
                        textView2.setText("   "+MacAddress+"\n          Rssi:  "+rssi);
                    }
                }

                private void auto_lock() {
                    // TODO Auto-generated method stub
                    try{
                        cLOCK= mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_LOCK_STATE);
                        cLOCK.setValue(new byte[] {0x01});
                        mBluetoothGatt.writeCharacteristic(cLOCK);
                        tvLockState.setText("Locked");
                    } catch(Exception e){}

                }

                private void auto_unlock() {
                    // TODO Auto-generated method stub
                    try{
                        cLOCK= mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_LOCK_STATE);
                        cLOCK.setValue(new byte[] {0x00});
                        mBluetoothGatt.writeCharacteristic(cLOCK);
                        tvLockState.setText("Unlocked");
                    } catch(Exception e){}
                }

            });
        }
    };
    protected void displayTV(final BluetoothGattCharacteristic characteristic) {
        // TODO Auto-generated method stub

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //only notify characteristic
                if(characteristic.equals(cNotify)){
                    int tempNotify = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

                    tvNotify.setText("N - "+tempNotify+" - C - "+fwCounter);
                    fwCounter++;


                    if(fwCounter < fileSize){
                        try{
                            BluetoothGattCharacteristic cWrite = mBluetoothGatt.getService(UUID_BOOT_SER).getCharacteristic(UUID_BOOT_WRITE);
                            String s = fwUpdateArray[fwCounter];
                            int len = s.length();
                            byte[] byteTemp = new byte[len/2];
                            for(int i=0; i<len; i+=2){
                                byteTemp[i/2] = (byte) ((Character.digit(s.charAt(i),16)<<4)
                                        + Character.digit(s.charAt(i+1), 16));
                            }
                            cWrite.setValue(byteTemp);
                            mBluetoothGatt.writeCharacteristic(cWrite);

                        }catch(Exception e){
                            Toast.makeText(getApplicationContext(),""+e.toString(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }else{
                        try {
                            BluetoothGattCharacteristic cDone= mBluetoothGatt.getService(UUID_BOOT_SER).getCharacteristic(UUID_BOOT_DONE);
                            cDone.setValue(new byte[] {(byte) 0x01});
                            mBluetoothGatt.writeCharacteristic(cDone);
                            //fwCounter = 0;
                        } catch(Exception e){
                            Toast.makeText(getApplicationContext(),"No device is connected",
                                    Toast.LENGTH_LONG).show();
                        }

                    }

                }else{

                    //Integer X = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                    //Integer Y = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2);
                    //Integer Z = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 4);
                    if(characteristic.equals(cAcc)){
                        Integer XMAV = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                        Integer YMAV = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2);
                        Integer ZMAV = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 4);
                        Integer XSD = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 6);
                        Integer YSD = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 8);
                        Integer ZSD = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 10);
                        Log.i("MAV & Var", " " + XMAV + " " + YMAV + " " + ZMAV + " " + XSD + " " + YSD + " " + ZSD + " ");

                        if(XMAV >= 1500 && XSD <= 3000){
                            theftAlert();
                        }
                        if(YMAV >= 1500 && YSD <= 3000){
                            theftAlert();
                        }
                        if(ZMAV >= 1500 && ZSD <= 3000){
                            theftAlert();
                        }

                        tvXSD.setText("" + XSD);
                        tvYSD.setText("" + YSD);
                        tvZSD.setText("" + ZSD);
                        tvXDisplay.setText("" + XMAV);
                        tvYDisplay.setText("" + YMAV);
                        tvZDisplay.setText("" + ZMAV);
                    }
                    //tvXDisplay.setText("" + X);
                    //tvYDisplay.setText("" + Y);
                    //tvZDisplay.setText("" + Z);


                    //check_shackle_insertion(Z);

                }

            }

            private void theftAlert(){
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setTitle("Theft Alert")
                        .setMessage("Theft Alert detected!")
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                dialog2 = null;
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert);
                if(dialog2==null){
                    dialog2 = builder1.create();
                    if(!dialog2.isShowing())
                        dialog2.show();
                }


            }

            private void check_shackle_insertion(Integer z) {
                // TODO Auto-generated method stub
                //if(z>=9751 || z <= 5000){
                if(z>=9751 ){
                    tvMagOutput.setText("Shackle not inserted");
                    //}else if(z >= 6000 && z <= 9750){
                }else if(z <= 9750){
                    tvMagOutput.setText("Shackle inserted!!!");
                }
            }

        });

    }


    protected void displayMotorTest(final BluetoothGattCharacteristic characteristic) {
        // TODO Auto-generated method stub
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (characteristic != null) {
                    Integer temp = cBattVolt.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                    Integer temp1 = cBattVolt.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);
                    Integer temp2 = cBattVolt.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 3);
                    Integer temp3 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 4);


                    //tvStartTest.setText("LockState: "+temp3);
                    //tvStartTest2.setText("Temp: "+temp1);
                    //tvStartTest1.setText("Count: "+motCount1);

                    if (temp3 == 0) {
                        lock();
                        if (motCount2 == 0) {
                            motCount1++;
                            motCount2 = 1;
                        }


                    } else if (temp3 == 1) {
                        unlock();
                        if (motCount2 == 1) {
                            motCount1++;
                            motCount2 = 0;
                        }

                    }
                }
            }
        });

    }

    private void ledOn(){
        cLED_STATE.setValue(new byte[] {(byte) 0xFF});
        mBluetoothGatt.writeCharacteristic(cLED_STATE);
    }

    private void ledOff(){
        cLED_STATE.setValue(new byte[] {(byte) 0x00});
        mBluetoothGatt.writeCharacteristic(cLED_STATE);
    }

    private void lock() {
        // TODO Auto-generated method stub
        try {
            cLOCK.setValue(new byte[] {0x01});
            mBluetoothGatt.writeCharacteristic(cLOCK);
            getValues();

        } catch(Exception e){}
    }

    private void unlock() {
        // TODO Auto-generated method stub
        try {
            //cUNLOCK = mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_LOCK_STATE);
            cLOCK.setValue(new byte[] {0x00});
            mBluetoothGatt.writeCharacteristic(cLOCK);
            getValues();

        } catch(Exception e){}
    }


    protected void getValues() {
        // TODO Auto-generated method stub
        try{
            cBattVolt= mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_BAT_VOLT);
            mBluetoothGatt.readCharacteristic(cBattVolt);

        } catch(Exception e){}
    }


    protected void displayBattTemp(final BluetoothGattCharacteristic characteristic) {
        // TODO Auto-generated method stub
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(characteristic != null){
                    Integer temp = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                    Integer temp1 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);
                    Integer temp2 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 3);
                    Integer temp3 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 4);

                    String sTemp = bytesToHex(characteristic.getValue());
                    Log.i("0x5E43 value  ",""+sTemp);

                    if(temp3 == 2){
                        Toast.makeText(getApplicationContext(),"Latch state - Locked",
                                Toast.LENGTH_LONG).show();
                    }else if(temp3 == 0){
                        Toast.makeText(getApplicationContext(),"Latch state - Unlocked",
                                Toast.LENGTH_LONG).show();
                    }
                    //else if(temp3 == 2){
                    //    unlock();
                    //}
                    if(pdLock != null){
                        if(pdLock.isShowing())pdLock.dismiss();
                    }

                    float tempf = (float) temp/1000;
                    tvBattVolt.setText("BattVolt: "+tempf);
                    tvTemp.setText("Temp: "+temp1);
                    tvRssi.setText("Rssi: "+temp2);
                    tvLS.setText("Lock: "+temp3);

                }
            }
        });

    }

    private void LEDDisplay(int count){
        if(count == 0){
            cLED_STATE= mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_LED_STATE);
            cLED_STATE.setValue(new byte[] {(byte) 0x01});
            mBluetoothGatt.writeCharacteristic(cLED_STATE);
            countLED = 1;
        }else if(count == 1){
            cLED_STATE= mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_LED_STATE);
            cLED_STATE.setValue(new byte[] {(byte) 0x02});
            mBluetoothGatt.writeCharacteristic(cLED_STATE);
            countLED = 2;
        }else if(count == 2){
            cLED_STATE= mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_LED_STATE);
            cLED_STATE.setValue(new byte[] {(byte) 0x04});
            mBluetoothGatt.writeCharacteristic(cLED_STATE);
            countLED = 3;
        }else if(count == 3){
            cLED_STATE= mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_LED_STATE);
            cLED_STATE.setValue(new byte[] {(byte) 0x08});
            mBluetoothGatt.writeCharacteristic(cLED_STATE);
            countLED = 0;
        }

    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void getKeys(){
        String str_temp = null;


        if((PreferenceHandler.getStringFromPreference(context,MacAddress))==null) {
            JSONClass objJSONClass = new JSONClass(context);

            //KEYS -- MAC ADDRESS
            String url2 = "https://skylock-beta.herokuapp.com/api/v1/users/415/keys/";
            JSONObject json = new JSONObject();
            try {

                json.put("mac_id", "" + MacAddress);

                Log.i("JSON", json.toString());

                se = new StringEntity("" + json.toString());
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("requestcode", "10"));
            nameValuePair.add(new BasicNameValuePair("devicetype", "phone"));
            objJSONClass.putURL(url2, POST, nameValuePair, se);
            json = objJSONClass.executeJSON();

            //PUBKEY AND SIGNED MESSAGE
            try {
                JSONObject temp1 = json.getJSONObject(TAG_PAYLOAD);
                PUB_KEY = temp1.getString(TAG_PUBLIC_KEY);
                MESSAGE = temp1.getString(TAG_MESSAGE);
                PreferenceHandler.storeStringtoPerference(context,MacAddress,MESSAGE);
            } catch (Exception e) {
                str_temp = e.toString();
            }
        }else{
            MESSAGE = PreferenceHandler.getStringFromPreference(context,MacAddress);
            System.out.println("message : " + MESSAGE);
        }


        //Challenge Key
        /*String url = "https://skylock-beta.herokuapp.com/api/v1/users/415/challenge_key/";
        objJSONClass.putURL(url, GET, null, null); //put the URL from which JSON object has to be obtained

        JSONObject temp = objJSONClass.executeJSON(); //executes JSON - returns JSON object from the URL
        try{
            JSONObject temp1 = temp.getJSONObject(TAG_PAYLOAD);
            challengeKey = temp1.getString(TAG_CHALLENGE_KEY);

        }catch(Exception e){
            str_temp = e.toString();
        }*/
        challengeKey = ChallengeResultGenerator.getChallengeKey("415");

        /*//Challenge data
        String url1 = "https://skylock-beta.herokuapp.com/api/v1/users/415/challenge_data/";
        JSONObject json1 = new JSONObject();
        try {

            json1.put("c_data", ""+challengeData);
            json1.put("c_key", "415");

            se = new StringEntity("" + json1.toString());
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("requestcode", "10"));
        nameValuePair.add(new BasicNameValuePair("devicetype", "phone"));
        objJSONClass.putURL(url1, POST, nameValuePair, se);
        json1 = objJSONClass.executeJSON();

        //Challenge Result

        try{
            JSONObject temp1 = json1.getJSONObject(TAG_PAYLOAD);
            challengeResult = temp1.getString(TAG_CHALLENGE_RESULT);
        }catch(Exception e){
            str_temp = e.toString();
        }
*/
        challengeResult = ChallengeResultGenerator.getChallengeResult("415",challengeData);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //alertDialogBuilder.setTitle("Test Challenge");
        //alertDialogBuilder.setMessage("PUB_KEY \n"+PUB_KEY+"\nSign_MSG \n"+MESSAGE+"\nChall_KEY \n"+challengeKey+"\nChall_DATA \n"+challengeData+"\nChall_RESULT \n"+challengeResult)
        //        .setCancelable(true);
        //AlertDialog alertDialog = alertDialogBuilder.create();
        //alertDialog.show();
    }

    public void enableVerify(){
        try{
            cVerify= mBluetoothGatt.getService(UUID_CRYPTO_SER).getCharacteristic(UUID_CRYPTO_VERIFY);
            mBluetoothGatt.setCharacteristicNotification(cVerify, true);
            descriptorVerify = cVerify.getDescriptor(UUID_ACC_DESC);
            descriptorVerify.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptorVerify);
            //mBluetoothGatt.readCharacteristic(cVerify);
            //Integer temp = cVerify.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            //tvReadVerify.setText("Read " + temp);


        }catch(Exception e){
            Toast.makeText(getApplicationContext(),""+e.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void writePUBKey(){
        try{
            cPUB= mBluetoothGatt.getService(UUID_CRYPTO_SER).getCharacteristic(UUID_CRYPTO_PUB);

            //String s= "7ca3d7b9bf13765862a64c87acb848a8dc1637fd08a3215d1404218f9aae35436b91348f53db429fd5e515d503e9438b50fff282ed1fcc4664ea42766f04bc40";
            String s = PUB_KEY;
            int len = s.length();
            byte[] byteTemp = new byte[len/2];
            for(int i=0; i<len; i+=2){
                byteTemp[i/2] = (byte) ((Character.digit(s.charAt(i),16)<<4)
                        + Character.digit(s.charAt(i+1), 16));
            }

            cPUB.setValue(byteTemp);
            mBluetoothGatt.writeCharacteristic(cPUB);

        }catch(Exception e){
            Toast.makeText(getApplicationContext(),""+e.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void writeMSG(){
        try{
            cMSG= mBluetoothGatt.getService(UUID_CRYPTO_SER).getCharacteristic(UUID_CRYPTO_MSG);

            //String s = "0000405c78cf9bbffac5201ae9d1351d8c5e193fed61729433ae4379e01dbf47ffffffff002c2a26ff5b9391ba398117d827bb5be77fe5be8944e583a370f56bb3dcb4c162c5cfe1afcccc8149dc9ac94389cee012ab64c75118d4f1f31d38336168e04cd8";
            String s = MESSAGE;
            int len = s.length();
            byte[] byteTemp = new byte[len/2];
            for(int i=0; i<len; i+=2){
                byteTemp[i/2] = (byte) ((Character.digit(s.charAt(i),16)<<4)
                        + Character.digit(s.charAt(i+1), 16));
            }
            cMSG.setValue(byteTemp);
            mBluetoothGatt.writeCharacteristic(cMSG);

        }catch(Exception e){
            Toast.makeText(getApplicationContext(),""+e.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void writeChallKey(){
        try{
            cChallengeKey= mBluetoothGatt.getService(UUID_CRYPTO_SER).getCharacteristic(UUID_CHALLENGE_KEY);
            String s = challengeKey;
            int len = s.length();
            byte[] byteTemp = new byte[len/2];
            for(int i=0; i<len; i+=2){
                byteTemp[i/2] = (byte) ((Character.digit(s.charAt(i),16)<<4)
                        + Character.digit(s.charAt(i+1), 16));
            }
            cChallengeKey.setValue(byteTemp);
            mBluetoothGatt.writeCharacteristic(cChallengeKey);

        }catch(Exception e){
            Toast.makeText(getApplicationContext(),""+e.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void readChallData(){
        try{
            cChallengeData = mBluetoothGatt.getService(UUID_CRYPTO_SER).getCharacteristic(UUID_CHALLENGE_DATA);
            mBluetoothGatt.readCharacteristic(cChallengeData);


        }catch(Exception e){
            Toast.makeText(getApplicationContext(),""+e.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void writeChallResult(){
        try{

            cChallengeData = mBluetoothGatt.getService(UUID_CRYPTO_SER).getCharacteristic(UUID_CHALLENGE_DATA);
            String s = challengeResult;
            Log.d("Challenge", "Challenge Key " + challengeKey);
            Log.d("Challenge", "Challenge Data "+challengeData);
            Log.d("Challenge", "Challenge Result "+challengeResult);
            int len = s.length();
            byte[] byteTemp = new byte[len/2];
            for(int i=0; i<len; i+=2){
                byteTemp[i/2] = (byte) ((Character.digit(s.charAt(i),16)<<4)
                        + Character.digit(s.charAt(i+1), 16));
            }
            cChallengeData.setValue(byteTemp);
            mBluetoothGatt.writeCharacteristic(cChallengeData);

        }catch(Exception e){
            Toast.makeText(getApplicationContext(),""+e.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
