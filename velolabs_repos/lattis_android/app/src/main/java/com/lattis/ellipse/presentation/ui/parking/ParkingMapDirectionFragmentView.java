package com.lattis.ellipse.presentation.ui.parking;

import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.domain.model.map.Direction;
import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.ui.base.fragment.bluetooth.BluetoothFragmentView;
import com.lattis.ellipse.presentation.ui.ride.service.util.UpdateTripData;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by ssd3 on 3/28/17.
 */

public interface ParkingMapDirectionFragmentView extends BluetoothFragmentView {
    void setUserPosition(Location location);
}
