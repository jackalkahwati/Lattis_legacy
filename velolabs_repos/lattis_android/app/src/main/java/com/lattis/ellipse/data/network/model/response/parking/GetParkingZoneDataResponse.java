package com.lattis.ellipse.data.network.model.response.parking;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by lattis on 24/05/17.
 */

public class GetParkingZoneDataResponse {
    @SerializedName("parking_area_id")
    private int parking_area_id;
    @SerializedName("name")
    private String zone_Name;
    @SerializedName("type")
    private String type;
    @SerializedName("zone")
    private String zone;

    @SerializedName("geometry")
    List<ParkingZoneGeometryResponse> parkingZoneGeometry;
    @SerializedName("operator_id")
    private int operatorID;
    @SerializedName("customer_id")
    private int customerID;
    @SerializedName("fleet_id")
    private int fleetID;


    public List<ParkingZoneGeometryResponse> getParkingZoneGeometry() {
        return parkingZoneGeometry;
    }

    public int getParking_area_id() {
        return parking_area_id;
    }

    public String getZone_Name() {
        return zone_Name;
    }

    public String getType() {
        return type;
    }

    public String getZone() {
        return zone;
    }

    public int getOperatorID() {
        return operatorID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public int getFleetID() {
        return fleetID;
    }

}
