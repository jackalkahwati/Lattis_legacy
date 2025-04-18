package com.lattis.data.net.base

import android.accounts.AccountManager
import android.util.Log
import com.lattis.data.BuildConfig
import com.lattis.data.BuildConfig.DEBUG
import com.lattis.data.entity.body.authentication.RefreshTokenBody
import com.lattis.data.net.authentication.AuthenticationApiClient
import com.lattis.data.repository.datasources.api.authentication.AuthenticationApi
import com.lattis.data.repository.implementation.api.AndroidAccountRepository.Companion.USER_DATA_USER_ID_KEY
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class BaseHttpClient @Inject constructor(
    val accountManager: AccountManager,
    val authenticationApiClient: AuthenticationApiClient,
    @param:Named("AccountType") val accountType: String,
    @param:Named("AuthenticationTokenType") val authenticationTokenType: String,
    @param:Named("UserId") val userId: String,
    @param:Named("User-Agent")private val user_agent:String

) {

    fun getClient(requireAuthorization: Boolean = true): OkHttpClient {
        val httpBuilder = OkHttpClient()
            .newBuilder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                }
            )

        if (requireAuthorization) {

        }

        return httpBuilder.build()
    }



    fun provideApiOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(60,TimeUnit.SECONDS)
        builder.writeTimeout(60,TimeUnit.SECONDS)
        builder.readTimeout(60,TimeUnit.SECONDS)
        builder.addInterceptor(generateClientHeaderInterceptor())
        builder.addInterceptor(provideRefresh412Intercepter())
        builder.addInterceptor(generateHttpLoggingInterceptor())
        return builder.build()
    }

    private fun generateHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logger = HttpLoggingInterceptor()
        if(BuildConfig.DEBUG)
            logger.level = HttpLoggingInterceptor.Level.BODY
        else
            logger.level = HttpLoggingInterceptor.Level.NONE
        return logger
    }

    private fun generateClientHeaderInterceptor(): Interceptor {
        return Interceptor { chain: Interceptor.Chain -> val accounts = accountManager.getAccountsByType(accountType)
            val builder = chain.request().newBuilder()
            builder.header(HEADER_KEY_CONTENT_TYPE, HEADER_VALUE_CONTENT_TYPE)
            builder.header(HEADER_USER_AGENT,user_agent)
            if (accounts.size > 0) {
                val token = accountManager.peekAuthToken(accounts[0], authenticationTokenType)
                Log.d("NetworkModule", "Token is $token")
                if (token != null) {
                    builder.header(HEADER_KEY_AUTHORIZATION, token)
                }
            }
            chain.proceed(builder.build())
        }
    }

    fun provideRefresh412Intercepter(): Interceptor {
        return Interceptor { chain: Interceptor.Chain ->
            val originalRequest = chain.request()
            val response = chain.proceed(originalRequest)
            if (response.code == 412) {
                val accounts = accountManager.getAccountsByType(accountType)
                if (accounts.size > 0) {
                    val account = accounts[0]
                    val userId = accountManager.getUserData(account, USER_DATA_USER_ID_KEY)
                    val refreshTokenResponse = authenticationApiClient.api.refreshToken(RefreshTokenBody(userId, accountManager.getPassword(account))).execute()
                    if (refreshTokenResponse.isSuccessful) {
                        val expiredToken = accountManager.peekAuthToken(account, authenticationTokenType)
                        accountManager.invalidateAuthToken(accountType, expiredToken)
                        accountManager.setAuthToken(account, authenticationTokenType, refreshTokenResponse.body()!!.tokenResponse!!.restToken)
                        accountManager.setPassword(account, refreshTokenResponse.body()!!.tokenResponse!!.refreshToken)
                        chain.proceed(originalRequest.newBuilder().header(HEADER_KEY_AUTHORIZATION, refreshTokenResponse.body()?.tokenResponse?.restToken?:"").build())
                    }
                }
            }
            response
        }
    }



    companion object {
        const val HEADER_KEY_CONTENT_TYPE = "Content-Type"
        const val HEADER_KEY_AUTHORIZATION = "Authorization"
        const val HEADER_USER_AGENT = "User-Agent"
        const val HEADER_VALUE_CONTENT_TYPE = "application/json"
    }

}