package com.lattis.ellipse.presentation.ui.ride.fee;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.common.api.ResolvableApiException;
import com.lattis.ellipse.Utils.CurrencyUtil;
import com.lattis.ellipse.domain.model.LocationSettingsResult;
import com.lattis.ellipse.presentation.ui.base.activity.BaseActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomButton;
import com.lattis.ellipse.presentation.view.CustomTextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.lattis.ellipse.presentation.ui.ride.fee.ParkingFeeActivityPermissionsDispatcher.getLocationPermissionWithPermissionCheck;
import static com.lattis.ellipse.presentation.ui.ride.fee.ParkingFeeActivityPresenter.ARGS_END_RIDE;
import static com.lattis.ellipse.presentation.ui.ride.fee.ParkingFeeActivityPresenter.ARGS_END_RIDE_VALUE;
import static com.lattis.ellipse.presentation.ui.ride.fee.ParkingFeeActivityPresenter.ARGS_FIND_ZONES;
import static com.lattis.ellipse.presentation.ui.ride.fee.ParkingFeeActivityPresenter.ARGS_FIND_ZONES_VALUE;

/**
 * Created by ssd3 on 8/1/17.
 */
@RuntimePermissions
public class ParkingFeeActivity extends BaseActivity<ParkingFeeActivityPresenter> implements ParkingFeeActivityView {

    private final String TAG = ParkingFeeActivity.class.getName();

    private static final int REQUEST_CHECK_LOCATION_SETTINGS = 2016;
    private static final int REQUEST_CODE_SYSTEM_LOCATION_PERMISSION_DENIED = 2017;
    private static final int REQUEST_CODE_LOCATION_PERMISSION_DENIED = 2018;

    @BindView(R.id.cv_title)
    CustomTextView cv_title;

    @BindView(R.id.cv_subtitle1)
    CustomTextView cv_subtitle1;


    @BindView(R.id.find_nearby_zone_btn)
    CustomButton find_nearby_zone_btn;

    @BindView(R.id.end_ride_btn)
    CustomButton end_ride_btn;

    @BindView(R.id.rl_parking_fee_details)
    RelativeLayout rl_parking_fee_details;


    @BindView((R.id.rl_loading_operation))
    View parking_fee_loading_operation_view;
    @BindView(R.id.label_operation_name)
    CustomTextView loading_operation_name;


    @OnClick(R.id.iv_close_pop_up)
    public void actionCancel(){
        setCancelAndFinish();
    }

    @OnClick(R.id.end_ride_btn)
    public void endRide(){
        Intent intent = new Intent();
        intent.putExtra(ARGS_END_RIDE,ARGS_END_RIDE_VALUE);
        setOKAndFinish(intent);
    }

    @OnClick(R.id.find_nearby_zone_btn)
    public void findZones(){
        Intent intent = new Intent();
        intent.putExtra(ARGS_FIND_ZONES,ARGS_FIND_ZONES_VALUE);
        setOKAndFinish(intent);
    }



    @Inject
    ParkingFeeActivityPresenter parkingFeeActivityPresenter;


    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @Override
    protected void configureViews() {
        super.configureViews();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showOperationLoading(getString(R.string.checking_parking_label));
    }

    public void showOperationLoading(String operationName) {
        parking_fee_loading_operation_view.setVisibility(View.VISIBLE);
        loading_operation_name.setText(operationName);
    }

    @Override
    public void hideOperationLoading() {
        parking_fee_loading_operation_view.setVisibility(View.INVISIBLE);
    }



    @NonNull
    @Override
    protected ParkingFeeActivityPresenter getPresenter() {
        return parkingFeeActivityPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_parking_fee_for_fleet;
    }


    @Override
    public void showServerErrorUI() {
        rl_parking_fee_details.setVisibility(View.VISIBLE);
        cv_title.setText(getString(R.string.get_current_status_failure_title));
        cv_subtitle1.setText(getString(R.string.get_current_status_failure_subtitle));
        find_nearby_zone_btn.setVisibility(View.GONE);
        end_ride_btn.setVisibility(View.GONE);
    }

