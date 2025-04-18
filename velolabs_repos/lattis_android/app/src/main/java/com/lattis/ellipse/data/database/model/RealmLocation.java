package com.lattis.ellipse.data.database.model;

import io.realm.RealmObject;

public class RealmLocation extends RealmObject{

    private double latitude;
    private double longitude;

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
        return "RealmLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
