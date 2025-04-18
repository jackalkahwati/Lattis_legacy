package com.lattis.ellipse.data.network.model.response.parking;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 3/21/17.
 */

public class FindParkingDataResponse {

    @SerializedName("parking_spot_id")
    private int parking_spot_id;
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("pic")
    private String pic;
    @SerializedName("type")
    private String type;
    @SerializedName("fleet_id")
    private int fleet_id;
    @SerializedName("operator_id")
    private int operator_id;
    @SerializedName("customer_id")
    private int customer_id;
    @SerializedName("parking_area_id")
    private int parking_area_id;

    public int getParking_spot_id() {
        return parking_spot_id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPic() {
        return pic;
    }

    public String getType() {
        return type;
    }

    public int getFleet_id() {
        return fleet_id;
    }

    public int getOperator_id() {
        return operator_id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public int getParking_area_id() {
        return parking_area_id;
    }



}
