package io.lattis.data.net.base

import android.accounts.AccountManager
import android.util.Log
import io.lattis.data.BuildConfig
import io.lattis.data.BuildConfig.DEBUG
import io.lattis.data.net.authentication.AuthenticationApiClient
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
    @param:Named("UserId") val userId: String

) {

    fun provideApiOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(60, TimeUnit.SECONDS)
        builder.writeTimeout(60, TimeUnit.SECONDS)
        builder.readTimeout(60, TimeUnit.SECONDS)
        builder.addInterceptor(generateClientHeaderInterceptor())
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
            if (accounts.size > 0) {
                val token = accountManager.peekAuthToken(accounts[0], authenticationTokenType)
                Log.d("NetworkModule", "Token is $token")
                if (token != null) {
                    builder.header(HEADER_KEY_AUTHORIZATION, "Bearer "+token)
                }
            }
            chain.proceed(builder.build())
        }
    }



    companion object {
        const val HEADER_KEY_CONTENT_TYPE = "Content-Type"
        const val HEADER_KEY_AUTHORIZATION = "Authorization"
        const val HEADER_VALUE_CONTENT_TYPE = "application/json"
    }

}