package com.lattis.ellipse.bluetooth;

import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.ScannedLock;
import com.lattis.ellipse.domain.repository.BluetoothRepository;

import java.util.Locale;

import javax.inject.Inject;

import io.lattis.ellipse.sdk.Ellipse;
import io.lattis.ellipse.sdk.manager.IEllipseManager;
import io.lattis.ellipse.sdk.model.KeyCache;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class LattisBluetoothRepository implements BluetoothRepository {

        private final IEllipseManager ellipseManager;
        private final BluetoothLockMapper bluetoothLockMapper;
        private final BluetoothStateMapper bluetoothStateMapper;
        private final LockPositionMapper lockPositionMapper;
        private final HardwareStateMapper hardwareStateMapper;


        @Inject
        public LattisBluetoothRepository(IEllipseManager ellipseManager,
                                         BluetoothLockMapper bluetoothLockMapper,
                                         BluetoothStateMapper bluetoothStateMapper,
                                         LockPositionMapper lockPositionMapper,
                                         HardwareStateMapper hardwareStateMapper
                                         ) {
            this.ellipseManager = ellipseManager;
            this.bluetoothLockMapper = bluetoothLockMapper;
            this.bluetoothStateMapper = bluetoothStateMapper;
            this.lockPositionMapper = lockPositionMapper;
            this.hardwareStateMapper = hardwareStateMapper;
        }

        @Override
        public Observable<ScannedLock> startScan(int scanDuration) {
            return ellipseManager.startScan(scanDuration)
                    .map(bluetoothLockMapper::mapOut);
        }


    @Override
    public Observable<Lock.Connection.Status> connectTo(Lock lock) {
        return ellipseManager.isConnectedTo(bluetoothLockMapper.mapIn(lock))
                .flatMap(new Function<Boolean, Observable<Lock.Connection.Status>>() {
                    @Override
                    public Observable<Lock.Connection.Status> apply(Boolean connected) {
                        if(connected){
                            return ellipseManager.observeLockState(bluetoothLockMapper.mapIn(lock))
                                    .flatMap(state -> Observable.just(bluetoothStateMapper.mapIn(state)));
                        } else {
                            return ellipseManager.disconnectAllLocks()
                                    .flatMap( success -> ellipseManager.connect(bluetoothLockMapper.mapIn(lock)))
                                    .flatMap(state -> Observable.just(bluetoothStateMapper.mapIn(state)));
                        }
                    }
                });
    }

    @Override
    public Observable<Boolean> isConnectedTo(Lock lock) {
        return ellipseManager.isConnectedTo(bluetoothLockMapper.mapIn(lock));
    }


    @Override
    public synchronized Observable<Boolean> setPosition(Lock lock, boolean locked) {
        return ellipseManager.setPosition(bluetoothLockMapper.mapIn(lock), locked);
    }

    @Override
    public Observable<Lock.Hardware.Position> observePosition(Lock lock) {
        return ellipseManager.observeLockPosition(bluetoothLockMapper.mapIn(lock))
                .map(lockPositionMapper::mapOut);
    }


    @Override
    public Observable<Lock.Hardware.State> observeHardwareState(Lock lock) {
        return ellipseManager.observeHardwareState(bluetoothLockMapper.mapIn(lock))
                .map(hardwareStateMapper::mapIn);
    }

    @Override
    public Observable<Lock.Connection.Status> observeLockConnectionState(Lock lock) {
        return ellipseManager.observeLockState(bluetoothLockMapper.mapIn(lock))
                .map(bluetoothStateMapper::mapIn);
    }

    @Override
    public Observable<Boolean> disconnect(Lock lock) {
        return ellipseManager.disconnect(bluetoothLockMapper.mapIn(lock));
    }

    @Override
    public Observable<Lock> getLastConnectedLock() {
        return ellipseManager.getLastConnectedLock()
                .map(bluetoothLockMapper::mapOut);
    }

    @Override
    public Observable<Boolean> disconnectAllLocks() {
        return ellipseManager.disconnectAllLocks();
    }


    @Override
    public synchronized Observable<Void> blinkLed(String macAddress) {
        return ellipseManager.blinkLed(macAddress);
    }

    @Override
    public Observable<String> getLockFirmwareVersion(final Lock lock) {
        return ellipseManager.getFirmwareVersion(bluetoothLockMapper.mapIn(lock))
                .flatMap(new Function<Ellipse.Boot.Version, Observable<String>>() {
                    @Override
                    public Observable<String> apply(Ellipse.Boot.Version version) {
                        int appVersion = version.getApplicationVersion();
                        int appReversion = version.getApplicationRevision();

                        if(version.getApplicationRevision()<10){
                            return Observable.just(String.format(Locale.getDefault(),"%d.0%d", version.getApplicationVersion(), version.getApplicationRevision()));
                        }else{
                            return Observable.just(String.format(Locale.getDefault(),"%d.%d", version.getApplicationVersion(), version.getApplicationRevision()));
                        }

                    }
                });
    }


    @Override
    public Observable<KeyCache> getKeys(String macId) {
        return ellipseManager.getKeys(macId);
    }

    @Override
    public Observable<Boolean> addKeys(KeyCache keyCache) {
        return ellipseManager.addKeys(keyCache);
    }
}
