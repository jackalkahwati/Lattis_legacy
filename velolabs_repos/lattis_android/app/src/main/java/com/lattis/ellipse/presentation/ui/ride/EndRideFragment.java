package com.lattis.ellipse.presentation.ui.ride;

import android.Manifest;
import android.content.Intent;
import android.os.Handler;
import android.text.Html;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.lattis.ellipse.Utils.CurrencyUtil;
import com.lattis.ellipse.Utils.UtilHelper;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.Parking;
import com.lattis.ellipse.domain.model.ParkingZone;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.fragment.BaseFragment;
import com.lattis.ellipse.presentation.ui.bike.BikeInfoActivity;
import com.lattis.ellipse.presentation.ui.parking.ParkingMapDirectionFragment;
import com.lattis.ellipse.presentation.ui.payment.AddCardActivity;
import com.lattis.ellipse.presentation.ui.ride.fee.ParkingFeeActivity;
import com.lattis.ellipse.presentation.ui.ridemenu.RideMenuActivity;
import com.lattis.ellipse.presentation.ui.utils.IsRidePaid;
import com.lattis.ellipse.presentation.view.CustomTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.lattis.ellipse.presentation.ui.bike.BikeInfoActivity.ARGS_IS_BIKE_RESERVED;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.ACCURACY_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.END_RIDE_PAYMENT_FAILURE;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.FLEET_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.FLEET_TYPE;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.FORCE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.HAS_ACCURACY_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.LATITUDE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.LOCK_BATTERY;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.LONGITUDE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.PARKING_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.TRIP_ID;
import static com.lattis.ellipse.presentation.ui.ride.fee.ParkingFeeActivityPresenter.ARGS_END_RIDE;
import static com.lattis.ellipse.presentation.ui.ride.fee.ParkingFeeActivityPresenter.ARGS_FIND_ZONES;

/**
 * Created by ssd3 on 3/30/17.
 */

@RuntimePermissions
public class EndRideFragment extends BaseFragment<EndRideFragmentPresenter> implements EndRideFragmentView {

    private final String TAG = EndRideFragment.class.getName();
    private static final int REQUEST_END_RIDE_CHECKLIST = 888;
    private static final int REQUEST_RIDE_SUMMARY = 999;
    private static final int REQUEST_PARKING_FEE_ACTIVITY =6839;
    private static final int REQUEST_RIDE_MENU = 1014;
    public static final String REPORT_THEFT_SUCCESS="REPORT_THEFT_SUCCESS";
    public static final String DAMAGE_REPORT_SUCCESS="DAMAGE_REPORT_SUCCESS";
    private static final int REQUEST_CODE_ADD_CARD_ACTIVITY = 5092;
    private Integer lock_battery=null;
    private final int REQUEST_CODE_FOR_BIKE_INFO = 7438;


    Timer timer = null;
    private Ride ride;
    private Location location;
    private Fragment parentFragment;
    private Parking parking;
    private List<ParkingZone> zoneBoundaryList = new ArrayList<ParkingZone>();
    @BindView(R.id.tv_ride_time)
    CustomTextView tv_ride_time;
    @BindView(R.id.cv_connecting_label)
    CustomTextView cv_connecting_label;

    @BindView(R.id.cv_riding_bike_label)
    CustomTextView cv_riding_bike_label;

    @BindView(R.id.tv_ride_cost)
    CustomTextView tv_ride_cost;

    @Inject
    EndRideFragmentPresenter presenter;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @NonNull
    @Override
    protected EndRideFragmentPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getFragmentLayoutId() {
        return R.layout.end_ride_fragment;
    }

    @OnClick(R.id.cv_riding_bike_label)
    public void showBikeDetails(){
        Gson gson = new Gson();
        String info = gson.toJson(getPresenter().getBikeModelMapper().mapOut(ride));
        startActivityForResult(new Intent(getActivity(), BikeInfoActivity.class)
                .putExtra(ARGS_IS_BIKE_RESERVED,true)
                .putExtra(BikeInfoActivity.ARGS_BIKE_DETAILS, info), REQUEST_CODE_FOR_BIKE_INFO);
    }

