package io.lattis.data.repository.implementation.api

import io.lattis.data.entity.body.authentication.SignInRequestBody
import io.lattis.data.mapper.UserToAccountMapper
import io.lattis.data.net.authentication.AuthenticationApiClient
import io.lattis.domain.models.Account
import io.lattis.domain.models.User
import io.lattis.domain.repository.AccountRepository
import io.lattis.domain.repository.Authenticator
import io.reactivex.Observable
import javax.inject.Inject

class OperatorAuthenticator @Inject
constructor(
        private val authenticationApiClient: AuthenticationApiClient,
        private val userToAccountMapper: UserToAccountMapper,
        private val accountRepository: AccountRepository
) : Authenticator {



    override fun signIn(email: String?,
                        password: String?): Observable<User> {
        return authenticationApiClient.api.signIn(
            SignInRequestBody(
                email,
                password)
        ).flatMap { user ->
            accountRepository.addAccountExplicitly(userToAccountMapper.mapIn(user))
        }.flatMap { account ->
            val user = User()
            user.operator?.id = account.userId
            Observable.just(user)
        }
    }



}