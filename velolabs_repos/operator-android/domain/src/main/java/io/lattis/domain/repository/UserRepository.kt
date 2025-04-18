package io.lattis.domain.repository

import io.lattis.domain.models.User
import io.reactivex.Observable

interface UserRepository {
    fun getMe(): Observable<User.Operator>
}