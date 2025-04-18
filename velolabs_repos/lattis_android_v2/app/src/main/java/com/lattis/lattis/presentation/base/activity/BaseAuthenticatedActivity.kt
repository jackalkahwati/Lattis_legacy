package com.lattis.lattis.presentation.base.activity

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle
import com.lattis.lattis.presentation.base.BaseView
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import io.lattis.lattis.R

open abstract class BaseAuthenticatedActivity<Presenter : ActivityPresenter<V>,V:BaseView> :
    BaseActivity<Presenter,V>() {
    override fun onResume() {
        authenticateAccount(null)
        super.onResume()
    }

   fun authenticateAccount(appStatusBundle: Bundle?): Boolean {
        val account = account
        val accountManager = AccountManager.get(this)
        return if (account != null) {
            accountManager.getAuthToken(
                account,
                getString(R.string.account_authentication_token_type),
                appStatusBundle,
                this,
                null,
                null
            )
            true
        } else {
            accountManager.addAccount(
                getString(R.string.account_type),
                getString(R.string.account_authentication_token_type),
                null,
                appStatusBundle,
                this,
                null,
                null
            )
            false
        }
    }

    private val account: Account?
        private get() {
            val accountManager = AccountManager.get(this)
            val accounts =
                accountManager.getAccountsByType(getString(R.string.account_type))
            return if (accounts.size > 0) {
                accounts[0]
            } else {
                null
            }
        }
}