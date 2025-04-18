package com.lattis.ellipse.presentation.ui.ride;

import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.presentation.ui.base.fragment.bluetooth.BluetoothFragmentView;

public interface ActiveRideFragmentView extends BluetoothFragmentView {
    void setUserPosition(Location location);
}
