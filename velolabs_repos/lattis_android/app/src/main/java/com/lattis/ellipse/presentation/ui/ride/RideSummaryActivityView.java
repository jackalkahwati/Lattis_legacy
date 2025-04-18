package com.lattis.ellipse.presentation.ui.ride;

import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by ssd3 on 7/18/17.
 */

public interface RideSummaryActivityView extends BaseView {

    void onGetRideSuccess(Ride ride);
    void onGetRideFailure();
    void onGetRideSummarySuccess(RideSummaryResponse rideSummaryResponse);
    void onGetRideSummaryFailure();
    void onRideRatingSuccess();
    void onRideRatingFailure();
}
