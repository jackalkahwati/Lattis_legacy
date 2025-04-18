package com.lattis.ellipse.presentation.ui.history;

import com.lattis.ellipse.data.network.model.response.history.RideHistoryDataResponse;
import com.lattis.ellipse.presentation.ui.base.BaseView;

import java.util.List;

/**
 * Created by ssd3 on 8/16/17.
 */

public interface RideHistoryListingActivityView extends BaseView {
    void onRideHistorySuccess(List<RideHistoryDataResponse> rideHistoryDataResponses);
    void onRideHistoryFailure();
    void onNoRideHistory();
    void showOperationLoading();
    void hideOperationLoading();
}
