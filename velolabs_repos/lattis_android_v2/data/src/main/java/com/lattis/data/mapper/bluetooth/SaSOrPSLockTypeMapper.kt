package com.lattis.data.mapper.bluetooth

import com.lattis.data.mapper.AbstractDataMapper
import com.lattis.domain.models.Lock
import com.lattis.domain.usecase.base.UseCase
import io.lattis.ellipse.sdk.locktype.LockType
import io.lattis.ellipse.sdk.model.Status
import javax.inject.Inject

class SaSOrPSLockTypeMapper @Inject constructor(): AbstractDataMapper<UseCase.LockVendor?,LockType?>() {
    override fun mapOut(lockType: LockType?): UseCase.LockVendor? {
        return when(lockType){
            LockType.SAS -> UseCase.LockVendor.SAS
            LockType.PSLOCK -> UseCase.LockVendor.PSLOCK
            null -> null
        }
    }

    override fun mapIn(out: UseCase.LockVendor?): LockType? {
        return when(out){
            UseCase.LockVendor.PSLOCK -> LockType.PSLOCK
            UseCase.LockVendor.SAS -> LockType.SAS
            else -> {null}
        }
    }
}