package com.lattis.ellipse.presentation.ui.base.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import androidx.annotation.Nullable;

import io.lattis.ellipse.R;


public abstract class BaseAuthenticatedActivity<Presenter extends ActivityPresenter> extends BaseActivity<Presenter> {

    @Override
    protected void onResume() {
        authenticateAccount();
        super.onResume();
    }

    @SuppressWarnings("MissingPermission")
    private boolean authenticateAccount() {
        Account account = getAccount();
        AccountManager accountManager = AccountManager.get(this);
        if (account != null) {
            accountManager.getAuthToken(account, getString(R.string.account_authentication_token_type), null, this, null, null);
            return true;
        } else {
            noAccountAdded();
            accountManager.addAccount(getString(R.string.account_type),getString(R.string.account_authentication_token_type),
                    null, null, this, null, null);
            return false;
        }
    }

    @Nullable
    @SuppressWarnings("MissingPermission")
    private Account getAccount() {
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(getString(R.string.account_type));
        if (accounts.length > 0) {
            return accounts[0];
        } else {
            return null;
        }
    }

    protected void noAccountAdded(){}
}
