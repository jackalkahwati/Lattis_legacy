package com.lattis.ellipse.presentation.ui.bike;


import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.lattis.ellipse.domain.model.TermsAndConditions;
import com.lattis.ellipse.presentation.ui.base.activity.BaseAuthenticatedActivity;
import com.lattis.ellipse.presentation.view.CustomTextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

public class TermsConditionForRide extends BaseAuthenticatedActivity<TermsConditionForRidePresenter> implements TermsConditionForRideView {
    @BindView(R.id.tv_description)
    CustomTextView decriptionInput;

    @Inject
    TermsConditionForRidePresenter termsConditionForRidePresenter;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected TermsConditionForRidePresenter getPresenter() {
        return termsConditionForRidePresenter;
    }

    @OnClick(R.id.button_accept)
    public void onAcceptButtonClicked() {
        setResult(RESULT_OK);
        finish();
    }

    @OnClick(R.id.button_decline)
    public void onDeclineButtonClicked() {
        setResult(RESULT_CANCELED);
        finish();
    }


    public static void launchForResult(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, TermsConditionForRide.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_tc_ride;
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onTermsAndConditionsLoaded(TermsAndConditions termsAndConditions) {
        decriptionInput.setText(termsAndConditions.getContent());
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
