package com.lattis.ellipse.data.network.model.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.data.network.model.response.parking.FindParkingDataResponse;
import com.lattis.ellipse.domain.model.Parking;

import javax.inject.Inject;


public class ParkingResponseMapper extends AbstractDataMapper<FindParkingDataResponse,Parking> {

    @Inject
    public ParkingResponseMapper() {
    }

    @NonNull
    @Override
    public Parking mapIn(@NonNull FindParkingDataResponse findParkingDataResponse) {

        Parking parking = new Parking();
        parking.setLongitude(findParkingDataResponse.getLongitude());
        parking.setParking_spot_id(findParkingDataResponse.getParking_spot_id());
        parking.setName(findParkingDataResponse.getName());
        parking.setDescription(findParkingDataResponse.getDescription());
        parking.setPic(findParkingDataResponse.getPic());
        parking.setType(findParkingDataResponse.getType());
        parking.setLatitude(findParkingDataResponse.getLatitude());
        parking.setParking_area_id(findParkingDataResponse.getParking_area_id());
        parking.setFleet_id(findParkingDataResponse.getFleet_id());
        parking.setOperator_id(findParkingDataResponse.getOperator_id());
        parking.setCustomer_id(findParkingDataResponse.getCustomer_id());
        return parking;
    }

    @NonNull
    @Override
    public FindParkingDataResponse mapOut(@NonNull Parking parking) {
        return null;
    }
}
