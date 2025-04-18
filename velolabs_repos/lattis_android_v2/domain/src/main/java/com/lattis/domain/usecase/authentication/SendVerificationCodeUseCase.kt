package com.lattis.domain.usecase.authentication

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.Authenticator
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class SendVerificationCodeUseCase @Inject constructor(
    threadExecutor: ThreadExecutor?,
    postExecutionThread: PostExecutionThread?,
    private val authenticator: Authenticator
) : UseCase<Boolean>(threadExecutor!!, postExecutionThread!!) {
    private var user_id: String? = null
    private var account_type: String? = null
    fun forUser(user_id: String?): SendVerificationCodeUseCase {
        this.user_id = user_id
        return this
    }

    fun forAccountType(account_type: String?): SendVerificationCodeUseCase {
        this.account_type = account_type
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return authenticator.sendVerificationCode(user_id, account_type)
    }

}