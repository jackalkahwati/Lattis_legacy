package com.lattis.ellipse.presentation.ui.ride;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.LocationSettingsResult;
import com.lattis.ellipse.presentation.ui.base.activity.BaseCloseActivity;
import com.lattis.ellipse.presentation.ui.utils.ImageCompressor;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomTextView;
import com.sandrios.sandriosCamera.internal.SandriosCamera;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;
import com.sandrios.sandriosCamera.internal.ui.model.Media;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivityPermissionsDispatcher.getLocationPermissionWithPermissionCheck;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivityPermissionsDispatcher.takePictureWithPermissionCheck;

@RuntimePermissions
public class EndRideCheckListActivity extends BaseCloseActivity<EndRideCheckListActivityPresenter> implements EndRideCheckListActivityView {

    private final String TAG = EndRideCheckListActivity.class.getName();


    public final static String TRIP_ID = "TRIP_ID";
    public final static String LATITUDE_END_RIDE_ID = "LATITUDE_END_RIDE_ID";
    public final static String LONGITUDE_END_RIDE_ID = "LONGITUDE_END_RIDE_ID";
    public final static String HAS_ACCURACY_END_RIDE_ID = "HAS_ACCURACY_END_RIDE_ID";
    public final static String ACCURACY_END_RIDE_ID = "ACCURACY_END_RIDE_ID";
    public final static String PARKING_END_RIDE_ID = "PARKING_END_RIDE_ID";
    public final static String FLEET_ID = "FLEET_ID";
    public final static String FLEET_TYPE = "FLEET_TYPE";
    public final static String FORCE_END_RIDE_ID = "FORCE_END_RIDE_ID";
    public final static String END_RIDE_ID_LOADING_STRING = "END_RIDE_ID_LOADING_STRING";
    public final static String LOCK_BATTERY = "LOCK_BATTERY";
    private final static int PERMISSION_CODE_CAMERA = 2909;
    private final static int REQUEST_UPLOAD_FAILURE_CODE = 2013;
    private final static int REQUEST_END_RIDE_FAILURE_POP_UP = 2014;
    private final static int REQUEST_UPDATE_PAYMENT_DETAILS = 2015;
    private static final int REQUEST_CHECK_LOCATION_SETTINGS = 2016;
    private static final int REQUEST_CODE_SYSTEM_LOCATION_PERMISSION_DENIED = 2017;
    private static final int REQUEST_CODE_LOCATION_PERMISSION_DENIED = 2018;

    public static final String END_RIDE_PAYMENT_FAILURE="END_RIDE_PAYMENT_FAILURE";


    @Inject
    EndRideCheckListActivityPresenter presenter;

    @BindView(R.id.cb_next_active)
    RelativeLayout cb_next_active;

    @BindView(R.id.iv_camera)
    ImageView cameraImageView;
    @BindView((R.id.rl_loading_operation))
    View end_ride_check_list_loading_operation_view;
    @BindView(R.id.label_operation_name)
    CustomTextView loading_operation_name;


