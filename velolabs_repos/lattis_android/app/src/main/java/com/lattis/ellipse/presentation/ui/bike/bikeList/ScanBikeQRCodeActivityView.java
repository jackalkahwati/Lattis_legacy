package com.lattis.ellipse.presentation.ui.bike.bikeList;

import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.ui.base.activity.bluetooth.BaseBluetoothActivityView;

import java.util.List;

/**
 * Created by ssd3 on 9/5/17.
 */

public interface ScanBikeQRCodeActivityView extends BaseBluetoothActivityView {

    void onGetCardSuccess(List<Card> cards);
    void onGetCardFailure();

    void onInvalidQRCode();
    void restartScanner();

     void onBikeDetailsSuccess(Bike bike);

    void onBikeUnAuthorised();
    void onBikeAlreadyRented();
    void onBikeNotFound();
    void onBikeNotAvailable();

     void onBikeDetailsFailure();

    void startRide();
    void onStartRideSuccess();
    void onStartRideFail();


    void OnReserveBikeSuccess();
    void OnReserveBikeFail();
    void OnReserveBikeNotFound();

    void onCancelBikeSuccess();
    void onCancelBikeFail();


    ////////////////////////////////// LOCK CONNECTION CODE : START //////////////////////////////////////////////////

    void onLockScanned(LockModel lockModel);
    void onScanStart();
    void onScanStop();


    void onLockConnected(LockModel lockModel);
    void showConnecting(boolean requiresReconnection);
    void onLockConnectionFailed();
    void onLockConnectionAccessDenied();


    void OnSignedMessagePublicKeySuccess(String signedMessage, String publicKey);
    void OnSignedMessagePublicKeyFailure();


    void onSaveLockSuccess(Lock lock);
    void onSaveLockFailure();

    void OnSetPositionStatus(Boolean status);

    void onGetUserProfile();

}
