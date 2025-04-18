package com.lattis.ellipse.domain.model;

public class Bike {

    private int bike_id;
    private String bike_name;
    private String make;
    private String model;
    private String type;
    private String description;
    private int date_created;
    private String status;
    private String battery_level;
    private String bike_battery_level;
    private String current_status;
    private String maintenance_status;
    private String pic;
    private double distance;
    private double latitude;
    private int lock_id;
    private double longitude;
    private int fleet_id;
    private int parking_spot_id;
    private int user_id;
    private String mac_id;
    private String name;
    private int operator_id;
    private int customer_id;
    private int hub_id;
    private String fleet_key;
    private String fleet_logo;
    private String fleet_name;
    private String tariff;
    private boolean isBikeBooked;
    private long booked_on;
    private int expires_in;
    private String fleet_bikes;
    private String fleet_parking_spots;

    private String price_for_membership;
    private String price_type_value;
    private String price_type;
    private String ride_deposit;
    private String price_for_ride_deposit;
    private String price_for_ride_deposit_type;
    private String excess_usage_fees;
    private String excess_usage_type_value;
    private String excess_usage_type;
    private String excess_usage_type_after_value;
    private String excess_usage_type_after_type;
    private String terms_condition_url;
    private boolean skip_parking_image;
    private int max_trip_length;
    private String usage_surcharge;
    private String currency;
    private boolean require_phone_number;
    private boolean do_not_track_trip;
    
    public boolean isDo_not_track_trip() {
        return do_not_track_trip;
    }

    public void setDo_not_track_trip(boolean do_not_track_trip) {
        this.do_not_track_trip = do_not_track_trip;
    }






    public boolean isRequire_phone_number() {
        return require_phone_number;
    }

