package com.lattis.ellipse.presentation.ui.base.fragment.bluetooth;

import com.lattis.ellipse.domain.model.LocationSettingsResult;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.ui.base.BaseView;
import com.lattis.ellipse.presentation.ui.ride.service.util.UpdateTripData;

public interface BluetoothFragmentView extends BaseView {

    void onBluetoothEnabled();

    void requestEnableBluetooth();


    void onLocationSettingsPermissionRequired(LocationSettingsResult locationSettingsResult);
    void onLocationSettingsON();
    void onLocationSettingsNotAvailable();



    void OnRideSuccess(Ride ride);
    void OnRideFailure();


    void onLockConnected(LockModel lockModel);

    void showConnecting();
    void showDisconnected();
    void showConnected();
    void showConnectionTimeOut();

    void onLockConnectionFailed();
    void onLockConnectionAccessDenied();


    void onSignedMessagePublicKeySuccess(String signedMessage, String publicKey);
    void onSignedMessagePublicKeyFailure();

    void onSetPositionStatus(Boolean status);
    void onSetPositionFailure();

    void showLockPositionError();
    void showLockPositionSuccess(Lock.Hardware.Position position);

    void setRideDurationAndCost(UpdateTripData updateTripData);

}