    @Override
    public void showOutOfParkingZoneUI(float zoneFee, String currency) { // in
        rl_parking_fee_details.setVisibility(View.VISIBLE);
        cv_title.setText(getString(R.string.active_ride_out_of_zones_title));
        cv_subtitle1.setText(getString(R.string.parking_fee_public_out_of_zone_subtitle, CurrencyUtil.getCurrencySymbolByCode(currency),Float.toString(zoneFee)));
        find_nearby_zone_btn.setVisibility(View.VISIBLE);
        end_ride_btn.setVisibility(View.VISIBLE);
    }

    @Override
    public void showOutOfParkingSpotUI(float spotFee) {
//        rl_parking_fee_details.setVisibility(View.VISIBLE);
//        cv_title.setText(getString(R.string.parking_fee_public_out_of_spot_title));
//        cv_subtitle1.setText(getString(R.string.parking_fee_public_out_of_spot_subtitle,Float.toString(spotFee)));
//        find_nearby_zone_btn.setVisibility(View.VISIBLE);
//        end_ride_btn.setVisibility(View.VISIBLE);
    }

    @Override
    public void showRestrictedParkingUI() {         // in
        rl_parking_fee_details.setVisibility(View.VISIBLE);
        cv_title.setText(getString(R.string.notice));
        cv_subtitle1.setText(getString(R.string.parking_restricted_warning_message));
        find_nearby_zone_btn.setVisibility(View.VISIBLE);
        end_ride_btn.setVisibility(View.GONE);
    }

    @Override
    public void showDisplinaryActionForSpotUI() {
//        rl_parking_fee_details.setVisibility(View.VISIBLE);
//        cv_title.setText(getString(R.string.active_ride_out_of_zones_title));
//        cv_subtitle1.setText(getString(R.string.parking_fee_public_disciplinary_action_for_spot_subtitle));
//        find_nearby_zone_btn.setVisibility(View.VISIBLE);
//        end_ride_btn.setVisibility(View.VISIBLE);
    }

    @Override
    public void showDisplinaryActionForZoneUI() {   // in
        rl_parking_fee_details.setVisibility(View.VISIBLE);
        cv_title.setText(getString(R.string.active_ride_out_of_zones_title));
        cv_subtitle1.setText(getString(R.string.parking_fee_public_disciplinary_action_for_zone_subtitle));
        find_nearby_zone_btn.setVisibility(View.VISIBLE);
        end_ride_btn.setVisibility(View.VISIBLE);
    }

    @Override
    public void showNormalEndRide() {
        endRide();
    }


    private void setOKAndFinish(Intent intent){
        setResult(RESULT_OK,intent);
        finish();
    }

    private void setCancelAndFinish(){
        setResult(RESULT_CANCELED);
        finish();
    }
    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CHECK_LOCATION_SETTINGS) {
            if (resultCode == RESULT_OK) {
                onLocationSettingsON();
            } else {
                PopUpActivity.launchForResult(this, REQUEST_CODE_SYSTEM_LOCATION_PERMISSION_DENIED, getString(R.string.notice),
                        getString(R.string.privacy_location_alert_text), "", getString(R.string.privacy_location_submit_button));
            }
        }else if (requestCode==REQUEST_CODE_SYSTEM_LOCATION_PERMISSION_DENIED ){
            getPresenter().getLocationSetting();
        }else if(requestCode == REQUEST_CODE_LOCATION_PERMISSION_DENIED){
            getLocationPermissionWithPermissionCheck(this);
        }
    }

    @Override
    public void checkForLocation() {
        getLocationPermissionWithPermissionCheck(this);
    }


    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void getLocationPermission() {
        getPresenter().getLocationSetting();
    }

    @Override
    public void onLocationSettingsPermissionRequired(LocationSettingsResult locationSettingsResult) {
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            ResolvableApiException resolvable = (ResolvableApiException) locationSettingsResult.getApiException();
            resolvable.startResolutionForResult(this, REQUEST_CHECK_LOCATION_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            // Ignore the error.
            onLocationSettingsNotAvailable();
        }
    }

    @Override
    public void onLocationSettingsON() {
        getPresenter().requestLocationUpdates();
    }

    @Override
    public void onLocationSettingsNotAvailable() {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PopUpActivity.launchForResult(this, REQUEST_CODE_LOCATION_PERMISSION_DENIED, getString(R.string.notice),
                    getString(R.string.privacy_location_explanation), "", getString(R.string.privacy_location_submit_button));

            logCustomException(new Throwable("Permission denied: Parking Fee"));
        }
        ParkingFeeActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

}
