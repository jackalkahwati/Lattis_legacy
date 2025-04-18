package cc.skylock.myapplication.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import cc.skylock.myapplication.FWUpgradeProgress;
import cc.skylock.myapplication.Uitls.UtilHelper;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT16;


/**
 * Created by admin on 05/05/16.
 */
public class SkylockBluetoothManage extends cc.skylock.myapplication.bluetooth.BluetoothScaner {
    private static SkylockBluetoothManage mSkylockBluetoothManage = null;
    private Handler mHandler = null;
    public static BluetoothGattCharacteristic CHAR_PUB_KEY, CHAR_SIGN_MSG, CHAR_CHALL_KEY, CHAR_CHALL_DATA, CHAR_SECURITY_STATE,
            CHAR_HW_LED, CHAR_HW_LOCK, CHAR_HW_INFO, CHAR_HW_TX, CHAR_CONFIG_FW_VER,
            CHAR_CONFIG_RESET, CHAR_CONFIG_LOCK_ADJUST, CHAR_CONFIG_OPEN_ADJUST, CHAR_CONFIG_CAP_PIN,
            CHAR_ACC, CHAR_MAG, WRITE_DATA, CODE_VERSION;
    BluetoothGattDescriptor mbluetoothGattDescriptor;
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
    private UUID UUID_CONFIG_FW_VER = UUID.fromString("d3995d01-fa57-11e4-ae59-0002a5d5c51b");

