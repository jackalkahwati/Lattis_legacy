package com.lattis.domain.usecase.user

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.UserRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class ChangePasswordUseCase @Inject protected constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val userRepository: UserRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    private var password: String? = null
    private var new_password: String? = null
    fun withPassword(password: String?): ChangePasswordUseCase {
        this.password = password
        return this
    }

    fun withNewPassword(newPassword: String?): ChangePasswordUseCase {
        new_password = newPassword
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return userRepository.changePassword(password!!, new_password!!)
    }

}