package com.lattis.data.mapper.bluetooth

import com.lattis.data.mapper.AbstractDataMapper
import com.lattis.domain.models.Lock
import io.lattis.ellipse.sdk.model.BluetoothLock
import javax.inject.Inject

class BluetoothLockMapper @Inject internal constructor(var alertMapper: AlertMapper) :
    AbstractDataMapper<Lock?, BluetoothLock?>() {
    override fun mapIn(lock: Lock?): BluetoothLock {
        val bluetoothLock = BluetoothLock()
        bluetoothLock.lockId = lock?.lockId
        bluetoothLock.macId = lock?.macId
        bluetoothLock.macAddress = lock?.macAddress
        bluetoothLock.name = lock?.name
        bluetoothLock.publicKey = lock?.publicKey
        bluetoothLock.signedMessage = lock?.signedMessage
        bluetoothLock.userId = lock?.userId
        bluetoothLock.isAutoLockActive = lock?.isAutoProximityLock
        bluetoothLock.isAutoUnLockActive = lock?.isAutoProximityUnlock
        bluetoothLock.alertMode = alertMapper.mapIn(lock?.alertMode)
        if (lock?.activityClassName != null) {
            var activityClass: Class<*>? = null
            try {
                activityClass = Class.forName(lock?.activityClassName!!)
                if (activityClass != null) {
                    bluetoothLock.alertMode.forActivity(activityClass)
                }
            } catch (e: ClassNotFoundException) {

            }
        }
        return bluetoothLock
    }

    override fun mapOut(bluetoothLock: BluetoothLock?): Lock {
        val lock = Lock()
        lock.lockId = bluetoothLock?.lockId
        lock.macAddress = bluetoothLock?.macAddress
        lock.macId = bluetoothLock?.macId
        lock.name = bluetoothLock?.name
        lock.signedMessage = bluetoothLock?.signedMessage
        lock.publicKey = bluetoothLock?.publicKey
        lock.userId = bluetoothLock?.userId
        lock.userId = bluetoothLock?.userId
        lock.userId = bluetoothLock?.userId
        lock.isAutoProximityLock = bluetoothLock?.isAutoLockActive
        lock.isAutoProximityLock = bluetoothLock?.isAutoUnLockActive
        lock.alertMode = alertMapper.mapOut(bluetoothLock?.alertMode)
        if (bluetoothLock?.alertMode != null) {
            if (bluetoothLock?.alertMode.activity != null) {
                lock.activityClassName = bluetoothLock?.alertMode.activity.name
            }
        }
        return lock
    }

}