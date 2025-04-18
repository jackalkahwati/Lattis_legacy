package com.lattis.domain.usecase.user

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.UserRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class SendCodeToPhoneNumberUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    val userRepository: UserRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    private var countryCode: String? = null
    private var phoneNumber: String? = null

    fun withCountryCode(countryCode: String): SendCodeToPhoneNumberUseCase {
        this.countryCode = countryCode
        return this
    }

    fun withPhoneNumber(phoneNumber: String?): SendCodeToPhoneNumberUseCase {
        this.phoneNumber = phoneNumber
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return userRepository.sendCodeToUpdatePhoneNumber(countryCode!!, phoneNumber!!.replace("\\s".toRegex(), ""))
    }
}