package com.lattis.domain.usecase.user


import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.UserRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.User
import javax.inject.Inject

import io.reactivex.rxjava3.core.Observable

class GetUserUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val userRepository: UserRepository
) : UseCase<User>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(): Observable<User> {
        return userRepository.getUser()
    }
}
