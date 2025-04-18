package io.lattis.data.net.base

import android.accounts.AccountManager
import android.util.Log
import io.lattis.data.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class BaseAuthHttpClient @Inject constructor(
    val accountManager: AccountManager,
    @param:Named("AccountType") val accountType: String,
    @param:Named("AuthenticationTokenType") val authenticationTokenType: String
){

    fun getAuthOkHttpClient(): OkHttpClient {
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
            chain.proceed(builder.build())
        }
    }

}