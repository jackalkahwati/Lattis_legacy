package com.lattis.ellipse.data.database.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ssd3 on 4/4/17.
 */

public class RealmRide extends RealmObject {

    @PrimaryKey
    private String id;
    private int bikeId;
    private long bike_booked_on;
    private int bike_expires_in;
    private int rideId;
    private long ride_booked_on;


    private String bike_bike_name;
    private String bike_make;
    private String bike_model;
    private String bike_type;
    private String bike_description;
    private int bike_date_created;
    private String bike_status;
    private String bike_battery_level;
    private String bike_current_status;
    private String bike_maintenance_status;
    private String bike_pic;
    private double bike_distance;
    private double bike_latitude;
    private int bike_lock_id;
    private double bike_longitude;
    private int bike_fleet_id;
    private int bike_parking_spot_id;
    private int bike_user_id;
    private String bike_mac_id;
    private String bike_name;
    private int bike_bike_operator_id;
    private int bike_customer_id;
    private int bike_hub_id;
    private String bike_bike_operator_email;
    private String bike_fleet_logo;
    private String bike_fleet_name;
    private String bike_tariff;
    private Boolean bike_isBikeBooked;
    private String bike_on_call_operator;
    private String support_phone;


    private String bike_price_for_membership;
    private String bike_price_type_value;
    private String bike_price_type;
    private String bike_ride_deposit;
    private String bike_price_for_ride_deposit;
    private String bike_price_for_ride_deposit_type;
    private String bike_excess_usage_fees;
    private String bike_excess_usage_type_value;
    private String bike_excess_usage_type;
    private String bike_excess_usage_type_after_value;
    private String bike_excess_usage_type_after_type;
    private String bike_terms_condition_url;
    private String bike_fleet_type;

    private boolean bike_skip_parking_image;
    private int bike_max_trip_length;

    private boolean first_lock_connect;
    private Boolean do_not_track_trip;

    public Boolean getDo_not_track_trip() {
        return do_not_track_trip;
    }

    public void setDo_not_track_trip(Boolean do_not_track_trip) {
        this.do_not_track_trip = do_not_track_trip;
    }




    public boolean isFirst_lock_connect() {
        return first_lock_connect;
    }

    public void setFirst_lock_connect(boolean first_lock_connect) {
        this.first_lock_connect = first_lock_connect;
    }


    public boolean getBike_skip_parking_image() {
        return bike_skip_parking_image;
    }

    public void setBike_skip_parking_image(boolean bike_skip_parking_image) {
        this.bike_skip_parking_image = bike_skip_parking_image;
    }

    public int getBike_max_trip_length() {
        return bike_max_trip_length;
    }

    public void setBike_max_trip_length(int bike_max_trip_length) {
        this.bike_max_trip_length = bike_max_trip_length;
    }

    public String getBike_fleet_type() {
        return bike_fleet_type;
    }

    public void setBike_fleet_type(String bike_fleet_type) {
        this.bike_fleet_type = bike_fleet_type;
    }

    public String getBike_terms_condition_url() {
        return bike_terms_condition_url;
    }

    public void setBike_terms_condition_url(String bike_terms_condition_url) {
        this.bike_terms_condition_url = bike_terms_condition_url;
    }


    public Boolean getBike_isBikeBooked() {
        return bike_isBikeBooked;
    }

    public String getBike_price_for_membership() {
        return bike_price_for_membership;
    }

    public void setBike_price_for_membership(String bike_price_for_membership) {
        this.bike_price_for_membership = bike_price_for_membership;
    }

    public String getBike_price_type_value() {
        return bike_price_type_value;
    }

    public void setBike_price_type_value(String bike_price_type_value) {
        this.bike_price_type_value = bike_price_type_value;
    }

    public String getBike_price_type() {
        return bike_price_type;
    }

    public void setBike_price_type(String bike_price_type) {
        this.bike_price_type = bike_price_type;
    }

    public String getBike_ride_deposit() {
        return bike_ride_deposit;
    }

    public void setBike_ride_deposit(String bike_ride_deposit) {
        this.bike_ride_deposit = bike_ride_deposit;
    }

    public String getBike_price_for_ride_deposit() {
        return bike_price_for_ride_deposit;
    }

