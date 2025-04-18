package com.lattis.ellipse.domain.repository;

import com.lattis.ellipse.domain.model.ParkingZone;

import java.util.List;

import io.reactivex.Observable;


/**
 * Created by lattis on 24/05/17.
 */

public interface ParkingZoneRepository {
    Observable<List<ParkingZone>> getParkingZone(int fleetID);

}
