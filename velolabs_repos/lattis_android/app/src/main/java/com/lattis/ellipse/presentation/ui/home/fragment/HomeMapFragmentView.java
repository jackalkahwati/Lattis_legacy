package com.lattis.ellipse.presentation.ui.home.fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.lattis.ellipse.data.network.model.response.GetCurrentUserStatusResponse;
import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.LocationSettingsResult;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by ssd3 on 3/29/17.
 */

public interface HomeMapFragmentView extends BaseView {

    void onGetCurrentUserStatusSuccess(GetCurrentUserStatusResponse getCurrentUserStatusResponse);
    void onGetCurrentUserStatusFailure();

    void onSaveRideSuccess(Ride ride);
    void onSaveRideFailure();

    void onBikeDetailsSuccess(Bike bike);
    void onBikeDetailsFailure();

    void onGetRideSummarySuccess(RideSummaryResponse rideSummaryResponse);
    void onGetRideSummaryFailure();


    void onRideDeleted();


    void onRideSuccess(Ride ride);
    void onRideFailure();


    void onLocationSettingsPermissionRequired(LocationSettingsResult locationSettingsResult);
    void onLocationSettingsON();
    void onLocationSettingsNotAvailable();

    void onAppUpdateAvailable(AppUpdateManager appUpdateManager, AppUpdateInfo appUpdateInfo);
    void onAppUpdateNotAvailable();

}
