package com.lattis.ellipse.bluetooth;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.domain.model.Lock;

import javax.inject.Inject;

import io.lattis.ellipse.sdk.model.Status;


public class BluetoothStateMapper extends AbstractDataMapper<Status,Lock.Connection.Status> {

    private BluetoothLockMapper bluetoothLockMapper;

    @Inject
    BluetoothStateMapper(BluetoothLockMapper bluetoothLockMapper) {
        this.bluetoothLockMapper = bluetoothLockMapper;
    }

    @NonNull
    @Override
    public Lock.Connection.Status mapIn(@NonNull Status status) {
        Lock lock = null;
        if(status.getBluetoothLock() != null){
            lock = bluetoothLockMapper.mapOut(status.getBluetoothLock());
        }
        switch (status) {
            case SCANNING:
                return Lock.Connection.Status.SCANNING;
            case DEVICE_FOUND:
                return Lock.Connection.Status.DEVICE_FOUND.forLock(lock);
            case DISCOVER_SERVICE:
                return Lock.Connection.Status.DISCOVER_SERVICE.forLock(lock);
            case DISCONNECTED:
                return Lock.Connection.Status.DISCONNECTED.forLock(lock);
            case SERVICE_DISCOVERED:
                return Lock.Connection.Status.SERVICE_DISCOVERED.forLock(lock);
            case OWNER_REQUEST:
                return Lock.Connection.Status.OWNER_REQUEST.forLock(lock);
            case GUEST_REQUEST:
                return Lock.Connection.Status.GUEST_REQUEST.forLock(lock);
            case OWNER_VERIFIED:
                return Lock.Connection.Status.OWNER_VERIFIED.forLock(lock);
            case GUEST_VERIFIED:
                return Lock.Connection.Status.GUEST_VERIFIED.forLock(lock);
            case FIRMWARE_VERSION:
                break;
            case UPDATING_FIRMWARE:
                return Lock.Connection.Status.UPDATING_FIRMWARE.forLock(lock);
            case ERROR:
                return Lock.Connection.Status.ERROR.forLock(lock);
            case ACCESS_DENIED:
                return Lock.Connection.Status.ACCESS_DENIED.forLock(lock);
            default:return Lock.Connection.Status.DISCONNECTED;
        }
        return Lock.Connection.Status.DISCONNECTED;
    }

    @NonNull
    @Override
    public Status mapOut(@NonNull Lock.Connection.Status status) {
        return null;
    }
}
