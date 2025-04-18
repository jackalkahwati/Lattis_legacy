
/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package cc.skylock.skylock.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import cc.skylock.skylock.Bean.CrashAndTheftParameter;
import cc.skylock.skylock.Bean.CrashResponse;
import cc.skylock.skylock.Bean.FirmwareUpdates;
import cc.skylock.skylock.Bean.LockKeyGen;
import cc.skylock.skylock.Bean.LockMessagesResponse;
import cc.skylock.skylock.Bean.SendMacIdAsParameter;
import cc.skylock.skylock.Bean.SetFWVersion;
import cc.skylock.skylock.Bean.TheftResponse;
import cc.skylock.skylock.operation.LockWebServiceApi;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.service.LocationService;
import cc.skylock.skylock.utils.EncryptDecrypt;
import cc.skylock.skylock.utils.Network.NetworkUtil;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class SkylockBluetoothLEService extends Service implements SkylockBluetoothScanCallback, BluetoothDeviceStatus, SkylockBLEFirmwareUpdateStatus {
    private final static String TAG = SkylockBluetoothLEService.class.getSimpleName();
    public static BluetoothDeviceStatus mUiCall;
    private Context mContext;
    private PrefUtil mPrefUtil;
    private Timer timer;
    public static boolean isConnectionExist = false;
    public static boolean mBluetoothGattDescriptorEnable = false;
    public static boolean mProximityLocked = false;
    public static boolean mProximityUnLocked = false;
    public static int crashTheftselection = 0;
    public static SkylockBLEFirmwareUpdateStatus mSkylockBLEFirmwareUpdateStatus;
    private String lock_Mac_id;
    boolean isBound;
    private String lock_Mode = null;

    @Override
    public void doUpdateFirmware() {
        getFirmwareDataFromServer();
    }

    @Override
    public void onGetFirmwareImageData(FirmwareUpdates mFirmwareUpdates) {

    }

    @Override
    public void onUpdateFirmwareImage(int mProgressStatus) {
        mSkylockBLEFirmwareUpdateStatus.onUpdateFirmwareImage(mProgressStatus);
    }

    @Override
    public void oncompleteFirmwareImage() {
        mSkylockBLEFirmwareUpdateStatus.oncompleteFirmwareImage();

    }

    @Override
    public void onCompleteFirmwareWithExisitingVersion() {
        mSkylockBLEFirmwareUpdateStatus.onCompleteFirmwareWithExisitingVersion();
    }

    public void increaseTxPower() {
        SkylockBluetoothManage.getInstance().increaseRssIRange();
    }


    public class LocalBinder extends Binder {
        public SkylockBluetoothLEService getService() {
            return SkylockBluetoothLEService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.

        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mPrefUtil = new PrefUtil(mContext);
        SkylockBluetoothManage.getInstance(mContext);

    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        mContext = this;
        if (mContext != null) {
            SkylockBluetoothManage.getInstance(mContext).scanLeDevice(true);
            return true;
        } else {
            return false;
        }
    }

    public boolean stopScanning() {
        SkylockBluetoothManage.getInstance().scanLeDevice(false);
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final BluetoothDevice device, boolean blinkLED, String generationID, boolean isRefreshBle) {
        if (device != null) {
            SkylockBluetoothManage.getInstance().connect(device, blinkLED, generationID, isRefreshBle);
            return true;
        } else
            return false;

    }

    public void clearBLeStack() {
        SkylockBluetoothManage.getInstance().disconnect();
    }

    public void lock(boolean autoLockUnlock) {
        SkylockBluetoothManage.getInstance().lock(autoLockUnlock);
    }

    public void putShippingMode() {
        SkylockBluetoothManage.getInstance().putShippingMode();
    }

    public void enableCrashAndTheft(int selection) {
        SkylockBluetoothManage.getInstance().enableAcctodevice();
        mBluetoothGattDescriptorEnable = true;
        crashTheftselection = selection;

    }

    public void disableCrashAndTheft(int selection) {
        SkylockBluetoothManage.getInstance().disableAcctodevice();
        mBluetoothGattDescriptorEnable = false;
        crashTheftselection = selection;

    }

    public void unLock() {
        SkylockBluetoothManage.getInstance().unLock();
    }

    public void getHardwareInfo() {
        SkylockBluetoothManage.getInstance().getHWInfo();
    }

    public void close() {
        if (SkylockBluetoothManage.getInstance() != null)
            SkylockBluetoothManage.getInstance().close();
        else
            SkylockBluetoothManage.getInstance(mContext).close();

    }

    public void getRssiValue() {
        try {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    SkylockBluetoothManage.getInstance().getRSSIvalue();
                }
            }, 0, 5000);
        } catch (Exception e) {

        }

    }

    public void stopRssiValue() {
        try {
            if (timer != null) {
                timer.cancel();
            }

        } catch (Exception e) {

        }

    }

    public void setCapPin(String pinCode) {
        SkylockBluetoothManage.getInstance().resetCapPin(pinCode);
    }

    public void callBootService() {
        stopRssiValue();
        SkylockBluetoothManage.getInstance().enableVerifyBootservices();
    }

    public void callFWInfo() {
        SkylockBluetoothManage.getInstance().getVersionInfo();
    }

    public void getLockSerialNumber() {
        SkylockBluetoothManage.getInstance().getLockSerialNumber();
    }

    public void stopTimer() {
        try {
            if (timer != null)
                timer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerBluetoothDeviceStatusListener(BluetoothDeviceStatus listener) {
        mUiCall = listener;
    }

    public static void unregisterBluetoothDeviceStatusListener() {
        mUiCall = null;
    }

    public static void registerFirmwareUpdateListener(SkylockBLEFirmwareUpdateStatus listener) {
        mSkylockBLEFirmwareUpdateStatus = listener;
    }

// --------- new work  -----

    //ble scaned device
    @Override
    public void onScaningDevice(HashSet<BluetoothDevice> device) {
        if (device != null && mUiCall != null)
            mUiCall.onScanedDevice(device);
    }

    //ble scaned error
    @Override
    public void onError() {

        mUiCall.onScanFailed();

    }


    @Override
    public void onBoardFailed() {
        if (mUiCall != null)
            mUiCall.onBoardFailed();

    }


    @Override
    public void onBoardCompleted(BluetoothGatt mBluetoothGatt, String mode) {
        mUiCall.onBoardCompleted(mBluetoothGatt, lock_Mode);
        isConnectionExist = true;
        try {
            mPrefUtil.setStringPref(UtilHelper.getLockMacIDFromName(mBluetoothGatt.getDevice()
                            .getName()) + SkylockConstant.LAST_CONNECTED_TIMESTAMP,
                    UtilHelper.getCurrentTimeStamp());
        } catch (Exception e) {
            e.printStackTrace();
        }
        final Intent intent = new Intent(SkylockConstant.ACTION_GATT_CONNECTED);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onGetHardwareInfo(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (mUiCall != null) {
            mUiCall.onGetHardwareInfo(gatt, characteristic);
            lock_Mac_id = UtilHelper.getLockMacIDFromName(gatt.getDevice().getName());
        }
    }

    @Override
    public void onConnect() {
        mUiCall.onConnect();

    }

    @Override
    public void onConnectionTimeOut() {
        mUiCall.onConnectionTimeOut();
    }

    @Override
    public void onCrashed(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        mUiCall.onCrashed(gatt, characteristic);
        onCrashDetection(characteristic);
    }


    @Override
    public void onCrashedAndTheft(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        mUiCall.onCrashedAndTheft(gatt, characteristic);
    }

    @Override
    public void onDeviceConnected(BluetoothDevice device) {
        if (device != null) {
            mUiCall.onDeviceConnected(device);
        }


    }

    @Override
    public void onDeviceDisconnected(boolean shippingModeEnabled) {
        mUiCall.onDeviceDisconnected(shippingModeEnabled);
        isConnectionExist = false;
        stopRssiValue();
        final Intent intent = new Intent(SkylockConstant.ACTION_GATT_DISCONNECTED);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onDeviceStatus(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        mUiCall.onDeviceStatus(gatt, characteristic);
        byte[] mode = characteristic.getValue();
        lock_Mode = UtilHelper.bytesToHex(mode);
        lock_Mode = lock_Mode.substring(0, Math.min(lock_Mode.length(), 2));
        // Log.i("lock_Mode", lock_Mode);
        final BluetoothDevice mBluetoothDevice = gatt.getDevice();
        final String macAddress = UtilHelper.getLockMacIDFromName(mBluetoothDevice.getName());
        if (lock_Mode.equals("05") || lock_Mode.equals("06")) {
            connectLockCall(lock_Mode, macAddress);
        } else if (lock_Mode.equals("00")) {
            final String signedMessageFromPreference = mPrefUtil.getStringPref(macAddress + SkylockConstant.SKYLOCK_SIGNED_MESSAGES, "");
            if (!signedMessageFromPreference.equals("") && signedMessageFromPreference != null) {
                final String skylockSignedmessage = EncryptDecrypt.crypto(SkylockConstant.CRYPT_KEY + macAddress, signedMessageFromPreference, true);
                SkylockBluetoothManage.getInstance().writeSignedMessage(skylockSignedmessage);
            } else if (NetworkUtil.isNetworkAvailable(mContext)) {
                connectLockCall(lock_Mode, macAddress);
            }
        } else if (lock_Mode.equals("04")) {
            mUiCall.onBoardCompleted(gatt, lock_Mode);
        } else if (lock_Mode.equals("01")) {

        } else if (lock_Mode.equals("03")) {

        } else {
            mUiCall.onDeviceDisconnected(false);
        }

    }

    public void enableHardwareNotification() {
        SkylockBluetoothManage.getInstance().enableHwInfo();
    }


    private void connectLockCall(final String lock_mode, final String macAddress) {
        lock_Mac_id = macAddress;
        final SendMacIdAsParameter sendMacIdAsParameter = new SendMacIdAsParameter();
        if (macAddress != null)
            sendMacIdAsParameter.setMac_id(macAddress);
        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<LockMessagesResponse> mlockKeyGenMessage = lockWebServiceApi.GetLocksignedAndPublicMessage(sendMacIdAsParameter);
        mlockKeyGenMessage.enqueue(new Callback<LockMessagesResponse>() {
            @Override
            public void onResponse(Call<LockMessagesResponse> call, Response<LockMessagesResponse> mlockKeyGen) {
                if (mlockKeyGen.code() == 200) {
                    final String skylockLockPublicKey = EncryptDecrypt.crypto(SkylockConstant.CRYPT_KEY + macAddress, mlockKeyGen.body().getPayload().getPublic_key(), false);
                    mPrefUtil.setStringPref(macAddress + SkylockConstant.SKYLOCK_PUBLIC_KEYS, skylockLockPublicKey);
                    final String skylockSignedMessage = EncryptDecrypt.crypto(SkylockConstant.CRYPT_KEY + macAddress, mlockKeyGen.body().getPayload().getSigned_message(), false);
                    mPrefUtil.setStringPref(macAddress + SkylockConstant.SKYLOCK_SIGNED_MESSAGES, skylockSignedMessage);
                    if (lock_mode.equals("00")) {
                        SkylockBluetoothManage.getInstance().writeSignedMessage(mlockKeyGen.body().getPayload().getSigned_message());
                    } else if (lock_mode.equals("05") || lock_mode.equals("06")) {
                        SkylockBluetoothManage.getInstance().writePublickey(mlockKeyGen.body().getPayload().getPublic_key());
                    } else {
                        SkylockBluetoothManage.getInstance().clearCacheDataFromDevice();
                        mUiCall.onBoardFailed();
                    }
                    UtilHelper.analyticTrackUserAction("Add lock", "Custom", "Ellipses", null, "ANDROID");
                } else if (mlockKeyGen.body().getStatus() == 400 || mlockKeyGen.body().getStatus() == 404) {
                    Toast.makeText(mContext, "" + mlockKeyGen.body().getError(), Toast.LENGTH_LONG).show();
                    close();
                    mUiCall.onDeviceDisconnected(false);
                }


            }

            @Override
            public void onFailure(Call<LockMessagesResponse> call, Throwable t) {


            }
        });

    }


    @Override
    public void onScanFailed() {
        if (mUiCall != null)
            mUiCall.onScanFailed();
    }

    @Override
    public void onScanedDevice(HashSet<BluetoothDevice> device) {

    }

    @Override
    public void onGetRSSi(BluetoothGatt gatt, int rssi) {
        if (mUiCall != null)
            mUiCall.onGetRSSi(gatt, rssi);

    }

    @Override
    public void getFWinfo(String version) {
        if (mUiCall != null)
            mUiCall.getFWinfo(version);

    }

    @Override
    public void readSerialNumber(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        mUiCall.readSerialNumber(gatt, characteristic);


    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

    }


    @Override
    public void onLocked() {
        mUiCall.onLocked();
        if (mPrefUtil.getBooleanPref(lock_Mac_id + SkylockConstant.SKYLOCK_PROXIMITY_UNLOCK_ENABLE, false) || mPrefUtil.getBooleanPref(lock_Mac_id + SkylockConstant.SKYLOCK_PROXIMITY_LOCK_ENABLE, false)) {
            mProximityLocked = true;
            mProximityUnLocked = false;
        }

    }

    @Override
    public void onTheft(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        mUiCall.onTheft(gatt, characteristic);
        onTheftDetection(characteristic);
    }


    @Override
    public void onUnLocked() {
        mUiCall.onUnLocked();
        if (mPrefUtil.getBooleanPref(lock_Mac_id + SkylockConstant.SKYLOCK_PROXIMITY_UNLOCK_ENABLE, false) || mPrefUtil.getBooleanPref(lock_Mac_id + SkylockConstant.SKYLOCK_PROXIMITY_LOCK_ENABLE, false)) {
            mProximityUnLocked = true;
            mProximityLocked = false;
        }
    }

    @Override
    public void onLockMalfunctioned() {
        if (mUiCall != null)
            mUiCall.onLockMalfunctioned();
    }

    private void getFirmwareDataFromServer() {
        final LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        SetFWVersion mSetFWVersion = new SetFWVersion();
        //   mSetFWVersion.setVersion("02.19");
        Call<FirmwareUpdates> mFirmwareUpdates = lockWebServiceApi.GetFirmwareUpdates();
        mFirmwareUpdates.enqueue(new Callback<FirmwareUpdates>() {
            @Override
            public void onResponse(Call<FirmwareUpdates> call, Response<FirmwareUpdates> mFirmwareUpdates) {
                if (mFirmwareUpdates.code() == 200) {
                    mSkylockBLEFirmwareUpdateStatus.doUpdateFirmware();
                    FirmwareUpdates firmwareUpdates = mFirmwareUpdates.body();
                    mSkylockBLEFirmwareUpdateStatus.onGetFirmwareImageData(firmwareUpdates);
                    SkylockBluetoothManage.getInstance().setFirmwareupdateData(firmwareUpdates);
                }
            }

            @Override
            public void onFailure(Call<FirmwareUpdates> call, Throwable t) {
            }
        });
    }

    private void onTheftDetection(BluetoothGattCharacteristic characteristic) {
        handleCrashAndTheftDataWithBean(characteristic);
        final LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<TheftResponse> crashdetction = lockWebServiceApi.TheftDetection(handleCrashAndTheftDataWithBean(characteristic));
        crashdetction.enqueue(new Callback<TheftResponse>() {
            @Override
            public void onResponse(Call<TheftResponse> call, Response<TheftResponse> response) {
                if (response.code() == 200) {
                    SkylockConstant.LOCK_CRASH_ID = response.body().getPayload().getTheft_id();
                }

            }

            @Override
            public void onFailure(Call<TheftResponse> call, Throwable t) {

            }
        });

    }

    private CrashAndTheftParameter handleCrashAndTheftDataWithBean(BluetoothGattCharacteristic characteristic) {

        final CrashAndTheftParameter mCrashAndTheftParameter = new CrashAndTheftParameter();
        final CrashAndTheftParameter.AccelerometerDataEntity mAccelerometerDataEntity = new CrashAndTheftParameter.AccelerometerDataEntity();
        final CrashAndTheftParameter.LocationEntity mLocationEntity = new CrashAndTheftParameter.LocationEntity();
        final Integer mavX = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
        final Integer mavY = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2);
        final Integer mavZ = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 4);
        final Integer sdX = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 6);
        final Integer sdY = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 8);
        final Integer sdZ = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 10);
        mAccelerometerDataEntity.setX_ave(mavX);
        mAccelerometerDataEntity.setY_ave(mavY);
        mAccelerometerDataEntity.setZ_ave(mavZ);
        mAccelerometerDataEntity.setX_dev(sdX);
        mAccelerometerDataEntity.setY_dev(sdY);
        mAccelerometerDataEntity.setZ_ave(sdZ);
        mCrashAndTheftParameter.setAccelerometer_data(mAccelerometerDataEntity);
        mCrashAndTheftParameter.setMac_id(lock_Mac_id);
        try {
            final LocationService location = new LocationService(mContext);
            final LatLng currentLocation = location.updateCoordinates();
            if (currentLocation != null) {
                mLocationEntity.setLatitude(currentLocation.latitude);
                mLocationEntity.setLongitude(currentLocation.longitude);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCrashAndTheftParameter.setLocation(mLocationEntity);

        return mCrashAndTheftParameter;
    }


    private void onCrashDetection(BluetoothGattCharacteristic characteristic) {
        final LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<CrashResponse> crashdetction = lockWebServiceApi.CrashDetection(handleCrashAndTheftDataWithBean(characteristic));
        crashdetction.enqueue(new Callback<CrashResponse>() {
            @Override
            public void onResponse(Call<CrashResponse> call, Response<CrashResponse> response) {

                if (response.code() == 200) {
                    SkylockConstant.LOCK_CRASH_ID = response.body().getPayload().getCrash_id();
                }
            }

            @Override
            public void onFailure(Call<CrashResponse> call, Throwable t) {

            }
        });

    }

}
