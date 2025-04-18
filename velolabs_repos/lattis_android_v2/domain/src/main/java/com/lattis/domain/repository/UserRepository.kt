package com.lattis.domain.repository

import com.lattis.domain.models.Bike
import com.lattis.domain.models.UserCurrentStatus
import com.lattis.domain.models.PrivateNetwork
import com.lattis.domain.models.User
import io.reactivex.rxjava3.core.Observable

interface UserRepository{

    fun getUserCurrentStatus(): Observable<UserCurrentStatus>
    fun getUser(): Observable<User>
    fun saveUserLocally(user: User): Observable<User>
    fun saveUser(user: User): Observable<User>
    fun getLocalUser():Observable<User>
    fun confirmVerificationCodeForPrivateNetwork(
        userId: String,
        account_type: String,
        confirmationCode: String
    ): Observable<Boolean>
    fun addPrivateNetworkEmail(email: String): Observable<List<PrivateNetwork>>

    fun sendCodeToUpdatePhoneNumber(
        countryCode: String,
        phoneNumber: String
    ): Observable<Boolean>

    fun validateCodeForChangePhoneNumber(
        code: String,
        phoneNumber: String
    ): Observable<Boolean>

    fun changePassword(
        password: String,
        new_password: String
    ): Observable<Boolean>

    fun validateCodeForEmailChange(
        code: String,
        email: String
    ): Observable<Boolean>

    fun sendCodeToUpdateEmail(email: String): Observable<Boolean>

    fun sendForgotPasswordCode(email: String): Observable<Boolean>

    fun confirmCodeForForgotPassword(
        email: String,
        code: String,
        password: String
    ): Observable<Boolean>

    fun getUserFleet():Observable<List<Bike.Fleet>>
    fun deleteAccount():Observable<Boolean>
}