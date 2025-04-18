package com.lattis.ellipse.data.network.store;

import com.lattis.ellipse.data.network.api.ParkingApi;
import com.lattis.ellipse.data.network.model.body.parking.GetParkingZoneBody;
import com.lattis.ellipse.data.network.model.mapper.ParkingZoneResponseMapper;
import com.lattis.ellipse.domain.model.ParkingZone;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ParkingZoneDataStore {

    private ParkingApi parkingApi;
    private ParkingZoneResponseMapper parkingZoneResponseMapper;


    @Inject
    public ParkingZoneDataStore(ParkingApi parkingApi,
                                ParkingZoneResponseMapper parkingZoneResponseMapper) {
        this.parkingApi = parkingApi;
        this.parkingZoneResponseMapper = parkingZoneResponseMapper;
    }

    public Observable<List<ParkingZone>> getParkingZone(int fleetId) {

        return this.parkingApi.getParkingZone(new GetParkingZoneBody(fleetId)).map(response -> {
            List<ParkingZone> parkingZone = new ArrayList<>();
            parkingZone.addAll(parkingZoneResponseMapper.mapIn(response.getParkingZoneResponse()));
            return parkingZone;
        });
    }

}
