package com.lattis.ellipse.presentation.ui.bike;

import com.lattis.ellipse.data.network.model.response.GetCurrentUserStatusResponse;
import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.data.network.model.response.ride.UpdateTripResponse;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.ui.base.fragment.bluetooth.BluetoothFragmentView;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.List;


public interface BikeDirectionFragmentView extends BluetoothFragmentView {
    void setTime(String time);
    void setUserPosition(Location location);
    void onStartRideSuccess();
    void onStartRideFail();


    void onLockConnected(LockModel lockModel);
    void showConnecting();
    void onLockConnectionFailed();
    void onLockConnectionAccessDenied();



    void OnSignedMessagePublicKeySuccess(String signedMessage, String publicKey);
    void OnSignedMessagePublicKeyFailure();


    void onSaveLockSuccess(Lock lock);
    void onSaveLockFailure();


    void OnDisconnectSuccess(boolean endRide);
    void OnDisconnectFailure(boolean endRide);

    void onGetCurrentUserStatusSuccess(GetCurrentUserStatusResponse getCurrentUserStatusResponse);
    void onGetCurrentUserStatusFailure();


    void onBikeDetailsSuccess(Bike bike);
    void onBikeUnAuthorised();
    void onBikeAlreadyRented();
    void onBikeNotFound();
    void onBikeNotAvailable();
    void onBikeDetailsFailure();

    void onGetRideSummarySuccess(RideSummaryResponse rideSummaryResponse);
    void onGetRideSummaryFailure();

    void onSaveRideSuccess(Ride ride);
    void onSaveRideFailure();

    void onTripDataSuccess(UpdateTripResponse updateTripResponse);
    void onTripDataFailure();

    void onCancelBikeSuccess();
    void onCancelBikeFail();

    void onLockConnectionStatus(Boolean status);


}
