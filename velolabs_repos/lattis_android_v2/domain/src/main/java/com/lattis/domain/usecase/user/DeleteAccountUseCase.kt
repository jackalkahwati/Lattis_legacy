package com.lattis.domain.usecase.user

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.UserRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class DeleteAccountUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val userRepository: UserRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return userRepository.deleteAccount()
    }
}