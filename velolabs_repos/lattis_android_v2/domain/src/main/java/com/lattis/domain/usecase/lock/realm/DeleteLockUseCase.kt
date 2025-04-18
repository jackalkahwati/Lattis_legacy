package com.lattis.domain.usecase.lock.realm

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.LockRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class DeleteLockUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val lockRepository: LockRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(): Observable<Boolean> {
        return lockRepository.deleteLock()
    }

}