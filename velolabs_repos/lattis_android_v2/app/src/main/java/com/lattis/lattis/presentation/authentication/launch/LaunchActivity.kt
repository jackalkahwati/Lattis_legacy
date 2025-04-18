package com.lattis.lattis.presentation.authentication.launch

import android.accounts.Account
import android.accounts.AccountManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusActivity
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusPresenter.Companion.CURRENT_STATUS
import com.lattis.lattis.presentation.home.activity.HomeActivity
import com.lattis.lattis.presentation.home.activity.HomeActivity.Companion.SUBSCRIPTION_LIST
import com.lattis.lattis.utils.AccountAuthenticatorHelper.getAppStartBundle
import io.lattis.lattis.R
import java.io.Serializable
import javax.inject.Inject

class LaunchActivity : BaseUserCurrentStatusActivity<LaunchActivityPresenter,LaunchActivityView>(),LaunchActivityView{
    @Inject
    override lateinit var presenter: LaunchActivityPresenter
    override val activityLayoutId = R.layout.activity_launch
    override var view: LaunchActivityView = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        removeAndroid12SplashScreen()
        resetBadgeCounterOfPushMessages()
    }

    fun removeAndroid12SplashScreen(){
        val content = findViewById<View>(android.R.id.content)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            content.viewTreeObserver.addOnDrawListener { false }
        }
    }

    override fun onResume() {
        super.onResume()
        if(account!=null){
            presenter.getSubscriptions()
            presenter.userCurrentStatus()
        }else{
            presenter.subscribeToLaunchScreenTimer(true)
        }
    }

    override fun onLaunchScreenTimer() {
        closeThisActivity()
    }

    fun closeThisActivity(){
        if (authenticateAccount()) {
            val i = Intent(this, HomeActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            i.putExtra(CURRENT_STATUS, presenter.currentStatus)
            if(presenter.subscriptionList!=null) i.putExtra(SUBSCRIPTION_LIST,presenter.subscriptionList as Serializable)
            startActivity(i)
        }
        finishMe()
    }

    private fun authenticateAccount(): Boolean {
        val account = account
        val accountManager = AccountManager.get(this)
        return if (account != null) {
            accountManager.getAuthToken(account, getString(R.string.account_authentication_token_type), getAppStartBundle(), this, null, null)
            true
        } else {
            accountManager.addAccount(getString(R.string.account_type), getString(R.string.account_authentication_token_type),
                null, getAppStartBundle(), this, null, null)
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


    override fun onUserCurrentStatusSuccess() {
        closeThisActivity()
    }

    override fun onUserCurrentStatusFailure() {
        closeThisActivity()
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {}

    public override fun onDestroy() {
        super.onDestroy()
        presenter.subscribeToLaunchScreenTimer(false)
    }


    private fun resetBadgeCounterOfPushMessages() {
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager?.cancelAll()
        }
    }
}