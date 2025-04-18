package com.lattis.data.repository.datasources.api.user

import com.lattis.data.entity.body.authentication.VerificationCodeBody
import com.lattis.data.entity.body.user.*
import com.lattis.data.entity.response.BasicResponse
import com.lattis.data.entity.response.user.AddPrivateNetworkResponse
import com.lattis.data.entity.response.user.GetUserFleetsResponse
import com.lattis.data.entity.response.user.GetUserResponse
import com.lattis.data.entity.response.user.current_status.GetCurrentUserStatusResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApi{

    @POST("api/users/get-current-status/")
    fun getCurrentUserStatus(@Body getUserCurrentStatusBody: GetUserCurrentStatusBody): Observable<GetCurrentUserStatusResponse>

    @get:POST("api/users/get-user/")
    val user: Observable<GetUserResponse>

    @POST("api/users/update-user/")
    fun saveUser(@Body updateUserBody: UpdateUserBody): Observable<GetUserResponse>

    @POST("api/users/confirm-email-verification-code/")
    fun confirmVerificationCodeForPrivateNetwork(@Body verificationCodeBody: VerificationCodeBody): Observable<BasicResponse>

    @POST("api/users/add-private-account/")
    fun addPrivateNetworkEmail(@Body updateEmailCodeBody: UpdateEmailCodeBody): Observable<AddPrivateNetworkResponse>

    @POST("api/users/update-phone-number-code/")
    fun sendCodeForPhoneNumber(@Body body: SendCodeUpdatePhoneNumberBody): Observable<BasicResponse>

    @POST("api/users/update-phone-number/")
    fun validateCodeForChangePhoneNumber(@Body body: ValidateCodeForChangePhoneNumberBody): Observable<BasicResponse>

    @POST("api/users/change-password/")
    fun changePassword(@Body body: ChangePasswordBody): Observable<BasicResponse>

    @POST("api/users/update-email/")
    fun validateCodeForChangeMail(@Body validateCodeUpdateEmailBody: ValidateCodeUpdateEmailBody): Observable<BasicResponse>

    @POST("api/users/update-email-code/")
    fun sendCodeForUpdateEmail(@Body updateEmailCodeBody: UpdateEmailCodeBody): Observable<BasicResponse>

    @POST("api/users/forgot-password")
    fun sendForgotPasswordCode(@Body sendForgotPasswordCodeBody: SendForgotPasswordCodeBody): Observable<BasicResponse>

    @POST("api/users/confirm-forgot-password/")
    fun confirmCodeForForgotPassword(@Body confirmCodeForForgotPasswordBody: ConfirmCodeForForgotPasswordBody): Observable<BasicResponse>

    @GET("api/fleet")
    fun getUserFleet(): Observable<GetUserFleetsResponse>

    @PUT("api/users/delete")
    fun deleteAccount():Observable<BasicResponse>
}