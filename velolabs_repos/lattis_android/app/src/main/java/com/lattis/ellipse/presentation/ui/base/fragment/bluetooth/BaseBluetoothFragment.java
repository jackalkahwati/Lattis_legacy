package com.lattis.ellipse.presentation.ui.base.fragment.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.lattis.ellipse.domain.model.LocationSettingsResult;
import com.lattis.ellipse.presentation.ui.base.fragment.BaseFragment;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;

import io.lattis.ellipse.R;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static android.app.Activity.RESULT_OK;
import static com.lattis.ellipse.presentation.ui.base.fragment.bluetooth.BaseBluetoothFragmentPermissionsDispatcher.getLocationPermissionWithPermissionCheck;

@RuntimePermissions
public abstract class BaseBluetoothFragment<Presenter extends BluetoothFragmentPresenter> extends BaseFragment<Presenter>
        implements BluetoothFragmentView {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 5647;
    private final int REQUEST_CHECK_LOCATION_SETTINGS = 2106;
    private final int REQUEST_CODE_SYSTEM_LOCATION_PERMISSION_DENIED = 2107;
    private final int REQUEST_CODE_LOCATION_PERMISSION_DENIED = 2108;


    @Override
    public void requestEnableBluetooth() {
        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                REQUEST_CODE_ENABLE_BLUETOOTH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ENABLE_BLUETOOTH && resultCode == RESULT_OK){
            getPresenter().onBluetoothEnabled();
            onBluetoothEnabled();
        }else if(requestCode == REQUEST_CHECK_LOCATION_SETTINGS) {
            if (resultCode == RESULT_OK) {
                getPresenter().onLocationSettingsON();
                onLocationSettingsON();
            } else {
                PopUpActivity.launchForResultFromFragment(this,getActivity(), REQUEST_CODE_SYSTEM_LOCATION_PERMISSION_DENIED, getString(R.string.notice),
                        getString(R.string.privacy_location_alert_text), "", getString(R.string.privacy_location_submit_button));
            }
        }else if (requestCode==REQUEST_CODE_SYSTEM_LOCATION_PERMISSION_DENIED ){
            getPresenter().getLocationSetting();
        }else if(requestCode == REQUEST_CODE_LOCATION_PERMISSION_DENIED){
            getLocationPermissionWithPermissionCheck(this);
        }
    }

    @Override
    public void onBluetoothEnabled() {

    }


    protected void checkForLocation() {
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
            startIntentSenderForResult(resolvable.getResolution().getIntentSender(),REQUEST_CHECK_LOCATION_SETTINGS , null, 0, 0, 0, null);
        } catch (IntentSender.SendIntentException e) {
            // Ignore the error.
            onLocationSettingsNotAvailable();
        }
    }

    @Override
    public void onLocationSettingsON() {

    }

    @Override
    public void onLocationSettingsNotAvailable() {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PopUpActivity.launchForResultFromFragment(this,getActivity(), REQUEST_CODE_LOCATION_PERMISSION_DENIED, getString(R.string.notice),
                    getString(R.string.privacy_location_alert_text), "", getString(R.string.privacy_location_submit_button));
        }
        BaseBluetoothFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}
