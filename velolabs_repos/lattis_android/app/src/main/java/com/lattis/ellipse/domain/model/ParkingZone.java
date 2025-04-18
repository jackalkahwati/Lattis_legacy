package com.lattis.ellipse.domain.model;

import java.util.List;

/**
 * Created by lattis on 24/05/17.
 */

public class ParkingZone {
    private int parking_area_id;
    private String zone_Name;
    private String type;
    private String zone;

    public int getOperatorID() {
        return operatorID;
    }

    public void setOperatorID(int operatorID) {
        this.operatorID = operatorID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public int getFleetID() {
        return fleetID;
    }

    public void setFleetID(int fleetID) {
        this.fleetID = fleetID;
    }

    private int operatorID;
    private int customerID;
    private int fleetID;

    List<ParkingZoneGeometry> parkingZoneGeometry;

    public int getParking_area_id() {
        return parking_area_id;
    }

    public void setParking_area_id(int parking_area_id) {
        this.parking_area_id = parking_area_id;
    }

    public String getZone_Name() {
        return zone_Name;
    }

    public void setZone_Name(String zone_Name) {
        this.zone_Name = zone_Name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public List<ParkingZoneGeometry> getParkingZoneGeometry() {
        return parkingZoneGeometry;
    }

    @Override
    public String toString() {
        return "Lock{" +
                "parking_area_id='" + parking_area_id + '\'' +
                ", zone_Name='" + zone_Name + '\'' +
                ", type='" + type + '\'' +
                ", zone='" + zone + '\'' +
                ", fleet_id='" + fleetID + '\'' +
                ", operator_id=" + operatorID +
                ", customer_id=" + customerID +
                '}';
    }

    public void setParkingZoneGeometry(List<ParkingZoneGeometry> parkingZoneGeometry) {
        this.parkingZoneGeometry = parkingZoneGeometry;

    }

}
