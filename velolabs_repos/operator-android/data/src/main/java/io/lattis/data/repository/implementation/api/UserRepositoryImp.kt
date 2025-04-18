package io.lattis.data.repository.implementation.api

import io.lattis.data.net.user.UserApiClient
import io.lattis.domain.models.User
import io.lattis.domain.repository.UserRepository
import io.reactivex.Observable
import javax.inject.Inject

class UserRepositoryImp @Inject constructor(
    val userApiClient: UserApiClient
):UserRepository {

    override fun getMe(): Observable<User.Operator> {
        return userApiClient.api.getMe()
    }
}