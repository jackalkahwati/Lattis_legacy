package com.lattis.domain.usecase.authentication


import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.AccountRepository
import com.lattis.domain.repository.Authenticator
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.utils.StringUtils
import java.util.ArrayList

import javax.inject.Inject

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Function

import com.lattis.domain.models.User

class SignInUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val authenticator: Authenticator,
            private val accountRepository: AccountRepository
            ) : UseCase<User>(threadExecutor, postExecutionThread) {

    private var email: String? = null
    private var password: String? = null
    private var userType: String? = null

//    private val errors: List<SignInValidationError.Status>
//        get() {
//            val statuses = ArrayList<SignInValidationError.Status>()
//
////            if (isPasswordInvalid) {
////                statuses.add(INVALID_PASSWORD)
////            }
//            return statuses
//        }
//
//    protected val isPasswordInvalid: Boolean
//        get() = !StringUtils.isLongerThanMinLength(password) || !StringUtils.isShorterThanMaxLength(password)

    fun withValues(email: String,
                   password: String): SignInUseCase {
        this.email = email
        this.password = password
        this.userType = User.Type.LATTIS.value
        return this
    }

    override fun buildUseCaseObservable(): Observable<User> {
        return trySignIn()
    }

    protected fun trySignIn(): Observable<User> {
//        val errors = errors
//        return if (errors.isEmpty()) {
            return authenticator.signIn(userType,
                    email,
                    password,
                    "fcm_token")
                    .flatMap { account ->
                        if (account!=null && account.isVerified?:false) {
                            accountRepository.addAccountExplicitly(account)
                        } else Observable.just(account)
                    }.flatMap { account ->
                        val user = User()
                        user.id = account.userId
                        user.isVerified = account.isVerified
                        Observable.just(user)
                    }
//        } else {
//            Observable.error(SignInValidationError(errors))
//        }
    }
}