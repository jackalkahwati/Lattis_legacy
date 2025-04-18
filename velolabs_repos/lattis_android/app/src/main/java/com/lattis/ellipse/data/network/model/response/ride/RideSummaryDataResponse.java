package com.lattis.ellipse.data.network.model.response.ride;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/11/17.
 */

public class RideSummaryDataResponse {

    @SerializedName("trip_id")
    private int trip_id;

    @SerializedName("steps")
    private double[][] steps;

    @SerializedName("start_address")
    private String start_address;

    @SerializedName("date_created")
    private long date_created;

    @SerializedName("date_endtrip")
    private String date_endtrip;

    @SerializedName("parking_image")
    private String parking_image;

    @SerializedName("rating")
    private float rating;

    @SerializedName("user_id")
    private int user_id;

    @SerializedName("operator_id")
    private int operator_id;

    @SerializedName("customer_id")
    private int customer_id;

    @SerializedName("lock_id")
    private int lock_id;

    @SerializedName("bike_id")
    private int bike_id;

    @SerializedName("fleet_id")
    private int fleet_id;

    @SerializedName("transaction_id")
    private String transaction_id;

    @SerializedName("duration")
    private String duration;

    @SerializedName("charge_for_duration")
    private String charge_for_duration;


    @SerializedName("currency")
    private String currency;

    @SerializedName("penalty_fees")
    private String penalty_fees;

    @SerializedName("deposit")
    private String deposit;

    @SerializedName("total")
    private String total;

    @SerializedName("over_usage_fees")
    private String over_usage_fees;

    @SerializedName("user_profile_id")
    private String user_profile_id;

    @SerializedName("card_id")
    private String card_id;

    @SerializedName("price_for_membership")
    private String price_for_membership;

    @SerializedName("price_type_value")
    private String price_type_value;

    @SerializedName("price_type")
    private String price_type;

    @SerializedName("ride_deposit")
    private String ride_deposit;

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


    @SerializedName("first_lock_connect")
    private boolean first_lock_connect;

    public String getCurrency() {
        return currency;
    }

    @SerializedName("do_not_track_trip")
    private Boolean do_not_track_trip;

    public Boolean getDo_not_track_trip() {
        return do_not_track_trip;
    }

    public boolean isFirst_lock_connect() {
        return first_lock_connect;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public String getDuration() {
        return duration;
    }

    public String getCharge_for_duration() {
        return charge_for_duration;
    }

    public String getPenalty_fees() {
        return penalty_fees;
    }

    public String getDeposit() {
        return deposit;
    }

    public String getTotal() {
        return total;
    }

    public String getOver_usage_fees() {
        return over_usage_fees;
    }

    public String getUser_profile_id() {
        return user_profile_id;
    }

    public String getCard_id() {
        return card_id;
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




    public int getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(int trip_id) {
        this.trip_id = trip_id;
    }

    public double[][] getSteps() {
        return steps;
    }

    public void setSteps(double[][] steps) {
        this.steps = steps;
    }

    public String getStart_address() {
        return start_address;
    }

    public void setStart_address(String start_address) {
        this.start_address = start_address;
    }

    public long getDate_created() {
        return date_created;
    }

    public void setDate_created(long date_created) {
        this.date_created = date_created;
    }

    public String getParking_image() {
        return parking_image;
    }

    public void setParking_image(String parking_image) {
        this.parking_image = parking_image;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
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

    public int getLock_id() {
        return lock_id;
    }

    public void setLock_id(int lock_id) {
        this.lock_id = lock_id;
    }

    public int getBike_id() {
        return bike_id;
    }

    public void setBike_id(int bike_id) {
        this.bike_id = bike_id;
    }

    public int getFleet_id() {
        return fleet_id;
    }

    public void setFleet_id(int fleet_id) {
        this.fleet_id = fleet_id;
    }


    public String getDate_endtrip() {
        return date_endtrip;
    }



}
