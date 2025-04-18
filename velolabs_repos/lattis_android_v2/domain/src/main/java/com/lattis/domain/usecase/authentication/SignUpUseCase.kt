package com.lattis.domain.usecase.authentication

import com.lattis.domain.error.SignUpValidationError
import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.mapper.AccountMapper
import com.lattis.domain.repository.AccountRepository
import com.lattis.domain.repository.Authenticator
import com.lattis.domain.repository.UserRepository
import com.lattis.domain.usecase.authentication.BaseAuthenticationUseCase
import com.lattis.domain.utils.StringUtils
import java.util.ArrayList

import javax.inject.Inject

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Function

import com.lattis.domain.models.User
import com.lattis.domain.models.VerificationBundle

class SignUpUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            authenticator: Authenticator,
            private val accountRepository: AccountRepository,
            private val accountMapper: AccountMapper
) : BaseAuthenticationUseCase(threadExecutor, postExecutionThread, authenticator, accountRepository) {

    private val errors: List<SignUpValidationError.Status>
        get() {
            val statuses = ArrayList<SignUpValidationError.Status>()
            if (isPasswordInvalid) {
                statuses.add(SignUpValidationError.Status.INVALID_PASSWORD)
            }
            if (isEmailInvalid) {
                statuses.add(SignUpValidationError.Status.INVALID_EMAIL)
            }
            return statuses
        }

    private val isEmailInvalid: Boolean
        get() = !StringUtils.isLongerThanMinLength(password)


    fun withValue(email: String,
                  firstName: String,
                  lastName: String,
                  password: String): SignUpUseCase {
        this.email = email
        this.firstName = firstName
        this.lastName = lastName
        this.password = password
        this.userType = User.Type.LATTIS.value
        return this
    }

    override fun buildUseCaseObservable(): Observable<VerificationBundle> {
        return trySignUp()
    }

    private fun trySignUp(): Observable<VerificationBundle> {
        val errors = errors
        return if (errors.isEmpty()) {
            authenticator.signUp(userType,
                    email,
                    password,
                    "fcm_token", firstName, lastName)
                    .flatMap { user ->
                        if (user?.isVerified?:false) {
                            accountRepository.addAccountExplicitly(accountMapper.mapIn(user))
                        } else Observable.just(accountMapper.mapIn(user))
                    }.flatMap { account ->
                        val verificationBundle = VerificationBundle()
                        verificationBundle.userId = account.userId
                        verificationBundle.isVerified = account.isVerified
                        Observable.just(verificationBundle)
                    }


        } else {
            Observable.error(SignUpValidationError(errors))
        }
    }
}
