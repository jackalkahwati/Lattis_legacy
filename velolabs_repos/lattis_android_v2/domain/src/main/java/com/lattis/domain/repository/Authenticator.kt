package com.lattis.domain.repository

import com.lattis.domain.models.Account
import com.lattis.domain.models.User

import io.reactivex.rxjava3.core.Observable

interface Authenticator {

    fun signIn(userType: String?,
               usersId: String?,
               password: String?,
               fcmRegistrationId: String?): Observable<Account>

    fun signUp(userType: String?,
               usersId: String?,
               password: String?,
               fcmRegistrationId: String?, firstName: String?,
               lastName: String?): Observable<User>


    fun sendVerificationCode(
        user_id: String?,
        account_type: String?
    ): Observable<Boolean>

    fun confirmVerificationCode(
        userId: String?,
        account_type: String?,
        confirmationCode: String?,
        password: String?
    ): Observable<Account>


}
