package com.lattis.ellipse.data.network.model.body.bike;

import com.google.gson.annotations.SerializedName;


public class FindBikesBody {

    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;

    public FindBikesBody(double latitude,
                         double longitude
                             ) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
