package io.lattis.ellipse.sdk.mapper;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import io.lattis.ellipse.sdk.model.BluetoothLock;

public class BluetoothLockMapper extends AbstractDataMapper<BluetoothLock,BluetoothDevice> {

    @NonNull
    @Override
    public BluetoothDevice mapIn(@NonNull BluetoothLock bluetoothLock) {
        return null;
    }

    @NonNull
    @Override
    public BluetoothLock mapOut(@NonNull BluetoothDevice bluetoothDevice) {
        BluetoothLock bluetoothLock = new BluetoothLock();
        if(bluetoothDevice.getName().contains("-")){
            bluetoothLock.setMacId(bluetoothDevice.getName().split("-")[1]);
        } else {
            bluetoothLock.setMacId(bluetoothDevice.getName().split(" ")[1]);
        }
        bluetoothLock.setName(bluetoothDevice.getName());
        bluetoothLock.setMacAddress(bluetoothDevice.getAddress());
        return bluetoothLock;
    }
}
