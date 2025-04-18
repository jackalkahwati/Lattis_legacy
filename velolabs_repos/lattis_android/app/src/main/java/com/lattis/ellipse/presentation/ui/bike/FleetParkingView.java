package com.lattis.ellipse.presentation.ui.bike;

import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Parking;
import com.lattis.ellipse.domain.model.ParkingZone;
import com.lattis.ellipse.presentation.ui.base.BaseView;

import java.util.List;

/**
 * Created by lattis on 24/05/17.
 */

public interface FleetParkingView extends BaseView {

    void onFindingParkingSuccess(List<Parking> parkings);

    void onFindingParkingFailure();

    void onFindingZoneSuccess(List<ParkingZone> parkingZone);

    void setUserPosition(Location location);

}
