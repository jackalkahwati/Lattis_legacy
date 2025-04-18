package com.lattis.ellipse.presentation.ui.bike.bikeList;

import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.presentation.ui.base.BaseView;

import java.util.List;

/**
 * Created by lattis on 02/06/17.
 */

public interface BikeListView extends BaseView {
    void setUserPosition(Location location);
    void onFindNearBikesSuccess(List<Bike> bikes);
    void onFindAvailableBikeSuccess(List<Bike> bikes);
    void onFindBikeNoServiceAvailable();
    void onFindBikeFailure();
    void onFindBikesEmpty();
    void initializeView();
    void onGetCardSuccess(List<Card>cards);
    void onGetCardFailure();
    void hideOperatingLoading();
    void showToolTip();

    void onGetUserProfile();

}