    @OnClick(R.id.cb_next_active)
    public void nextClicked() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            takePictureWithPermissionCheck(this);
        }else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            takePictureWithPermissionCheck(this);
        }else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            takePictureWithPermissionCheck(this);
        }else{
            takePicture();
        }

    }

    @NonNull
    @Override
    protected EndRideCheckListActivityPresenter getPresenter() {
        return presenter;
    }

    @Override
    public void onEndTripSuccess() {
        getPresenter().stopUpdateTripService();
        getPresenter().disconnectAllLocks();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPresenter().cancelAllSubscription();
    }

    @Override
    public void onEndTripFailure() {
        hideProgressLoading();
        PopUpActivity.launchForResult(this, REQUEST_END_RIDE_FAILURE_POP_UP,getString(R.string.alert_error_server_title),
                getString(R.string.alert_error_server_subtitle),null,getString(R.string.ok));

    }

    @Override
    public void onEndTripStripeConnectFailure() {
        onEndTripFailure();
    }

    @Override
    public void onEndTripPaymentFailure() {
        hideProgressLoading();
        PopUpActivity.launchForResult(this,REQUEST_UPDATE_PAYMENT_DETAILS ,getString(R.string.active_ride_jamming_title),
                getString(R.string.end_ride_strip_error_subtitle),null,getString(R.string.end_ride_strip_error_action));

    }

    @Override
    public void onUploadImageSuccess(String imageUrl) {
        Log.e("EndRideCheckListAct","onUploadImageSuccess");
        hideProgressLoading();
        getPresenter().setForceEndRide();
        getLocationPermissionWithPermissionCheck(this);
    }

    @Override
    public void onUploadImageFailure() {
        Log.e("EndRideCheckListAct","onUploadImageFailure");
        hideProgressLoading();
        PopUpActivity.launchForResult(this,REQUEST_UPLOAD_FAILURE_CODE ,getString(R.string.alert_error_server_title),
                getString(R.string.alert_error_server_subtitle),null,getString(R.string.ok));
    }

    @Override
    public void setUserPosition(Location location) {
        getPresenter().setUserLocation(location);
        if(getPresenter().isForceEndRide()){
            getPresenter().requestStopLocationUpdates();
            showEndRideLoading(getPresenter().getLoadingString());
            forceEndRide();
        }
    }


    @Override
    public void onLockDisconnectionSuccess() {
        Log.e("EndRideCheckListAct","onLockDisconnectionSuccess");
        finishActivity();
    }

    @Override
    public void onLockDisconnectionFail() {
        Log.e("EndRideCheckListAct","onLockDisconnectionFail");
        hideProgressLoading();
        finishActivity();
    }


    private void finishActivity(){
        getPresenter().requestStopLocationUpdates();
        setResult(RESULT_OK);
        finish();
    }



    @Override
    protected int getViewStubLayoutId() {
        return super.getViewStubLayoutId();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.end_ride_checklist_header_label));
        cb_next_active.setEnabled(true);
    }

    private void showLoading(String message) {
        end_ride_check_list_loading_operation_view.setVisibility(View.VISIBLE);
        loading_operation_name.setText(message);
    }

    @Override
    public void showEndRideLoading(String loadingString) {
        if(loadingString==null){
            showLoading(getString(R.string.active_ride_ending_trip));
        }else{
            showLoading(loadingString);
        }
    }

    private void hideProgressLoading() {
        end_ride_check_list_loading_operation_view.setVisibility(View.GONE);
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_end_ride_checklist;
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }



    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public void takePicture() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        SandriosCamera
                .with()
                .setShowPicker(false)
                .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO)
                .enableImageCropping(false)
                .launchCamera(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (resultCode == Activity.RESULT_OK
                && requestCode == SandriosCamera.RESULT_CODE
                && data != null) {
            if (data.getSerializableExtra(SandriosCamera.MEDIA) instanceof Media) {
                Media media = (Media) data.getSerializableExtra(SandriosCamera.MEDIA);
                Log.e("File", "" + media.getPath());
                Log.e("Type", "" + media.getType());
                String filePath = media.getPath();
                filePath = ImageCompressor.resizeAndCompressImageBeforeSend(this, filePath, "damage");
                if(filePath!=null) {
                    showLoading(getString(R.string.uploading));
                    getPresenter().uploadImage(filePath);
                }
            }
        }else if(requestCode == REQUEST_END_RIDE_FAILURE_POP_UP){
            getPresenter().requestStopLocationUpdates();
            setResult(RESULT_CANCELED);
            finish();
        } else if (requestCode == REQUEST_UPDATE_PAYMENT_DETAILS) {
            Intent intent = new Intent();
            if (resultCode == RESULT_OK) {
                intent.putExtra(END_RIDE_PAYMENT_FAILURE, true);
            }
            getPresenter().requestStopLocationUpdates();
            setResult(RESULT_CANCELED, intent);
            finish();
        }else if(requestCode == REQUEST_CHECK_LOCATION_SETTINGS) {
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
    public void forceEndRide() {
        Log.e("EndRideCheckListAc","forceEndRide");
        getPresenter().endRide();
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

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
        if(getPresenter().isForceEndRide()){
            showEndRideLoading(getPresenter().getLoadingString());
        }
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
                    getString(R.string.privacy_location_alert_text), "", getString(R.string.privacy_location_submit_button));
            logCustomException(new Throwable("Permission denied: End Ride"));
        }
        EndRideCheckListActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}
