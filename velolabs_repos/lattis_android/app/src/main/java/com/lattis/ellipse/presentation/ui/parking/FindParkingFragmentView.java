package com.lattis.ellipse.presentation.ui.parking;

import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Parking;
import com.lattis.ellipse.domain.model.ParkingZone;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.BaseView;
import com.lattis.ellipse.presentation.ui.base.fragment.bluetooth.BluetoothFragmentView;

import java.util.List;

/**
 * Created by ssd3 on 3/27/17.
 */

public interface FindParkingFragmentView extends BluetoothFragmentView {

    void setUserPosition(Location location);
    void onFindingParkingSuccess(List<Parking> parkings);
    void onFindingParkingFailure();
    void onFindingZoneSuccess(List<ParkingZone> parkingZone);

}
