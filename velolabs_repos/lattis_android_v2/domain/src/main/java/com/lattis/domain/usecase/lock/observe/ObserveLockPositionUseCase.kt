package com.lattis.domain.usecase.lock.observe

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


class ObserveLockPositionUseCase @Inject internal constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    bluetoothRepository: BluetoothRepository,
    lockRepository: LockRepository,
    saSOrPSLockRepository: SaSOrPSLockRepository
) : BaseLockUseCase<Lock.Hardware.Position>(
    threadExecutor,
    postExecutionThread,
    bluetoothRepository,
    lockRepository,
    saSOrPSLockRepository
) {

    fun withLockVendor(vendor:LockVendor): ObserveLockPositionUseCase {
        this.lockVendor = vendor
        return this
    }

    fun forLock(lock: Lock): ObserveLockPositionUseCase {
        setScannedLock(lock)
        return this
    }

    override fun buildUseCaseObservable(): Observable<Lock.Hardware.Position> {
        return lockPositionObservable(lockVendor)
    }
}