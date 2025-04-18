package com.lattis.lattis.uimodel.mapper

import com.lattis.domain.mapper.base.AbstractDataMapper
import com.lattis.domain.models.Lock
import com.lattis.domain.models.Version
import com.lattis.lattis.uimodel.model.LockModel
import javax.inject.Inject

class LockModelMapper @Inject constructor() :
    AbstractDataMapper<Lock, LockModel>() {
    override fun mapIn(lock: Lock?): LockModel {
        val lockModel = LockModel()
        lockModel.lockId = lock?.lockId
        lockModel.macId = lock?.macId
        lockModel.macAddress = lock?.macAddress
        lockModel.userId = lock?.userId
        lockModel.serialNumber = lock?.serialNumber
        lockModel.usersId = lock?.usersId
        lockModel.shareId = lock?.shareId
        lockModel.sharedWithUserId = lock?.sharedWithUserId
        lockModel.isSharedWithMe = lock?.isSharedWithMe
        lockModel.isSharedWithOther = lock?.isSharedWithOther
        lockModel.lastLocation = lock?.lastLocation
        lockModel.isLocked = lock?.isLocked
        lockModel.lockedDate = lock?.lockedDate
        lockModel.name = lock?.name
        lockModel.alert = lock?.alertMode
        lockModel.isAutoProximityLock = lock?.isAutoProximityLock
        lockModel.isAutoProximityUnlock = lock?.isAutoProximityUnlock
        lockModel.connectedDate = lock?.connectedDate
        lockModel.isUseDefaultPinCode = lock?.isDefaultPinCode
        lockModel.signedMessage = lock?.signedMessage
        lockModel.publicKey = lock?.publicKey
        if (lock?.version != null) {
            lockModel.version = lock?.version!!.version.toString() + "." + lock?.version!!.revision
        }
        return lockModel
    }

    override fun mapOut(lockModel: LockModel?): Lock {
        val lock = Lock()
        lock.lockId = lockModel?.lockId
        lock.macId = lockModel?.macId
        lock.macAddress = lockModel?.macAddress
        lock.userId = lockModel?.userId
        lock.name = lockModel?.name
        lock.serialNumber = lockModel?.serialNumber
        lock.usersId = lockModel?.usersId
        lock.shareId = lockModel?.shareId
        lock.sharedWithUserId = lockModel?.sharedWithUserId
        lock.isSharedWithMe = lockModel?.isSharedWithMe
        lock.isSharedWithOther = lockModel?.isSharedWithOther
        lock.lastLocation = lockModel?.lastLocation
        lock.isLocked = lockModel?.isLocked
        lock.lockedDate = lockModel?.lockedDate
        lock.alertMode = lockModel?.alert
        lock.isDefaultPinCode=lockModel?.isUseDefaultPinCode
        lock.isAutoProximityLock = lockModel?.isAutoProximityLock
        lock.isAutoProximityUnlock = lockModel?.isAutoProximityUnlock
        lock.connectedDate = lockModel?.connectedDate
        lock.signedMessage = lockModel?.signedMessage
        lock.publicKey = lockModel?.publicKey
        if (lockModel?.version != null) {
            val version = lockModel?.version!!.split("\\.").toTypedArray()
            lock.version = Version(
                Integer.valueOf(version[0]),
                Integer.valueOf(version[1])
            )
        }
        return lock
    }
}