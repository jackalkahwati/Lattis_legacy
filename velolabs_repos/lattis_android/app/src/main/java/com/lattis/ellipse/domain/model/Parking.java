package com.lattis.ellipse.domain.model;

/**
 * Created by ssd3 on 3/21/17.
 */

public class Parking {


    private int parking_spot_id;
    private String name;
    private String description;
    private String pic;
    private String type;
    private int parking_area_id;
    private int fleet_id;
    private int operator_id;
    private int customer_id;
    private double latitude;
    private double longitude;


    public int getParking_spot_id() {
        return parking_spot_id;
    }

    public void setParking_spot_id(int parking_spot_id) {
        this.parking_spot_id = parking_spot_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getParking_area_id() {
        return parking_area_id;
    }

    public void setParking_area_id(int parking_area_id) {
        this.parking_area_id = parking_area_id;
    }

    public int getFleet_id() {
        return fleet_id;
    }

    public void setFleet_id(int fleet_id) {
        this.fleet_id = fleet_id;
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





    @Override
    public String toString() {
        return "Lock{" +
                "parking_spot_id='" + parking_spot_id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", pic='" + pic + '\'' +
                ", fleet_id='" + fleet_id + '\'' +
                ", operator_id=" + operator_id +
                ", customer_id=" + customer_id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", type='" + type + '\'' +
                '}';
    }



}
