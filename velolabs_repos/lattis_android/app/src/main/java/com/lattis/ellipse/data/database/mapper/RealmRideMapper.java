package com.lattis.ellipse.data.database.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.database.base.AbstractRealmDataMapper;
import com.lattis.ellipse.data.database.model.RealmRide;
import com.lattis.ellipse.domain.model.Ride;

import javax.inject.Inject;

/**
 * Created by ssd3 on 4/4/17.
 */

public class RealmRideMapper extends AbstractRealmDataMapper<Ride,RealmRide> {

    @Inject
    public RealmRideMapper() {}

    @NonNull
    @Override
    public RealmRide mapIn(@NonNull Ride ride) {

        RealmRide realmRide = new RealmRide();
        realmRide.setId(ride.getId());
        realmRide.setBikeId(ride.getBikeId());
        realmRide.setBike_booked_on(ride.getBike_booked_on());
        realmRide.setBike_expires_in(ride.getBike_expires_in());
        realmRide.setRideId(ride.getRideId());
        realmRide.setRide_booked_on(ride.getRide_booked_on());

        realmRide.setBike_bike_name(ride.getBike_bike_name());
        realmRide.setBike_make(ride.getBike_make());
        realmRide.setBike_model(ride.getBike_model());
        realmRide.setBike_type(ride.getBike_type());
        realmRide.setBike_description(ride.getBike_description());
        realmRide.setBike_date_created(ride.getBike_date_created());
        realmRide.setBike_status(ride.getBike_status());
        realmRide.setBike_battery_level(ride.getBike_battery_level());
        realmRide.setBike_current_status(ride.getBike_current_status());
        realmRide.setBike_maintenance_status(ride.getBike_maintenance_status());
        realmRide.setBike_pic(ride.getBike_pic());
        realmRide.setBike_distance(ride.getBike_distance());
        realmRide.setBike_latitude(ride.getBike_latitude());
        realmRide.setBike_lock_id(ride.getBike_lock_id());
        realmRide.setBike_longitude(ride.getBike_longitude());
        realmRide.setBike_fleet_id(ride.getBike_fleet_id());
        realmRide.setBike_parking_spot_id(ride.getBike_parking_spot_id());
        realmRide.setBike_user_id(ride.getBike_user_id());
        realmRide.setBike_mac_id(ride.getBike_mac_id());
        realmRide.setBike_name(ride.getBike_name());
        realmRide.setBike_bike_operator_id(ride.getBike_bike_operator_id());
        realmRide.setBike_customer_id(ride.getBike_customer_id());
        realmRide.setBike_hub_id(ride.getBike_hub_id());
        realmRide.setBike_bike_operator_email(ride.getBike_bike_fleet_key());
        realmRide.setBike_fleet_logo(ride.getBike_fleet_logo());
        realmRide.setBike_fleet_name(ride.getBike_fleet_name());
        realmRide.setBike_tariff(ride.getBike_tariff());
        realmRide.setBike_isBikeBooked(ride.isBike_isBikeBooked());

        realmRide.setBike_on_call_operator(ride.getBike_on_call_operator());
        realmRide.setSupport_phone(ride.getSupport_phone());


        realmRide.setBike_price_for_membership(ride.getBike_price_for_membership());
        realmRide.setBike_price_type_value(ride.getBike_price_type_value());
        realmRide.setBike_price_type(ride.getBike_price_type());
        realmRide.setBike_ride_deposit(ride.getBike_ride_deposit());
        realmRide.setBike_price_for_ride_deposit(ride.getBike_price_for_ride_deposit());
        realmRide.setBike_price_for_ride_deposit_type(ride.getBike_price_for_ride_deposit_type());
        realmRide.setBike_excess_usage_fees(ride.getBike_excess_usage_fees());
        realmRide.setBike_excess_usage_type_value(ride.getBike_excess_usage_type_value());
        realmRide.setBike_excess_usage_type(ride.getBike_excess_usage_type());
        realmRide.setBike_excess_usage_type_after_value(ride.getBike_excess_usage_type_after_value());
        realmRide.setBike_excess_usage_type_after_type(ride.getBike_excess_usage_type_after_type());

        realmRide.setBike_terms_condition_url(ride.getBike_terms_condition_url());
        realmRide.setBike_fleet_type(ride.getBike_fleet_type());
        realmRide.setBike_skip_parking_image(ride.getBike_skip_parking_image());
        realmRide.setBike_max_trip_length(ride.getBike_max_trip_length());

        realmRide.setFirst_lock_connect(ride.isFirst_lock_connect());

        realmRide.setDo_not_track_trip(ride.getDo_not_track_trip());
        return realmRide;
    }

