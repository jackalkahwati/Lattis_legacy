package com.lattis.ellipse.data.network.model.body.parking;

import com.google.gson.annotations.SerializedName;

public class FindParkingBody {

    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;
    @SerializedName("range")
    private float range;

    public FindParkingBody(double latitude,
                           double longitude,
                        float rangeInMiles) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.range = rangeInMiles;
    }

}
