package com.lattis.ellipse.presentation.ui.profile.logout;

import android.Manifest;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.lattis.ellipse.presentation.ui.base.activity.BaseActivity;

import javax.inject.Inject;

import butterknife.OnClick;
import io.lattis.ellipse.R;
import permissions.dispatcher.NeedsPermission;

/**
 * Created by ssd3 on 5/13/17.
 */

public class LogOutAfterEndingRideActivity extends BaseActivity<LogOutAfterEndingRideActivityPresenter> implements LogOutAfterEndingRideActivityView {

    @Inject
    LogOutAfterEndingRideActivityPresenter logOutAfterEndingRideActivityPresenter;
    private final int REQUEST_END_RIDE_CHECKLIST_FOR_LOGOUT = 930;

    @OnClick({R.id.btn_cancel_end_ride_for_logout,R.id.iv_close})
    public void cancelEndRide(){
        getPresenter().requestStopLocationUpdates();
        setResult(RESULT_OK);
        finish();
    }

//    @OnClick(R.id.btn_end_ride_for_logout)
//    public void endRideForLogOut(){
//        if(getPresenter().getCurrentUserLocation()==null){
//            requestLocationUpdates();
//            return;
//        }
//
//        Intent intent = new Intent(this, EndRideCheckListActivity.class);
//        intent.putExtra(TRIP_ID, getPresenter().getTrip_id());
//        intent.putExtra(LONGITUDE_END_RIDE_ID, getPresenter().getCurrentUserLocation().getLongitude());
//        intent.putExtra(LATITUDE_END_RIDE_ID, getPresenter().getCurrentUserLocation().getLatitude());
//        startActivityForResult(intent, REQUEST_END_RIDE_CHECKLIST_FOR_LOGOUT);
//
//    }


    @NonNull
    @Override
    protected LogOutAfterEndingRideActivityPresenter getPresenter() {
        return logOutAfterEndingRideActivityPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_log_out_after_end_ride;
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        requestLocationUpdates();
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void requestLocationUpdates() {
        getPresenter().requestLocationUpdates();
    }

    @Override
    public void onLogOutSuccess() {
        finish();
    }

    @Override
    public void onLogOutFailure() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_END_RIDE_CHECKLIST_FOR_LOGOUT && resultCode == RESULT_OK){
            getPresenter().logOut();
        }else if(requestCode==REQUEST_END_RIDE_CHECKLIST_FOR_LOGOUT && resultCode == RESULT_CANCELED){
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }
}
