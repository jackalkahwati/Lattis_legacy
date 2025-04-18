package com.lattis.ellipse.data.network.model.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.data.network.model.response.bike.FindBikeDataResponse;
import com.lattis.ellipse.domain.model.Bike;

import javax.inject.Inject;

public class BikeResponseMapper extends AbstractDataMapper<FindBikeDataResponse,Bike> {

    @Inject
    public BikeResponseMapper() {}

    @NonNull
    @Override
    public Bike mapIn(@NonNull FindBikeDataResponse findBikeDataResponse) {
        Bike bike = new Bike();
        bike.setBike_id(findBikeDataResponse.getBike_id());
        bike.setBike_name(findBikeDataResponse.getBike_name());
        bike.setMake(findBikeDataResponse.getMake());
        bike.setModel(findBikeDataResponse.getModel());
        bike.setType(findBikeDataResponse.getType());
        bike.setDescription(findBikeDataResponse.getDescription());
        bike.setDate_created(findBikeDataResponse.getDate_created());
        bike.setStatus(findBikeDataResponse.getStatus());
        bike.setBattery_level(findBikeDataResponse.getBattery_level());
        bike.setBike_battery_level(findBikeDataResponse.getBike_battery_level());
        bike.setCurrent_status(findBikeDataResponse.getCurrent_status());
        bike.setMaintenance_status(findBikeDataResponse.getMaintenance_status());
        bike.setLock_id(findBikeDataResponse.getLock_id());
        bike.setFleet_id(findBikeDataResponse.getFleet_id());
        bike.setUser_id(findBikeDataResponse.getUser_id());
        bike.setMac_id(findBikeDataResponse.getMac_id());
        bike.setName(findBikeDataResponse.getName());
        bike.setOperator_id(findBikeDataResponse.getOperator_id());
        bike.setCustomer_id(findBikeDataResponse.getCustomer_id());
        bike.setHub_id(findBikeDataResponse.getHub_id());
        bike.setLatitude(findBikeDataResponse.getLatitude());
        bike.setLongitude(findBikeDataResponse.getLongitude());
        bike.setFleet_id(findBikeDataResponse.getFleet_id());
        bike.setFleet_name(findBikeDataResponse.getFleet_name());
        bike.setFleet_logo(findBikeDataResponse.getFleet_logo());
        bike.setPic(findBikeDataResponse.getPic());
        bike.setFleet_key(findBikeDataResponse.getFleet_key());
        bike.setTariff(findBikeDataResponse.getTariff());
        bike.setFleet_bikes(findBikeDataResponse.getFleet_bikes());
        bike.setFleet_parking_spots(findBikeDataResponse.getFleet_parking_spots());
        bike.setPrice_for_membership(findBikeDataResponse.getPrice_for_membership());
        bike.setDistance(findBikeDataResponse.getDistance());
        bike.setPrice_for_membership(findBikeDataResponse.getPrice_for_membership());
        bike.setPrice_type_value(findBikeDataResponse.getPrice_type_value());
        bike.setPrice_type(findBikeDataResponse.getPrice_type());
        bike.setRide_deposit(findBikeDataResponse.getRide_deposit());
        bike.setPrice_for_ride_deposit(findBikeDataResponse.getPrice_for_ride_deposit());
        bike.setPrice_for_ride_deposit_type(findBikeDataResponse.getPrice_for_ride_deposit_type());
        bike.setExcess_usage_fees(findBikeDataResponse.getExcess_usage_fees());
        bike.setExcess_usage_type_value(findBikeDataResponse.getExcess_usage_type_value());
        bike.setExcess_usage_type(findBikeDataResponse.getExcess_usage_type());
        bike.setExcess_usage_type_after_value(findBikeDataResponse.getExcess_usage_type_after_value());
        bike.setExcess_usage_type_after_type(findBikeDataResponse.getExcess_usage_type_after_type());
        bike.setTerms_condition_url(findBikeDataResponse.getTerms_condition_url());
        bike.setFleet_type(findBikeDataResponse.getFleet_type());
        bike.setSkip_parking_image(findBikeDataResponse.getSkip_parking_image());
        bike.setMax_trip_length(findBikeDataResponse.getMax_trip_length());
        bike.setUsage_surcharge(findBikeDataResponse.getUsage_surcharge());
        bike.setCurrency(findBikeDataResponse.getCurrency());
        bike.setRequire_phone_number(findBikeDataResponse.isRequire_phone_number());
        bike.setDo_not_track_trip(findBikeDataResponse.isDo_not_track_trip());
        return bike;
    }

    @NonNull
    @Override
    public FindBikeDataResponse mapOut(@NonNull Bike bike) {
        return null;
    }
}
