package com.lattis.lattis.uimodel.mapper

import com.lattis.domain.mapper.base.AbstractDataMapper
import com.lattis.domain.models.Lock
import com.lattis.domain.models.ScannedLock
import com.lattis.lattis.uimodel.model.LockModel
import javax.inject.Inject

class ScannedLockModelMapper @Inject constructor() :
    AbstractDataMapper<ScannedLock, LockModel>() {
    override fun mapIn(lock: ScannedLock?): LockModel {
        val lockModel = LockModel()
        lockModel.macId = lock?.macId
        lockModel.macAddress = lock?.macAddress
        lockModel.name = lock?.name
        return lockModel
    }

    override fun mapOut(lockModel: LockModel?): ScannedLock {
        val lock = Lock()
        lock.macId = lockModel?.macId
        lock.macAddress = lockModel?.macAddress
        lock.name = lockModel?.name
        return lock
    }
}