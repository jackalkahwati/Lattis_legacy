package com.lattis.ellipse.data;

import com.lattis.ellipse.data.network.model.response.parking.GetParkingFeeForFleetResponse;
import com.lattis.ellipse.data.network.store.ParkingNetworkDataStore;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Parking;
import com.lattis.ellipse.domain.repository.ParkingRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;


public class ParkingDataRepository implements ParkingRepository{

    private ParkingNetworkDataStore parkingNetworkDataStore;

    @Inject
    public ParkingDataRepository(ParkingNetworkDataStore parkingNetworkDataStore) {
        this.parkingNetworkDataStore = parkingNetworkDataStore;
    }

    @Override
    public Observable<List<Parking>> findParkings(double latitude, double longitude, float rangeInMiles) {
        return parkingNetworkDataStore.findParkings(latitude,longitude,rangeInMiles);
    }

    @Override
    public Observable<List<Parking>> getParkingSpotsForFleet(int fleetId) {
        return parkingNetworkDataStore.getParkingSpotsForFleet(fleetId);
    }


    @Override
    public Observable<GetParkingFeeForFleetResponse> getParkingFeeForFleet(Location location,
                                                                           int fleet_id) {
        return parkingNetworkDataStore.getParkingFeeForFleet(location,fleet_id);
    }




}
