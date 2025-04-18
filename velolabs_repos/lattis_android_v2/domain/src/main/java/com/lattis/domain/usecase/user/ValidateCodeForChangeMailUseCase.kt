package com.lattis.domain.usecase.user

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.UserRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class ValidateCodeForChangeMailUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val userRepository: UserRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    private var code: String? = null
    private var email: String? = null
    fun withCode(code: String?): ValidateCodeForChangeMailUseCase {
        this.code = code
        return this
    }

    fun withEmail(email: String?): ValidateCodeForChangeMailUseCase {
        this.email = email
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return userRepository.validateCodeForEmailChange(code!!, email!!)
    }

}