package com.lattis.ellipse.presentation.model.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Ride;

import javax.inject.Inject;

/**
 * Created by ssd3 on 4/28/17.
 */

public class BikeModelMapper extends AbstractDataMapper<Bike,Ride> {

    @Inject
    public BikeModelMapper() {
    }

    @NonNull
    @Override
    public Ride mapIn(@NonNull Bike bike) {

        Ride ride = new Ride();

        if(bike!=null) {
            ride.setBike_bike_name(bike.getBike_name());
            ride.setBike_make(bike.getMake());
            ride.setBike_model(bike.getModel());
            ride.setBike_type(bike.getType());
            ride.setBike_description(bike.getDescription());
            ride.setBike_date_created(bike.getDate_created());
            ride.setBike_status(bike.getStatus());
            ride.setBike_battery_level(bike.getBattery_level());
            ride.setBike_bike_battery_level(bike.getBike_battery_level());
            ride.setBike_current_status(bike.getCurrent_status());
            ride.setBike_maintenance_status(bike.getMaintenance_status());
            ride.setBike_pic(bike.getPic());
            ride.setBike_distance(bike.getDistance());
            ride.setBike_latitude(bike.getLatitude());
            ride.setBike_lock_id(bike.getLock_id());
            ride.setBike_longitude(bike.getLongitude());
            ride.setBike_fleet_id(bike.getFleet_id());
            ride.setBike_parking_spot_id(bike.getParking_spot_id());
            ride.setBike_user_id(bike.getUser_id());
            ride.setBike_mac_id(bike.getMac_id());
            ride.setBike_name(bike.getBike_name());
            ride.setBike_bike_operator_id(bike.getOperator_id());
            ride.setBike_customer_id(bike.getCustomer_id());
            ride.setBike_hub_id(bike.getHub_id());
            ride.setBike_bike_fleet_key(bike.getFleet_key());
            ride.setBike_fleet_logo(bike.getFleet_logo());
            ride.setBike_fleet_name(bike.getFleet_name());
            ride.setBike_tariff(bike.getTariff());
            ride.setBike_isBikeBooked(bike.isBikeBooked());


            ride.setBike_price_for_membership(bike.getPrice_for_membership());
            ride.setBike_price_type_value(bike.getPrice_type_value());
            ride.setBike_price_type(bike.getPrice_type());
            ride.setBike_ride_deposit(bike.getRide_deposit());
            ride.setBike_price_for_ride_deposit(bike.getPrice_for_ride_deposit());
            ride.setBike_price_for_ride_deposit_type(bike.getPrice_for_ride_deposit_type());
            ride.setBike_excess_usage_fees(bike.getExcess_usage_fees());
            ride.setBike_excess_usage_type_value(bike.getExcess_usage_type_value());
            ride.setBike_excess_usage_type(bike.getExcess_usage_type());
            ride.setBike_excess_usage_type_after_value(bike.getExcess_usage_type_after_value());
            ride.setBike_excess_usage_type_after_type(bike.getExcess_usage_type_after_type());

            ride.setBike_terms_condition_url(bike.getTerms_condition_url());
            ride.setBike_fleet_type(bike.getFleet_type());
            ride.setBike_skip_parking_image(bike.getSkip_parking_image());
            ride.setBike_max_trip_length(bike.getMax_trip_length());
            ride.setBike_usage_surcharge(bike.getUsage_surcharge());

        }
        return ride;

    }

    @NonNull
    @Override
    public Bike mapOut(@NonNull Ride ride) {
        Bike bike = new Bike();

        if(ride!=null) {
            bike.setBike_name(ride.getBike_bike_name());
            bike.setBike_name(ride.getBike_make());
            bike.setModel(ride.getBike_model());
            bike.setType(ride.getBike_type());
            bike.setDescription(ride.getBike_description());
            bike.setDate_created(ride.getBike_date_created());
            bike.setStatus(ride.getBike_status());
            bike.setBattery_level(ride.getBike_battery_level());
            bike.setBike_battery_level(ride.getBike_bike_battery_level());
            bike.setCurrent_status(ride.getBike_current_status());
            bike.setMaintenance_status(ride.getBike_maintenance_status());
            bike.setPic(ride.getBike_pic());
            bike.setDistance(ride.getBike_distance());
            bike.setLatitude(ride.getBike_latitude());
            bike.setLock_id(ride.getBike_lock_id());
            bike.setLongitude(ride.getBike_longitude());
            bike.setFleet_id(ride.getBike_fleet_id());
            bike.setParking_spot_id(ride.getBike_parking_spot_id());
            bike.setUser_id(ride.getBike_user_id());
            bike.setMac_id(ride.getBike_mac_id());
            bike.setName(ride.getBike_name());
            bike.setOperator_id(ride.getBike_bike_operator_id());
            bike.setCustomer_id(ride.getBike_customer_id());
            bike.setHub_id(ride.getBike_hub_id());
            bike.setFleet_key(ride.getBike_bike_fleet_key());
            bike.setFleet_logo(ride.getBike_fleet_logo());
            bike.setFleet_name(ride.getBike_fleet_name());
            bike.setTariff(ride.getBike_tariff());
            bike.setBikeBooked(ride.getBike_isBikeBooked());


            bike.setPrice_for_membership(ride.getBike_price_for_membership());
            bike.setPrice_type_value(ride.getBike_price_type_value());
            bike.setPrice_type(ride.getBike_price_type());
            bike.setRide_deposit(ride.getBike_ride_deposit());
            bike.setPrice_for_ride_deposit(ride.getBike_price_for_ride_deposit());
            bike.setPrice_for_ride_deposit_type(ride.getBike_price_for_ride_deposit_type());
            bike.setExcess_usage_fees(ride.getBike_excess_usage_fees());
            bike.setExcess_usage_type_value(ride.getBike_excess_usage_type_value());
            bike.setExcess_usage_type(ride.getBike_excess_usage_type());
            bike.setExcess_usage_type_after_value(ride.getBike_excess_usage_type_after_value());
            bike.setExcess_usage_type_after_type(ride.getBike_excess_usage_type_after_type());

            bike.setTerms_condition_url(ride.getBike_terms_condition_url());
            bike.setFleet_type(ride.getBike_fleet_type());
            bike.setSkip_parking_image(ride.getBike_skip_parking_image());
            bike.setMax_trip_length(ride.getBike_max_trip_length());
            bike.setUsage_surcharge(ride.getBike_usage_surcharge());

        }
        return bike;
    }

}

