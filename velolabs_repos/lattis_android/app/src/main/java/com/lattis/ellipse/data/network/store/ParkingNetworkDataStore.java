package com.lattis.ellipse.data.network.store;

import com.lattis.ellipse.data.network.api.ParkingApi;
import com.lattis.ellipse.data.network.model.body.parking.FindParkingBody;
import com.lattis.ellipse.data.network.model.body.parking.GetParkingFeeForFleetBody;
import com.lattis.ellipse.data.network.model.body.parking.GetParkingZoneBody;
import com.lattis.ellipse.data.network.model.mapper.ParkingResponseMapper;
import com.lattis.ellipse.data.network.model.response.parking.GetParkingFeeForFleetResponse;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Parking;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ParkingNetworkDataStore {

    private ParkingApi parkingApi;
    private ParkingResponseMapper parkingResponseMapper;


    @Inject
    public ParkingNetworkDataStore(ParkingApi parkingApi,
                                   ParkingResponseMapper parkingResponseMapper) {
        this.parkingApi = parkingApi;
        this.parkingResponseMapper = parkingResponseMapper;
    }

    public Observable<List<Parking>> findParkings(double latitude, double longitude, float rangeInMiles ){

        return this.parkingApi.findParkings(new FindParkingBody(latitude,longitude,rangeInMiles)).map(response -> {
            List<Parking> parkings = new ArrayList<>();
            parkings.addAll(parkingResponseMapper.mapIn(response.getFindParkingResponse()));
            return parkings;
        });
    }

    public Observable<List<Parking>> getParkingSpotsForFleet(int fleetId ){

        return this.parkingApi.getParkingSpotForFleet(new GetParkingZoneBody(fleetId)).map(response -> {
            List<Parking> parkings = new ArrayList<>();
            parkings.addAll(parkingResponseMapper.mapIn(response.getFindParkingResponse()));
            return parkings;
        });
    }


    public Observable<GetParkingFeeForFleetResponse> getParkingFeeForFleet(Location location,
                                                                           int fleet_id) {
        return this.parkingApi.getParkingFeeForFleet(new GetParkingFeeForFleetBody(location,fleet_id));
    }

}
