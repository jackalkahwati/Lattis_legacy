package com.lattis.domain.usecase.lock.scanner

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.BluetoothRepository
import com.lattis.domain.repository.LockRepository
import com.lattis.domain.usecase.lock.base.BaseLockUseCase
import com.lattis.domain.models.ScannedLock
import com.lattis.domain.repository.SaSOrPSLockRepository
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class ScanForLockUseCase @Inject internal constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    bluetoothRepository: BluetoothRepository,
    lockRepository: LockRepository,
    saSOrPSLockRepository: SaSOrPSLockRepository
) : BaseLockUseCase<ScannedLock>(
    threadExecutor,
    postExecutionThread,
    bluetoothRepository,
    lockRepository,
    saSOrPSLockRepository
) {
    override fun buildUseCaseObservable(): Observable<ScannedLock> {
        return bluetoothRepository.startScan(35000)
    }
}