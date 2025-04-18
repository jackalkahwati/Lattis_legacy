package com.lattis.ellipse.data.network.model.body.commun;

import com.google.gson.annotations.SerializedName;

public class LocationBody {

    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;

    public LocationBody(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "LocationBody{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
