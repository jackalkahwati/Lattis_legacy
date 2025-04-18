package cc.skylock.skylock;

/**
 * Created by AlexVijayRaj on 7/13/2015.
 */
import android.app.Activity;
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
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothClass extends Activity{

    Context context;
    ImageView ivSignal;
    ListView bluetoothList;
    BluetoothManager manager;
    List<BluetoothDevice> tmpBtChecker;
    BluetoothDevice Device;
    BluetoothGatt mBluetoothGatt;
    BluetoothAdapter myBluetoothAdapter;
    ArrayAdapter<String> BTArrayAdapter;
    BroadcastReceiver bReceiver;
    CrashTheftAlert objCrashTheft;
    Button scanBluetooth;
    Timer timer, timerBluetooth, ledBlinkTimer;
    ProgressDialog pdBluetooth, pdLock;
    Dialog dialogAddLock;
    ObjectRepo objRepo;
    ListView listView;
    BluetoothGattService sLED, sLOCK;
    BluetoothGattCharacteristic cLED_ON, cLED_OFF, cLOCK, cUNLOCK, cLED_STATE, c_TX, c_Testing, cAcc, cResetCapPin;
    BluetoothGattCharacteristic CHAR_PUB_KEY, CHAR_SIGN_MSG, CHAR_CHALL_KEY, CHAR_CHALL_DATA, CHAR_SECURITY_STATE,
                                CHAR_HW_LED, CHAR_HW_LOCK, CHAR_HW_INFO, CHAR_HW_TX,
                                CHAR_CONFIG_RESET, CHAR_CONFIG_LOCK_ADJUST, CHAR_CONFIG_OPEN_ADJUST, CHAR_CONFIG_CAP_PIN,
                                CHAR_ACC, CHAR_MAG;

    int timerCount = 0;
    int LEDBlinkCounter = 1;
    int flagLockState = 0;

    private final static String TAG = "BluetoothGatt";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private UUID UUID_SECURITY_SER = UUID.fromString("d3995e00-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_SECURITY_SIGN_MSG = UUID.fromString("d3995e01-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_SECURITY_PUB_KEY = UUID.fromString("d3995e02-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_SECURITY_CHALL_KEY = UUID.fromString("d3995e03-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_SECURITY_CHALL_DATA = UUID.fromString("d3995e04-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_SECURITY_STATE = UUID.fromString("d3995e05-fa57-11e4-ae59-0002a5d5c51b");

    private UUID UUID_HW_SER = UUID.fromString("d3995e40-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_HW_LED = UUID.fromString("d3995e41-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_HW_LOCK = UUID.fromString("d3995e42-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_HW_INFO = UUID.fromString("d3995e43-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_HW_TX = UUID.fromString("d3995e45-fa57-11e4-ae59-0002a5d5c51b");

    private UUID UUID_CONFIG_SER = UUID.fromString("d3995e80-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_CONFIG_RESET = UUID.fromString("d3995e81-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_CONFIG_LOCK_ADJUST = UUID.fromString("d3995e82-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_CONFIG_OPEN_ADJUST = UUID.fromString("d3995e83-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_CONFIG_CAP_PIN = UUID.fromString("d3995e84-fa57-11e4-ae59-0002a5d5c51b");

    private UUID UUID_TEST_SER = UUID.fromString("d3995eC0-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_TEST_MAG = UUID.fromString("d3995eC3-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_TEST_ACC = UUID.fromString("d3995eC4-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_ACC_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    private UUID UUID_RESET_PIN = UUID.fromString("d3995e84-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_LED_STATE = UUID.fromString("d3995e41-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_LOCK_STATE = UUID.fromString("d3995e42-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_ACC_MAG_SER = UUID.fromString("d3995ec0-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_MAG = UUID.fromString("d3995ec3-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_ACC = UUID.fromString("d3995ec4-fa57-11e4-ae59-0002a5d5c51b");


    public int AUTO_LOCK = 0;
    public int theftLevel = 2;
    public int flagDeviceConnected = 0; //0 = not connected; 1 = connected
    private int LOCK_COUNT = 0;
    private int[] countArray = new int[3];
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

    public BluetoothClass(Context context, ObjectRepo objRepo1) {
        this.context = context;
        objRepo = objRepo1;
        ivSignal = objRepo.ivSignal;
        scanBluetooth = objRepo.scanBluetooth;
        bluetoothList = objRepo.bluetoothList;
        manager = objRepo.manager;
        tmpBtChecker = objRepo.tmpBtChecker;
        mBluetoothGatt = objRepo.mBluetoothGatt;
        Device = objRepo.Device;
        myBluetoothAdapter = objRepo.myBluetoothAdapter;
        BTArrayAdapter = objRepo.BTArrayAdapter;
        bReceiver = objRepo.bReceiver;

    }

    //initialize Bluetooth
    public void bluetooth_setup() {

        tmpBtChecker = new ArrayList<BluetoothDevice>();
        BTArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
        objCrashTheft = new CrashTheftAlert(context);


      }

    //setup all BluetoothCharacteristics
    private void setupCHARs(){
        CHAR_PUB_KEY = mBluetoothGatt.getService(UUID_SECURITY_SER).getCharacteristic(UUID_SECURITY_PUB_KEY);
        CHAR_SIGN_MSG = mBluetoothGatt.getService(UUID_SECURITY_SER).getCharacteristic(UUID_SECURITY_SIGN_MSG);
        CHAR_CHALL_KEY= mBluetoothGatt.getService(UUID_SECURITY_SER).getCharacteristic(UUID_SECURITY_CHALL_KEY);
        CHAR_CHALL_DATA = mBluetoothGatt.getService(UUID_SECURITY_SER).getCharacteristic(UUID_SECURITY_CHALL_DATA);
        CHAR_SECURITY_STATE = mBluetoothGatt.getService(UUID_SECURITY_SER).getCharacteristic(UUID_SECURITY_STATE);

        CHAR_HW_LED = mBluetoothGatt.getService(UUID_HW_SER).getCharacteristic(UUID_HW_LED);
        CHAR_HW_LOCK = mBluetoothGatt.getService(UUID_HW_SER).getCharacteristic(UUID_HW_LOCK);
        CHAR_HW_INFO = mBluetoothGatt.getService(UUID_HW_SER).getCharacteristic(UUID_HW_INFO);
        CHAR_HW_TX = mBluetoothGatt.getService(UUID_HW_SER).getCharacteristic(UUID_HW_TX);

        CHAR_CONFIG_RESET = mBluetoothGatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CONFIG_RESET);
        CHAR_CONFIG_LOCK_ADJUST = mBluetoothGatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CONFIG_LOCK_ADJUST);
        CHAR_CONFIG_OPEN_ADJUST = mBluetoothGatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CONFIG_OPEN_ADJUST);
        CHAR_CONFIG_CAP_PIN = mBluetoothGatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CONFIG_CAP_PIN);

        CHAR_MAG = mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_TEST_MAG);
        CHAR_ACC = mBluetoothGatt.getService(UUID_TEST_SER).getCharacteristic(UUID_TEST_ACC);
    }

    public void temp_add_lock(){
        //Temp Add Lock setup
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                myBluetoothAdapter.cancelDiscovery();
                myBluetoothAdapter.stopLeScan(mLeScanCallback);
                String[] lines = ((String) listView.getItemAtPosition(position)).split("\\n");
                String line2 = lines[1];
                Toast.makeText(context, "Connecting to " + line2,
                        Toast.LENGTH_LONG).show();
                Device = myBluetoothAdapter.getRemoteDevice("" + line2);
                try {
                    bluetoothConnect();

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "" + e.toString(),
                            Toast.LENGTH_LONG).show();
                }
                objRepo.objAddLock.dialog.dismiss();

            }
        });

    }

    //setup the theft level required - 1 = low; 2 = med; 3 = high
    public void put_theft_level(int theftLevel1){
        theftLevel = theftLevel1;
    }

    //set the current bluetooth device to be used
    public void setDevice(BluetoothDevice deviceTemp){
        Device = deviceTemp;
    }

    //Connect to the Bluetooth device
    public void bluetoothConnect() {
        // TODO Auto-generated method stub
        mBluetoothGatt = Device.connectGatt(context, true, mGattCallback); //connect gatt using callback
        //Device.createBond();
        pdBluetooth = new ProgressDialog(context);                         //setup Bluetooth progress dialog
        pdBluetooth.setMessage("Connecting...");
        pdBluetooth.setCancelable(false);
        pdBluetooth.show();

        timerBluetooth = new Timer();                                      //Timer to kill connecting in 25 seconds - toasts not connected
        timerBluetooth.schedule(new TimerTask() {
            @Override
            public void run() {
                timerCount++;
                if(timerCount >= 40){                                      //25 seconds
                    if (pdBluetooth.isShowing()) {
                        pdBluetooth.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Device not connected",
                                        Toast.LENGTH_LONG).show();
                                flagDeviceConnected = 0;
                                mBluetoothGatt.disconnect();
                            }
                        });
                    }
                    timerBluetooth.cancel();
                    timerCount = 0;
                }
            }
        }, 0, 1000);

    }

    //disconnects Bluetooth connection with the device
    public void bluetoothDisconnect(){
        try{
            myBluetoothAdapter.stopLeScan(mLeScanCallback);
        }catch(Exception ignored){}
        try{
            mBluetoothGatt.disconnect();

        }catch(Exception ignored){}
        try{
            //mBluetoothGatt.close();
        }catch(Exception ignored){}
    }

    //writes the lock characteristic
    public void lock(){
            try {
                cLOCK = mBluetoothGatt.getService(UUID_HW_SER).getCharacteristic(UUID_LOCK_STATE);
                cLOCK.setValue(new byte[]{0x00});
                mBluetoothGatt.writeCharacteristic(cLOCK);

            }catch (Exception e){
                Toast.makeText(context, "No device is connected",
                        Toast.LENGTH_LONG).show();
            }

    }

    //writes the unlock characteristic
    public void unlock(){
        try{
            cLOCK = mBluetoothGatt.getService(UUID_HW_SER).getCharacteristic(UUID_LOCK_STATE);
            cLOCK.setValue(new byte[]{0x01});
            mBluetoothGatt.writeCharacteristic(cLOCK);


        }catch (Exception e){
        Toast.makeText(context, "No device is connected",
                Toast.LENGTH_LONG).show();
        }

    }

    //writes the led - ON characteristic
    public void LED_ON(){
        try {
            CHAR_HW_LED = mBluetoothGatt.getService(UUID_HW_SER).getCharacteristic(UUID_HW_LED);
            CHAR_HW_LED.setValue(new byte[]{(byte) 0xFF});
            mBluetoothGatt.writeCharacteristic(CHAR_HW_LED);
        }catch (Exception e){
            Log.e("Bluetooth","LED - ON Unsuccessful");
            }

    }

    //writes the led - OFF characteristic
    public void LED_OFF(){
        try{
            CHAR_HW_LED = mBluetoothGatt.getService(UUID_HW_SER).getCharacteristic(UUID_HW_LED);
            CHAR_HW_LED.setValue(new byte[]{0x00});
            mBluetoothGatt.writeCharacteristic(CHAR_HW_LED);
        }catch (Exception e){
            Log.e("Bluetooth", "LED - OFF Unsuccessful");
        }

    }

    public void resetCapPin(int[] intArray){
        String[] capPinArray = new String[16];
        String s0 = "00";
        String s1 = "01";
        String s2 = "02";
        String s3 = "04";
        String s4 = "08";

        for(int i=0; i<intArray.length; i++){
            if(intArray[i]==1){
                capPinArray[i] = s1;
            }else if(intArray[i]==2){
                capPinArray[i] = s2;
            }else if(intArray[i]==4){
                capPinArray[i] = s3;
            }else if(intArray[i]==8){
                capPinArray[i] = s4;
            }else if(intArray[i]==0){
                capPinArray[i] = s0;
            }
        }
        StringBuilder sb = new StringBuilder();
        for(String s : capPinArray){
            sb.append(s);
        }

        Log.d("CapPinArray", sb.toString());

        try{
            cResetCapPin = mBluetoothGatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_RESET_PIN);

            String s = sb.toString();
            int len = s.length();
            byte[] byteTemp = new byte[len/2];
            for(int i=0; i<len; i+=2){
                byteTemp[i/2] = (byte) ((Character.digit(s.charAt(i),16)<<4)
                        + Character.digit(s.charAt(i+1), 16));
            }

            cResetCapPin.setValue(byteTemp);
            mBluetoothGatt.writeCharacteristic(cResetCapPin);

        }catch(Exception e){
            Toast.makeText(getApplicationContext(),""+e.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    //enables the acc characteristic - temp = true; disables the acc characteristic - temp = false
    //flag - for crash and theft.  0 = off; 1 = crash ON; 2 = theft ON;
    public void enableAcc(boolean temp, int flag) {

        if (temp) {
            try {
                cAcc = mBluetoothGatt.getService(UUID_ACC_MAG_SER).getCharacteristic(UUID_ACC);
                mBluetoothGatt.setCharacteristicNotification(cAcc, true);
                BluetoothGattDescriptor descriptor = cAcc.getDescriptor(UUID_ACC_DESC);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
                objCrashTheft.flagCrashTheft(flag, theftLevel); //turning on - crash or theft
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "No device is connected",
                        Toast.LENGTH_LONG).show();
            }

        } else {
            try {
                cAcc = mBluetoothGatt.getService(UUID_ACC_MAG_SER).getCharacteristic(UUID_ACC);
                mBluetoothGatt.setCharacteristicNotification(cAcc, true);
                BluetoothGattDescriptor descriptor = cAcc.getDescriptor(UUID_ACC_DESC);
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
                objCrashTheft.flagCrashTheft(flag, theftLevel); // turning off crash or theft

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "No device is connected",
                        Toast.LENGTH_LONG).show();
            }

        }
    }

    public void checkBluetoothState(){
        if (!myBluetoothAdapter.isEnabled()) {

            BluetoothAdapter.getDefaultAdapter().enable();

        }
    }

    private String logFoundLocks(){
        String deviceMacAddress = null;
        int devicesNumber = BTArrayAdapter.getCount();
        for(int i=0; i<devicesNumber; i++){
            Log.i("Locks Found",""+BTArrayAdapter.getItem(i));
            if(BTArrayAdapter.getItem(i).contains("Skylock-")){
                String[] name = BTArrayAdapter.getItem(i).split("-");
                deviceMacAddress = objRepo.macAddColon(""+name[1].substring(0,12));
                Log.i("Unprovisioned Locks","MAC - "+deviceMacAddress);
            }else{
                objRepo.displayToast("No locks found");
                objRepo.objAddLock.ivBluetoothProgress.setImageDrawable(context.getResources().getDrawable(R.drawable.bluetooth_progress_2));
                objRepo.objAddLock.bluetoothTimer.cancel();
                objRepo.objAddLock.bluetoothProgressCount = 1;
            }
        }
        return deviceMacAddress;
    }

    public void addLockStartSearch(){

        BTArrayAdapter.clear();
        tmpBtChecker.clear();
        myBluetoothAdapter.startLeScan(mLeScanCallback);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                myBluetoothAdapter.stopLeScan(mLeScanCallback);
                String deviceMacADdress = logFoundLocks();
                if (deviceMacADdress != null) {
                    BluetoothDevice device = myBluetoothAdapter.getRemoteDevice("" + deviceMacADdress);
                    if (device != null) {
                        setDevice(device);

                        try {
                            bluetoothConnect();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "" + e.toString(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }, 10000);

    }



    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!tmpBtChecker.contains(device)){
                                if(device.getName() != null) {
                                    if (device.getName().contains("Sky")) {
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

    //GattCallback
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        //Called when Bluetooth connection state changes
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //if bluetooth state is connected
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" );
                mBluetoothGatt.discoverServices(); //start discovering services
                flagDeviceConnected = 1;                                    //set Bluetooth connection flag
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pdBluetooth.setMessage("Discovering Services...");  //change message in  progress dialog
                    }
                });
            //if bluetooth state is disconnected
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                Log.i(TAG, "Disconnected from GATT server.");
                //mBluetoothGatt = null;
                timer.cancel();                                             //stop RSSI timer
                flagDeviceConnected = 0;                                    //reset Bluetooth connection flag
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ivSignal.setImageDrawable(null);
                        Toast.makeText(context, "Connection lost",          //toasts bluetooth connection
                                Toast.LENGTH_LONG).show();
                    }
                });

            }

        }

        //is called when the bluetooth discovery is over
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            timer = new Timer();                                                //start RSSI timer
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mBluetoothGatt.readRemoteRssi();
                }
            }, 0, 1000);
            setupCHARs();                                                       //setup all services and characteristics
            getKeys();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Connection Successful",            //toast Bluetooth connection and dismiss progress dialog
                            Toast.LENGTH_LONG).show();
                    if(pdBluetooth.isShowing()){
                        pdBluetooth.dismiss();
                    }
                    /*pdBluetooth.setMessage("Writing keys...");
                    if (Device.getName().substring(0,8).equals("Skylock-")) {
                        Log.i("Services Discovered", "Unprovisioned Lock found");
                        enableVerify();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                objRepo.objBluetoothClass.writePUBKey();
                            }
                        }, 500);
                    } else if (Device.getName().substring(0, 8).equals("Skylock ")){
                        Log.i("Services Discovered", "provisioned Lock found");
                        enableVerify();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                objRepo.objBluetoothClass.writeMSG();
                            }
                        }, 500);


                    }*/
                }
            });

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(characteristic.equals(CHAR_CHALL_DATA)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(CHAR_CHALL_DATA != null){
                            byte[] temp = CHAR_CHALL_DATA.getValue();
                            challengeData = bytesToHex(temp);
                            getKeys();
                            writeChallResult();
                        }
                    }
                });

            }else if(characteristic.equals(CHAR_HW_INFO)){
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        Integer latchPosition = CHAR_HW_INFO.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 4);
                        Log.i("latch position",""+latchPosition);
                        if(latchPosition ==1 ){
                            if(flagLockState ==0){
                                objRepo.bLock.setImageResource(R.drawable.unlock_btn);
                                objRepo.bLock.setTag("unlock");
                                flagLockState = 1;
                            }else{
                                objRepo.displayToast("Latch Error");
                            }
                        }else if(latchPosition == 0){
                            if(flagLockState == 1) {
                                objRepo.bLock.setImageResource(R.drawable.lock_btn);
                                objRepo.bLock.setTag("lock");
                                flagLockState = 0;
                            }else{
                                objRepo.displayToast("Latch Error");
                            }
                        }else if(latchPosition == 2){
                            unlock();
                        }else{
                            objRepo.displayToast("Latch Error");
                        }
                        if (pdLock.isShowing()) {
                            pdLock.dismiss();
                        }
                    }
                });
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
             final BluetoothGattCharacteristic char1 = characteristic;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(char1.equals(CHAR_PUB_KEY)){
                        Log.i("Char-write","Public Key - "+PUB_KEY);
                        writeChallKey();
                    }else if (char1.equals(CHAR_CHALL_KEY)){
                        Log.i("Char-write","Challenge Key - "+challengeKey);
                        writeMSG();
                    }else if(char1.equals(CHAR_SIGN_MSG)){
                        Log.i("Char-write","Signed Message - "+MESSAGE);
                        readChallData();
                    }else if(char1.equals(CHAR_HW_LED)){
                        Log.i("Char-write","LED State");
                    }else if(char1.equals(CHAR_HW_LOCK)){
                        Log.i("Char-write","Lock State");
                        readLatchPosition();
                    }else if(char1.equals(CHAR_CHALL_DATA)){
                        Log.i("Char-write","Challenge Data ");
                        if (pdBluetooth.isShowing()) pdBluetooth.dismiss();
                    }
                }
            });

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(characteristic.equals(CHAR_SECURITY_STATE)){
                        Integer temp = CHAR_SECURITY_STATE.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                        Log.i("Notification", "Value: " + temp);
                    }else{
                        objCrashTheft.putCharacterstic(characteristic);
                    }

                }
            });
                }

                @Override
                public void onReadRemoteRssi(BluetoothGatt gatt, final int rssi, int status) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            signal_update(rssi);
                            auto_lock_unlock(rssi);
                        }
                    });
                    
                }
            };

    private void readLatchPosition() {

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.readCharacteristic(CHAR_HW_INFO);
            }
        }, 300);

    }

    public void startLEDBlink() {
        ledBlinkTimer = new Timer();
        ledBlinkTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(LEDBlinkCounter == 1 ){
                    LED_ON();
                    Log.i("timer","LED - ON");
                    LEDBlinkCounter = 2;
                }else{
                    LED_OFF();
                    Log.i("timer", "LED - OFF");
                    LEDBlinkCounter = 1;
                }
            }
        }, 0, 500);

    }

    //RSSI method to determine suto-lock/unlock using rssi
    private void auto_lock_unlock(int rssi) {
        if(AUTO_LOCK == 1){
            countArray[LOCK_COUNT] = rssi;
            LOCK_COUNT++;
            if(LOCK_COUNT >= 2){
                LOCK_COUNT = 0;
                int meanRssi = (Math.abs(countArray[0])+Math.abs(countArray[1])+Math.abs(countArray[2]))/3;
                if(meanRssi <= 50){
                    auto_unlock();
                }else if(meanRssi > 50 ){
                    auto_lock();
                }

            }

        }
    }

    private void auto_lock() {
        // TODO Auto-generated method stub
        try{
            cLOCK= mBluetoothGatt.getService(UUID_HW_SER).getCharacteristic(UUID_LOCK_STATE);
            cLOCK.setValue(new byte[] {0x01});
            mBluetoothGatt.writeCharacteristic(cLOCK);

        } catch(Exception e){}

    }

    private void auto_unlock() {
        // TODO Auto-generated method stub
        try{
            cLOCK= mBluetoothGatt.getService(UUID_HW_SER).getCharacteristic(UUID_LOCK_STATE);
            cLOCK.setValue(new byte[] {0x00});
            mBluetoothGatt.writeCharacteristic(cLOCK);

        } catch(Exception e){}
    }


    //RSSI method to update the RSSI strength image view
    private void signal_update(int rssi) {

        countArray[LOCK_COUNT] = rssi;
        LOCK_COUNT++;
        if(LOCK_COUNT >= 2) {
            LOCK_COUNT = 0;
            int meanRssi = (Math.abs(countArray[0]) + Math.abs(countArray[1]) + Math.abs(countArray[2])) / 3;
            if(meanRssi <= 40){
                ivSignal.setImageDrawable(context.getResources().getDrawable(R.drawable.wifi_5));
            }else if(meanRssi > 40 && meanRssi <= 46){
                ivSignal.setImageDrawable(context.getResources().getDrawable(R.drawable.wifi_4));
            }else if(meanRssi > 46 && meanRssi <= 53){
                ivSignal.setImageDrawable(context.getResources().getDrawable(R.drawable.wifi_3));
            }else if(meanRssi > 53 && meanRssi <= 58){
                ivSignal.setImageDrawable(context.getResources().getDrawable(R.drawable.wifi_2));
            }else if(meanRssi > 58 && meanRssi <= 63){
                ivSignal.setImageDrawable(context.getResources().getDrawable(R.drawable.wifi_1));
            }else if(meanRssi > 63 ){
                ivSignal.setImageDrawable(context.getResources().getDrawable(R.drawable.wifi_0));
            }
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

        JSONClass objJSONClass = new JSONClass(context);

        //KEYS -- MAC ADDRESS
        String url2 = "https://skylock-beta.herokuapp.com/users/11111/keys/";
        JSONObject json = new JSONObject();
        try {

            json.put("mac_id", "" + objRepo.macRemoveColon(Device.getAddress()));

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
        try{
            JSONObject temp1 = json.getJSONObject(TAG_PAYLOAD);
            PUB_KEY = temp1.getString(TAG_PUBLIC_KEY);
            MESSAGE = temp1.getString(TAG_MESSAGE);
        }catch(Exception e){
            str_temp = e.toString();
        }


        //Challenge Key
        String url = "https://skylock-beta.herokuapp.com/users/11111/challenge_key/";
        objJSONClass.putURL(url, GET, null, null); //put the URL from which JSON object has to be obtained

        JSONObject temp = objJSONClass.executeJSON(); //executes JSON - returns JSON object from the URL
        try{
            JSONObject temp1 = temp.getJSONObject(TAG_PAYLOAD);
            challengeKey = temp1.getString(TAG_CHALLENGE_KEY);

        }catch(Exception e){
            str_temp = e.toString();
        }

        //Challenge data
        String url1 = "https://skylock-beta.herokuapp.com/users/11111/challenge_data/";
        JSONObject json1 = new JSONObject();
        try {

            json1.put("c_data", ""+challengeData);
            json1.put("c_key", "11111");

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

        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //alertDialogBuilder.setTitle("Test Challenge");
        //alertDialogBuilder.setMessage("PUB_KEY \n"+PUB_KEY+"\nSign_MSG \n"+MESSAGE+"\nChall_KEY \n"+challengeKey+"\nChall_DATA \n"+challengeData+"\nChall_RESULT \n"+challengeResult)
        //        .setCancelable(true);
        //AlertDialog alertDialog = alertDialogBuilder.create();
        //alertDialog.show();
    }

    public void enableVerify(){
        try{
            CHAR_SECURITY_STATE = mBluetoothGatt.getService(UUID_SECURITY_SER).getCharacteristic(UUID_SECURITY_STATE);
            mBluetoothGatt.setCharacteristicNotification(CHAR_SECURITY_STATE, true);
            BluetoothGattDescriptor descriptor = CHAR_SECURITY_STATE.getDescriptor(UUID_ACC_DESC);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

            mBluetoothGatt.writeDescriptor(descriptor);

        }catch(Exception e){
            Log.e("Bluetooth","Enable Sec-state sacrifice");
        }
    }

    public void writePUBKey(){
        try{
            //String s= "7ca3d7b9bf13765862a64c87acb848a8dc1637fd08a3215d1404218f9aae35436b91348f53db429fd5e515d503e9438b50fff282ed1fcc4664ea42766f04bc40";
            String s = PUB_KEY;
            int len = s.length();
            byte[] byteTemp = new byte[len/2];
            for(int i=0; i<len; i+=2){
                byteTemp[i/2] = (byte) ((Character.digit(s.charAt(i),16)<<4)
                        + Character.digit(s.charAt(i+1), 16));
            }

            CHAR_PUB_KEY.setValue(byteTemp);
            mBluetoothGatt.writeCharacteristic(CHAR_PUB_KEY);

        }catch(Exception e){
        }
    }

    public void writeMSG(){
        try{
            //String s = "0000405c78cf9bbffac5201ae9d1351d8c5e193fed61729433ae4379e01dbf47ffffffff002c2a26ff5b9391ba398117d827bb5be77fe5be8944e583a370f56bb3dcb4c162c5cfe1afcccc8149dc9ac94389cee012ab64c75118d4f1f31d38336168e04cd8";
            String s = MESSAGE;
            int len = s.length();
            byte[] byteTemp = new byte[len/2];
            for(int i=0; i<len; i+=2){
                byteTemp[i/2] = (byte) ((Character.digit(s.charAt(i),16)<<4)
                        + Character.digit(s.charAt(i+1), 16));
            }
            CHAR_SIGN_MSG.setValue(byteTemp);
            mBluetoothGatt.writeCharacteristic(CHAR_SIGN_MSG);

        }catch(Exception e){

        }
    }

    public void writeChallKey(){
        try{
            String s = challengeKey;
            int len = s.length();
            byte[] byteTemp = new byte[len/2];
            for(int i=0; i<len; i+=2){
                byteTemp[i/2] = (byte) ((Character.digit(s.charAt(i),16)<<4)
                        + Character.digit(s.charAt(i+1), 16));
            }
            CHAR_CHALL_KEY.setValue(byteTemp);
            mBluetoothGatt.writeCharacteristic(CHAR_CHALL_KEY);

        }catch(Exception e){
        }
    }

    public void readChallData(){
        try{
            mBluetoothGatt.readCharacteristic(CHAR_CHALL_DATA);


        }catch(Exception e){
        }
    }

    public void writeChallResult(){
        try{

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
            CHAR_CHALL_DATA.setValue(byteTemp);
            mBluetoothGatt.writeCharacteristic(CHAR_CHALL_DATA);

        }catch(Exception e){
            Toast.makeText(getApplicationContext(),""+e.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }
}


