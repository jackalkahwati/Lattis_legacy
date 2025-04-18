package com.lattis.ellipse.presentation.ui.ride;

import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.LocationSettingsResult;
import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by ssd3 on 4/4/17.
 */

public interface EndRideCheckListActivityView extends BaseView {

    void onEndTripSuccess();

    void onEndTripFailure();

    void onEndTripPaymentFailure();

    void onEndTripStripeConnectFailure();

    void onUploadImageSuccess(String imageUrl);

    void onUploadImageFailure();

    void setUserPosition(Location location);

    void onLockDisconnectionSuccess();

    void onLockDisconnectionFail();

    void showEndRideLoading(String loadingString);

    void forceEndRide();

    void checkForLocation();
    void onLocationSettingsPermissionRequired(LocationSettingsResult locationSettingsResult);
    void onLocationSettingsON();
    void onLocationSettingsNotAvailable();
}
