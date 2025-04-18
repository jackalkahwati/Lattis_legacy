package com.lattis.domain.usecase.user

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.AccountRepository
import com.lattis.domain.repository.Authenticator
import com.lattis.domain.repository.UserRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class ConfirmVerificationForPrivateNetworkUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val userRepository: UserRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    private var confirmationCode: String? = null
    private var user_id: String? = null
    private var account_type: String? = null
    fun forUser(user_id: String?): ConfirmVerificationForPrivateNetworkUseCase {
        this.user_id = user_id
        return this
    }

    fun forAccountType(account_type: String?): ConfirmVerificationForPrivateNetworkUseCase {
        this.account_type = account_type
        return this
    }

    fun withConfirmationCode(confirmationCode: String?): ConfirmVerificationForPrivateNetworkUseCase {
        this.confirmationCode = confirmationCode
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return userRepository.confirmVerificationCodeForPrivateNetwork(
            user_id!!,
            account_type!!,
            confirmationCode!!
        )
    }

}