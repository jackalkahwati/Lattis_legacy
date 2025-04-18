package com.lattis.data.repository.implementation.api

import com.lattis.data.mapper.bluetooth.*
import com.lattis.domain.models.KeyCache
import com.lattis.domain.repository.BluetoothRepository
import com.lattis.domain.models.Lock
import com.lattis.domain.models.ScannedLock
import com.lattis.domain.usecase.base.UseCase
import io.lattis.ellipse.sdk.manager.IEllipseManager
import io.lattis.ellipse.sdk.model.Status
import io.reactivex.rxjava3.core.Observable
import java.util.*
import javax.inject.Inject


class BluetoothRepositoryImp @Inject constructor(
    private val ellipseManager: IEllipseManager,
    private val bluetoothLockMapper: BluetoothLockMapper,
    private val bluetoothStateMapper: BluetoothStateMapper,
    private val lockPositionMapper: LockPositionMapper,
    private val hardwareStateMapper: HardwareStateMapper,
    private val saSOrPSLockTypeMapper: SaSOrPSLockTypeMapper
):BluetoothRepository{


    override fun startScan(scanDuration: Int): Observable<ScannedLock> {
        return ellipseManager.startScan(scanDuration)
            .map<ScannedLock>(bluetoothLockMapper::mapOut)
    }


    override fun connectTo(lock: Lock): Observable<Lock.Connection.Status> {
        return ellipseManager.isConnectedTo(bluetoothLockMapper.mapIn(lock))
            .flatMap{ connected ->
                if (connected) {
                    ellipseManager.observeLockState(bluetoothLockMapper.mapIn(lock))
                        .flatMap<Lock.Connection.Status?> { state: Status? ->
                            Observable.just(bluetoothStateMapper.mapIn(state))
                        }
                } else {
                    ellipseManager.disconnectAllLocks()
                        .flatMap { success: Boolean? ->
                            ellipseManager.connect(bluetoothLockMapper.mapIn(lock))
                        }
                        .flatMap<Lock.Connection.Status?> { state: Status? ->
                            Observable.just(bluetoothStateMapper.mapIn(state))
                        }
                }
            }
    }

    override fun isConnectedTo(lock: Lock): Observable<Boolean> {
        return ellipseManager.isConnectedTo(bluetoothLockMapper.mapIn(lock))
    }

    @Synchronized
    override fun setPosition(
        lock: Lock,
        locked: Boolean
    ): Observable<Boolean> {
        return ellipseManager.setPosition(bluetoothLockMapper.mapIn(lock), locked)
    }

    override fun observePosition(lock: Lock): Observable<Lock.Hardware.Position> {
        return ellipseManager.observeLockPosition(bluetoothLockMapper.mapIn(lock))
            .map(lockPositionMapper::mapOut)
    }


    override fun observeHardwareState(lock: Lock): Observable<Lock.Hardware.State> {
        return ellipseManager.observeHardwareState(bluetoothLockMapper.mapIn(lock))
            .map(hardwareStateMapper::mapIn)
    }

    override fun observeLockConnectionState(lock: Lock): Observable<Lock.Connection.Status> {
        return ellipseManager.observeLockState(bluetoothLockMapper.mapIn(lock))
            .map(bluetoothStateMapper::mapIn)
    }

    override fun disconnect(lock: Lock): Observable<Boolean> {
        return ellipseManager.disconnect(bluetoothLockMapper.mapIn(lock))
    }

    override fun getLastConnectedLock(): Observable<Lock>{
        return ellipseManager.lastConnectedLock
            .map(bluetoothLockMapper::mapOut)
    }

    override fun disconnectAllLocks(): Observable<Boolean> {
        return ellipseManager.disconnectAllLocks()
    }


    @Synchronized
    override fun blinkLed(macAddress: String): Observable<Void> {
        return ellipseManager.blinkLed(macAddress)
    }

    override fun getLockFirmwareVersion(lock: Lock): Observable<String> {
        return ellipseManager.getFirmwareVersion(bluetoothLockMapper.mapIn(lock))
            .flatMap { version ->
                val appVersion = version.applicationVersion
                val appReversion = version.applicationRevision
                if (version.applicationRevision < 10) {
                    Observable.just(
                        String.format(
                            Locale.getDefault(),
                            "%d.0%d",
                            version.applicationVersion,
                            version.applicationRevision
                        )
                    )
                } else {
                    Observable.just(
                        String.format(
                            Locale.getDefault(),
                            "%d.%d",
                            version.applicationVersion,
                            version.applicationRevision
                        )
                    )
                }
            }
    }


    override fun getKeys(macId: String): Observable<KeyCache> {
        return ellipseManager.getKeys(macId).map {
            KeyCache(it.signedMessage,it.publicKey)
        }
    }

    override fun addKeys(keyCache: KeyCache): Observable<Boolean> {
        var ellipseKeyCache = io.lattis.ellipse.sdk.model.KeyCache()
        ellipseKeyCache.macId = keyCache.macId
        ellipseKeyCache.publicKey = keyCache.publicKey
        ellipseKeyCache.signedMessage = keyCache.signedMessage
        ellipseKeyCache.time = keyCache.time
        return ellipseManager.addKeys(io.lattis.ellipse.sdk.model.KeyCache())
    }

    //// AXA :start
    override fun getLastConnectedAxaLock(): Observable<Lock> {
        return ellipseManager.lastConnectedAxaLock
            .map(bluetoothLockMapper::mapOut)
    }

    override fun connectToAxa(lock: Lock): Observable<Lock.Connection.Status> {
        return ellipseManager.isConnectedToAxa(bluetoothLockMapper.mapIn(lock))
            .flatMap{ connected ->
                if (connected) {
                    ellipseManager.observeAxaLockState(bluetoothLockMapper.mapIn(lock))
                        .flatMap<Lock.Connection.Status?> { state: Status? ->
                            Observable.just(bluetoothStateMapper.mapIn(state))
                        }
                } else {
                    ellipseManager.disconnectAllAxaLocks()
                        .flatMap { success: Boolean? ->
                            ellipseManager.connectToAxa(bluetoothLockMapper.mapIn(lock))
                        }
                        .flatMap<Lock.Connection.Status?> { state: Status? ->
                            Observable.just(bluetoothStateMapper.mapIn(state))
                        }
                }
            }
    }

    override fun disconnectAllAxaLocks(): Observable<Boolean> {
        return ellipseManager.disconnectAllAxaLocks()
    }

    override fun isConnectedToAxa(lock: Lock): Observable<Boolean> {
        return ellipseManager.isConnectedToAxa(bluetoothLockMapper.mapIn(lock))
    }

    override fun observeAxaPosition(lock: Lock): Observable<Lock.Hardware.Position> {
        return ellipseManager.observeAxaLockPosition(bluetoothLockMapper.mapIn(lock))
            .map(lockPositionMapper::mapOut)
    }

    @Synchronized
    override fun setPositionForAxa(
        lock: Lock,
        locked: Boolean
    ): Observable<Boolean> {
        return ellipseManager.setPositionForAxa(bluetoothLockMapper.mapIn(lock), locked)
    }

    override fun observeAxaHardwareState(lock: Lock): Observable<Lock.Hardware.State> {
        return ellipseManager.observeAxaHardwareState(bluetoothLockMapper.mapIn(lock))
            .map(hardwareStateMapper::mapIn)
    }

    //// Axa :end




    //// Tapkey :start
    override fun getLastConnectedTapkeyLock(): Observable<Lock> {
        return ellipseManager.lastConnectedTapkeyLock
            .map(bluetoothLockMapper::mapOut)
    }

    override fun connectToTapkey(lock: Lock): Observable<Lock.Connection.Status> {
        return ellipseManager.isConnectedToTapkey(bluetoothLockMapper.mapIn(lock))
            .flatMap{ connected ->
                if (connected) {
                    ellipseManager.observeTapkeyLockState(bluetoothLockMapper.mapIn(lock))
                        .flatMap<Lock.Connection.Status?> { state: Status? ->
                            Observable.just(bluetoothStateMapper.mapIn(state))
                        }
                } else {
                    ellipseManager.disconnectAllTapkeyLocks()
                        .flatMap { success: Boolean? ->
                            ellipseManager.connectToTapkey(bluetoothLockMapper.mapIn(lock))
                        }
                        .flatMap<Lock.Connection.Status?> { state: Status? ->
                            Observable.just(bluetoothStateMapper.mapIn(state))
                        }
                }
            }
    }

    override fun disconnectAllTapkeyLocks(): Observable<Boolean> {
        return ellipseManager.disconnectAllTapkeyLocks()
    }

    override fun isConnectedToTapkey(lock: Lock): Observable<Boolean> {
        return ellipseManager.isConnectedToTapkey(bluetoothLockMapper.mapIn(lock))
    }

    override fun observeTapkeyPosition(lock: Lock): Observable<Lock.Hardware.Position> {
        return ellipseManager.observeTapkeyLockPosition(bluetoothLockMapper.mapIn(lock))
            .map(lockPositionMapper::mapOut)
    }

    @Synchronized
    override fun setPositionForTapkey(
        lock: Lock,
        locked: Boolean
    ): Observable<Boolean> {
        return ellipseManager.setPositionForTapkey(bluetoothLockMapper.mapIn(lock), locked)
    }

    override fun observeTapkeyHardwareState(lock: Lock): Observable<Lock.Hardware.State> {
        return ellipseManager.observeTapkeyHardwareState(bluetoothLockMapper.mapIn(lock))
            .map(hardwareStateMapper::mapIn)
    }

    //// Tapkey :end


    //// SAS  PSLOCK :start
    override fun getLastConnectedSaSOrPSLock(vendor: UseCase.LockVendor): Observable<Lock> {
        return ellipseManager.getLastConnectedSASOrPSLOCK(saSOrPSLockTypeMapper.mapIn(vendor))
            .map(bluetoothLockMapper::mapOut)
    }

    override fun connectToSaSOrPSLock(
        lock: Lock,
        vendor: UseCase.LockVendor
    ): Observable<Lock.Connection.Status> {
        return ellipseManager.isConnectedToSASOrPSLOCK(bluetoothLockMapper.mapIn(lock),saSOrPSLockTypeMapper.mapIn(vendor))
            .flatMap{ connected ->
                if (connected) {
                    ellipseManager.observeSASOrPSLOCKState(bluetoothLockMapper.mapIn(lock),saSOrPSLockTypeMapper.mapIn(vendor))
                        .flatMap<Lock.Connection.Status?> { state: Status? ->
                            Observable.just(bluetoothStateMapper.mapIn(state))
                        }
                } else {
                    ellipseManager.disconnectAllSASOrPSLOCKs(saSOrPSLockTypeMapper.mapIn(vendor))
                        .flatMap { success: Boolean? ->
                            ellipseManager.connectToSASOrPSLOCK(bluetoothLockMapper.mapIn(lock),saSOrPSLockTypeMapper.mapIn(vendor))
                        }
                        .flatMap<Lock.Connection.Status?> { state: Status? ->
                            Observable.just(bluetoothStateMapper.mapIn(state))
                        }
                }
            }
    }

    override fun disconnectAllSaSOrPSLocks(vendor: UseCase.LockVendor): Observable<Boolean> {
        return ellipseManager.disconnectAllSASOrPSLOCKs(saSOrPSLockTypeMapper.mapIn(vendor))
    }

    override fun isConnectedToSaSOrPSLock(
        lock: Lock,
        vendor: UseCase.LockVendor
    ): Observable<Boolean> {
        return ellipseManager.isConnectedToSASOrPSLOCK(bluetoothLockMapper.mapIn(lock),saSOrPSLockTypeMapper.mapIn(vendor))
    }

    override fun observeSaSOrPSLockPosition(
        lock: Lock,
        vendor: UseCase.LockVendor
    ): Observable<Lock.Hardware.Position> {
        return ellipseManager.observeSASOrPSLOCKPosition(bluetoothLockMapper.mapIn(lock),saSOrPSLockTypeMapper.mapIn(vendor))
            .map(lockPositionMapper::mapOut)
    }

    override fun observeSaSOrPSLockHardwareState(
        lock: Lock,
        vendor: UseCase.LockVendor
    ): Observable<Lock.Hardware.State> {
        return ellipseManager.observeSASOrPSLOCKHardwareState(bluetoothLockMapper.mapIn(lock),saSOrPSLockTypeMapper.mapIn(vendor))
            .map(hardwareStateMapper::mapIn)
    }

    override fun setPositionForSaSOrPSLock(
        lock: Lock,
        locked: Boolean,
        vendor: UseCase.LockVendor
    ): Observable<Boolean> {
        return ellipseManager.setPositionForSASOrPSLOCK(bluetoothLockMapper.mapIn(lock), locked,saSOrPSLockTypeMapper.mapIn(vendor))
    }

    override fun getNonceTokenForSasOrPSLock(
        lock: Lock,
        vendor: UseCase.LockVendor
    ): Observable<String> {
        return ellipseManager.getNonceTokenForSasOrPSLock(bluetoothLockMapper.mapIn(lock),saSOrPSLockTypeMapper.mapIn(vendor))
    }


    //// SAS  PSLOCK :end
}