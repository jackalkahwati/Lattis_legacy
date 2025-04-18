package com.lattis.domain.usecase.lock.firmware

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.BluetoothRepository
import com.lattis.domain.repository.LockRepository
import com.lattis.domain.usecase.lock.base.BaseLockUseCase
import com.lattis.domain.models.Lock
import com.lattis.domain.repository.SaSOrPSLockRepository
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetLockFirmwareVersionCase @Inject internal constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    bluetoothRepository: BluetoothRepository, lockRepository: LockRepository,
    saSOrPSLockRepository: SaSOrPSLockRepository
) : BaseLockUseCase<String>(
    threadExecutor,
    postExecutionThread,
    bluetoothRepository,
    lockRepository,
    saSOrPSLockRepository
) {
    private val TAG = GetLockFirmwareVersionCase::class.java.name
    fun forLock(lock: Lock): GetLockFirmwareVersionCase {
        setScannedLock(lock)
        return this
    }

    override fun buildUseCaseObservable(): Observable<String> {
        return lockFirmwareVersion
    }
}