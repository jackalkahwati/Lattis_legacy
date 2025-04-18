package com.lattis.data.repository.implementation.api

import com.lattis.data.entity.body.authentication.*
import com.lattis.data.entity.response.user.GetUserPayloadResponse
import com.lattis.data.entity.response.user.GetUserResponse
import com.lattis.data.mapper.AccountMapper
import com.lattis.data.mapper.UserMapper
import com.lattis.data.mapper.UserToAccountMapper
import com.lattis.data.net.authentication.AuthenticationApiClient
import com.lattis.domain.repository.Authenticator
import com.lattis.domain.models.Account
import com.lattis.domain.models.User
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Function
import javax.inject.Inject
import javax.inject.Named

class LattisAuthenticator @Inject
constructor(private val authenticationApiClient: AuthenticationApiClient,
            private val userMapper: UserMapper,
            private val userToAccountMapper: UserToAccountMapper,
            @param:Named("DeviceLanguage") private val deviceLanguage: String
            ) : Authenticator {



    override fun signIn(userType: String?,
                        usersId: String?,
                        password: String?,
                        fcmToken: String?): Observable<Account> {
        return authenticationApiClient.api.signIn(
            SignInRequestBody(
                userType,
                usersId,
                fcmToken,
                password,
                false)
        ).flatMap { authenticationResponse ->

            if (authenticationResponse.user?.isVerified?:false) {
                authenticationApiClient.api
                    .getNewTokens(GetNewTokensBody(authenticationResponse.user?.userId, password))
                    .flatMap { newTokenResponse ->
                        authenticationResponse.user?.restToken = newTokenResponse.token?.restToken
                        authenticationResponse.user?.refreshToken =
                            newTokenResponse.token?.refreshToken
                        Observable.just(userToAccountMapper.mapIn(authenticationResponse.user))
                    }
            }else{
                Observable.just(userToAccountMapper.mapIn(authenticationResponse.user))
            }
        }
    }

    override fun signUp(userType: String?,
                        usersId: String?,
                        password: String?,
                        fcmToken: String?, firstName: String?,
                        lastName: String?): Observable<User> {
        return authenticationApiClient.api.signUp(
            SignUpRequestBody(
                userType,
                usersId,
                fcmToken,
                password,
                true, firstName,
                lastName,
                deviceLanguage
            )
        ).flatMap { authenticationResponse ->
            if (authenticationResponse.user?.isVerified?:false) {
                authenticationApiClient.api.getNewTokens(GetNewTokensBody(authenticationResponse.user?.userId, password))
                    .flatMap { newTokenResponse ->
                        authenticationResponse.user?.restToken = newTokenResponse.token?.restToken
                        authenticationResponse.user?.refreshToken = newTokenResponse.token?.refreshToken
                        var getUserResponse= GetUserResponse()
                        getUserResponse.getUserPayloadResponse = GetUserPayloadResponse()
                        getUserResponse.getUserPayloadResponse?.userResponse = authenticationResponse.user
                        getUserResponse.userResponse =authenticationResponse.user
                        Observable.just(userMapper.mapIn(getUserResponse))
                    }
            } else {
                var getUserResponse= GetUserResponse()
                getUserResponse.getUserPayloadResponse = GetUserPayloadResponse()
                getUserResponse.getUserPayloadResponse?.userResponse = authenticationResponse.user
                Observable.just(userMapper.mapIn(getUserResponse))
            }
        }
    }


    override fun sendVerificationCode(
        user_id: String?,
        account_type: String?
    ): Observable<Boolean> {
        return authenticationApiClient.api.sendVerificationCode(
                SendVerificationCodeBody(
                    user_id,
                    account_type
                )
            )
            .flatMap({ basicResponse -> Observable.just(true) })
    }

    override fun confirmVerificationCode(
        userId: String?,
        account_type: String?,
        confirmationCode: String?,
        password: String?
    ): Observable<Account> {
        return authenticationApiClient.api.confirmVerificationCode(
                VerificationCodeBody(
                    userId,
                    account_type,
                    confirmationCode
                )
            )
            .flatMap { validationResponse ->
                authenticationApiClient.api
                    .getNewTokens(
                        GetNewTokensBody(
                            validationResponse.getUserResponse()?.userId, password
                        )
                    )
                    .flatMap { newTokenResponse ->
                        validationResponse.getUserResponse()?.restToken = (newTokenResponse.token?.restToken)
                        validationResponse.getUserResponse()?.refreshToken= (newTokenResponse.token?.refreshToken)
                        Observable.just(
                            userToAccountMapper.mapIn(
                                validationResponse.getUserResponse()
                            )
                        )
                    }
            }
    }


}