    @OnClick(R.id.iv_settings_menu)
    public void onMenuClicked() {

        if (parentFragment instanceof ActiveRideFragment) {
            ((ActiveRideFragment) parentFragment).setWhetherConnectionRequiredOnResume(false);
        } else if (parentFragment instanceof ParkingMapDirectionFragment) {
            ((ParkingMapDirectionFragment) parentFragment).setWhetherConnectionRequiredOnResume(false);
        }

        Gson gson = new Gson();
        String json = gson.toJson(ride);
        startActivityForResult(new Intent(getActivity(), RideMenuActivity.class)
                .putExtra("RIDE_DETAILS", json), REQUEST_RIDE_MENU);
    }

    @OnClick(R.id.end_ride_btn)
    public void endRideClicked() {
        if (parentFragment instanceof ActiveRideFragment) {
            Lock.Hardware.Position lockPosition = ((ActiveRideFragment) parentFragment).getLockStatus();
            if (lockPosition == null || (lockPosition != Lock.Hardware.Position.LOCKED)) {
                ((ActiveRideFragment) parentFragment).showLockPositionErrorPopUp(getString(R.string.lock_position_error_pop_title),getString(R.string.lock_position_error_pop_subtitle1));
                return;
            }
            lock_battery =  ((ActiveRideFragment) parentFragment).getLockBattery();
            ((ActiveRideFragment) parentFragment).setWhetherConnectionRequiredOnResume(false);
            ((ActiveRideFragment) parentFragment).setEllipseLockUnlockButton(false);
        } else if (parentFragment instanceof ParkingMapDirectionFragment) {

            Lock.Hardware.Position lockPosition = ((ParkingMapDirectionFragment) parentFragment).getLockStatus();
            if (lockPosition == null || (lockPosition != Lock.Hardware.Position.LOCKED)) {
                ((ParkingMapDirectionFragment) parentFragment).showLockPositionErrorPopUp(getString(R.string.lock_position_error_pop_title),getString(R.string.lock_position_error_pop_subtitle1));
                return;
            }
            lock_battery =  ((ParkingMapDirectionFragment) parentFragment).getLockBattery();
            ((ParkingMapDirectionFragment) parentFragment).setWhetherConnectionRequiredOnResume(false);
            ((ParkingMapDirectionFragment) parentFragment).setEllipseLockUnlockButton(false);
        }

        launchParkingFeeActivity();
    }

    private void launchParkingFeeActivity(){

        if(ride==null){
            return;
        }

        Intent intent = new Intent(getActivity(), ParkingFeeActivity.class);
        intent.putExtra(FLEET_ID, ride.getBike_fleet_id());
        intent.putExtra(FLEET_TYPE, ride.getBike_fleet_type());
        if (location != null) {
            double lat = location.getLatitude();
            double longitude = location.getLongitude();
            intent.putExtra(LONGITUDE_END_RIDE_ID, longitude);
            intent.putExtra(LATITUDE_END_RIDE_ID, lat);
            intent.putExtra(HAS_ACCURACY_END_RIDE_ID, location.hasAccuracy());
            intent.putExtra(ACCURACY_END_RIDE_ID, location.getAccuracy());
        }
        startActivityForResult(intent, REQUEST_PARKING_FEE_ACTIVITY);
    }

    void launchEndRideCheckListActivity(boolean isForceEndRide) {
        Intent intent = new Intent(getActivity(), EndRideCheckListActivity.class);
        intent.putExtra(TRIP_ID, this.ride.getRideId());
        if (location != null) {
            double lat = location.getLatitude();
            double longitude = location.getLongitude();
            intent.putExtra(LONGITUDE_END_RIDE_ID, longitude);
            intent.putExtra(LATITUDE_END_RIDE_ID, lat);
        }
        if(lock_battery!=null)
            intent.putExtra(LOCK_BATTERY, lock_battery);

        if (parking != null) {
            intent.putExtra(PARKING_END_RIDE_ID, parking.getParking_spot_id());
        }

        if (isForceEndRide) {
            intent.putExtra(FORCE_END_RIDE_ID, isForceEndRide);
        }
        startActivityForResult(intent, REQUEST_END_RIDE_CHECKLIST);
    }