    public void setBike_price_for_ride_deposit(String bike_price_for_ride_deposit) {
        this.bike_price_for_ride_deposit = bike_price_for_ride_deposit;
    }

    public String getBike_price_for_ride_deposit_type() {
        return bike_price_for_ride_deposit_type;
    }

    public void setBike_price_for_ride_deposit_type(String bike_price_for_ride_deposit_type) {
        this.bike_price_for_ride_deposit_type = bike_price_for_ride_deposit_type;
    }

    public String getBike_excess_usage_fees() {
        return bike_excess_usage_fees;
    }

    public void setBike_excess_usage_fees(String bike_excess_usage_fees) {
        this.bike_excess_usage_fees = bike_excess_usage_fees;
    }

    public String getBike_excess_usage_type_value() {
        return bike_excess_usage_type_value;
    }

    public void setBike_excess_usage_type_value(String bike_excess_usage_type_value) {
        this.bike_excess_usage_type_value = bike_excess_usage_type_value;
    }

    public String getBike_excess_usage_type() {
        return bike_excess_usage_type;
    }

    public void setBike_excess_usage_type(String bike_excess_usage_type) {
        this.bike_excess_usage_type = bike_excess_usage_type;
    }

    public String getBike_excess_usage_type_after_value() {
        return bike_excess_usage_type_after_value;
    }

    public void setBike_excess_usage_type_after_value(String bike_excess_usage_type_after_value) {
        this.bike_excess_usage_type_after_value = bike_excess_usage_type_after_value;
    }

    public String getBike_excess_usage_type_after_type() {
        return bike_excess_usage_type_after_type;
    }

    public void setBike_excess_usage_type_after_type(String bike_excess_usage_type_after_type) {
        this.bike_excess_usage_type_after_type = bike_excess_usage_type_after_type;
    }



    public String getSupport_phone() {
        return support_phone;
    }

    public void setSupport_phone(String support_phone) {
        this.support_phone = support_phone;
    }


    public String getBike_on_call_operator() {
        return bike_on_call_operator;
    }

    public void setBike_on_call_operator(String bike_on_call_operator) {
        this.bike_on_call_operator = bike_on_call_operator;
    }

    public String getBike_bike_name() {
        return bike_bike_name;
    }

    public void setBike_bike_name(String bike_bike_name) {
        this.bike_bike_name = bike_bike_name;
    }

    public String getBike_make() {
        return bike_make;
    }

    public void setBike_make(String bike_make) {
        this.bike_make = bike_make;
    }

    public String getBike_model() {
        return bike_model;
    }

    public void setBike_model(String bike_model) {
        this.bike_model = bike_model;
    }

    public String getBike_type() {
        return bike_type;
    }

    public void setBike_type(String bike_type) {
        this.bike_type = bike_type;
    }

    public String getBike_description() {
        return bike_description;
    }

    public void setBike_description(String bike_description) {
        this.bike_description = bike_description;
    }

    public int getBike_date_created() {
        return bike_date_created;
    }

    public void setBike_date_created(int bike_date_created) {
        this.bike_date_created = bike_date_created;
    }

    public String getBike_status() {
        return bike_status;
    }

    public void setBike_status(String bike_status) {
        this.bike_status = bike_status;
    }

    public String getBike_battery_level() {
        return bike_battery_level;
    }

    public void setBike_battery_level(String bike_battery_level) {
        this.bike_battery_level = bike_battery_level;
    }

    public String getBike_current_status() {
        return bike_current_status;
    }

    public void setBike_current_status(String bike_current_status) {
        this.bike_current_status = bike_current_status;
    }

    public String getBike_maintenance_status() {
        return bike_maintenance_status;
    }

    public void setBike_maintenance_status(String bike_maintenance_status) {
        this.bike_maintenance_status = bike_maintenance_status;
    }

    public String getBike_pic() {
        return bike_pic;
    }

    public void setBike_pic(String bike_pic) {
        this.bike_pic = bike_pic;
    }

    public double getBike_distance() {
        return bike_distance;
    }

    public void setBike_distance(double bike_distance) {
        this.bike_distance = bike_distance;
    }

    public double getBike_latitude() {
        return bike_latitude;
    }

