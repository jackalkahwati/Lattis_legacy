package com.lattis.ellipse.presentation.ui.authentication.verification.activity;

import androidx.annotation.NonNull;

import com.lattis.ellipse.presentation.ui.base.activity.BaseActivity;

import javax.inject.Inject;

import io.lattis.ellipse.R;

public class VerificationActivity extends BaseActivity<VerificationPresenter> implements VerificationView {

    public final static String ARG_USER_ID = "ARG_USER_ID";

    @Inject VerificationPresenter presenter;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected VerificationPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_verification;
    }

    @Override
    protected void configureViews() {
    }


    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }
}
