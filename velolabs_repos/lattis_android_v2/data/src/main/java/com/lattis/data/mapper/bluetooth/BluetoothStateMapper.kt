package com.lattis.data.mapper.bluetooth

import com.lattis.data.mapper.AbstractDataMapper
import com.lattis.domain.models.Lock
import io.lattis.ellipse.sdk.model.Status
import javax.inject.Inject

class BluetoothStateMapper @Inject internal constructor(private val bluetoothLockMapper: BluetoothLockMapper) :
    AbstractDataMapper<Status?, Lock.Connection.Status?>() {
    override fun mapIn(status: Status?): Lock.Connection.Status {
        var lock: Lock? = null
        if (status?.bluetoothLock != null) {
            lock = bluetoothLockMapper.mapOut(status.bluetoothLock)
        }
        when (status) {
            Status.SCANNING -> return Lock.Connection.Status.SCANNING
            Status.DEVICE_FOUND -> return Lock.Connection.Status.DEVICE_FOUND.forLock(
                lock
            )
            Status.DISCOVER_SERVICE -> return Lock.Connection.Status.DISCOVER_SERVICE.forLock(
                lock
            )
            Status.DISCONNECTED -> return Lock.Connection.Status.DISCONNECTED.forLock(
                lock
            )
            Status.SERVICE_DISCOVERED -> return Lock.Connection.Status.SERVICE_DISCOVERED.forLock(
                lock
            )
            Status.OWNER_REQUEST -> return Lock.Connection.Status.OWNER_REQUEST.forLock(
                lock
            )
            Status.GUEST_REQUEST -> return Lock.Connection.Status.GUEST_REQUEST.forLock(
                lock
            )
            Status.OWNER_VERIFIED -> return Lock.Connection.Status.OWNER_VERIFIED.forLock(
                lock
            )
            Status.GUEST_VERIFIED -> return Lock.Connection.Status.GUEST_VERIFIED.forLock(
                lock
            )
            Status.FIRMWARE_VERSION -> {
            }
            Status.UPDATING_FIRMWARE -> return Lock.Connection.Status.UPDATING_FIRMWARE.forLock(
                lock
            )
            Status.ERROR -> return Lock.Connection.Status.ERROR.forLock(
                lock
            )
            Status.ACCESS_DENIED -> return Lock.Connection.Status.ACCESS_DENIED.forLock(
                lock
            )
            else -> return Lock.Connection.Status.DISCONNECTED
        }
        return Lock.Connection.Status.DISCONNECTED
    }

    override fun mapOut(status: Lock.Connection.Status?): Status {
        return Status.ERROR
    }

}