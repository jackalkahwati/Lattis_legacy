package com.lattis.data.database.mapper

import android.util.Log
import com.lattis.data.database.base.AbstractRealmDataMapper
import com.lattis.data.database.model.RealmLock
import com.lattis.domain.models.Alert
import com.lattis.domain.models.Lock
import com.lattis.domain.models.Version
import javax.inject.Inject
import javax.inject.Named

class RealmLockMapper @Inject constructor(
    private val locationMapper: RealmLocationMapper,
    @param:Named("UUID") private val fleetId: String) :
    AbstractRealmDataMapper<Lock, RealmLock>() {
    override fun mapIn(lock: Lock): RealmLock {
        val realmLock = RealmLock()
        realmLock.id = fleetId
        realmLock.lockId = lock.lockId
        realmLock.macId = lock.macId
        realmLock.macAddress = lock.macAddress
        realmLock.name = lock.name
        realmLock.serialNumber = lock.serialNumber
        realmLock.publicKey = lock.publicKey
        realmLock.userId = lock.userId
        realmLock.signedMessage = lock.signedMessage
        realmLock.usersId = lock.usersId
        realmLock.shareId = lock.shareId
        realmLock.shareWithUserId = lock.sharedWithUserId
        realmLock.isSharedWithMe = lock.isSharedWithMe
        realmLock.isSharedWithOther = lock.isSharedWithOther
        realmLock.isLocked = lock.isLocked
        realmLock.lockedDate = lock.lockedDate
        realmLock.connectedDate = lock.connectedDate
        realmLock.isAutoProximityLock = lock.isAutoProximityLock
        realmLock.isAutoProximityUnlock = lock.isAutoProximityUnlock
        realmLock.isDefaultPinCode = (lock.isDefaultPinCode)
        if (lock.version != null) {
            realmLock.version = lock.version!!.version.toString()
            realmLock.revision = lock.version!!.revision.toString()
        }
        if (lock.alertMode != null) {
            realmLock.alertMode = lock.alertMode!!.name
        } else {
            realmLock.alertMode = Alert.OFF.name
        }
        if (lock.lastLocation != null) {
            realmLock.lastLocation = locationMapper.mapIn(lock.lastLocation!!)
        }
        Log.e("RealmLockMapper", "RealmLock: $realmLock")
        return realmLock
    }

    override fun mapOut(realmLock: RealmLock): Lock {
        val lock = Lock()
        lock.lockId = realmLock.lockId
        lock.macId = realmLock.macId
        lock.macAddress = realmLock.macAddress
        lock.name = realmLock.name
        lock.userId = realmLock.userId
        lock.serialNumber = realmLock.serialNumber
        lock.publicKey = realmLock.publicKey
        lock.signedMessage = realmLock.signedMessage
        lock.userId = realmLock.userId
        lock.usersId = realmLock.usersId
        lock.shareId = realmLock.shareId
        lock.sharedWithUserId = realmLock.shareWithUserId
        lock.isSharedWithMe=(realmLock.isSharedWithMe)
        lock.isSharedWithOther=(realmLock.isSharedWithOther)
        lock.isLocked = realmLock.isLocked
        lock.lockedDate = realmLock.lockedDate
        lock.connectedDate = realmLock.connectedDate
        lock.isAutoProximityLock = realmLock.isAutoProximityLock
        lock.isAutoProximityUnlock = realmLock.isAutoProximityUnlock
        lock.isDefaultPinCode=(realmLock.isDefaultPinCode)
        if (realmLock.version != null && realmLock.revision != null) {
            lock.version = Version(
                Integer.valueOf(realmLock.version!!),
                Integer.valueOf(realmLock.revision!!)
            )
        }
        if (realmLock.alertMode != null) {
            lock.alertMode = Alert.forValue(realmLock.alertMode!!)
        } else {
            lock.alertMode = Alert.OFF
        }
        if (realmLock.lastLocation != null) {
            lock.lastLocation = locationMapper.mapOut(realmLock.lastLocation!!)
        }
        Log.e("RealmLockMapper", "Lock: $lock")
        return lock
    }

}