    void launchParkingFragment() {
        stopTimer();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (parentFragment instanceof ActiveRideFragment) {
                    ((ActiveRideFragment) parentFragment).showParkingFragment();
                } else if (parentFragment instanceof ParkingMapDirectionFragment) {
                    // error, execution should never reach here.
                }
            }
        }, 100);
    }


    public void setParentFragment(Fragment parentFragment) {
        this.parentFragment = parentFragment;
    }

    public void showConnectingLabel(boolean value) {
        if (value) {
            cv_connecting_label.setVisibility(View.VISIBLE);
        } else {
            cv_connecting_label.setVisibility(View.GONE);
        }
    }

    public void launchRideSummaryFragment() {
        stopTimer();
        if (parentFragment instanceof ActiveRideFragment) {
            ((ActiveRideFragment) parentFragment).showRideSummary();
        } else if (parentFragment instanceof ParkingMapDirectionFragment) {
            ((ParkingMapDirectionFragment) parentFragment).showRideSummary();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_END_RIDE_CHECKLIST && resultCode == RESULT_OK) {
            launchRideSummary();
        } else if (requestCode == REQUEST_END_RIDE_CHECKLIST && resultCode == RESULT_CANCELED) {
            if(data!=null){
                if(data.hasExtra(END_RIDE_PAYMENT_FAILURE)){
                    if(data.getExtras().getBoolean(END_RIDE_PAYMENT_FAILURE)){
                        startActivityForResult(new Intent(getActivity(), AddCardActivity.class), REQUEST_CODE_ADD_CARD_ACTIVITY);
                        return;
                    }
                }
            }
            getRideDetails();
            requestLocationUpdates();
            if (parentFragment instanceof ActiveRideFragment) {
                ((ActiveRideFragment) parentFragment).setWhetherConnectionRequiredOnResume(true);
            } else if (parentFragment instanceof ParkingMapDirectionFragment) {
                ((ParkingMapDirectionFragment) parentFragment).setWhetherConnectionRequiredOnResume(true);
            }
        }else if (requestCode == REQUEST_CODE_ADD_CARD_ACTIVITY) {
            getRideDetails();
            requestLocationUpdates();
            if (parentFragment instanceof ActiveRideFragment) {
                ((ActiveRideFragment) parentFragment).setWhetherConnectionRequiredOnResume(true);
            } else if (parentFragment instanceof ParkingMapDirectionFragment) {
                ((ParkingMapDirectionFragment) parentFragment).setWhetherConnectionRequiredOnResume(true);
            }
        } else if (requestCode == REQUEST_RIDE_MENU) {
            if(resultCode==RESULT_OK){
                if(data!=null){
                    if(data.hasExtra(REPORT_THEFT_SUCCESS)){
                        if(data.getExtras().getBoolean(REPORT_THEFT_SUCCESS)){
                            launchRideSummary();
                        }
                    }else if(data.hasExtra(DAMAGE_REPORT_SUCCESS)){
                        if(data.getExtras().getBoolean(DAMAGE_REPORT_SUCCESS)){
                            launchRideSummary();
                        }
                    }else{
                        if (parentFragment instanceof ActiveRideFragment) {
                            ((ActiveRideFragment) parentFragment).setWhetherConnectionRequiredOnResume(true);
                        } else if (parentFragment instanceof ParkingMapDirectionFragment) {
                            ((ParkingMapDirectionFragment) parentFragment).setWhetherConnectionRequiredOnResume(true);
                        }
                    }
                }else{
                    if (parentFragment instanceof ActiveRideFragment) {
                        ((ActiveRideFragment) parentFragment).setWhetherConnectionRequiredOnResume(true);
                    } else if (parentFragment instanceof ParkingMapDirectionFragment) {
                        ((ParkingMapDirectionFragment) parentFragment).setWhetherConnectionRequiredOnResume(true);
                    }
                }
            }else{
                if (parentFragment instanceof ActiveRideFragment) {
                    ((ActiveRideFragment) parentFragment).setWhetherConnectionRequiredOnResume(true);
                } else if (parentFragment instanceof ParkingMapDirectionFragment) {
                    ((ParkingMapDirectionFragment) parentFragment).setWhetherConnectionRequiredOnResume(true);
                }
            }
        }else if (requestCode == REQUEST_PARKING_FEE_ACTIVITY ) {
            if(resultCode == RESULT_OK) {
                if (data != null) {
                    if (data.hasExtra(ARGS_END_RIDE)) {
                        if (data.getExtras().getBoolean(ARGS_END_RIDE)) {
                            if (ride.getBike_skip_parking_image()) {
                                launchEndRideCheckListActivity(true);
                            } else {
                                launchEndRideCheckListActivity(false);
                            }
                        }
                    } else if (data.hasExtra(ARGS_FIND_ZONES)) {
                        if (data.getExtras().getBoolean(ARGS_FIND_ZONES)) {
                            launchParkingFragment();
                        }
                    }
                }
            }else{
                if (parentFragment instanceof ActiveRideFragment) {
                    ((ActiveRideFragment) parentFragment).setWhetherConnectionRequiredOnResume(true);
                } else if (parentFragment instanceof ParkingMapDirectionFragment) {
                    ((ParkingMapDirectionFragment) parentFragment).setWhetherConnectionRequiredOnResume(true);
                }
            }
        }else if (requestCode == REQUEST_CODE_FOR_BIKE_INFO){
            if (parentFragment instanceof ActiveRideFragment) {
                ((ActiveRideFragment) parentFragment).setWhetherConnectionRequiredOnResume(true);
            } else if (parentFragment instanceof ParkingMapDirectionFragment) {
                ((ParkingMapDirectionFragment) parentFragment).setWhetherConnectionRequiredOnResume(true);
            }
        }

    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);


        if (hidden) {
            // stop timer
            stopTimer();
            requestStopLocationUpdates();
        } else {
            getRideDetails();
            requestLocationUpdates();
        }
    }


    private void getRideDetails() {
        getPresenter().getRide();
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void requestLocationUpdates() {
        getPresenter().requestLocationUpdates();
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void requestStopLocationUpdates() {
        getPresenter().requestStopLocationUpdates();
    }

    @Override
    public void setUserPosition(Location location) {
        //Log.e(TAG, "User Location is " + location.getLatitude() + "  " + location.getLongitude());
        this.location = location;
    }

    @Override
    public void onBikeWithInBoundary() {
        //launchEndRideCheckListActivity(false);
    }

    @Override
    public void onBikeWithOutBoundary() {
        //launchEndRideOutOfBoundsActivity();
    }

    @Override
    public void onGetParkingZone(List<ParkingZone> parkingZone) {
        this.zoneBoundaryList = null;
        this.zoneBoundaryList = parkingZone;
    }

    @Override
    public void onGetRideSuccess(Ride ride) {
        this.ride = ride;
        cv_riding_bike_label.setText(Html.fromHtml(getString(R.string.active_ride_in_ride_with) + " <u>"+ride.getBike_bike_name()+"</u>"));
//        cv_riding_bike_label.setText(ride.getBike_bike_name());
        startTimer();
        //getPresenter().getParkingZone(ride.getBike_fleet_id());
    }

    @Override
    public void onGetRideFailure() {

    }

    public void setRideDurationAndCost(float rideCost, String currencyCode){
        if (IsRidePaid.isRidePaidForFleet(ride.getBike_fleet_type())) {
            tv_ride_cost.setText(CurrencyUtil.getCurrencySymbolByCode(currencyCode) + UtilHelper.getDotAfterNumber(Float.toString(rideCost)));
        }else{
            tv_ride_cost.setText(getString(R.string.label_cost_ride_free_default));
        }
    }

    private void stopTimer() {
        if (timer != null)
            timer.cancel();
    }


    public void launchRideSummary() {

        if (parentFragment instanceof ActiveRideFragment) {
            ((ActiveRideFragment) parentFragment).unsubscribeAllSubscription();
        } else if (parentFragment instanceof ParkingMapDirectionFragment) {
            ((ParkingMapDirectionFragment) parentFragment).unsubscribeAllSubscription();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                launchRideSummaryFragment();
            }
        }, 10);
    }

    public void setParking(Parking parking) {
        this.parking = parking;
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    setText();
                } catch (Exception e) {

                }
            }
        }, 0, 1000);
    }


    void setText() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_ride_time.setText(UtilHelper.getDurationBreakdown(getActivity(),UtilHelper.getTime() - (long) ride.getRide_booked_on()));
            }
        });
    }





    @Override
    public void onPause() {
        super.onPause();
        stopTimer();
    }
}