    private UUID UUID_TEST_SER = UUID.fromString("d3995e40-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_TEST_MAG = UUID.fromString("d3995e44-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_TEST_ACC = UUID.fromString("d3995e46-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_ACC_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private UUID UUID_BOOT_DESC = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb");
    private final UUID UUID_LED_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    private UUID UUID_WRITE_DATA = UUID.fromString("d3995d02-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_CODE_VERSION  = UUID.fromString("d3995d01-fa57-11e4-ae59-0002a5d5c51b");
    private List<String>  fileBytes ;
    private int updatePosition;
    private int TOTALBYTESSEND=128;
    private Handler handler = new Handler();


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final static String TAG = SkylockBluetoothManage.class.getSimpleName();
    private BluetoothGatt mBluetoothGatt;
    int ledonoffcounter = 0;
    private Context mContext;
    public cc.skylock.myapplication.bluetooth.SkylockBluetoothScanCallback mBleScanCallBack;
    public static cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus mUiCall;
    boolean ledOnFlag = false;
    private boolean shippingModeEnabled = false;
    int counter = 0;
    Timer selfTimer;
    public static String ACTION_SERIAL_NUMBER_FAILED = "lock.write.serial.number.failed";
    private BluetoothDevice connectingBluetoothDevice;
    Handler byteWriteHandler = new Handler();
    private boolean firmwareUpgradeON=false;
    private SkylockBluetoothManage(Context mContext) {
        super(mContext);
        this.mContext = mContext;
        if ((Service) mContext instanceof cc.skylock.myapplication.bluetooth.SkylockBluetoothScanCallback) {
            mBleScanCallBack = (cc.skylock.myapplication.bluetooth.SkylockBluetoothScanCallback) (Service) mContext;
            mUiCall = (cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus) (Service) mContext;
        } else {
            Log.w(TAG, String.format(cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus.class.getName()));
        }
    }

    public static SkylockBluetoothManage getInstance(Context context) {
        if (mSkylockBluetoothManage == null) {
            mSkylockBluetoothManage = new SkylockBluetoothManage(context);
        }

        return mSkylockBluetoothManage;
    }

    public static SkylockBluetoothManage getInstance() {
        if (mSkylockBluetoothManage == null) {
            return null;
        } else {
            return mSkylockBluetoothManage;
        }
    }

    public static void clearInstance() {
        mSkylockBluetoothManage = null;
    }

    @Override
    public void scanedList(BluetoothDevice bluetoothDevice) {
//        Log.i(TAG, "" + bluetoothDevices.size());
//        if (bluetoothDevices != null && bluetoothDevices.size() > 0) {
//            if (bluetoothDevices.iterator().hasNext()) {
//                BluetoothDevice device = bluetoothDevices.iterator().next();
                mBleScanCallBack.onScaningDevice(bluetoothDevice);
//            }
//        } else {
//            mUiCall.onScanFailed();
//            mBleScanCallBack.onError();
//        }
    }


    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param bluetoothDevice The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final BluetoothDevice bluetoothDevice) {
        // Previously connected device.  Try to reconnect.
        mHandler = new Handler(mContext.getMainLooper());
        if (bluetoothDevice != null
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            mUiCall.onConnected(mBluetoothGatt);
            if (mBluetoothGatt.connect()) {
                ledonoffcounter = 0;
                return true;
            } else {
                return false;
            }
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
                if (bluetoothDevice != null) {
                    mBluetoothGatt = bluetoothDevice.connectGatt(context, false, mGattCallback);
                    mBluetoothGatt.connect();
                }
//            }
//        });

        return true;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            Log.d(TAG, "onConnectionStateChange (device : " + gatt.getDevice()
                    + ", status : " + status + " , newState :  " + newState
                    + ")");

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Connected to GATT server.");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mBluetoothGatt.discoverServices();
                        }
                    });
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    mUiCall.onConnectionFailed(shippingModeEnabled);
                    Log.i("status" + status, "Disconnected from GATT server.");

                }
            } else if (status == 133) {
                mUiCall.onConnectionFailed(shippingModeEnabled);
                Log.i("status" + status, "Disconnected from GATT server.");
            } else {
                mUiCall.onConnectionFailed(false);
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Disconnected from GATT server.");
                    if (shippingModeEnabled) {
                        mUiCall.onConnectionFailed(true);
                        shippingModeEnabled = false;
                        close();
                    } else {
                        mUiCall.onConnectionFailed(shippingModeEnabled);
                    }

                }
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                setupBluetoothDeviceCharacteristis(gatt);
                mUiCall.onConnected(gatt);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (descriptor.getCharacteristic().equals(CHAR_HW_LED)) {
                enableHwInfo(true);
            } else if (descriptor.getCharacteristic().equals(CHAR_SECURITY_STATE)) {
                Log.i("onDescriptorWrite", descriptor.toString());
                getmodeInfo();
            } else if (descriptor.getCharacteristic().equals(CHAR_HW_INFO)) {
                Log.i("onDescriptorWrite Touch", descriptor.toString());
                mUiCall.onDescriptorWrite(gatt);
            } else if (descriptor.getCharacteristic().equals(CHAR_CONFIG_OPEN_ADJUST)) {
                mUiCall.onDescriptorWrite(gatt);
            }
        }


        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            counter++;
            if (counter == 4) {
                if (mBluetoothGatt != null) {
                    CHAR_HW_INFO.setValue(new byte[]{(byte) 0x01});
                    mBluetoothGatt.writeCharacteristic(CHAR_HW_INFO);
                }
            }
            if (counter == 7) {
                if (mBluetoothGatt != null)
                    mBluetoothGatt.readCharacteristic(CHAR_HW_INFO);
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicRead", "Value: " + characteristic.getUuid());
            System.out.println("ValueFormat : "+ getValueFormat(characteristic));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic.equals(CHAR_HW_INFO)) {
                    mUiCall.onGetHardWareInfo(gatt, characteristic);
                } else if (characteristic.equals(CHAR_SECURITY_STATE)) {
                    mUiCall.onDeviceStatus(gatt, characteristic);
                } else if (characteristic.equals(CHAR_CONFIG_OPEN_ADJUST)) {
                    mUiCall.onGetSerialInfo(gatt, characteristic);
                } else if (characteristic.equals(CHAR_HW_LED)) {
                    mUiCall.onLedBlink(gatt, characteristic);
                } else if(characteristic.equals(CODE_VERSION)){
                    int imageVersion = characteristic.getIntValue(FORMAT_UINT16, 14);
                    int imageRevision = characteristic.getIntValue(FORMAT_UINT16, 16);

                    firmwareUpgradeON =false;
                    if(imageRevision!=0 && imageVersion!=0){
                        CHAR_CONFIG_RESET = mBluetoothGatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CONFIG_RESET);
                        CHAR_CONFIG_RESET.setValue(new byte[]{(byte) 0x00});
                        mBluetoothGatt.writeCharacteristic(CHAR_CONFIG_RESET);

                        FWUpgradeProgress fwUpgradeProgress = new FWUpgradeProgress();
                        fwUpgradeProgress.fwProgress = FWUpgradeProgress.FWProgress.FW_SUCCESS;
                        mUiCall.onFWUpgradeProgress(fwUpgradeProgress);

                    }else{
                        FWUpgradeProgress fwUpgradeProgress = new FWUpgradeProgress();
                        fwUpgradeProgress.fwProgress = FWUpgradeProgress.FWProgress.FW_FAIL;
                        mUiCall.onFWUpgradeProgress(fwUpgradeProgress);
                    }


                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if (characteristic.equals(CHAR_HW_LED)) {
                if (mBluetoothGatt != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mBluetoothGatt.readCharacteristic(characteristic);
                        }
                    });

                }
            } else if (characteristic.equals(CHAR_HW_INFO)) {
                if (mBluetoothGatt != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mBluetoothGatt.readCharacteristic(characteristic);
                        }
                    });
                }
            } else if (characteristic.equals(CHAR_HW_LOCK)) {
                if (mBluetoothGatt != null) {
                    {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mBluetoothGatt.readCharacteristic(CHAR_HW_INFO);
                            }
                        });
                    }
                }

            } else if (characteristic.equals(CHAR_CONFIG_OPEN_ADJUST)) {
                mUiCall.onDeviceStatus(gatt, characteristic);
            }else if(characteristic.equals(CODE_VERSION)){
                forceRead(gatt,20);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic) {
            Log.i("onCharacteristicChanged", ""+characteristic.getUuid());
            System.out.format("%d ", characteristic.getValue()[0]);


            if (characteristic.equals(CHAR_HW_INFO)) {
                Log.i("onCharacteristicChanged", "called");
                byte[] solar = characteristic.getValue();
                final String solar_temp = UtilHelper.bytesToHex(solar);
                final String substring = solar_temp.substring(Math.max(solar_temp.length() - 16, 0));
                final String upToNCharacters = substring.substring(0, Math.min(substring.length(), 2));
                Log.i("onCharacteristicChanged", "" + upToNCharacters);
                mUiCall.onGetHardWareInfo(gatt, characteristic);
            }else if(characteristic.equals(CHAR_SECURITY_STATE)){
                Log.i("onCharacteristicChanged", "byte write success for " + updatePosition + " status: "+bytesToHex(new byte[]{characteristic.getValue()[0]}));
                if(firmwareUpgradeON && characteristic.getValue()[0] == (byte)0X00) {
                    FWUpgradeProgress fwUpgradeProgress = new FWUpgradeProgress();
                    fwUpgradeProgress.currentProgress=updatePosition;
                    fwUpgradeProgress.maxProgress=fileBytes.size();

                    fwUpgradeProgress.fwProgress = FWUpgradeProgress.FWProgress.FW_IN_PROGRESS;
                    mUiCall.onFWUpgradeProgress(fwUpgradeProgress);
                    byteWriteHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendFW();
                        }
                    }, 500);
                }else if(firmwareUpgradeON && characteristic.getValue()[0] != (byte)0XFF){
                    if(updatePosition<fileBytes.size()){
                        firmwareUpgradeON=false;
                        FWUpgradeProgress fwUpgradeProgress = new FWUpgradeProgress();
                        fwUpgradeProgress.fwProgress = FWUpgradeProgress.FWProgress.FW_FAIL;
                        mUiCall.onFWUpgradeProgress(fwUpgradeProgress);
                    }
                }
            }
        }
    };

    public static @Nullable
    byte forCommandV1(@NonNull BluetoothGattCharacteristic bluetoothGattCharacteristic){
        return  bluetoothGattCharacteristic.getValue()[0];
    }

    public static boolean isVersion1(@NonNull BluetoothGattCharacteristic bluetoothGattCharacteristic){
        return CHAR_SECURITY_STATE.getUuid().equals(bluetoothGattCharacteristic.getUuid()) && bluetoothGattCharacteristic.getValue().length == 1;
    }

    private void sendFW(){

                // this code will be executed after 2 seconds

                if(updatePosition % 15 == 0){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                    }
                }


                updatePosition++;
                if(updatePosition < fileBytes.size()){
                    boolean writeStatus = WRITE_DATA.setValue(encodeMessage(fileBytes.get(updatePosition)));
                    mBluetoothGatt.writeCharacteristic(WRITE_DATA);
                }else{
                    if (mBluetoothGatt != null) {
                        try {

                            CODE_VERSION.setValue(new byte[]{(byte) 0x00});
                            mBluetoothGatt.writeCharacteristic(CODE_VERSION);

//                                    CHAR_CONFIG_RESET = mBluetoothGatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CONFIG_RESET);
//                                    CHAR_CONFIG_RESET.setValue(new byte[]{(byte) 0x00});
//                                    mBluetoothGatt.writeCharacteristic(CHAR_CONFIG_RESET);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
    }





    public boolean forceRead(@NonNull final BluetoothGatt bluetoothGatt, int numberAttempts){
        boolean valid;
        do{
            valid = bluetoothGatt.readCharacteristic(CODE_VERSION);
            numberAttempts--;
        }while (!valid && numberAttempts > 0);


        if(!valid){
            bluetoothGatt.readCharacteristic(CODE_VERSION);
        }

        return valid;
    }

    public void getVersionInfo() {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mBluetoothGatt != null)
                        mBluetoothGatt.readCharacteristic(CHAR_CONFIG_FW_VER);
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void writeSerialInfo(final String serialNumber) {
        try {
            Log.i("serial_number", "" + serialNumber);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBluetoothGatt != null) {
                        CHAR_CONFIG_OPEN_ADJUST.setValue(serialNumber.getBytes());
                        mBluetoothGatt.writeCharacteristic(CHAR_CONFIG_OPEN_ADJUST);

                    }
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setupBluetoothDeviceCharacteristis(BluetoothGatt gatt) {
        {
            try {
                CHAR_PUB_KEY = gatt.getService(UUID_SECURITY_SER).getCharacteristic(UUID_SECURITY_PUB_KEY);
                CHAR_SIGN_MSG = gatt.getService(UUID_SECURITY_SER).getCharacteristic(UUID_SECURITY_SIGN_MSG);
                CHAR_CHALL_KEY = gatt.getService(UUID_SECURITY_SER).getCharacteristic(UUID_SECURITY_CHALL_KEY);
                CHAR_CHALL_DATA = gatt.getService(UUID_SECURITY_SER).getCharacteristic(UUID_SECURITY_CHALL_DATA);
                CHAR_SECURITY_STATE = gatt.getService(UUID_SECURITY_SER).getCharacteristic(UUID_SECURITY_STATE);

                CHAR_HW_LED = gatt.getService(UUID_HW_SER).getCharacteristic(UUID_HW_LED);
                CHAR_HW_LOCK = gatt.getService(UUID_HW_SER).getCharacteristic(UUID_HW_LOCK);
                CHAR_HW_INFO = gatt.getService(UUID_HW_SER).getCharacteristic(UUID_HW_INFO);
                CHAR_HW_TX = gatt.getService(UUID_HW_SER).getCharacteristic(UUID_HW_TX);

                CHAR_CONFIG_RESET = gatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CONFIG_RESET);
                CHAR_CONFIG_LOCK_ADJUST = gatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CONFIG_LOCK_ADJUST);
                CHAR_CONFIG_OPEN_ADJUST = gatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CONFIG_OPEN_ADJUST);
                CHAR_CONFIG_CAP_PIN = gatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CONFIG_CAP_PIN);
                CHAR_CONFIG_FW_VER = mBluetoothGatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CONFIG_FW_VER);

                CHAR_MAG = gatt.getService(UUID_TEST_SER).getCharacteristic(UUID_TEST_MAG);
                CHAR_ACC = gatt.getService(UUID_TEST_SER).getCharacteristic(UUID_TEST_ACC);

                WRITE_DATA=gatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_WRITE_DATA);
                CODE_VERSION=gatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CODE_VERSION);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    public void enableHwInfo(final boolean enable) {
        try {
            if (mBluetoothGatt != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        enableDescriptorNotification(CHAR_HW_INFO, enable);
                    }
                });


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enableDescriptorNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        try {
            boolean success = mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
            if (!success) {
                Log.e("------", "Seting proper notification status for characteristic failed!");
            }
            final BluetoothGattDescriptor bluetoothGattDescriptor = characteristic.getDescriptor(UUID_ACC_DESC);
            if (bluetoothGattDescriptor != null) {
                byte[] val = enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                bluetoothGattDescriptor.setValue(val);
                mBluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
            } else {
                mUiCall.onDescriptorWrite(mBluetoothGatt);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enableTouchHwInfo(final boolean enable) {
        try {
            if (mBluetoothGatt != null) {
                try {
                    if (mBluetoothGatt != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                enableDescriptorNotification(CHAR_HW_INFO, enable);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enableSecurityNotificationCharaterisitics(final boolean enable) {
        try {
            if (mBluetoothGatt != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        enableDescriptorNotification(CHAR_SECURITY_STATE, enable);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enableSerialNumberNotification(final boolean enable) {
        try {
            if (mBluetoothGatt != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        enableDescriptorNotification(CHAR_CONFIG_OPEN_ADJUST, enable);
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enableLedInfo(final boolean enable) {
        try {
            if (mBluetoothGatt != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        enableDescriptorNotification(CHAR_HW_LED, enable);
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getSerialInfo() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mBluetoothGatt != null)
                        mBluetoothGatt.readCharacteristic(CHAR_CONFIG_OPEN_ADJUST);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    public void putShippingMode() {
        if (mBluetoothGatt != null) {
            try {
                CHAR_CONFIG_RESET = mBluetoothGatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CONFIG_RESET);
                CHAR_CONFIG_RESET.setValue(new byte[]{(byte) 0xBC});
                mBluetoothGatt.writeCharacteristic(CHAR_CONFIG_RESET);
                shippingModeEnabled = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getMacAddress() {
        return mBluetoothGatt.getDevice().getAddress();
    }

    public void bluetoothWriteData(final String s, final BluetoothGattCharacteristic tempBluetoothGattCharacteristic) {

        int len = s.length();
        byte[] byteTemp = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            byteTemp[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        if (byteTemp != null && tempBluetoothGattCharacteristic != null) {
            tempBluetoothGattCharacteristic.setValue(byteTemp);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mBluetoothGatt.writeCharacteristic(tempBluetoothGattCharacteristic);
                }
            }).start();
        }

    }

    public void getRSSIvalue() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.readRemoteRssi();
        }
    }

    public void getRSSIvalueForSelf() {
        try {
            counter = 0;
            selfTimer = new Timer();
            selfTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    SkylockBluetoothManage.getInstance().getRSSIvalue();

                }
            }, 0, 1000);
        } catch (Exception e) {

        }

    }

    public void stopSelfTimer() {
        counter = 0;
        try {
            if(selfTimer!=null)
                selfTimer.cancel();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void registerBluetoothDeviceStatusListener(cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus listener) {
        mUiCall = listener;
        Log.i("listener", "Registered");
    }

    public static void unregisterBluetoothDeviceStatusListener() {
        mUiCall = null;
    }

    private String getChallengeResult(byte[] temp) {

        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[temp.length * 2];
        for (int j = 0; j < temp.length; j++) {
            int v = temp[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        String challengeData = new String(hexChars);
        return challengeData;
    }

    public void writePublickey(String public_key) {
        if (public_key != null)
            bluetoothWriteData(public_key, CHAR_PUB_KEY);
    }

    private void writeChallengekey(String challenge_key) {
        bluetoothWriteData(challenge_key, CHAR_CHALL_KEY);
    }

    public void writeSignedMessage(String signed_message) {
        if (signed_message != null)
            bluetoothWriteData(signed_message, CHAR_SIGN_MSG);
        Log.i("inside signedmessage", signed_message);

    }

    private void writeChallengeResult(String challenge_data) {
        if (challenge_data != null)
            bluetoothWriteData(challenge_data, CHAR_CHALL_DATA);
    }


    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void lock() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothGatt != null) {
                    CHAR_HW_LOCK.setValue(new byte[]{(byte) 0x01});
                    mBluetoothGatt.writeCharacteristic(CHAR_HW_LOCK);
                }
            }

        });

    }

    public void updateFirmware(final byte[] fileList) {
        firmwareUpgradeON = true;

        this.fileBytes = convertByteArrayToList(fileList,TOTALBYTESSEND);



        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothGatt != null) {
                    updatePosition=0;
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                    }

                    boolean writeStatus = WRITE_DATA.setValue(encodeMessage(fileBytes.get(updatePosition)));
                    mBluetoothGatt.writeCharacteristic(WRITE_DATA);
                }
            }

        });

    }

    public List<String>  convertStringArray(String text) {
        List<String> list= new ArrayList<String>();
        int index = 0;
        while (index<text.length()) {
            list.add(text.substring(index, Math.min(index+4,text.length())));
            index=index+4;
        }
        return list;
    }

    public static List<String> convertByteArrayToList(byte[] source, int chunksize) {

        List<byte[]> result = new ArrayList<byte[]>();
        int start = 0;
        byte [] offsetArray = new byte[4];
        offsetArray[0]=(byte) 0X00;
        offsetArray[1]=(byte) 0X00;
        offsetArray[2]=(byte) 0X00;
        offsetArray[3]=(byte) 0X00;

        int remainingBytes=0;

        while (start < source.length) {
            int end = Math.min(source.length, start + chunksize);
            byte [] newArray = new byte[chunksize+4];
            newArray[0]=offsetArray[0];
            newArray[1]=offsetArray[1];
            newArray[2]=offsetArray[2];
            newArray[3]=offsetArray[3];

            for(int i= 4; i<chunksize+4;i++){
                newArray[i]=(byte)0XFF;
            }

            byte [] copyOfArray=Arrays.copyOfRange(source, start, end);
            remainingBytes=copyOfArray.length;
            for(int i= 0; i<copyOfArray.length;i++){
                newArray[i+4]=copyOfArray[i];
            }
            result.add(newArray);
            start += chunksize;

            if(offsetArray[0]==0X00){
                offsetArray[0]= (byte)0X80;
            }else{
                offsetArray[0]= (byte)0X00;
            }

            if(offsetArray[0]==(byte) 0X00){
                offsetArray[1]= (byte) (offsetArray[1]+1);
            }


        }



//        byte [] lastByte = result.get(result.size()-1);
//        byte [] newLastByte = new byte[chunksize+4];
//
//
//
//        for(int i= 0; i<chunksize+4;i++){
//            if(i<4){
//                newLastByte[i]=lastByte[i];
//            }else if(i<remainingBytes){
//                newLastByte[i]=lastByte[i];
//            }else{
//                newLastByte[i]=(byte) 0XFF;
//            }
//        }
//        result.remove(result.size()-1);
//        result.add(newLastByte);


        List<String> finalByteStringArray = new ArrayList<>();

        for(int i =0 ; i< result.size() ;i++){
            String byteString = bytesToHex(result.get(i));
            finalByteStringArray.add(byteString);
        }

        return finalByteStringArray;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }



    public static byte[] encodeMessage(String message){
        int len = message.length();
        byte[] byteMessage = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            byteMessage[i / 2] = (byte) ((Character.digit(message.charAt(i), 16) << 4)
                    + Character.digit(message.charAt(i + 1), 16));
        }
        return byteMessage;
    }

    public void unLock() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothGatt != null) {
                    CHAR_HW_LOCK.setValue(new byte[]{(byte) 0x00});
                    mBluetoothGatt.writeCharacteristic(CHAR_HW_LOCK);
                }
            }
        });

    }

    public void ledON(final byte arg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothGatt != null) {
                    CHAR_HW_LED.setValue(new byte[]{(byte) arg});
                    mBluetoothGatt.writeCharacteristic(CHAR_HW_LED);
                }
            }
        });

    }

    public void ledOFF() {
        ledOnFlag = false;
        CHAR_HW_LED.setValue(new byte[]{(byte) 0x00});
        mBluetoothGatt.writeCharacteristic(CHAR_HW_LED);
    }

    public void getHWInfo() {
        try {
            if (mBluetoothGatt != null)
                mBluetoothGatt.readCharacteristic(CHAR_HW_INFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getmodeInfo() {
        try {
            if (mBluetoothGatt != null)
                mBluetoothGatt.readCharacteristic(CHAR_SECURITY_STATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void writeHardwareInfo() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mBluetoothGatt != null) {
                        CHAR_HW_INFO.setValue(new byte[]{0x01});
                        mBluetoothGatt.writeCharacteristic(CHAR_HW_INFO);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
    public int getValueFormat(BluetoothGattCharacteristic ch) {
        int properties = ch.getProperties();

        if((BluetoothGattCharacteristic.FORMAT_FLOAT & properties) != 0) return BluetoothGattCharacteristic.FORMAT_FLOAT;
        if((BluetoothGattCharacteristic.FORMAT_SFLOAT & properties) != 0) return BluetoothGattCharacteristic.FORMAT_SFLOAT;
        if((BluetoothGattCharacteristic.FORMAT_SINT16 & properties) != 0) return BluetoothGattCharacteristic.FORMAT_SINT16;
        if((BluetoothGattCharacteristic.FORMAT_SINT32 & properties) != 0) return BluetoothGattCharacteristic.FORMAT_SINT32;
        if((BluetoothGattCharacteristic.FORMAT_SINT8 & properties) != 0) return BluetoothGattCharacteristic.FORMAT_SINT8;
        if((BluetoothGattCharacteristic.FORMAT_UINT16 & properties) != 0) return BluetoothGattCharacteristic.FORMAT_UINT16;
        if((BluetoothGattCharacteristic.FORMAT_UINT32 & properties) != 0) return BluetoothGattCharacteristic.FORMAT_UINT32;
        if((BluetoothGattCharacteristic.FORMAT_UINT8 & properties) != 0) return BluetoothGattCharacteristic.FORMAT_UINT8;

        return 0;
    }
}
