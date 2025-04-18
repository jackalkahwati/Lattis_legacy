package io.lattis.operator.infrastructure.di.module

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context

import dagger.Module
import dagger.Provides
import io.lattis.operator.R
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


    companion object {
        const val USER_DATA_USER_ID_KEY = "USER_DATA_USER_ID_KEY"
    }
}