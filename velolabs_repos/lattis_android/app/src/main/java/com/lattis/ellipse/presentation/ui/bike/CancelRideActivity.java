package com.lattis.ellipse.presentation.ui.bike;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.presentation.ui.base.activity.BaseAuthenticatedActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;

import javax.inject.Inject;

import butterknife.OnClick;
import io.lattis.ellipse.R;

/**
 * Created by Velo Labs Android on 20-04-2017.
 */

public class CancelRideActivity extends BaseAuthenticatedActivity<CancelRidePresenter> implements CancelRideView {

    private static final int REQUEST_CODE_CANCEL_BIKE_RESERVATION_FAIL_ACTIVITY = 5199;
    private boolean isBikeDamage = false;


    @Inject
    CancelRidePresenter cancelRidePresenter;
    private Bike bike;
    private boolean lockIssue;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @OnClick(R.id.cancel_ride)
    public void cancelRideButtonClicked() {
        getPresenter().cancelBikeReservation(bike, isBikeDamage,lockIssue);
    }

    @OnClick(R.id.iv_close)
    public void closeButtonClicked() {
        finish();
    }

    @OnClick(R.id.label_dont_cancel)
    public void dontCancelClicked() {
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            String jsonString = getIntent().getStringExtra("BIKE_DETAILS");
            isBikeDamage = getIntent().getBooleanExtra("BIKE_DAMAGE", false);
            lockIssue = getIntent().getBooleanExtra("LOCK_ISSUE", false);
            if (jsonString != null && !jsonString.isEmpty()) {
                Gson gson = new Gson();
                this.bike = gson.fromJson(jsonString, Bike.class);
            }
        }
    }

    @NonNull
    @Override
    protected CancelRidePresenter getPresenter() {
        return cancelRidePresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_cancel_ride;
    }


    @Override
    public void onCancelBikeSuccess() {
        getPresenter().disconnectAllLocks();
    }

    @Override
    public void onCancelBikeFail() {
        PopUpActivity.launchForResult(this, REQUEST_CODE_CANCEL_BIKE_RESERVATION_FAIL_ACTIVITY, getString(R.string.alert_error_server_title),
                getString(R.string.alert_error_server_subtitle), null, getString(R.string.ok));
    }


    @Override
    public void onLockDisconnectionSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onLockDisconnectionFail() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CANCEL_BIKE_RESERVATION_FAIL_ACTIVITY) {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }
}


