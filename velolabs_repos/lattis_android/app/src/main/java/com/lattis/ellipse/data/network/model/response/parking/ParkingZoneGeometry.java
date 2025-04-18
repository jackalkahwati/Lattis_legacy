package com.lattis.ellipse.data.network.model.response.parking;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lattis on 24/05/17.
 */

public class ParkingZoneGeometry {
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
