package io.lattis.operator.presentation.base.activity

import android.accounts.Account
import android.accounts.AccountManager
import io.lattis.operator.R
import io.lattis.operator.presentation.base.BaseView
import io.lattis.operator.presentation.ui.base.activity.BaseActivity

open abstract class BaseAuthenticatedActivity<Presenter : ActivityPresenter<V>,V: BaseView> :
    BaseActivity<Presenter, V>() {
    override fun onResume() {
        authenticateAccount()
        super.onResume()
    }

   fun authenticateAccount(): Boolean {
        val account = account
        val accountManager = AccountManager.get(this)
        return if (account != null) {
            accountManager.getAuthToken(
                account,
                getString(R.string.account_authentication_token_type),
                null,
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
                null,
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