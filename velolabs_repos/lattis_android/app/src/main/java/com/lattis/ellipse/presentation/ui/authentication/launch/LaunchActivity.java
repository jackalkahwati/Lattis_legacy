package com.lattis.ellipse.presentation.ui.authentication.launch;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.lattis.ellipse.presentation.ui.base.activity.BaseActivity;
import com.lattis.ellipse.presentation.ui.home.HomeActivity;

import javax.inject.Inject;

import io.lattis.ellipse.R;

/**
 * Created by ssd3 on 10/3/17.
 */

public class LaunchActivity extends BaseActivity<LaunchActivityPresenter> implements LaunchActivityView{

    @Inject
    LaunchActivityPresenter presenter;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected LaunchActivityPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_launch;
    }



    @Override
    protected void onResume() {
        super.onResume();
        getPresenter().subscribeToLaunchScreenTimer(true);
    }

    @Override
    public void onLaunchScreenTimer() {
        if(authenticateAccount()){
            Intent i = new Intent(this, HomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
    }

    private boolean authenticateAccount() {
        Account account = getAccount();
        AccountManager accountManager = AccountManager.get(this);
        if (account != null) {
            accountManager.getAuthToken(account, getString(R.string.account_authentication_token_type), null, this, null, null);
            return true;
        } else {
            accountManager.addAccount(getString(R.string.account_type),getString(R.string.account_authentication_token_type),
                    null, null, this, null, null);
            return false;
        }
    }

    private Account getAccount() {
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(getString(R.string.account_type));
        if (accounts.length > 0) {
            return accounts[0];
        } else {
            return null;
        }
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().subscribeToLaunchScreenTimer(false);
    }


}
