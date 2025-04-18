package com.lattis.domain.usecase.user

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.UserRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class ValidateCodeForChangePhoneNumberUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    val userRepository: UserRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    private var code: String? = null
    private var phoneNumber: String? = null
    private val countryCode: String? = null
    fun withCode(code: String?): ValidateCodeForChangePhoneNumberUseCase {
        this.code = code
        return this
    }

    fun withPhoneNumber(phoneNumber: String?): ValidateCodeForChangePhoneNumberUseCase {
        this.phoneNumber = phoneNumber
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return userRepository.validateCodeForChangePhoneNumber(code!!, phoneNumber!!.replace("\\s".toRegex(), ""))
    }
}