package com.lattis.ellipse.presentation.ui.ride;

import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.ParkingZone;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.BaseView;

import java.util.List;

/**
 * Created by ssd3 on 3/30/17.
 */

public interface EndRideFragmentView extends BaseView {
    void onGetRideSuccess(Ride ride);
    void onGetRideFailure();
    void setUserPosition(Location location);
    void onBikeWithInBoundary();
    void onBikeWithOutBoundary();
    void onGetParkingZone(List<ParkingZone> parkingZone);


}
