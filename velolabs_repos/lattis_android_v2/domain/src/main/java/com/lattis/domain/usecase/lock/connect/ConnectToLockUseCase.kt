package com.lattis.domain.usecase.lock.connect

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.BluetoothRepository
import com.lattis.domain.repository.LockRepository
import com.lattis.domain.usecase.lock.base.BaseLockUseCase
import com.lattis.domain.models.Lock
import com.lattis.domain.repository.SaSOrPSLockRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableObserver
import javax.inject.Inject

open class ConnectToLockUseCase @Inject internal constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    bluetoothRepository: BluetoothRepository, lockRepository: LockRepository,
    saSOrPSLockRepository: SaSOrPSLockRepository
) : BaseLockUseCase<Lock.Connection.Status>(
    threadExecutor,
    postExecutionThread,
    bluetoothRepository,
    lockRepository,
    saSOrPSLockRepository
) {
    override fun buildUseCaseObservable(): Observable<Lock.Connection.Status> {
        return super.connectToLock(lockVendor)
    }

    fun withLockVendor(vendor:LockVendor):ConnectToLockUseCase{
        this.lockVendor = vendor
        return this
    }



    fun execute(
        lock: Lock,
        useCaseSubscriber: DisposableObserver<Lock.Connection.Status>
    ): Disposable {
        setScannedLock(lock!!)
        return super.execute(useCaseSubscriber)
    }
}