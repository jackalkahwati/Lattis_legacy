
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
package cc.skylock.myapplication.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.HashSet;
import java.util.Timer;

import cc.skylock.myapplication.FWUpgradeProgress;


/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class SkylockBluetoothLEService extends Service implements cc.skylock.myapplication.bluetooth.SkylockBluetoothScanCallback, cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus {
    private final static String TAG = SkylockBluetoothLEService.class.getSimpleName();
    public static cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus mUiCall;
    private Context mContext;
    private HashSet<BluetoothDevice> bluetoothDevices;
    private Timer timer;
    public static BluetoothGatt mCurrentlyconnectedGatt = null;
    Timer selfTimer;
    int counter = 0;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        onConnectionFailed(false);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        stopTimer();
                        stopSelfTimer();
                        break;
                    case BluetoothAdapter.STATE_ON:

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }

        }
    };

    @Override
    public void onScanFailed() {

    }

    @Override
    public void onScanedDevice(BluetoothDevice device) {
        mUiCall.onScanedDevice(device);
    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onConnected(BluetoothGatt mBluetoothGatt) {
        mUiCall.onConnected(mBluetoothGatt);
        mCurrentlyconnectedGatt = mBluetoothGatt;
    }


    @Override
    public void onConnectionFailed(boolean shippingModeEnabled) {
        mUiCall.onConnectionFailed(shippingModeEnabled);
        mCurrentlyconnectedGatt = null;
    }

    @Override
    public void onDisConnect() {
        mCurrentlyconnectedGatt = null;

    }

    @Override
    public void onGetHardWareInfo(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic mCharacteristic) {
        mUiCall.onGetHardWareInfo(mBluetoothGatt, mCharacteristic);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt mBluetoothGatt) {
        mUiCall.onDescriptorWrite(mBluetoothGatt);

    }

    @Override
    public void onDeviceStatus(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic mCharacteristic) {
        mUiCall.onDeviceStatus(mBluetoothGatt, mCharacteristic);
    }

    @Override
    public void onGetSerialInfo(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        mUiCall.onGetSerialInfo(gatt, characteristic);
    }

    @Override
    public void onLedBlink(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        mUiCall.onLedBlink(gatt, characteristic);

    }

    public void close() {
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().close();
    }

    public class LocalBinder extends Binder {
        public SkylockBluetoothLEService getService() {
            return SkylockBluetoothLEService.this;
        }
    }


    @Override
    public void onFWUpgradeProgress(FWUpgradeProgress fwUpgradeProgress) {
        if(mUiCall!=null);
            mUiCall.onFWUpgradeProgress(fwUpgradeProgress);
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
        registerReceiver(mBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance(mContext);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance(mContext).scanLeDevice(true);

        return true;
    }

    public boolean stopScan() {
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance(mContext).scanLeDevice(false);

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
    public boolean connect(final BluetoothDevice device) {

        if (device != null) {
            cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().connect(device);
            return true;
        } else
            return false;

    }

    public void enableHardwareNotification(boolean enable) {
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().enableHwInfo(enable);
    }
    public void enableSecurityNotificationCharaterisitics(final boolean enable) {
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().enableSecurityNotificationCharaterisitics(true);
    }
    public void writeSerialInfo(final String serialNumber) {
        if (serialNumber!=null) {
            cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().writeSerialInfo(serialNumber);
        }
    }
    public void enableLEDNotification(boolean enable) {
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().enableLedInfo(enable);
    }

    public void clearBLeStack() {
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().disconnect();
    }

    public void lock() {
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().lock();
    }

    public void putShippingMode() {
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().putShippingMode();
    }

    public void unLock() {
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().unLock();
    }

    public void enableSerialNumberNotification(final boolean enable) {
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().enableSerialNumberNotification(enable);
    }

    public void getHardwareInfo() {
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().getHWInfo();
    }

    public void writeHardwareInfo() {
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().writeHardwareInfo();
    }

    public void getRSSIvalueForSelf() {
        try {
            cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().getRSSIvalueForSelf();
        } catch (Exception e) {

        }

    }
    public void getSerialInfo() {
        try {
            cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().getSerialInfo();
        } catch (Exception e) {

        }

    }

    public void updateFirmware(byte[] fileList) {
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().updateFirmware(fileList);
    }

    public void stopTimer() {
        try {
            if (timer != null)
                timer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopSelfTimer() {
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().stopSelfTimer();
    }

    public static void registerBluetoothDeviceStatusListener(cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus listener) {
        mUiCall = listener;
        Log.i("listener", "Registered");
    }

    public static void unregisterBluetoothDeviceStatusListener() {
        mUiCall = null;
    }

    //ble scaned device
    @Override
    public void onScaningDevice(BluetoothDevice device) {
        if (device != null && mUiCall != null)
            mUiCall.onScanedDevice(device);
    }

    @Override
    public void onError() {

        mUiCall.onScanFailed();

    }

    public void ledON(final byte arg) {
        cc.skylock.myapplication.bluetooth.SkylockBluetoothManage.getInstance().ledON(arg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}
