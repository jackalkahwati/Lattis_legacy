package com.lattis.domain.usecase.authentication

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.UserRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class ResetPasswordUseCase @Inject protected constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val userRepository: UserRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    private var email: String? = null
    private var confirmation_code: String? = null
    private var password: String? = null

    fun withValues(
        email: String,
        confirmation_code: String,
        password: String
    ): ResetPasswordUseCase {
        this.email = email
        this.confirmation_code = confirmation_code
        this.password = password
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return userRepository.confirmCodeForForgotPassword(email!!, confirmation_code!!, password!!)
    }

}