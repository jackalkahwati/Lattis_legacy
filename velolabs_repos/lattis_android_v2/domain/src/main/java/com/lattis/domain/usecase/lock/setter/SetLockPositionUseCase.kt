package com.lattis.domain.usecase.lock.setter

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.BluetoothRepository
import com.lattis.domain.repository.LockRepository
import com.lattis.domain.usecase.lock.base.BaseLockUseCase
import com.lattis.domain.models.Lock
import com.lattis.domain.repository.SaSOrPSLockRepository
import com.lattis.domain.usecase.lock.connect.ConnectToLockUseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class SetLockPositionUseCase @Inject internal constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    bluetoothRepository: BluetoothRepository, lockRepository: LockRepository,
    saSOrPSLockRepository: SaSOrPSLockRepository
) : BaseLockUseCase<Boolean>(
    threadExecutor,
    postExecutionThread,
    bluetoothRepository!!,
    lockRepository!!,
    saSOrPSLockRepository
) {
    private var locked = false
    private var fleetId:Int?=null

    fun withLockVendor(vendor:LockVendor): SetLockPositionUseCase {
        this.lockVendor = vendor
        return this
    }

    fun withState(locked: Boolean): SetLockPositionUseCase {
        this.locked = locked
        return this
    }

    fun forLock(lock: Lock): SetLockPositionUseCase {
        setScannedLock(lock)
        return this
    }

    fun withFleetId(fleetId: Int?): SetLockPositionUseCase {
        this.fleetId = fleetId
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return setPosition(lockVendor,locked,fleetId)
    }
}