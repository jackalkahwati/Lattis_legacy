package com.lattis.ellipse.presentation.ui.biketheft;


import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.activity.BaseCloseActivity;
import com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity;
import com.lattis.ellipse.presentation.ui.utils.IsRidePaid;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomTextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.FORCE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.LATITUDE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.LONGITUDE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.TRIP_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideFragment.REPORT_THEFT_SUCCESS;

public class ReportBikeTheft extends BaseCloseActivity<ReportBikeTheftPresenter> implements ReportBikeView {
    @Inject
    ReportBikeTheftPresenter reportBikeTheftPresenter;

    private int REQUEST_CODE_FOR_FAILURE = 4342;
    private static final int REQUEST_END_RIDE_CHECKLIST = 2028;
    private static final int REQUEST_CODE_GENERAL_POP_UP = 2029;
    @BindView(R.id.rl_layer_one)
    RelativeLayout introScreen_Layout;
    @BindView(R.id.rl_layer_two)
    RelativeLayout successScreen_Layout;
    boolean isReportTheftSucess = false;
    private boolean isForceEndRide = false;
    private boolean isRideStarted = false;
    private Ride ride;

    private boolean getRideUpdated=false;
    private boolean getCurrentStatusUpdated=false;


    @BindView((R.id.rl_loading_operation))
    View report_theft_loading_operation_view;
    @BindView(R.id.label_operation_name)
    CustomTextView report_theft_operation_name;

    @BindView(R.id.label_theft_reported_end)
    CustomTextView label_theft_reported_end;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }



    @OnClick(R.id.btn_report_theft)
    public void reportButtonClicked() {
        if(ride!=null){
            getPresenter().reportBikeTheft(ride.getBikeId(),ride.getRideId());
        }else{
            showGeneralPopUp();
        }
    }

    @OnClick(R.id.label_theft_reported_end)
    public void endRideAndPayClicked() {

        if(isRideStarted) {
            launchEndRideListActivity();
        }else{
            if(ride!=null){
                showOperatorLoading(getString(R.string.bike_booking_cancelling_loader));
                getPresenter().cancelBikeReservation(ride.getBikeId(),false);
            }else{
                showGeneralPopUp();
            }
        }
    }

    private void launchEndRideListActivity() {

//        if(ride!=null){
//            Intent intent = new Intent(this, EndRideCheckListActivity.class);
//            intent.putExtra(TRIP_ID, ride.getRideId());
//            intent.putExtra(FORCE_END_RIDE_ID, true);
//            if (getPresenter().getCurrentLocation() != null) {
//                double lat = getPresenter().getCurrentLocation().getLatitude();
//                double longitude = getPresenter().getCurrentLocation().getLongitude();
//                intent.putExtra(LONGITUDE_END_RIDE_ID, longitude);
//                intent.putExtra(LATITUDE_END_RIDE_ID, lat);
//            }
//            startActivityForResult(intent, REQUEST_END_RIDE_CHECKLIST);
//        }else{
//            showGeneralPopUp();
//        }
        showOperatorLoading(getString(R.string.active_ride_ending_trip));
        getPresenter().endRide(ride.getRideId());
    }



    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.report_theft));
        getPresenter().requestLocationUpdates();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @NonNull
    @Override
    protected ReportBikeTheftPresenter getPresenter() {
        return reportBikeTheftPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_report_bike_theft;
    }

    @Override
    public void reportTheftSuccess() {
        isReportTheftSucess = true;
        setToolbarHeader(getString(R.string.label_reported_theft));
        introScreen_Layout.setVisibility(View.GONE);
        successScreen_Layout.setVisibility(View.VISIBLE);
        setResult(RESULT_OK);
        getPresenter().stopUpdateTripService();
    }


    @Override
    public void reportTheftFailure() {
        PopUpActivity.launchForResult(this, REQUEST_CODE_FOR_FAILURE, getString(R.string.alert_error_server_title),
                getString(R.string.alert_error_server_subtitle), "", getString(R.string.ok));

        introScreen_Layout.setVisibility(View.VISIBLE);
        successScreen_Layout.setVisibility(View.GONE);
    }

    @Override
    public void isRideStarted(boolean status) {
        this.isRideStarted = status;
        getCurrentStatusUpdated=true;
        giveNameToEndRideLabel();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onRideSuccess(Ride ride) {
        this.ride=ride;
        getRideUpdated=true;
        giveNameToEndRideLabel();
    }

    @Override
    public void onRideFailure() {
        showGeneralPopUp();
    }

    @Override
    public void onGetCurrentStatusFailure() {
        showGeneralPopUp();
    }

    @Override
    public void onEndRide() {
        getPresenter().disconnectAllLocks();
    }

    @Override
    public void onCancelBikeSuccess() {
        getPresenter().disconnectAllLocks();
    }

    @Override
    public void onCancelBikeFail() {
        getPresenter().disconnectAllLocks();
    }

    @Override
    public void onLockDisconnectionSuccess() {
        finishActivityWithEndOrCancelRide();
    }

    @Override
    public void onLockDisconnectionFail() {
        finishActivityWithEndOrCancelRide();
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_END_RIDE_CHECKLIST && resultCode == RESULT_OK) {
            finishActivityWithEndOrCancelRide();
        }else if(requestCode==REQUEST_CODE_GENERAL_POP_UP){
            finish();
        }
    }


    private void finishActivityWithEndOrCancelRide(){
        Intent intent = new Intent();
        intent.putExtra(REPORT_THEFT_SUCCESS,true);
        setResult(RESULT_OK,intent);
        finish();
    }

    private void showGeneralPopUp(){
        hideOperatorLoading();
        PopUpActivity.launchForResult(this, REQUEST_CODE_GENERAL_POP_UP, getString(R.string.alert_error_server_title),
                getString(R.string.alert_error_server_subtitle), null, getString(R.string.ok));
    }

    private void showOperatorLoading(String message) {
        report_theft_loading_operation_view.setVisibility(View.VISIBLE);
        report_theft_operation_name.setText(message);
    }

    private void hideOperatorLoading() {
        report_theft_loading_operation_view.setVisibility(View.GONE);
    }

    private void giveNameToEndRideLabel(){
        if(getRideUpdated && getCurrentStatusUpdated){
            if(isRideStarted){
                if(ride!=null && IsRidePaid.isRidePaidForFleet(ride.getBike_fleet_type())){
                    label_theft_reported_end.setText(getString(R.string.label_theft_reported_payment_end_ride));
                }else{
                    label_theft_reported_end.setText(getString(R.string.label_theft_reported_no_payment_end_ride));
                }
            }
        }
    }

}
