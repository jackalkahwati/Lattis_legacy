package cc.skylock.myapplication.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.HashSet;

import cc.skylock.myapplication.FWUpgradeProgress;

/**
 * Created by admin on 07/05/16.
 */
public interface BluetoothDeviceStatus {

    public void onScanFailed();

    public void onScanedDevice(BluetoothDevice device);

    public void onConnect();

    public void onConnected(BluetoothGatt mBluetoothGatt);

    public void onConnectionFailed(boolean shippingModeEnabled);

    public void onDisConnect();

    public void onGetHardWareInfo(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic mCharacteristic);

    public void onDescriptorWrite(BluetoothGatt mBluetoothGatt);

    public void onDeviceStatus(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic mCharacteristic);

    public void onGetSerialInfo(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

    public void onLedBlink(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

    public void onFWUpgradeProgress(FWUpgradeProgress fwUpgradeProgress);


}
