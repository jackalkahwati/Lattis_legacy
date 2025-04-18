package com.lattis.ellipse.bluetooth;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.domain.model.Lock;

import javax.inject.Inject;

import io.lattis.ellipse.sdk.model.BluetoothLock;

public class LockMapper extends AbstractDataMapper<Lock,BluetoothLock> {

    @Inject
    public LockMapper() {
    }

    @NonNull
    @Override
    public BluetoothLock mapIn(@NonNull Lock lock) {
        BluetoothLock bluetoothLock = new BluetoothLock();
        bluetoothLock.setMacId(lock.getMacAddress());
        bluetoothLock.setName(lock.getName());
       // bluetoothLock.setLocked(lock.isLocked());
        return bluetoothLock;
    }

    @NonNull
    @Override
    public Lock mapOut(@NonNull BluetoothLock bluetoothLock) {
        Lock lock = new Lock();
        lock.setMacAddress(bluetoothLock.getMacId());
        lock.setName(bluetoothLock.getName());
        //lock.setLocked(bluetoothLock.isLocked());
        return lock;
    }
}
