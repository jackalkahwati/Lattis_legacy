package com.lattis.ellipse.presentation.ui.bike.bikeList;


import android.content.Intent;
import androidx.annotation.NonNull;

import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.presentation.ui.base.activity.BaseAuthenticatedActivity;

import javax.inject.Inject;

import butterknife.OnClick;
import io.lattis.ellipse.R;

public class NoServiceActivity extends BaseAuthenticatedActivity<NoServicePresenter> implements NoServiceView {
    private String userId;
    public static String USER_ID_FOR_ADD_PRIVATE_FLEET = "user_id_private_fleet";


    @Inject
    NoServicePresenter noServicePresenter;

    @OnClick(R.id.add_private_fleet)
    public void addPrivateFleet() {
        getPresenter().getUserProfile();
    }

    @OnClick(R.id.iv_close)
    public void closeButtonClicked() {
        finish();
    }

    @OnClick(R.id.cancel_action)
    public void CancelClicked() {
        finish();
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @Override
    protected void configureViews() {
        super.configureViews();
    }

    @NonNull
    @Override
    protected NoServicePresenter getPresenter() {
        return noServicePresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_no_services;
    }

    @Override
    public void onGetUserSuccess(User user) {
        userId = user.getId();
//        startActivity(new Intent(NoServiceActivity.this, ChangeMailActivity.class)
//                .putExtra(ARG_USER_ACCOUNT_TYPE, USER_ACCOUNT_TYPE_PRIVATE)
//                .putExtra(ARG_USER_ID, userId));

        Intent data = new Intent();
        data.putExtra(USER_ID_FOR_ADD_PRIVATE_FLEET, userId);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onGetUserFail() {
        finish();
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
