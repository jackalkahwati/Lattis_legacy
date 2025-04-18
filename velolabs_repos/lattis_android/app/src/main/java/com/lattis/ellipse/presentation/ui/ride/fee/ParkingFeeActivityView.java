package com.lattis.ellipse.presentation.ui.ride.fee;

import com.lattis.ellipse.domain.model.LocationSettingsResult;
import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by ssd3 on 8/1/17.
 */

public interface ParkingFeeActivityView extends BaseView{
    void showServerErrorUI();
    void showOutOfParkingZoneUI(float zoneFee,String currency);
    void showOutOfParkingSpotUI(float spotFee);
    void showRestrictedParkingUI();
    void showDisplinaryActionForSpotUI();
    void showDisplinaryActionForZoneUI();
    void showNormalEndRide();
    void hideOperationLoading();
    void checkForLocation();
    void onLocationSettingsPermissionRequired(LocationSettingsResult locationSettingsResult);
    void onLocationSettingsON();
    void onLocationSettingsNotAvailable();
}