    public void setBike_latitude(double bike_latitude) {
        this.bike_latitude = bike_latitude;
    }

    public int getBike_lock_id() {
        return bike_lock_id;
    }

    public void setBike_lock_id(int bike_lock_id) {
        this.bike_lock_id = bike_lock_id;
    }

    public double getBike_longitude() {
        return bike_longitude;
    }

    public void setBike_longitude(double bike_longitude) {
        this.bike_longitude = bike_longitude;
    }

    public int getBike_fleet_id() {
        return bike_fleet_id;
    }

    public void setBike_fleet_id(int bike_fleet_id) {
        this.bike_fleet_id = bike_fleet_id;
    }

    public int getBike_parking_spot_id() {
        return bike_parking_spot_id;
    }

    public void setBike_parking_spot_id(int bike_parking_spot_id) {
        this.bike_parking_spot_id = bike_parking_spot_id;
    }

    public int getBike_user_id() {
        return bike_user_id;
    }

    public void setBike_user_id(int bike_user_id) {
        this.bike_user_id = bike_user_id;
    }

    public String getBike_mac_id() {
        return bike_mac_id;
    }

    public void setBike_mac_id(String bike_mac_id) {
        this.bike_mac_id = bike_mac_id;
    }

    public String getBike_name() {
        return bike_name;
    }

    public void setBike_name(String bike_name) {
        this.bike_name = bike_name;
    }

    public int getBike_bike_operator_id() {
        return bike_bike_operator_id;
    }

    public void setBike_bike_operator_id(int bike_bike_operator_id) {
        this.bike_bike_operator_id = bike_bike_operator_id;
    }

    public int getBike_customer_id() {
        return bike_customer_id;
    }

    public void setBike_customer_id(int bike_customer_id) {
        this.bike_customer_id = bike_customer_id;
    }

    public int getBike_hub_id() {
        return bike_hub_id;
    }

    public void setBike_hub_id(int bike_hub_id) {
        this.bike_hub_id = bike_hub_id;
    }

    public String getBike_bike_operator_email() {
        return bike_bike_operator_email;
    }

    public void setBike_bike_operator_email(String bike_bike_operator_email) {
        this.bike_bike_operator_email = bike_bike_operator_email;
    }

    public String getBike_fleet_logo() {
        return bike_fleet_logo;
    }

    public void setBike_fleet_logo(String bike_fleet_logo) {
        this.bike_fleet_logo = bike_fleet_logo;
    }

    public String getBike_fleet_name() {
        return bike_fleet_name;
    }

    public void setBike_fleet_name(String bike_fleet_name) {
        this.bike_fleet_name = bike_fleet_name;
    }

    public String getBike_tariff() {
        return bike_tariff;
    }

    public void setBike_tariff(String bike_tariff) {
        this.bike_tariff = bike_tariff;
    }

    public Boolean isBike_isBikeBooked() {
        return bike_isBikeBooked;
    }

    public void setBike_isBikeBooked(Boolean bike_isBikeBooked) {
        this.bike_isBikeBooked = bike_isBikeBooked;
    }





    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public int getBikeId() {
        return bikeId;
    }

    public void setBikeId(int bikeId) {
        this.bikeId = bikeId;
    }

    public long getBike_booked_on() {
        return bike_booked_on;
    }

    public void setBike_booked_on(long bike_booked_on) {
        this.bike_booked_on = bike_booked_on;
    }

    public int getBike_expires_in() {
        return bike_expires_in;
    }

    public void setBike_expires_in(int bike_expires_in) {
        this.bike_expires_in = bike_expires_in;
    }

    public int getRideId() {
        return rideId;
    }

    public void setRideId(int rideId) {
        this.rideId = rideId;
    }

    public long getRide_booked_on() {
        return ride_booked_on;
    }

    public void setRide_booked_on(long ride_booked_on) {
        this.ride_booked_on = ride_booked_on;
    }



    @Override
    public String toString() {
        return "RealmUser{" +
                "bikeId='" + bikeId + '\'' +
                ", bike_booked_on='" + bike_booked_on + '\'' +
                ", bike_expires_in='" + bike_expires_in + '\'' +
                ", rideId='" + rideId + '\'' +
                ", ride_booked_on=" + ride_booked_on +
                '}';
    }




}
