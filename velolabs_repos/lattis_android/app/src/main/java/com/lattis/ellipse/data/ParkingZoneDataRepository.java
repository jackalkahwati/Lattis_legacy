package com.lattis.ellipse.data;

import com.lattis.ellipse.data.network.store.ParkingZoneDataStore;
import com.lattis.ellipse.domain.model.ParkingZone;
import com.lattis.ellipse.domain.repository.ParkingZoneRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;


public class ParkingZoneDataRepository implements ParkingZoneRepository{

    private ParkingZoneDataStore parkingZoneDataStore;

    @Inject
    public ParkingZoneDataRepository(ParkingZoneDataStore parkingZoneDataStore) {
        this.parkingZoneDataStore = parkingZoneDataStore;
    }


    @Override
    public Observable<List<ParkingZone>> getParkingZone(int fleetID) {
        return parkingZoneDataStore.getParkingZone(fleetID);
    }
}
