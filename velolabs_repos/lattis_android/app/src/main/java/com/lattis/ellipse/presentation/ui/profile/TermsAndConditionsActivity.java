package com.lattis.ellipse.presentation.ui.profile;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.view.View;

import com.lattis.ellipse.presentation.ui.base.activity.BaseAuthenticatedActivity;

import javax.inject.Inject;

import butterknife.OnClick;
import io.lattis.ellipse.R;

public class TermsAndConditionsActivity extends BaseAuthenticatedActivity<TermsAndConditionsPresenter> implements TermsAndConditionsView, TermsAndConditionsFragment.Listener {

    @Inject TermsAndConditionsPresenter mPresenter;

    public static void launchForResult(Activity activity, int requestCode){
        Intent intent = new Intent(activity, TermsAndConditionsActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected TermsAndConditionsPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_terms_and_condition;
    }

    @OnClick(R.id.cv_accept)
    public void onAcceptViewClicked(final View v) {
        getPresenter().acceptTermsAndConditions();
    }

    @OnClick(R.id.cv_decline)
    public void onDeclineViewClicked(final View v) {
        getPresenter().declineTermsAndConditions();
    }

    @Override
    public void onTermsAndConditionsAccepted() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onTermsAndConditionsDeclined() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onTermsAndConditionsLoaded() {
        hideLoading();
    }

    @Override
    public void onTermsAndConditionsFailedLoading() {
        hideLoading();
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
