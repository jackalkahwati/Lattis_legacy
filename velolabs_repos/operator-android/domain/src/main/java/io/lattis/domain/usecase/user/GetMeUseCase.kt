package io.lattis.domain.usecase.user

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.models.User
import io.lattis.domain.repository.UserRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetMeUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val userRepository: UserRepository
) : UseCase<User.Operator>(threadExecutor, postExecutionThread) {


    override fun buildUseCaseObservable(): Observable<User.Operator> {
        return userRepository.getMe()
    }

}