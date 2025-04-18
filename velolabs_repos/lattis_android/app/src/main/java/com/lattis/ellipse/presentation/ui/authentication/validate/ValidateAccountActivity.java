package com.lattis.ellipse.presentation.ui.authentication.validate;

import androidx.annotation.NonNull;

import com.lattis.ellipse.presentation.ui.base.activity.BaseActivity;

import javax.inject.Inject;

public class ValidateAccountActivity extends BaseActivity<ValidateAccountPresenter> implements ValidateAccountView{

    @Inject
    ValidateAccountPresenter presenter;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected ValidateAccountPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return 0;
    }

    @Override
    protected void configureViews() {

    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }
}
