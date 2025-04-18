package com.lattis.ellipse.presentation.ui.profile.logout;

import androidx.annotation.NonNull;
import android.view.View;

import com.lattis.ellipse.presentation.ui.base.activity.BaseActivity;

import javax.inject.Inject;

import butterknife.OnClick;
import io.lattis.ellipse.R;

public class LogOutActivity extends BaseActivity<LogOutPresenter> implements LogOutView {

    @Inject LogOutPresenter presenter;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected LogOutPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_logout;
    }
    @OnClick(R.id.iv_close)
    public void closeButtonClicked() {
        finish();
    }


    @OnClick(R.id.btn_cancel)
    public void onCancelViewClicked(final View view) {
        finish();
    }

    @OnClick(R.id.btn_logout)
    public void onLogOutViewClicked(final View view) {
        getPresenter().logOut();
    }

    @Override
    public void onLogOutSuccessful() {
        finish();
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
