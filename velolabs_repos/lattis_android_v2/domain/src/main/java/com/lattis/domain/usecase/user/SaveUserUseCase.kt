package com.lattis.domain.usecase.user

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.UserRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.User
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class SaveUserUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val userRepository: UserRepository
) : UseCase<User>(threadExecutor, postExecutionThread) {
    private var user: User? = null
    fun withUser(user: User): SaveUserUseCase {
        this.user = user
        return this
    }

    override fun buildUseCaseObservable(): Observable<User> {
        return userRepository.saveUser(user!!)
    }

}