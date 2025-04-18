package cc.skylock.skylock.bluetooth;

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
import android.util.Base64;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.UUID;

import cc.skylock.skylock.Bean.FirmwareUpdates;
import cc.skylock.skylock.utils.EncryptDecrypt;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;

import static android.R.attr.format;


/**
 * Created by admin on 05/05/16.
 */
public class SkylockBluetoothManage extends BluetoothScaner {
    private static SkylockBluetoothManage mSkylockBluetoothManage = null;
    private static boolean mAutoLockEnable = false;
    private Handler mHandler = null;
    private boolean isDoFWUpdate = false;
    private boolean autoConnect = false;
    private boolean lockStatus = false;
    private boolean isServiceDiscovered = false;
    public static BluetoothGattCharacteristic CHAR_PUB_KEY, CHAR_SIGN_MSG, CHAR_CHALL_KEY, CHAR_CHALL_DATA, CHAR_SECURITY_STATE,
            CHAR_HW_LED, CHAR_HW_LOCK, CHAR_HW_INFO, CHAR_HW_TX, CHAR_CONFIG_FW_VER,
            CHAR_CONFIG_RESET, CHAR_CONFIG_LOCK_ADJUST, CHAR_CONFIG_OPEN_ADJUST, CHAR_CONFIG_CAP_PIN,
            CHAR_TEST_SER, CHAR_ACC, CHAR_MAG, CHAR_BOOT_VER, CHAR_BOOT_DATA_WRITE, CHAR_BOOT_STATUS_NOTIFY, CHAR_BOOT_DOWNLOAD_DONE;
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
    private UUID UUID_CONFIG_BOOT_DATA_WRITE = UUID.fromString("d3995d02-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_CONFIG_BOOT_STATUS_NOTIFY = UUID.fromString("d3995d03-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_CONFIG_BOOT_DOWNLOAD_DONE = UUID.fromString("d3995d04-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_CONFIG_BOOT_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private UUID UUID_TEST_SER = UUID.fromString("d3995e40-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_TEST_MAG = UUID.fromString("d3995e44-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_TEST_ACC = UUID.fromString("d3995e46-fa57-11e4-ae59-0002a5d5c51b");
    private UUID UUID_ACC_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private UUID UUID_HW_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    private final static String TAG = SkylockBluetoothManage.class.getSimpleName();
    private BluetoothGatt mBluetoothGatt;
    int ledonoffcounter = 0;
    private Context mContext;
    public SkylockBluetoothScanCallback mBleScanCallBack;
    public BluetoothDeviceStatus mUiCall;
    private boolean ledOnFlag = false;
    public static SkylockCrashTheftAlert crashTheftAlert;
    private boolean shippingModeEnabled = false;
    private boolean initialConnection = true;
    private PrefUtil mPrefUtil;
    private SkylockBLEFirmwareUpdateStatus mSkylockBLEFirmwareUpdateStatus;
    private FirmwareUpdates firmwareUpdates;
    private int firmwareUpdatecount = 0;
    private String keyGenerationID = null;
    private boolean isLockMalfunction = false;

    private SkylockBluetoothManage(Context mContext) {
        super(mContext);
        this.mContext = mContext;
        if ((Service) mContext instanceof SkylockBluetoothScanCallback) {
            mBleScanCallBack = (SkylockBluetoothScanCallback) (Service) mContext;
            mUiCall = (BluetoothDeviceStatus) (Service) mContext;
            mSkylockBLEFirmwareUpdateStatus = (SkylockBLEFirmwareUpdateStatus) (Service) mContext;
            mHandler = new Handler(mContext.getMainLooper());
            mPrefUtil = new PrefUtil(mContext);
        } else {
            Log.w(TAG, String.format(BluetoothDeviceStatus.class.getName()));
        }
    }

    public static SkylockBluetoothManage getInstance(Context context) {
        if (mSkylockBluetoothManage == null) {
            mSkylockBluetoothManage = new SkylockBluetoothManage(context);
            crashTheftAlert = new SkylockCrashTheftAlert(context);
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
    public void scanedList(HashSet<BluetoothDevice> bluetoothDevices) {
        Log.i(TAG, "" + bluetoothDevices.size());
        if (bluetoothDevices != null && bluetoothDevices.size() > 0) {
            if (bluetoothDevices.iterator().hasNext()) {
                BluetoothDevice device = bluetoothDevices.iterator().next();
                mBleScanCallBack.onScaningDevice(bluetoothDevices);
            }
        } else {
            mUiCall.onScanFailed();
            mBleScanCallBack.onError();
        }
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
    public boolean connect(final BluetoothDevice bluetoothDevice, final boolean blinkLed, final String generationID, final boolean isRefreshBle) {
        // Previously connected device.  Try to reconnect.
        if (bluetoothDevice == null) {
            return false;
        }
        mUiCall.onConnect();
        if (bluetoothDevice != null
                && mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                mUiCall.onBoardCompleted(mBluetoothGatt, "");
                ledonoffcounter = 0;
                return true;
            } else {
                return false;
            }
        }
        this.keyGenerationID = generationID;
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                autoConnect = blinkLed;
                mBluetoothGatt = bluetoothDevice.connectGatt(context, false, mGattCallback);

            }
        });
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isServiceDiscovered && mBluetoothGatt != null) {
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                    mUiCall.onConnectionTimeOut();
                }

            }
        }, 35000);

        return true;
    }

    public void clearCacheDataFromDevice() {
        refreshDeviceCache(mBluetoothGatt);
    }

    private boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        } catch (Exception localException) {
            Log.e(TAG, "An exception occured while refreshing device");
        }
        return false;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                    mUiCall.onConnect();
                }
            } else if (status == 133 && !shippingModeEnabled) {
                gatt.connect();
            } else if (status == 8 && !shippingModeEnabled) {
                mUiCall.onDeviceDisconnected(shippingModeEnabled);
                gatt.connect();

            } else {
                if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                    lock(true);
                }
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (status == BluetoothProfile.GATT_SERVER) {
                        mAutoLockEnable = true;
                    }
                    if (shippingModeEnabled) {
                        mUiCall.onDeviceDisconnected(true);
                        shippingModeEnabled = false;
                        close();
                    } else {
                        mUiCall.onDeviceDisconnected(false);
                    }

                }
            }


        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    setupBluetoothDeviceCharacteristis(gatt);
                    mUiCall.onDeviceConnected(gatt.getDevice());
                    isServiceDiscovered = true;
                    if (autoConnect)
                        enableSecurityNotificationCharaterisitics();
                    else
                        ledON();
                }
            } catch (Exception e) {

            }
        }

        @Override
        public void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {

            if (descriptor.getCharacteristic().equals(CHAR_SECURITY_STATE)) {
                if (!isDoFWUpdate) {
                    getmodeInfo();
                } else {
                    mSkylockBLEFirmwareUpdateStatus.doUpdateFirmware();
                }
            } else if (descriptor.getCharacteristic().equals(CHAR_HW_INFO)) {
                if (gatt != null) {
                    gatt.readCharacteristic(CHAR_HW_INFO);
                }
            } else if (descriptor.getCharacteristic().equals(CHAR_CONFIG_OPEN_ADJUST)) {
                if (gatt != null) {
                    gatt.readCharacteristic(CHAR_CONFIG_OPEN_ADJUST);
                }

            } else if (descriptor.getCharacteristic().equals(CHAR_BOOT_STATUS_NOTIFY)) {
                if (gatt != null) {
                    mUiCall.onDescriptorWrite(gatt, descriptor, status);
                }
            } else if (descriptor.getCharacteristic().equals(CHAR_HW_LED)) {
                if (gatt != null) {
                    ledON();
                }
            } else if (descriptor.getCharacteristic().equals(CHAR_HW_TX)) {
                if (gatt != null) {
                    increaseRssIRange();
                }
            }


        }


        @Override
        public void onReadRemoteRssi(final BluetoothGatt gatt, final int rssi, final int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mUiCall.onGetRSSi(gatt, rssi);
            }

        }

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {


            if (status == BluetoothGatt.GATT_SUCCESS) {
                try {
                    if (characteristic.equals(CHAR_SECURITY_STATE)) {
                        mUiCall.onDeviceStatus(gatt, characteristic);
                    } else if (characteristic.equals(CHAR_CHALL_DATA)) {
                        byte[] temp = CHAR_CHALL_DATA.getValue();
                        String challengeResult = SkylockChallengeResultGenerator.getChallengeResult(keyGenerationID, getChallengeResult(temp));
                        writeChallengeResult(challengeResult);
                    } else if (characteristic.equals(CHAR_HW_INFO)) {
                        mUiCall.onGetHardwareInfo(gatt, characteristic);
                        final int position = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 4);
                        if (mUiCall != null) {
                            if (!lockStatus && position == 1) {
                                lockStatus = true;
                                mUiCall.onLocked();
                            } else if (lockStatus && position == 0) {
                                lockStatus = false;
                                mUiCall.onUnLocked();
                            } else if (position == 2 || position == 3) {
                                isLockMalfunction = false;
                                mUiCall.onLockMalfunctioned();
                            }
                        }

                    } else if (characteristic.equals(CHAR_CONFIG_FW_VER)) {
                        final byte[] temp_bytes = characteristic.getValue();
                        final String temp_hexvalues = UtilHelper.bytesToHex(temp_bytes);
                        processFwVersioncode(temp_hexvalues);
                        if (isDoFWUpdate) {
                            firmwareUpdatecount = 0;
                            processFwResultFromHexcode(temp_hexvalues);
                        }

                    } else if (characteristic.equals(CHAR_CONFIG_OPEN_ADJUST)) {
                        mUiCall.readSerialNumber(gatt, characteristic);
                    } else if ((characteristic.equals(CHAR_HW_TX))) {
                        final byte[] temp_bytes = characteristic.getValue();
                        final String rsssi = UtilHelper.bytesToHex(temp_bytes);
                        //   Log.i("rsssi", "" + rsssi);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mUiCall.onBoardFailed();
                }
            }


        }

        @Override
        public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if (characteristic.equals(CHAR_HW_LOCK)) {
                gatt.readCharacteristic(CHAR_HW_INFO);
            } else if (characteristic.equals(CHAR_HW_LED)) {
                if (initialConnection) {
                    if (ledonoffcounter < 10) {
                        if (ledOnFlag) {
                            ledOFF();
                            ledonoffcounter++;
                        } else
                            ledON();

                    } else {
                        ledonoffcounter = 0;
                        close();
                    }
                }
            } else if (characteristic.equals(CHAR_HW_INFO)) {
                if (gatt != null) {
                    gatt.readCharacteristic(CHAR_HW_INFO);
                }
            } else if (characteristic.equals(CHAR_HW_TX)) {
                if (gatt != null) {
                    gatt.readCharacteristic(CHAR_HW_TX);
                }
            }


        }

        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {

            //     Log.i("Notification value : ", "" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
            int temp = CHAR_SECURITY_STATE.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            if (!isLockMalfunction && temp == 130) {
                isLockMalfunction = true;
                mUiCall.onLockMalfunctioned();
            }
            if (characteristic.equals(CHAR_SECURITY_STATE)) {
                if (isDoFWUpdate && characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) == 0 && characteristic.getDescriptor(UUID_HW_DESC).getUuid().equals(UUID_HW_DESC)) {
                    writeFirmwareUpdateDataFromCloud();

                } else if (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) == 0) {
                    BluetoothGattCharacteristic publicChar = characteristic.getService().getCharacteristic(UUID_SECURITY_PUB_KEY);
                    BluetoothGattCharacteristic signMsgChar = characteristic.getService().getCharacteristic(UUID_SECURITY_SIGN_MSG);
                    BluetoothGattCharacteristic challKeyChar = characteristic.getService().getCharacteristic(UUID_SECURITY_CHALL_KEY);
                    if (publicChar.getValue() != null) {
                        if (challKeyChar.getValue() == null) {
                            writeChallengekey(SkylockChallengeResultGenerator.getChallengeKey(keyGenerationID));
                        } else {
                            if (signMsgChar.getValue() == null) {
                                try {
                                    final String macAddress = UtilHelper.getLockMacIDFromName(gatt.getDevice().getName());
                                    final String encrytedSignedmessage = mPrefUtil.getStringPref(macAddress + SkylockConstant.SKYLOCK_SIGNED_MESSAGES, "");
                                    final String skylockSignedmessage = EncryptDecrypt.crypto(SkylockConstant.CRYPT_KEY + macAddress, encrytedSignedmessage, true);
                                    writeSignedMessage(skylockSignedmessage);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                         }
                    }
                } else if (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) == 1 || characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) == 2) {
                    mBluetoothGatt.readCharacteristic(CHAR_CHALL_DATA);
                } else if (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) == 3 || characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) == 4) {
                    enableTouchHwInfo();
                    mUiCall.onBoardCompleted(gatt, "");
                } else if (characteristic.equals(CHAR_SECURITY_STATE) && characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) == 129
                        || characteristic.equals(CHAR_SECURITY_STATE) && characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) == 128) {
                    mUiCall.onBoardFailed();
                }
            } else if (characteristic.equals(CHAR_ACC)) {
                mUiCall.onCrashedAndTheft(gatt, characteristic);
            } else if (characteristic.equals(CHAR_HW_TX)) {
                enableHwInfo();
            } else if (characteristic.equals(CHAR_HW_INFO)) {

                byte[] mode = characteristic.getValue();
                int val = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                final int position = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 4);
                if (mUiCall != null) {
                    if (!lockStatus && position == 1) {
                        lockStatus = true;
                        mUiCall.onLocked();
                    } else if (lockStatus && position == 0) {
                        lockStatus = false;
                        mUiCall.onUnLocked();
                    } else if (position == 2 || position == 3) {
                        mUiCall.onLockMalfunctioned();
                    }
                }
            }
        }
    };

    public void increaseRssIRange() {

        try {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBluetoothGatt != null && CHAR_HW_TX != null) {
                        CHAR_HW_TX.setValue(new byte[]{(byte) 0x04});
                        mBluetoothGatt.writeCharacteristic(CHAR_HW_TX);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processFwVersioncode(String temp_hexvalues) {
        final String tempString = temp_hexvalues.substring(0, Math.min(temp_hexvalues.length(), 26));
        final String tempstring = tempString.substring(Math.max(tempString.length() - 16, 0));
        final String upToNCharacters = tempstring.substring(Math.min(tempstring.length() - 8, tempstring.length()));
        final String mainApplicationVersion = upToNCharacters.substring(0, Math.min(upToNCharacters.length(), 4));
        //   Log.i("version ", mainApplicationVersion);
        final String mainApplicationRevision = upToNCharacters.substring(4, Math.min(upToNCharacters.length(), 8));
        //   Log.i("revision ", mainApplicationRevision);
        final String litleEndianCoonversionVersion = UtilHelper.littleEndianconversion(mainApplicationVersion);
        final String litleEndianCoonversionrevision = UtilHelper.littleEndianconversion(mainApplicationRevision);
        final String one = litleEndianCoonversionVersion.substring(0, Math.min(litleEndianCoonversionVersion.length(), 2));
        final String two = litleEndianCoonversionVersion.substring(Math.max(litleEndianCoonversionVersion.length() - 2, 0));
        final String three = litleEndianCoonversionrevision.substring(0, Math.min(litleEndianCoonversionrevision.length(), 2));
        final String four = litleEndianCoonversionrevision.substring(Math.max(litleEndianCoonversionrevision.length() - 2, 0));
        final int mainApplicationVersionValue1 = Integer.parseInt(one, 16);
        final int mainApplicationVersionValue2 = Integer.parseInt(two, 16);
        final int mainApplicationRevisionValue1 = Integer.parseInt(three, 16);
        final int mainApplicationRevisionValue2 = Integer.parseInt(four, 16);
        final String fwVersion = Integer.toString(mainApplicationVersionValue2) + "." + String.format("%02d", mainApplicationRevisionValue2);
        //  Log.i("Firmware", fwVersion);
        mUiCall.getFWinfo(fwVersion);
    }

    private void processFwResultFromHexcode(String hexValue) {
        final String initialString = hexValue.substring(0, Math.min(hexValue.length(), 26));
        final String tempstring = initialString.substring(Math.max(initialString.length() - 16, 0));
        final String upToNCharacters = tempstring.substring(0, Math.min(tempstring.length(), 8));
        final String version = upToNCharacters.substring(0, Math.min(upToNCharacters.length(), 4));
        //  Log.i("version value ", version);
        final String revision = upToNCharacters.substring(4, Math.min(upToNCharacters.length(), 8));
        //  Log.i("revision ", revision);
        if (!version.equals("0000") && !revision.equals("0000")) {
            reBootBle();
            mSkylockBLEFirmwareUpdateStatus.oncompleteFirmwareImage();
            isDoFWUpdate = false;
        } else {
            mSkylockBLEFirmwareUpdateStatus.onCompleteFirmwareWithExisitingVersion();
        }

    }

    private void reBootBle() {

        try {
            if (mBluetoothGatt != null && CHAR_CONFIG_RESET != null) {
                CHAR_CONFIG_RESET = mBluetoothGatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CONFIG_RESET);
                CHAR_CONFIG_RESET.setValue(new byte[]{(byte) 0x01});
                mBluetoothGatt.writeCharacteristic(CHAR_CONFIG_RESET);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void getVersionInfo() {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mBluetoothGatt != null && CHAR_CONFIG_FW_VER != null)
                        mBluetoothGatt.readCharacteristic(CHAR_CONFIG_FW_VER);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


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
                CHAR_BOOT_DATA_WRITE = gatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CONFIG_BOOT_DATA_WRITE);
                CHAR_BOOT_STATUS_NOTIFY = gatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CONFIG_BOOT_STATUS_NOTIFY);
                CHAR_BOOT_DOWNLOAD_DONE = gatt.getService(UUID_CONFIG_SER).getCharacteristic(UUID_CONFIG_BOOT_DOWNLOAD_DONE);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void enableAcctodevice() {
        if (mBluetoothGatt != null && CHAR_ACC != null) {
            enableDescriptorNotification(CHAR_ACC, true);
        }
    }

    public void enableTouchHwInfo() {
        if (mBluetoothGatt != null && CHAR_HW_INFO != null) {
            enableDescriptorNotification(CHAR_HW_INFO, true);
        }
    }


    public void enableVerifyBootservices() {
        if (mBluetoothGatt != null && CHAR_SECURITY_STATE != null) {
            try {
                isDoFWUpdate = true;
                enableDescriptorNotification(CHAR_SECURITY_STATE, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void getLockSerialNumber() {
        try {
            if (mBluetoothGatt != null) {
                mBluetoothGatt.readCharacteristic(CHAR_CONFIG_OPEN_ADJUST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void enableLedInfo() {
        try {
            if (mBluetoothGatt != null && CHAR_HW_LED != null) {
                enableDescriptorNotification(CHAR_HW_LED, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disableAcctodevice() {
        try {
            enableDescriptorNotification(CHAR_ACC, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void enableHwInfo() {
        try {
            if (mBluetoothGatt != null && CHAR_HW_INFO != null) {
                enableDescriptorNotification(CHAR_HW_INFO, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enableMaxTxRange() {
        try {
            if (mBluetoothGatt != null && CHAR_HW_TX != null) {
                enableDescriptorNotification(CHAR_HW_TX, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enableSecurityNotificationCharaterisitics() {
        // enabling security descripter state
        if (mBluetoothGatt != null && CHAR_SECURITY_STATE != null) {
            try {
                enableDescriptorNotification(CHAR_SECURITY_STATE, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void enableDescriptorNotification(final BluetoothGattCharacteristic characteristic, final boolean enable) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
                    if (!success) {
                        Log.e("------", "Seting proper notification status for characteristic failed!");
                    }
                    final BluetoothGattDescriptor bluetoothGattDescriptor = characteristic.getDescriptor(UUID_ACC_DESC);
                    if (bluetoothGattDescriptor != null) {
                        byte[] val = enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                        bluetoothGattDescriptor.setValue(val);
                        final boolean isWriteDescriptor = mBluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
                        if (!isWriteDescriptor)
                            enableDescriptorNotification(characteristic, enable);
                    } else {
                        mUiCall.onDescriptorWrite(mBluetoothGatt, bluetoothGattDescriptor, 0);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }


    public void getSerialInfo() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mBluetoothGatt != null && CHAR_CONFIG_OPEN_ADJUST != null)
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
        try {
            int len = s.length();
            byte[] byteTemp = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                byteTemp[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
            }
            if (byteTemp != null && tempBluetoothGattCharacteristic != null && mBluetoothGatt != null) {
                tempBluetoothGattCharacteristic.setValue(byteTemp);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean isWrittern = mBluetoothGatt.writeCharacteristic(tempBluetoothGattCharacteristic);
                        if (!isWrittern) {
                            bluetoothWriteData(s, tempBluetoothGattCharacteristic);
                        }
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getRSSIvalue() {
        try {
            if (mBluetoothGatt != null) {
                mBluetoothGatt.readRemoteRssi();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        } else {
            isServiceDiscovered = false;
            if (mBluetoothGatt != null)
                mBluetoothGatt.close();
            mBluetoothGatt = null;
            mUiCall.onDeviceDisconnected(false);
        }

    }

    public void requestHighPriorityConnection() {
        if (mBluetoothGatt != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
            }
        }
    }

    public void requestBalancedPriorityConnection() {
        if (mBluetoothGatt != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
            }
        }
    }

    public void setFirmwareupdateData(FirmwareUpdates mfirmwareUpdates) {
        this.firmwareUpdates = mfirmwareUpdates;
        requestHighPriorityConnection();
        try {
            writeFirmwareUpdateDataFromCloud();
        } catch (Exception e) {
            e.printStackTrace();
        }
        requestBalancedPriorityConnection();
    }

    public void lock(final boolean autoLockUnlock) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothGatt != null && CHAR_HW_LOCK != null) {
                    isLockMalfunction = false;
                    if (autoLockUnlock) {
                        CHAR_HW_LOCK.setValue(new byte[]{(byte) 0xFF});
                    } else {
                        CHAR_HW_LOCK.setValue(new byte[]{(byte) 0x10});
                    }
                    mBluetoothGatt.writeCharacteristic(CHAR_HW_LOCK);
                }
            }
        });


    }

    public void unLock() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothGatt != null && CHAR_HW_LOCK != null) {
                    CHAR_HW_LOCK.setValue(new byte[]{(byte) 0x00});
                    mBluetoothGatt.writeCharacteristic(CHAR_HW_LOCK);
                }
            }
        });

    }

    public void ledON() {
        ledOnFlag = true;
        CHAR_HW_LED.setValue(new byte[]{(byte) 0xFF});
        mBluetoothGatt.writeCharacteristic(CHAR_HW_LED);
    }

    public void ledOFF() {
        ledOnFlag = false;
        CHAR_HW_LED.setValue(new byte[]{(byte) 0x00});
        mBluetoothGatt.writeCharacteristic(CHAR_HW_LED);
    }

    public void getHWInfo() {
        try {
            if (mBluetoothGatt != null && CHAR_HW_INFO != null)
                mBluetoothGatt.readCharacteristic(CHAR_HW_INFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getmodeInfo() {
        try {
            if (mBluetoothGatt != null && CHAR_SECURITY_STATE != null)
                mBluetoothGatt.readCharacteristic(CHAR_SECURITY_STATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetCapPin(String pinCode) {
        if (CHAR_CONFIG_CAP_PIN != null)
            bluetoothWriteData(pinCode, CHAR_CONFIG_CAP_PIN);
    }


    private void writeFirmwareUpdateDataFromCloud() {
        if (firmwareUpdatecount < firmwareUpdates.getPayload().size()) {
            mSkylockBLEFirmwareUpdateStatus.onUpdateFirmwareImage(firmwareUpdatecount);
            if (firmwareUpdates.getPayload().get(firmwareUpdatecount) != null && CHAR_BOOT_DATA_WRITE != null) {
                bluetoothWriteData(firmwareUpdates.getPayload().get(firmwareUpdatecount), CHAR_BOOT_DATA_WRITE);
                firmwareUpdatecount++;
            }
        } else if (firmwareUpdatecount == firmwareUpdates.getPayload().size()) {
            mSkylockBLEFirmwareUpdateStatus.onUpdateFirmwareImage(firmwareUpdatecount);
            firmwareUpdatecount++;
            refreshDeviceCache(mBluetoothGatt);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getVersionInfo();
                }
            }, 30000);
        }
    }

}
