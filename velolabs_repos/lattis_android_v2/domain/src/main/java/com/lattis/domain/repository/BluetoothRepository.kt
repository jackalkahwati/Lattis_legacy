package com.lattis.domain.repository

import com.lattis.domain.models.KeyCache
import com.lattis.domain.models.Lock
import com.lattis.domain.models.ScannedLock
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable

interface BluetoothRepository {
    fun startScan(scanDuration: Int): Observable<ScannedLock>
    fun connectTo(lock: Lock): Observable<Lock.Connection.Status>
    fun setPosition(
        lock: Lock,
        locked: Boolean
    ): Observable<Boolean>

    fun observePosition(lock: Lock): Observable<Lock.Hardware.Position>
    fun observeLockConnectionState(lock: Lock): Observable<Lock.Connection.Status>
    fun observeHardwareState(lock: Lock): Observable<Lock.Hardware.State>
    fun disconnect(lock: Lock): Observable<Boolean>
    fun getLastConnectedLock(): Observable<Lock>

    fun disconnectAllLocks(): Observable<Boolean>
    fun blinkLed(macAddress: String): Observable<Void>
    fun getLockFirmwareVersion(lock: Lock): Observable<String>
    fun getKeys(macId: String): Observable<KeyCache>
    fun addKeys(keyCache: KeyCache): Observable<Boolean>
    fun isConnectedTo(lock: Lock): Observable<Boolean>

    //// AXA :start
    fun getLastConnectedAxaLock(): Observable<Lock>
    fun connectToAxa(lock: Lock): Observable<Lock.Connection.Status>
    fun disconnectAllAxaLocks(): Observable<Boolean>
    fun isConnectedToAxa(lock: Lock): Observable<Boolean>
    fun observeAxaPosition(lock: Lock): Observable<Lock.Hardware.Position>
    fun observeAxaHardwareState(lock: Lock): Observable<Lock.Hardware.State>
    fun setPositionForAxa(
        lock: Lock,
        locked: Boolean
    ): Observable<Boolean>
    //// AXA :end


    //// Tapkey :start
    fun getLastConnectedTapkeyLock(): Observable<Lock>
    fun connectToTapkey(lock: Lock): Observable<Lock.Connection.Status>
    fun disconnectAllTapkeyLocks(): Observable<Boolean>
    fun isConnectedToTapkey(lock: Lock): Observable<Boolean>
    fun observeTapkeyPosition(lock: Lock): Observable<Lock.Hardware.Position>
    fun observeTapkeyHardwareState(lock: Lock): Observable<Lock.Hardware.State>
    fun setPositionForTapkey(
        lock: Lock,
        locked: Boolean
    ): Observable<Boolean>
    //// Tapkey :end


    //// SAS  PSLOCK :start
    fun getLastConnectedSaSOrPSLock(vendor: UseCase.LockVendor): Observable<Lock>
    fun connectToSaSOrPSLock(lock: Lock,vendor: UseCase.LockVendor): Observable<Lock.Connection.Status>
    fun disconnectAllSaSOrPSLocks(vendor: UseCase.LockVendor): Observable<Boolean>
    fun isConnectedToSaSOrPSLock(lock: Lock,vendor: UseCase.LockVendor): Observable<Boolean>
    fun observeSaSOrPSLockPosition(lock: Lock,vendor: UseCase.LockVendor): Observable<Lock.Hardware.Position>
    fun observeSaSOrPSLockHardwareState(lock: Lock,vendor: UseCase.LockVendor): Observable<Lock.Hardware.State>
    fun setPositionForSaSOrPSLock(
        lock: Lock,
        locked: Boolean,
        vendor: UseCase.LockVendor
    ): Observable<Boolean>

    fun getNonceTokenForSasOrPSLock(lock: Lock, vendor: UseCase.LockVendor): Observable<String>
    //// SAS  PSLOCK :end




}