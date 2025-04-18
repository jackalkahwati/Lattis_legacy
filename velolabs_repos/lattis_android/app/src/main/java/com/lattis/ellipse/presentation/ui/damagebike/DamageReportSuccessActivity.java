package com.lattis.ellipse.presentation.ui.damagebike;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lattis.ellipse.presentation.ui.base.activity.BaseCloseActivity;
import com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity;

import javax.inject.Inject;

import butterknife.OnClick;
import io.lattis.ellipse.R;

import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.FORCE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideFragment.DAMAGE_REPORT_SUCCESS;

public class DamageReportSuccessActivity extends BaseCloseActivity<DamageReportSuccessPresenter>
        implements DamageReportSuccessView {
    private static final int REQUEST_END_RIDE_CHECKLIST = 2013;
    int tripID;
    public final static String TRIP_ID = "TRIP_ID";
    public final static String BIKE_DAMAGE = "BIKE_DAMAGE";
    private boolean isForceEndRide = false;


    @Inject
    DamageReportSuccessPresenter damageReportSuccessPresenter;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.title_submit_damage_report_submitted));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @OnClick(R.id.tv_end_ride)
    public void viewEndRideClicked() {
        launchEndRideListActivity();
    }

    @OnClick(R.id.tv_find_bike)
    public void viewFindBikeClicked() {
        launchEndRideListActivity();
    }

    @OnClick(R.id.tv_continue)
    public void viewContinueClicked() {
        finish();
    }

    private void launchEndRideListActivity() {
        Intent intent = new Intent(this, EndRideCheckListActivity.class);
        intent.putExtra(TRIP_ID, tripID);
        intent.putExtra(BIKE_DAMAGE, true);
        intent.putExtra(FORCE_END_RIDE_ID, isForceEndRide);
        startActivityForResult(intent, REQUEST_END_RIDE_CHECKLIST);
    }

    @NonNull
    @Override
    protected DamageReportSuccessPresenter getPresenter() {
        return damageReportSuccessPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_damage_report_success;
    }

    public static void launchActivity(Activity activity, int tripID, int requestCode) {
        activity.startActivityForResult(new Intent(activity, DamageReportSuccessActivity.class)
                .putExtra("TRIP_ID", tripID), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_END_RIDE_CHECKLIST && resultCode == RESULT_OK) {
            Intent intent = new Intent();
            intent.putExtra(DAMAGE_REPORT_SUCCESS,true);
            setResult(RESULT_OK,intent);
            finish();
        }
    }

    @Override
    public void setTripID(int tripID) {
        this.tripID = tripID;
    }

    @Override
    public void setForceEndRide(boolean isForceEndRide) {
        this.isForceEndRide = isForceEndRide;
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
