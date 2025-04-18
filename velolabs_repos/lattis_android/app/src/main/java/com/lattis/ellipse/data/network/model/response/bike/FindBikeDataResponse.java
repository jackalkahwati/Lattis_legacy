package com.lattis.ellipse.data.network.model.response.bike;

import com.google.gson.annotations.SerializedName;


public class FindBikeDataResponse {

    @SerializedName("bike_id")
    private int bike_id;
    @SerializedName("bike_name")
    private String bike_name;
    @SerializedName("make")
    private String make;
    @SerializedName("model")
    private String model;
    @SerializedName("type")
    private String type;
    @SerializedName("description")
    private String description;
    @SerializedName("date_created")
    private int date_created;
    @SerializedName("status")
    private String status;
    @SerializedName("battery_level")
    private String battery_level;
    @SerializedName("bike_battery_level")
    private String bike_battery_level;
    @SerializedName("current_status")
    private String current_status;
    @SerializedName("maintenance_status")
    private String maintenance_status;
    @SerializedName("lock_id")
    private int lock_id;
    @SerializedName("fleet_id")
    private int fleet_id;
    @SerializedName("user_id")
    private int user_id;
    @SerializedName("mac_id")
    private String mac_id;
    @SerializedName("key")
    private String key;
    @SerializedName("name")
    private String name;
    @SerializedName("operator_id")
    private int operator_id;
    @SerializedName("customer_id")
    private int customer_id;
    @SerializedName("hub_id")
    private int hub_id;
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;
    @SerializedName("distance")
    private double distance;
    @SerializedName("pic")
    private String pic;
    @SerializedName("fleet_key")
    private String fleet_key;
    @SerializedName("tariff")
    private String tariff;
    @SerializedName("fleet_name")
    private String fleet_name;
    @SerializedName("fleet_logo")
    private String fleet_logo;
    @SerializedName("fleet_bikes")
    private String fleet_bikes;
    @SerializedName("fleet_parking_spots")
    private String fleet_parking_spots;
    @SerializedName("fleet_t_and_c")
    private String terms_condition_url;

    @SerializedName("price_for_membership")
    private String price_for_membership;

    @SerializedName("price_type_value")
    private String price_type_value;

    @SerializedName("price_type")
    private String price_type;

    @SerializedName("ride_deposit")
    private String ride_deposit;

    @SerializedName("price_for_ride_deposit")
    private String price_for_ride_deposit;

    @SerializedName("price_for_ride_deposit_type")
    private String price_for_ride_deposit_type;

    @SerializedName("excess_usage_fees")
    private String excess_usage_fees;

    @SerializedName("excess_usage_type_value")
    private String excess_usage_type_value;

    @SerializedName("excess_usage_type")
    private String excess_usage_type;

    @SerializedName("excess_usage_type_after_value")
    private String excess_usage_type_after_value;

    @SerializedName("excess_usage_type_after_type")
    private String excess_usage_type_after_type;

    @SerializedName("skip_parking_image")
    private boolean skip_parking_image;

    @SerializedName("max_trip_length")
    private int max_trip_length;

    @SerializedName("usage_surcharge")
    private String usage_surcharge;

    @SerializedName("currency")
    private String currency;

    @SerializedName("require_phone_number")
    private boolean require_phone_number;

    @SerializedName("do_not_track_trip")
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


    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getUsage_surcharge() {
        return usage_surcharge;
    }

    public boolean getSkip_parking_image() {
        return skip_parking_image;
    }

    public int getMax_trip_length() {
        return max_trip_length;
    }


    public String getFleet_type() {
        return fleet_type;
    }

    public void setFleet_type(String fleet_type) {
        this.fleet_type = fleet_type;
    }

    @SerializedName("fleet_type")

    private String fleet_type;

    public String getTerms_condition_url() {
        return terms_condition_url;
    }

    public void setTerms_condition_url(String terms_condition_url) {
        this.terms_condition_url = terms_condition_url;
    }

    public void setPrice_for_membership(String price_for_membership) {
        this.price_for_membership = price_for_membership;
    }

    public void setPrice_type_value(String price_type_value) {
        this.price_type_value = price_type_value;
    }

    public void setPrice_type(String price_type) {
        this.price_type = price_type;
    }

    public void setRide_deposit(String ride_deposit) {
        this.ride_deposit = ride_deposit;
    }

    public void setPrice_for_ride_deposit(String price_for_ride_deposit) {
        this.price_for_ride_deposit = price_for_ride_deposit;
    }

    public void setPrice_for_ride_deposit_type(String price_for_ride_deposit_type) {
        this.price_for_ride_deposit_type = price_for_ride_deposit_type;
    }

    public void setExcess_usage_fees(String excess_usage_fees) {
        this.excess_usage_fees = excess_usage_fees;
    }

    public void setExcess_usage_type_value(String excess_usage_type_value) {
        this.excess_usage_type_value = excess_usage_type_value;
    }

    public void setExcess_usage_type(String excess_usage_type) {
        this.excess_usage_type = excess_usage_type;
    }

    public void setExcess_usage_type_after_value(String excess_usage_type_after_value) {
        this.excess_usage_type_after_value = excess_usage_type_after_value;
    }

    public void setExcess_usage_type_after_type(String excess_usage_type_after_type) {
        this.excess_usage_type_after_type = excess_usage_type_after_type;
    }

    public String getPrice_for_membership() {
        return price_for_membership;
    }

    public String getPrice_type_value() {
        return price_type_value;
    }

    public String getPrice_type() {
        return price_type;
    }

    public String getRide_deposit() {
        return ride_deposit;
    }

    public String getPrice_for_ride_deposit() {
        return price_for_ride_deposit;
    }

    public String getPrice_for_ride_deposit_type() {
        return price_for_ride_deposit_type;
    }

    public String getExcess_usage_fees() {
        return excess_usage_fees;
    }

    public String getExcess_usage_type_value() {
        return excess_usage_type_value;
    }

    public String getExcess_usage_type() {
        return excess_usage_type;
    }

    public String getExcess_usage_type_after_value() {
        return excess_usage_type_after_value;
    }

    public String getExcess_usage_type_after_type() {
        return excess_usage_type_after_type;
    }


    public double getDistance() {
        return distance;
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

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getFleet_key() {
        return fleet_key;
    }

    public void setFleet_key(String fleet_key) {
        this.fleet_key = fleet_key;
    }

    public String getTariff() {
        return tariff;
    }

    public void setTariff(String tariff) {
        this.tariff = tariff;
    }

    public String getFleet_name() {
        return fleet_name;
    }

    public void setFleet_name(String fleet_name) {
        this.fleet_name = fleet_name;
    }

    public String getFleet_logo() {
        return fleet_logo;
    }

    public void setFleet_logo(String fleet_logo) {
        this.fleet_logo = fleet_logo;
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

    public int getLock_id() {
        return lock_id;
    }

    public void setLock_id(int lock_id) {
        this.lock_id = lock_id;
    }

    public int getFleet_id() {
        return fleet_id;
    }

    public void setFleet_id(int fleet_id) {
        this.fleet_id = fleet_id;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}