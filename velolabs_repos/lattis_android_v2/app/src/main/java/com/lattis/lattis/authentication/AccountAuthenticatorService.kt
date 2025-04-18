package com.lattis.lattis.authentication

import android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT
import android.app.Service
import android.content.Intent
import android.os.IBinder

class AccountAuthenticatorService : Service() {

    internal lateinit var accountAuthenticator: AccountAuthenticator

    override fun onCreate() {
        accountAuthenticator =
            AccountAuthenticator(applicationContext)
    }

    override fun onBind(intent: Intent): IBinder? {
        return if (intent.action == ACTION_AUTHENTICATOR_INTENT) getAuthenticator().iBinder else null
    }

    private fun getAuthenticator(): AccountAuthenticator {
        if (accountAuthenticator == null) {
            accountAuthenticator =
                AccountAuthenticator(applicationContext)
        }
        return accountAuthenticator
    }
}
