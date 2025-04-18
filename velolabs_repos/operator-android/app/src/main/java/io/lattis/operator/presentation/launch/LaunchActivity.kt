package io.lattis.operator.presentation.authentication.launch

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import io.lattis.operator.R
import io.lattis.operator.presentation.fleet.FleetDetailActivity
import io.lattis.operator.presentation.home.HomeActivity
import io.lattis.operator.presentation.ui.base.activity.BaseActivity
import javax.inject.Inject

class LaunchActivity : BaseActivity<LaunchActivityPresenter,LaunchActivityView>(),LaunchActivityView{
    @Inject
    override lateinit var presenter: LaunchActivityPresenter
    override val activityLayoutId = R.layout.activity_launch
    override var view: LaunchActivityView = this


    override fun onResume() {
        super.onResume()
        presenter.subscribeToLaunchScreenTimer(true)
    }

    override fun onUserSavedFleetSuccess() {
        closeThisActivityForUserSavedFleet()
    }

    override fun onUserSavedFleetFailure() {
        closeThisActivityForHomeActivity()
    }

    fun closeThisActivityForHomeActivity(){
        if (authenticateAccount()) {
            val i = Intent(this, HomeActivity::class.java)
            startActivity(i)
        }
        finishMe()
    }

    fun closeThisActivityForUserSavedFleet(){
        if (authenticateAccount()) {
            startActivity(FleetDetailActivity.getIntent(this,presenter.userSavedFleet!!))
        }
        finishMe()
    }

    private fun authenticateAccount(): Boolean {
        val account = account
        val accountManager = AccountManager.get(this)
        return if (account != null) {
            accountManager.getAuthToken(account, getString(R.string.account_authentication_token_type), null, this, null, null)
            true
        } else {
            accountManager.addAccount(getString(R.string.account_type), getString(R.string.account_authentication_token_type),
                null, null, this, null, null)
            false
        }
    }

    private val account: Account?
        private get() {
            val accountManager = AccountManager.get(this)
            val accounts = accountManager.getAccountsByType(getString(R.string.account_type))
            return if (accounts.size > 0) {
                accounts[0]
            } else {
                null
            }
        }


    override fun onInternetConnectionChanged(isConnected: Boolean) {}

    public override fun onDestroy() {
        super.onDestroy()
        presenter.subscribeToLaunchScreenTimer(false)
    }
}