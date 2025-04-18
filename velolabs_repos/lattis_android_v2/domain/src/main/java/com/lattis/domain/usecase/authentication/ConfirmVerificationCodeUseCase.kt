package com.lattis.domain.usecase.authentication

import com.lattis.domain.error.ConfirmCodeValidationError
import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.AccountRepository
import com.lattis.domain.repository.Authenticator
import com.lattis.domain.repository.UserRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Account
import com.lattis.domain.models.User
import com.lattis.domain.error.ConfirmCodeValidationError.Status
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Function
import java.util.*
import javax.inject.Inject

class ConfirmVerificationCodeUseCase @Inject constructor(
    threadExecutor: ThreadExecutor?,
    postExecutionThread: PostExecutionThread?,
    private val authenticator: Authenticator,
    private val accountRepository: AccountRepository
) : UseCase<User>(threadExecutor!!, postExecutionThread!!) {
    private var confirmationCode: String? = null
    private var user_id: String? = null
    private var account_type: String? = null
    private var password: String? = null
    fun forUser(user_id: String?): ConfirmVerificationCodeUseCase {
        this.user_id = user_id
        return this
    }

    fun forAccountType(account_type: String?): ConfirmVerificationCodeUseCase {
        this.account_type = account_type
        return this
    }

    fun withConfirmationCode(confirmationCode: String?): ConfirmVerificationCodeUseCase {
        this.confirmationCode = confirmationCode
        return this
    }

    fun withPassword(password: String?): ConfirmVerificationCodeUseCase {
        this.password = password
        return this
    }

    override fun buildUseCaseObservable(): Observable<User> {
        val errors = errors
        return if (errors.isEmpty()) {
            authenticator.confirmVerificationCode(
                    user_id,
                    account_type,
                    confirmationCode,
                    password
                )
                .flatMap{account->
                    accountRepository.addAccountExplicitly(account)
                }.map { account -> //return new User();
                    val user =
                        User()
                    user.id = account.userId
                    user.isVerified = account.isVerified
                    user
                }
        } else {
            Observable.error(
                ConfirmCodeValidationError(errors)
            )
        }
    }

    private val errors: List<ConfirmCodeValidationError.Status>
        private get() {
            val statuses: MutableList<ConfirmCodeValidationError.Status> =
                ArrayList()
            if (confirmationCode == null) {
                statuses.add(Status.INVALID_CONFIRMATION_CODE)
            }
            return statuses
        }

}