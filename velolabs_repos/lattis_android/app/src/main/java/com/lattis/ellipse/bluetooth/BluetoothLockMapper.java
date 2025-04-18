package com.lattis.ellipse.bluetooth;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.domain.model.Lock;

import javax.inject.Inject;

import io.lattis.ellipse.sdk.model.BluetoothLock;

/**
 * Created by ssd3 on 4/26/17.
 */

public class BluetoothLockMapper extends AbstractDataMapper<Lock,BluetoothLock> {

    AlertMapper alertMapper;

    @Inject
    BluetoothLockMapper(AlertMapper alertMapper) {
        this.alertMapper=alertMapper;
    }

    @NonNull
    @Override
    public BluetoothLock mapIn(@NonNull Lock lock) {
        BluetoothLock bluetoothLock = new BluetoothLock();
        bluetoothLock.setLockId(lock.getLockId());
        bluetoothLock.setMacId(lock.getMacId());
        bluetoothLock.setMacAddress(lock.getMacAddress());
        bluetoothLock.setName(lock.getName());
        bluetoothLock.setPublicKey(lock.getPublicKey());
        bluetoothLock.setSignedMessage(lock.getSignedMessage());
        bluetoothLock.setUserId(lock.getUserId());
        bluetoothLock.setAutoLockActive(lock.isAutoProximityLock());
        bluetoothLock.setAutoUnLockActive(lock.isAutoProximityUnlock());
        bluetoothLock.setAlertMode(alertMapper.mapIn(lock.getAlertMode()));
        if(lock.getActivityClassName()!=null){
            Class activityClass = null;
            try {
                activityClass = Class.forName(lock.getActivityClassName());
                if(activityClass!=null){
                    bluetoothLock.getAlertMode().forActivity(activityClass);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return bluetoothLock;
    }

    @NonNull
    @Override
    public Lock mapOut(@NonNull BluetoothLock bluetoothLock) {
        Lock lock = new Lock();
        lock.setLockId(bluetoothLock.getLockId());
        lock.setMacAddress(bluetoothLock.getMacAddress());
        lock.setMacId(bluetoothLock.getMacId());
        lock.setName(bluetoothLock.getName());
        lock.setSignedMessage(bluetoothLock.getSignedMessage());
        lock.setPublicKey(bluetoothLock.getPublicKey());
        lock.setUserId(bluetoothLock.getUserId());
        lock.setUserId(bluetoothLock.getUserId());
        lock.setUserId(bluetoothLock.getUserId());
        lock.setAutoProximityLock(bluetoothLock.isAutoLockActive());
        lock.setAutoProximityLock(bluetoothLock.isAutoUnLockActive());
        lock.setAlertMode(alertMapper.mapOut(bluetoothLock.getAlertMode()));
        if(bluetoothLock.getAlertMode()!=null){
            if(bluetoothLock.getAlertMode().getActivity()!=null){
                lock.setActivityClassName( bluetoothLock.getAlertMode().getActivity().getName());
            }
        }
        return lock;
    }
}
