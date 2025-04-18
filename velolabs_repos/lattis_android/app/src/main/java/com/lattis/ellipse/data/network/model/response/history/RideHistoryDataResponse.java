package com.lattis.ellipse.data.network.model.response.history;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 8/16/17.
 */

public class RideHistoryDataResponse {

    @SerializedName("trip_id")
    private int trip_id;

    @SerializedName("steps")
    private double[][] steps;

    @SerializedName("start_address")
    private String start_address;

    @SerializedName("end_address")
    private String end_address;

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

    @SerializedName("price_for_active_bike")
    private float price_for_active_bike;


    @SerializedName("price_for_archived_bike")
    private float price_for_archived_bike;

    @SerializedName("price_for_forget_plugin")
    private float price_for_forget_plugin;

    @SerializedName("price_for_membership")
    private String price_for_membership;

    @SerializedName("price_for_outofservice_bike")
    private String price_for_outofservice_bike;

    @SerializedName("price_for_ride_deposit")
    private String price_for_ride_deposit;

    @SerializedName("price_for_ride_deposit_type")
    private String price_for_ride_deposit_type;

    @SerializedName("price_for_staging_bike")
    private String price_for_staging_bike;

    @SerializedName("price_type")
    private String price_type;

    @SerializedName("price_type_value")
    private String price_type_value;

    @SerializedName("ride_deposit")
    private String ride_deposit;

    @SerializedName("price_for_penalty_outside_parking_below_battery_charge")
    private String price_for_penalty_outside_parking_below_battery_charge;

    @SerializedName("price_for_penalty_outside_parking")
    private String price_for_penalty_outside_parking;

    @SerializedName("transaction_id")
    private String transaction_id;

    @SerializedName("duration")
    private String duration;

    @SerializedName("charge_for_duration")
    private String charge_for_duration;

    @SerializedName("penalty_fees")
    private String penalty_fees;

    @SerializedName("deposit")
    private String deposit;

    @SerializedName("total")
    private String total;

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

    @SerializedName("over_usage_fees")
    private String over_usage_fees;

    @SerializedName("user_profile_id")
    private String user_profile_id;

    @SerializedName("card_id")
    private String card_id;

    @SerializedName("date_charged")
    private String date_charged;

    @SerializedName("date_created")
    private String date_created;

    @SerializedName("currency")
    private String currency;

    public String getCurrency() {
        return currency;
    }

    public String getCc_type() {
        return cc_type;
    }

    public void setCc_type(String cc_type) {
        this.cc_type = cc_type;
    }

    public String getCc_no() {
        return cc_no;
    }

    public void setCc_no(String cc_no) {
        this.cc_no = cc_no;
    }

    @SerializedName("cc_type")
    private String cc_type;

    @SerializedName("cc_no")
    private String cc_no;

    public String getFleet_name() {
        return fleet_name;
    }

    public void setFleet_name(String fleet_name) {
        this.fleet_name = fleet_name;
    }

    @SerializedName("fleet_name")
    private String fleet_name;

    public int getTrip_id() {
        return trip_id;
    }

    public double[][] getSteps() {
        return steps;
    }

    public String getStart_address() {
        return start_address;
    }

    public String getEnd_address() {
        return end_address;
    }

    public String getDate_endtrip() {
        return date_endtrip;
    }

    public String getParking_image() {
        return parking_image;
    }

    public float getRating() {
        return rating;
    }

    public int getUser_id() {
        return user_id;
    }

    public int getOperator_id() {
        return operator_id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public int getLock_id() {
        return lock_id;
    }

    public int getBike_id() {
        return bike_id;
    }

    public int getFleet_id() {
        return fleet_id;
    }

    public float getPrice_for_active_bike() {
        return price_for_active_bike;
    }

    public float getPrice_for_archived_bike() {
        return price_for_archived_bike;
    }

    public float getPrice_for_forget_plugin() {
        return price_for_forget_plugin;
    }

    public String getPrice_for_membership() {
        return price_for_membership;
    }

    public String getPrice_for_outofservice_bike() {
        return price_for_outofservice_bike;
    }

    public String getPrice_for_ride_deposit() {
        return price_for_ride_deposit;
    }

    public String getPrice_for_ride_deposit_type() {
        return price_for_ride_deposit_type;
    }

    public String getPrice_for_staging_bike() {
        return price_for_staging_bike;
    }

    public String getPrice_type() {
        return price_type;
    }

    public String getPrice_type_value() {
        return price_type_value;
    }

    public String getRide_deposit() {
        return ride_deposit;
    }

    public String getPrice_for_penalty_outside_parking_below_battery_charge() {
        return price_for_penalty_outside_parking_below_battery_charge;
    }

    public String getPrice_for_penalty_outside_parking() {
        return price_for_penalty_outside_parking;
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

    public String getOver_usage_fees() {
        return over_usage_fees;
    }

    public String getUser_profile_id() {
        return user_profile_id;
    }

    public String getCard_id() {
        return card_id;
    }

    public String getDate_charged() {
        return date_charged;
    }

    public String getDate_created() {
        return date_created;
    }

}
