package com.lattis.domain.usecase.lock.realm

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.LockRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Lock
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class SaveLockUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val lockRepository: LockRepository
) : UseCase<Lock>(threadExecutor, postExecutionThread) {
    private lateinit var lock: Lock
    fun withLock(lock: Lock): SaveLockUseCase {
        this.lock = lock
        return this
    }

    override fun buildUseCaseObservable(): Observable<Lock> {
        return lockRepository.createOrUpdateLock(lock)
    }

}