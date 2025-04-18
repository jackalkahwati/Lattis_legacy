package io.lattis.domain.repository


import io.lattis.domain.models.Account
import io.lattis.domain.models.User
import io.reactivex.Observable

interface Authenticator {

    fun signIn(email: String?,
               password: String?): Observable<User>

}
