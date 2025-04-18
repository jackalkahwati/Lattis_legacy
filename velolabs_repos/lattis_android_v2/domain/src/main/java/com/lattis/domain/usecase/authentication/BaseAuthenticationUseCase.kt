package com.lattis.domain.usecase.authentication


import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.AccountRepository
import com.lattis.domain.repository.Authenticator
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.utils.StringUtils
import com.lattis.domain.models.VerificationBundle
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Function

abstract class BaseAuthenticationUseCase(threadExecutor: ThreadExecutor,
                                         postExecutionThread: PostExecutionThread,
                                         protected var authenticator: Authenticator,
                                         private val accountRepository: AccountRepository
) : UseCase<VerificationBundle>(threadExecutor, postExecutionThread) {

    protected var email: String? = null
    protected var firstName: String? = null
    protected var lastName: String? = null
    protected var password: String? = null
    protected var userType: String? = null


    protected val isPasswordInvalid: Boolean
        get() = !StringUtils.isLongerThanMinLength(password?:"") || !StringUtils.isShorterThanMaxLength(password)

}
