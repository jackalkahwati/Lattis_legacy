package com.lattis.ellipse.domain.repository;

import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.ScannedLock;

import io.lattis.ellipse.sdk.model.KeyCache;
import io.reactivex.Observable;

public interface BluetoothRepository {

    Observable<ScannedLock> startScan(int scanDuration);
    Observable<Lock.Connection.Status> connectTo(final Lock lock);
    Observable<Boolean> setPosition(Lock lock, boolean locked);
    Observable<Lock.Hardware.Position> observePosition(Lock lock);
    Observable<Lock.Connection.Status> observeLockConnectionState(final Lock lock);
    Observable<Lock.Hardware.State> observeHardwareState(final Lock lock);
    Observable<Boolean> disconnect(final Lock lock);
    Observable<Lock> getLastConnectedLock();
    Observable<Boolean> disconnectAllLocks();
    Observable<Void> blinkLed(final String macAddress);
    Observable<String> getLockFirmwareVersion(final Lock lock);

    Observable<KeyCache> getKeys(String macId);
    Observable<Boolean> addKeys(KeyCache keyCache);
    public Observable<Boolean> isConnectedTo(Lock lock);
}
