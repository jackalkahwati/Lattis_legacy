package com.lattis.ellipse.presentation.ui.history;

import com.lattis.ellipse.data.network.model.response.history.RideHistoryDataResponse;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.presentation.ui.base.BaseView;

import java.util.List;

/**
 * Created by lattis on 18/08/17.
 */

public interface TripDetailsView  extends BaseView{
    void setTripDetails(RideHistoryDataResponse rideHistoryDataResponse);
    void onGetCardSuccess(List<Card> cards);
    void onGetCardFailure();
}
