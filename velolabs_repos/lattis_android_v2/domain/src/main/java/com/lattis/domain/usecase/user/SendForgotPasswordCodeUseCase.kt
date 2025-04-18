package com.lattis.domain.usecase.user

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.UserRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class SendForgotPasswordCodeUseCase @Inject protected constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val userRepository: UserRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    private var email: String? = null
    fun toEmail(email: String?): SendForgotPasswordCodeUseCase {
        this.email = email
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return userRepository.sendForgotPasswordCode(email!!)
    }

}