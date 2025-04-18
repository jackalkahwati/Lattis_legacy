package com.lattis.ellipse.domain.model;

/**
 * Created by lattis on 24/05/17.
 */

public class ParkingZoneGeometry {
    private double latitude;
    private double longitude;
    private double radius;

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
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




        return "ParkingZoneGeometryResponse{" +
                "latitude=" + latitude +
                "longitude=" + longitude +
                '}';

    }
}