    @NonNull
    @Override
    public Ride mapOut(@NonNull RealmRide realmRide) {
        Ride ride = new Ride();
        ride.setId(realmRide.getId());
        ride.setBikeId(realmRide.getBikeId());
        ride.setBike_booked_on(realmRide.getBike_booked_on());
        ride.setBike_expires_in(realmRide.getBike_expires_in());
        ride.setRideId(realmRide.getRideId());
        ride.setRide_booked_on(realmRide.getRide_booked_on());


        ride.setBike_bike_name(realmRide.getBike_bike_name());
        ride.setBike_make(realmRide.getBike_make());
        ride.setBike_model(realmRide.getBike_model());
        ride.setBike_type(realmRide.getBike_type());
        ride.setBike_description(realmRide.getBike_description());
        ride.setBike_date_created(realmRide.getBike_date_created());
        ride.setBike_status(realmRide.getBike_status());
        ride.setBike_battery_level(realmRide.getBike_battery_level());
        ride.setBike_current_status(realmRide.getBike_current_status());
        ride.setBike_maintenance_status(realmRide.getBike_maintenance_status());
        ride.setBike_pic(realmRide.getBike_pic());
        ride.setBike_distance(realmRide.getBike_distance());
        ride.setBike_latitude(realmRide.getBike_latitude());
        ride.setBike_lock_id(realmRide.getBike_lock_id());
        ride.setBike_longitude(realmRide.getBike_longitude());
        ride.setBike_fleet_id(realmRide.getBike_fleet_id());
        ride.setBike_parking_spot_id(realmRide.getBike_parking_spot_id());
        ride.setBike_user_id(realmRide.getBike_user_id());
        ride.setBike_mac_id(realmRide.getBike_mac_id());
        ride.setBike_name(realmRide.getBike_name());
        ride.setBike_bike_operator_id(realmRide.getBike_bike_operator_id());
        ride.setBike_customer_id(realmRide.getBike_customer_id());
        ride.setBike_hub_id(realmRide.getBike_hub_id());
        ride.setBike_bike_fleet_key(realmRide.getBike_bike_operator_email());
        ride.setBike_fleet_logo(realmRide.getBike_fleet_logo());
        ride.setBike_fleet_name(realmRide.getBike_fleet_name());
        ride.setBike_tariff(realmRide.getBike_tariff());
        ride.setBike_isBikeBooked(realmRide.isBike_isBikeBooked());
        ride.setBike_on_call_operator(realmRide.getBike_on_call_operator());
        ride.setSupport_phone(realmRide.getSupport_phone());


        ride.setBike_price_for_membership(realmRide.getBike_price_for_membership());
        ride.setBike_price_type_value(realmRide.getBike_price_type_value());
        ride.setBike_price_type(realmRide.getBike_price_type());
        ride.setBike_ride_deposit(realmRide.getBike_ride_deposit());
        ride.setBike_price_for_ride_deposit(realmRide.getBike_price_for_ride_deposit());
        ride.setBike_price_for_ride_deposit_type(realmRide.getBike_price_for_ride_deposit_type());
        ride.setBike_excess_usage_fees(realmRide.getBike_excess_usage_fees());
        ride.setBike_excess_usage_type_value(realmRide.getBike_excess_usage_type_value());
        ride.setBike_excess_usage_type(realmRide.getBike_excess_usage_type());
        ride.setBike_excess_usage_type_after_value(realmRide.getBike_excess_usage_type_after_value());
        ride.setBike_excess_usage_type_after_type(realmRide.getBike_excess_usage_type_after_type());

        ride.setBike_terms_condition_url(realmRide.getBike_terms_condition_url());
        ride.setBike_fleet_type(realmRide.getBike_fleet_type());

        ride.setBike_skip_parking_image(realmRide.getBike_skip_parking_image());
        ride.setBike_max_trip_length(realmRide.getBike_max_trip_length());
        ride.setFirst_lock_connect(realmRide.isFirst_lock_connect());

        ride.setDo_not_track_trip(realmRide.getDo_not_track_trip());
        return ride;
    }
}
