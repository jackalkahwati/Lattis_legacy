package com.lattis.domain.usecase.lock.disconnect

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.BluetoothRepository
import com.lattis.domain.repository.LockRepository
import com.lattis.domain.repository.SaSOrPSLockRepository
import com.lattis.domain.usecase.lock.base.BaseLockUseCase
import com.lattis.domain.usecase.lock.connect.ConnectToLockUseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class DisconnectAllLockUseCase @Inject internal constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    bluetoothRepository: BluetoothRepository, lockRepository: LockRepository,
    saSOrPSLockRepository: SaSOrPSLockRepository
) : BaseLockUseCase<Boolean>(
    threadExecutor,
    postExecutionThread,
    bluetoothRepository,
    lockRepository,
    saSOrPSLockRepository
) {

    fun withLockVendor(vendor:LockVendor):DisconnectAllLockUseCase{
        this.lockVendor = vendor
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return disconnectAllLocks(lockVendor)
    }
}