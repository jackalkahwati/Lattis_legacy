package com.lattis.data.repository.datasources.api.authentication


import com.lattis.data.entity.body.authentication.*
import com.lattis.data.entity.response.BasicResponse
import com.lattis.data.entity.response.authentication.AuthenticationResponse
import com.lattis.data.entity.response.authentication.NewTokenResponse
import com.lattis.data.entity.response.authentication.RefreshTokenResponse
import com.lattis.data.entity.response.authentication.ValidationResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthenticationApi {


    @POST("api/users/registration/")
    fun signIn(@Body body: SignInRequestBody): Observable<AuthenticationResponse>

    @POST("api/users/registration/")
    fun signUp(@Body body: SignUpRequestBody): Observable<AuthenticationResponse>

    @POST("api/users/new-tokens")
    fun getNewTokens(@Body getNewTokensBody: GetNewTokensBody): Observable<NewTokenResponse>

    @POST("api/users/refresh-tokens")
    fun refreshToken(@Body refreshTokenBody: RefreshTokenBody): Call<RefreshTokenResponse>

    @POST("api/users/email-verification-code/")
    fun sendVerificationCode(@Body sendVerificationCodeBody: SendVerificationCodeBody): Observable<BasicResponse>

    @POST("api/users/confirm-email-verification-code/")
    fun confirmVerificationCode(@Body verificationCodeBody: VerificationCodeBody): Observable<ValidationResponse>

}
