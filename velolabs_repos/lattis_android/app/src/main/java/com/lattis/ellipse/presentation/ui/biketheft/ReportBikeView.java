package com.lattis.ellipse.presentation.ui.biketheft;

import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by Velo Labs Android on 17-04-2017.
 */

public interface ReportBikeView extends BaseView {

    void reportTheftSuccess();
    void reportTheftFailure();

    void onRideSuccess(Ride ride);
    void onRideFailure();

    void isRideStarted(boolean status);
    void onGetCurrentStatusFailure();

    void onCancelBikeSuccess();
    void onCancelBikeFail();

    void onLockDisconnectionSuccess();
    void onLockDisconnectionFail();

    void onEndRide();
}
