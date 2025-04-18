package com.lattis.lattis.infrastructure.di.module

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import com.lattis.data.entity.body.authentication.RefreshTokenBody
import com.lattis.data.repository.datasources.api.authentication.AuthenticationApi
import com.lattis.lattis.data.network.base.ApiEndpoints
import dagger.Module
import dagger.Provides
import io.lattis.lattis.BuildConfig
import io.lattis.lattis.R
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.Route
import javax.inject.Named
import javax.inject.Singleton

@Module
class AuthenticationModule {
    @Provides
    @Singleton
    @Named("AuthenticationTokenType")
    fun provideAuthenticationTokenType(context: Context): String {
        return context.getString(R.string.account_authentication_token_type)
    }

    @Provides
    @Singleton
    @Named("AccountType")
    fun provideAccountType(context: Context): String {
        return context.getString(R.string.account_type)
    }

    @Provides
    @Singleton
    fun provideAccountManager(context: Context): AccountManager {
        return context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
    }



    @Provides
    fun provideAccount(accountManager: AccountManager,
                       @Named("AccountType") accountType: String?): Account? {
        val accounts = accountManager.getAccountsByType(accountType)
        return if (accounts != null && accounts.size > 0) {
            accountManager.getAccountsByType(accountType)[0]
        } else null
    }

    @Provides
    @Named("UserId")
    fun provideUserId(accountManager: AccountManager, @Named("AccountType") accountType: String?): String {
        val accounts = accountManager.getAccountsByType(accountType)
        if (accounts.size > 0) {
            val userId = accountManager.getUserData(accounts[0], USER_DATA_USER_ID_KEY)
            return userId ?: "none"
        }
        return "none"
    }

    @Provides
    @Named("UsersId")
    fun provideUsersId(accountManager: AccountManager, @Named("AccountType") accountType: String?): String {
        val accounts = accountManager.getAccountsByType(accountType)
        if (accounts.size > 0) {
            val usersId = accountManager.getUserData(accounts[0], USER_DATA_USERS_ID_KEY)
            return usersId ?: "none"
        }
        return "none"
    }

    @Provides
    @Named("UserType")
    fun provideUserType(accountManager: AccountManager, @Named("AccountType") accountType: String?): String {
        val accounts = accountManager.getAccountsByType(accountType)
        if (accounts.size > 0) {
            val userType = accountManager.getUserData(accounts[0], USER_DATA_USER_TYPE_KEY)
            return userType ?: "none"
        }
        return "none"
    }


    companion object {
        private const val HEADER_KEY_AUTHORIZATION = "Authorization"
        const val USER_DATA_USER_ID_KEY = "USER_DATA_USER_ID_KEY"
        const val USER_DATA_USERS_ID_KEY = "USER_DATA_USERS_ID_KEY"
        const val USER_DATA_USER_TYPE_KEY = "USER_DATA_USER_TYPE_KEY"
        const val USER_DATA_USER_VERIFIED = "USER_DATA_USER_VERIFIED"
    }
}