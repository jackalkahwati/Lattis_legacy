package com.lattis.ellipse.domain.repository;

import com.lattis.ellipse.data.network.model.response.parking.GetParkingFeeForFleetResponse;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Parking;

import java.util.List;

import io.reactivex.Observable;


public interface ParkingRepository {

    Observable<List<Parking>> findParkings(double latitude, double longitude, float rangeInMiles);

    Observable<List<Parking>> getParkingSpotsForFleet(int fleetId);

    Observable<GetParkingFeeForFleetResponse> getParkingFeeForFleet(Location location,
                                                                    int fleet_id);

}
