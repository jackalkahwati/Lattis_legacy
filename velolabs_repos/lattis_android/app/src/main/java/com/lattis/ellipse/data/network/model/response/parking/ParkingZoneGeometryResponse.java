package com.lattis.ellipse.data.network.model.response.parking;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lattis on 24/05/17.
 */

public class ParkingZoneGeometryResponse {
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;
    @SerializedName("radius")
    private double radius;

    public double getRadius() {
        return radius;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