    public void setRequire_phone_number(boolean require_phone_number) {
        this.require_phone_number = require_phone_number;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isSkip_parking_image() {
        return skip_parking_image;
    }

    public String getUsage_surcharge() {
        return usage_surcharge;
    }

    public void setUsage_surcharge(String usage_surcharge) {
        this.usage_surcharge = usage_surcharge;
    }

    public boolean getSkip_parking_image() {
        return skip_parking_image;
    }

    public void setSkip_parking_image(boolean skip_parking_image) {
        this.skip_parking_image = skip_parking_image;
    }

    public int getMax_trip_length() {
        return max_trip_length;
    }

    public void setMax_trip_length(int max_trip_length) {
        this.max_trip_length = max_trip_length;
    }


    public String getTerms_condition_url() {
        return terms_condition_url;
    }

    public void setTerms_condition_url(String terms_condition_url) {
        this.terms_condition_url = terms_condition_url;
    }


    private String fleet_type;

    public String getFleet_type() {
        return fleet_type;
    }

    public String getPrice_for_membership() {
        return price_for_membership;
    }

    public void setPrice_for_membership(String price_for_membership) {
        this.price_for_membership = price_for_membership;
    }

    public String getPrice_type_value() {
        return price_type_value;
    }

    public void setPrice_type_value(String price_type_value) {
        this.price_type_value = price_type_value;
    }

    public String getPrice_type() {
        return price_type;
    }

    public void setPrice_type(String price_type) {
        this.price_type = price_type;
    }

    public String getRide_deposit() {
        return ride_deposit;
    }

    public void setRide_deposit(String ride_deposit) {
        this.ride_deposit = ride_deposit;
    }

    public String getPrice_for_ride_deposit() {
        return price_for_ride_deposit;
    }

    public void setPrice_for_ride_deposit(String price_for_ride_deposit) {
        this.price_for_ride_deposit = price_for_ride_deposit;
    }

    public String getPrice_for_ride_deposit_type() {
        return price_for_ride_deposit_type;
    }

    public void setPrice_for_ride_deposit_type(String price_for_ride_deposit_type) {
        this.price_for_ride_deposit_type = price_for_ride_deposit_type;
    }

    public String getExcess_usage_fees() {
        return excess_usage_fees;
    }

    public void setExcess_usage_fees(String excess_usage_fees) {
        this.excess_usage_fees = excess_usage_fees;
    }

    public String getExcess_usage_type_value() {
        return excess_usage_type_value;
    }

    public void setExcess_usage_type_value(String excess_usage_type_value) {
        this.excess_usage_type_value = excess_usage_type_value;
    }

    public String getExcess_usage_type() {
        return excess_usage_type;
    }

    public void setExcess_usage_type(String excess_usage_type) {
        this.excess_usage_type = excess_usage_type;
    }

    public String getExcess_usage_type_after_value() {
        return excess_usage_type_after_value;
    }

    public void setExcess_usage_type_after_value(String excess_usage_type_after_value) {
        this.excess_usage_type_after_value = excess_usage_type_after_value;
    }

    public String getExcess_usage_type_after_type() {
        return excess_usage_type_after_type;
    }

    public void setExcess_usage_type_after_type(String excess_usage_type_after_type) {
        this.excess_usage_type_after_type = excess_usage_type_after_type;
    }


    public String getFleet_bikes() {
        return fleet_bikes;
    }

    public void setFleet_bikes(String fleet_bikes) {
        this.fleet_bikes = fleet_bikes;
    }

    public String getFleet_parking_spots() {
        return fleet_parking_spots;
    }

    public void setFleet_parking_spots(String fleet_parking_spots) {
        this.fleet_parking_spots = fleet_parking_spots;
    }

    public boolean isBikeBooked() {
        return isBikeBooked;
    }

    public void setBikeBooked(boolean bikeBooked) {
        isBikeBooked = bikeBooked;
    }

    public long getBooked_on() {
        return booked_on;
    }

    public void setBooked_on(long booked_on) {
        this.booked_on = booked_on;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }


    public int getBike_id() {
        return bike_id;
    }

    public void setBike_id(int bike_id) {
        this.bike_id = bike_id;
    }

    public String getBike_name() {
        return bike_name;
    }

    public void setBike_name(String bike_name) {
        this.bike_name = bike_name;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDate_created() {
        return date_created;
    }

    public void setDate_created(int date_created) {
        this.date_created = date_created;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBattery_level() {
        return battery_level;
    }

    public void setBattery_level(String battery_level) {
        this.battery_level = battery_level;
    }

    public String getBike_battery_level() {
        return bike_battery_level;
    }

    public void setBike_battery_level(String bike_battery_level) {
        this.bike_battery_level = bike_battery_level;
    }

    public String getCurrent_status() {
        return current_status;
    }

    public void setCurrent_status(String current_status) {
        this.current_status = current_status;
    }

    public String getMaintenance_status() {
        return maintenance_status;
    }

    public void setMaintenance_status(String maintenance_status) {
        this.maintenance_status = maintenance_status;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getLock_id() {
        return lock_id;
    }

    public void setLock_id(int lock_id) {
        this.lock_id = lock_id;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getFleet_id() {
        return fleet_id;
    }

    public void setFleet_id(int fleet_id) {
        this.fleet_id = fleet_id;
    }

    public int getParking_spot_id() {
        return parking_spot_id;
    }

    public void setParking_spot_id(int parking_spot_id) {
        this.parking_spot_id = parking_spot_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getMac_id() {
        return mac_id;
    }

    public void setMac_id(String mac_id) {
        this.mac_id = mac_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOperator_id() {
        return operator_id;
    }

    public void setOperator_id(int operator_id) {
        this.operator_id = operator_id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public int getHub_id() {
        return hub_id;
    }

    public void setHub_id(int hub_id) {
        this.hub_id = hub_id;
    }

    public String getFleet_key() {
        return fleet_key;
    }

    public void setFleet_key(String fleet_key) {
        this.fleet_key = fleet_key;
    }

    public String getFleet_logo() {
        return fleet_logo;
    }

    public void setFleet_logo(String fleet_logo) {
        this.fleet_logo = fleet_logo;
    }

    public String getFleet_name() {
        return fleet_name;
    }

    public void setFleet_name(String fleet_name) {
        this.fleet_name = fleet_name;
    }

    public String getTariff() {
        return tariff;
    }

    public void setTariff(String tariff) {
        this.tariff = tariff;
    }

    @Override
    public String toString() {
        return "Bike{" +
                "bike_id=" + bike_id +
                ", bike_name=" + bike_name +
                ", booked_on=" + booked_on +
                ", expires_in=" + expires_in +
                '}';
    }


    public void setFleet_type(String fleet_type) {
        this.fleet_type = fleet_type;
    }
}
