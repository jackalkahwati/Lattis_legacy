package com.lattis.ellipse.presentation.ui.ride;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.lattis.ellipse.presentation.ui.base.activity.BaseActivity;

import javax.inject.Inject;

import butterknife.OnClick;
import io.lattis.ellipse.R;

/**
 * Created by ssd3 on 4/24/17.
 */

public class EndRideOutOfBoundActivity extends BaseActivity<EndRideOutOfBoundActivityPresenter> implements EndRideOutOfBoundActivityView {

    public static String FIND_NEARBY_ZONE = "zone";

    @Inject
    EndRideOutOfBoundActivityPresenter presenter;

    @NonNull
    @Override
    protected EndRideOutOfBoundActivityPresenter getPresenter() {
        return presenter;
    }


    @Override
    protected int getViewStubLayoutId() {
        return super.getViewStubLayoutId();
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_end_ride_out_of_bounds;
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }


    @OnClick(R.id.iv_close_ride_out_of_bounds)
    public void closeActivity(){
        setResult(RESULT_CANCELED);
        finish();
    }

    @OnClick(R.id.end_ride_out_of_bounds_btn)
    public void closeActivityWithRide(){
        Intent resultIntent = new Intent();
        resultIntent.putExtra(FIND_NEARBY_ZONE, "false");
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @OnClick(R.id.find_nearby_zone_btn)
    public void closeActivityWithFindingZones(){
        Intent resultIntent = new Intent();
        resultIntent.putExtra(FIND_NEARBY_ZONE, "true");
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }


}
