package cc.skylock.skylock.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.HashSet;

/**
 * Created by admin on 07/05/16.
 */
public interface BluetoothDeviceStatus {
    public void onDeviceConnected(BluetoothDevice device);
    public void onConnect();
    public void onConnectionTimeOut();
    public void onDeviceDisconnected(boolean shippingModeEnabled);
    public void onBoardFailed();
    public void onBoardCompleted(BluetoothGatt gatt,String mode);
    public void onGetHardwareInfo(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic);
    public void onLocked();
    public void onUnLocked();
    public void onLockMalfunctioned();
    public void onCrashed(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);
    public void onTheft(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);
    public void onDeviceStatus(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic);
    public void onCrashedAndTheft(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic);
    public void onScanFailed();
    public void onScanedDevice(HashSet<BluetoothDevice> device);
    public void onGetRSSi(BluetoothGatt gatt, int rssi);
    public void getFWinfo(String version);
    public void readSerialNumber(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic);
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);

}
