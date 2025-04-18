package com.lattis.ellipse.data.network.model.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.data.network.model.response.parking.GetParkingZoneDataResponse;
import com.lattis.ellipse.data.network.model.response.parking.ParkingZoneGeometryResponse;
import com.lattis.ellipse.domain.model.ParkingZone;
import com.lattis.ellipse.domain.model.ParkingZoneGeometry;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class ParkingZoneResponseMapper extends AbstractDataMapper<GetParkingZoneDataResponse, ParkingZone> {

    @Inject
    public ParkingZoneResponseMapper() {
    }

    @NonNull
    @Override
    public ParkingZone mapIn(@NonNull GetParkingZoneDataResponse getParkingZoneDataResponse) {

        ParkingZone parkingZone = new ParkingZone();
        parkingZone.setCustomerID(getParkingZoneDataResponse.getCustomerID());
        parkingZone.setFleetID(getParkingZoneDataResponse.getFleetID());
        parkingZone.setParking_area_id(getParkingZoneDataResponse.getParking_area_id());
        parkingZone.setType(getParkingZoneDataResponse.getType());
        parkingZone.setZone_Name(getParkingZoneDataResponse.getZone_Name());
        parkingZone.setZone(getParkingZoneDataResponse.getZone());
        List<ParkingZoneGeometryResponse> responses = getParkingZoneDataResponse.getParkingZoneGeometry();
        List<ParkingZoneGeometry> parkingZoneGeometryList = new ArrayList<>();
        if (responses != null) {
            for (ParkingZoneGeometryResponse parkingZoneGeometryResponse : responses) {
                if (parkingZoneGeometryResponse != null) {
                    ParkingZoneGeometry parkingZoneGeometry = new ParkingZoneGeometry();
                    parkingZoneGeometry.setLatitude(parkingZoneGeometryResponse.getLatitude());
                    parkingZoneGeometry.setLongitude(parkingZoneGeometryResponse.getLongitude());
                    parkingZoneGeometry.setRadius(parkingZoneGeometryResponse.getRadius());
                    parkingZoneGeometryList.add(parkingZoneGeometry);
                }
            }
        }

        parkingZone.setParkingZoneGeometry(parkingZoneGeometryList);
        return parkingZone;
    }

    @NonNull
    @Override
    public GetParkingZoneDataResponse mapOut(@NonNull ParkingZone parkingZone) {
        return null;
    }

}
